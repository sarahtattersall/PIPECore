package uk.ac.imperial.pipe.animation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet;
import uk.ac.imperial.pipe.models.petrinet.TimingQueue;
import uk.ac.imperial.pipe.models.petrinet.Transition;

import com.google.common.collect.Sets;

/**
 * This class is defining the logic how a Petri Net is evaluated.
 * It provides the basic functionality defining when transitions can be fired and
 * implements the correct sequence of firing the different types of transitions.
 * It does as such not alter the current state of the PetriNet.
 * 
 * For altering a PetriNet the animator is using the AnimationLogic.
 * 
 * As there are also timed transitions the AnimationLogic depends on both:
 * 	- current state of the PN
 * 	- current time of PN 
 *  
 */
//TODO reallocate this logic; previously:  "It does not alter the state of the Petri net."
public final class PetriNetAnimationLogic implements AnimationLogic, PropertyChangeListener {

	private static Logger logger = LogManager.getLogger(PetriNetAnimationLogic.class);  

    /**
     * Executable Petri net this class represents the logic for
     */
    private final ExecutablePetriNet executablePetriNet;
    /**
     * Cache for storing a states enabled transitions
     * Needs to be concurrent thus to handle multiple calls to methods using this data structure
     * from different threads running in analysis modules
     */
    public Map<TimingQueue, Set<Transition>> cachedEnabledImmediateTransitions = new ConcurrentHashMap<>();
	
	/**
	 * Random for use in random firing.   
     */
	private Random random;

	private Set<Transition> markedEnabledTransitions; 
	
    /**
     * Constructor
     * @param executablePetriNet executable Petri net to perform animation logic on
     */
    public PetriNetAnimationLogic(ExecutablePetriNet executablePetriNet) {
    	this.executablePetriNet = executablePetriNet; 
    	this.executablePetriNet.addPropertyChangeListener(ExecutablePetriNet.PETRI_NET_REFRESHED_MESSAGE, this); 
		initMarkedEnabledTransitions();  
	}

	/**
	 * First, is looking for all immediate transitions.
	 * Only if there are none, current timed transitions are used.
	 * 
	 * @deprecated  can lead to confusion between immediate and timed transitions.  Use {@link #getRandomEnabledTransition(TimingQueue)}
     * @param state Must be a valid state for the Petri net this class represents
     * @return all transitions that are enabled in the given state
     */
//TODO: I propose to make this method private (at least from the interface)
// In my opinion, the AnimationLogic is following the right sequence of which transitions can
// be fired. The animator only accesses it through the getNextRandomTransition.
// Or if you really want to mess around you can getImmediateTransitions and getTimedTransitions 
// and do whatever you want with it. But putting them all in one place will surely lead
// to someone using it the wrong way.  
//Malte:  note that the GUI (TransitionAnimationHandler) allows the user to fire any arbitrary enabled transition
//  presumably the user accepts the risk of firing transitions in an order that wouldn't be used by this class.     
    @Override
    @Deprecated
    public Set<Transition> getEnabledTransitions(TimingQueue timedState) {
    	return getEnabledImmediateOrTimedTransitions(timedState);
    }
    /**
     * Replaces getEnabledTransitions; intended for internal use and testing
     * @param timedState
     * @return enabled immediate transitions, if any; else enabled timed transitions 
     */
	protected Set<Transition> getEnabledImmediateOrTimedTransitions(
			TimingQueue timedState) {
		// TODO: Turn on cached immediate transitions for current state.
    	//if (cachedEnabledTransitions.containsKey(state)) {
        //    return cachedEnabledTransitions.get(state);
        //}
    	// First: get current enabled immediate transitions.
		Set<Transition> enabledTransitions = 
				executablePetriNet.maximumPriorityTransitions(
				executablePetriNet.getEnabledImmediateTransitions());
//        Set<Transition> enabledTransitions = timedState.getEnabledImmediateTransitions();
        cachedEnabledImmediateTransitions.put(timedState, enabledTransitions);
        // Second: Checking timed transitions which should fire by now when there are no
        // immediate transitions left.
        if (enabledTransitions.isEmpty()) {
        	//TODO:  may have to pass in a state? 
        	enabledTransitions = timedState.getCurrentlyEnabledTimedTransitions(); 
        }
        return enabledTransitions;
	}
//	protected Set<Transition> getEnabledImmediateOrTimedTransitions(State state) {
//		// TODO: Turn on cached immediate transitions for current state.
//		//if (cachedEnabledTransitions.containsKey(state)) {
//		//    return cachedEnabledTransitions.get(state);
//		//}
//		// First: get current enabled immediate transitions.
//		Set<Transition> enabledTransitions = this.executablePetriNet.getEnabledImmediateTransitions();
////        Set<Transition> enabledTransitions = timedState.getEnabledImmediateTransitions();
//		//boolean hasImmediate = areAnyTransitionsImmediate(enabledTransitions);
//		int maxPriority = getMaxPriority(enabledTransitions);
//		if (maxPriority > 1) {
//			removePrioritiesLessThan(maxPriority, enabledTransitions);
//		}
//		cachedEnabledImmediateTransitions.put(state, enabledTransitions);
//		// Second: Checking timed transitions which should fire by now when there are no
//		// immediate transitions left.
//		if (enabledTransitions.isEmpty()) {
//			enabledTransitions = executablePetriNet.getCurrentlyEnabledTimedTransitions(); // delegates to timedQ 
//		}
//		return enabledTransitions;
//	}

	
    /**
    *
    * @return a random transition which is enabled given the Petri nets current state.
    * 
    * First, is looking for all immediate transitions.
	* Only if there are none, current timed transitions are used.
	* 
    * @param state Must be a valid state for the Petri net this class represents
    */
   @Override
   public Transition getRandomEnabledTransition(TimingQueue timedState) {
//       // TODO: Check if it is still enabled 
//       // and whenever a place is losing tokens it should be checked if
//       // a timed transitions becomes disabled again.
//       // APPROACH: for enabled timed transitions put 
//       // observed places in a map and for every changed place do a lookup if this might affect
//       // a timed transition.
	   Set<Transition> enabledTransitions = getEnabledImmediateOrTimedTransitions(timedState);
       if (enabledTransitions.isEmpty()) {
    	   return null;
       }
       Transition[] enabledTransitionsArray = enabledTransitions.toArray(new Transition[]{}); 
       int index = getRandom().nextInt(enabledTransitions.size());
       return enabledTransitionsArray[index]; 
   }


    /**
     * @param state to be evaluated
     * @return all successors of this state
     */
    @Override
    public Map<TimingQueue, Collection<Transition>> getSuccessors(TimingQueue timedState) {
    	TimingQueue startState = timedState.makeCopy();
    	
    	//Set<Transition> enabledTransitions = startState.getEnabledTimedTransitionsNew();
    	//startState.registerEnabledTimedTransitions(enabledTransitions);
    	// TODO: successor has to be adapted.
        Collection<Transition> enabled = getEnabledImmediateOrTimedTransitions(timedState);
        logger.debug("Get Succ: " + timedState);
        if (enabled.size() > 0) {
        	logger.debug("Enabled : " + enabled.iterator().next());
        }
        Map<TimingQueue, Collection<Transition>> successors = new HashMap<>();
        for (Transition transition : enabled) {
            TimingQueue successor = getFiredState( startState, transition);
            if (!successors.containsKey(successor)) {
                successors.put(successor, new LinkedList<Transition>());
            }
            successors.get(successor).add(transition);
        }
        return successors;
    }

    /**
     * Creates a map for the successor of the given state after firing the
     * transition.
     * calculating the decremented token counts and then calculating the incremented
     * token counts.
     * <p>
     * We cannot set the token counts in the decrement phase in case an increment
     * depends on this value. </p>
     * <p>
     * E.g. if P0 -- T0 -- P1 and T0 -- P1 has a weight of #(P0) then we expect
     * #(P0) to refer to the number of tokens before firing. </p>
     *
     * @param state to be evaluated
     * @param transition  to be fired 
     * @return Map of places whose token counts differ from those in the initial state
     */
    //TODO:  refactor to ExecutablePetriNet 
    // This clearly has to move - it depends very much on the implementation e.g. of TimedState.
    // Maybe into TimedState?
    @Override
    public TimingQueue getFiredState(TimingQueue timedState, Transition transition) {
    	// TODO: Turn on HashedStateBuilder
    	TimingQueue returnState = timedState.makeCopy();
    	
        Set<Transition> enabled = getEnabledImmediateOrTimedTransitions(returnState);
        
        if (enabled.contains(transition)) {
        	// Has to be guaranteed!
        	this.executablePetriNet.setTimedState(timedState);
        	this.executablePetriNet.fireTransition(transition, returnState);
        	//TODO keep refactoring....
        	//builder = ((AbstractTransition) transition).fire(executablePetriNet, timedState.getState(), builder);
//            fireTransition(state, transition, builder);
        	
        }
//        // NEW - the Fired Timed Transitions have to be removed from the enabled map.
//        State returnState = builder.build();
        // Check all timed and waiting transitions, if they are still active.
        verifyPendingTransitionsStillActive(returnState);
        updateAffectedTransitionsStatus(returnState);
        return ( returnState );
    }
    //TODO test this...
	protected void verifyPendingTransitionsStillActive(TimingQueue timedState) {
		Iterator<Long> nextFiringTimes = timedState.getAllFiringTimes().iterator();
        while (nextFiringTimes.hasNext()) {
        	long nextFiringTime = nextFiringTimes.next();
        	Iterator<Transition> checkStillEnabled = timedState.getEnabledTransitionsAtTime(nextFiringTime).iterator();	
        	while (checkStillEnabled.hasNext()) {
        		Transition nextChecked = checkStillEnabled.next();
//        		if (!(returnState.isEnabled( nextChecked ) )) {
        		if (!(this.executablePetriNet.isEnabled( nextChecked,timedState.getState() ) )) {
        			//System.out.println(nextChecked);
        			timedState.unregisterTimedTransition(nextChecked, nextFiringTime);
        		}
        	}
        }
	}

    /**
     * Computes transitions which need to be disabled because they are no longer enabled and
     * those that need to be enabled because they have been newly enabled.
     */
    protected void updateAffectedTransitionsStatus(TimingQueue state) {
    	Set<Transition> enabled = getEnabledImmediateOrTimedTransitions(state);
    	for (Transition transition : Sets.difference(markedEnabledTransitions, enabled)) {
    		transition.disable();
    		markedEnabledTransitions.remove(transition);
    	}
    	for (Transition transition : Sets.difference(enabled, markedEnabledTransitions)) {
    		transition.enable();
    		markedEnabledTransitions.add(transition);
    	}
    }
    /**
     * Clears cached transitions
     */
    @Override
    public void clear() {
        cachedEnabledImmediateTransitions.clear();
    }

    
	private Random getRandom() {
		if (random == null) {
			random = new Random(); 
		}
		return random;
	}
	/**
	 * Generate predictable results for repeated testing of a given Petri net by providing a Random built from the same long seed for each run.  
	 * Otherwise, a new Random will be used on each execution, leading to different firing patterns. 
	 * @param random
	 */
	public void setRandom(Random random) {
		this.random = random;
	}
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		clear();
	}
    //TODO consider moving to animator
	@Override
	public void stopAnimation() {
		for (Transition transition : markedEnabledTransitions) {
			transition.disable(); 
		}
	}

	@Override
	public void startAnimation() {
		initMarkedEnabledTransitions();  
    	updateAffectedTransitionsStatus(executablePetriNet.getTimingQueue()); 
	}

	protected void initMarkedEnabledTransitions() {
		markedEnabledTransitions = new HashSet<Transition>();
	}

}
