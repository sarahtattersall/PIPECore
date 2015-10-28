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

import uk.ac.imperial.pipe.models.petrinet.Arc;
import uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.TimedState;
import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.state.HashedStateBuilder;
import uk.ac.imperial.state.State;

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
//<<<<<<< 11df5b8893857df071ff771a524c093a3c114042
//TODO reallocate this logic; previously:  "It does not alter the state of the Petri net."
public final class PetriNetAnimationLogic implements AnimationLogic, PropertyChangeListener {
//SJDClean=======
//public final class PetriNetAnimationLogic implements AnimationLogic {
//	
//	/**
//	 *  Logger  
//	 */
	private static Logger logger = LogManager.getLogger(PetriNetAnimationLogic.class);  
//>>>>>>> Deprecate AnimationLogic.getEnabledTransitions, convert 2 tests to log4j

    /**
     * Executable Petri net this class represents the logic for
     */
    private final ExecutablePetriNet executablePetriNet;
    /**
     * Cache for storing a states enabled transitions
     * Needs to be concurrent thus to handle multiple calls to methods using this data structure
     * from different threads running in analysis modules
     */
    public Map<TimedState, Set<Transition>> cachedEnabledImmediateTransitions = new ConcurrentHashMap<>();
	
	/**
	 * Random for use in random firing.   
     */
	private Random random;
//<<<<<<< 9b197a93f9333000d7fddd936f4cbd1d5ce13fae
//	private long timeStep;
//	private long currentTime; 
//SJDclean=======

	private Set<Transition> markedEnabledTransitions; 
	
    /**
     * Constructor
     * @param executablePetriNet executable Petri net to perform animation logic on
     */
    public PetriNetAnimationLogic(ExecutablePetriNet executablePetriNet) {
    	this.executablePetriNet = executablePetriNet; 
//<<<<<<< 9b197a93f9333000d7fddd936f4cbd1d5ce13fae
    	this.executablePetriNet.addPropertyChangeListener(ExecutablePetriNet.PETRI_NET_REFRESHED_MESSAGE, this); 
//=======
		initMarkedEnabledTransitions();  
//>>>>>>> refactor to stop using getEnabledTransitions in animator and animatorLogic
	}

	/**
	 * First, is looking for all immediate transitions.
	 * Only if there are none, current timed transitions are used.
	 * 
	 * @deprecated  can lead to confusion between immediate and timed transitions.  Use {@link #getRandomEnabledTransition(TimedState)}
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
    public Set<Transition> getEnabledTransitions(TimedState timedState) {
    	return getEnabledImmediateOrTimedTransitions(timedState);
    }
    /**
     * Replaces getEnabledTransitions; intended for internal use and testing
     * @param timedState
     * @return enabled immediate transitions, if any; else enabled timed transitions 
     */
	protected Set<Transition> getEnabledImmediateOrTimedTransitions(
			TimedState timedState) {
		// TODO: Turn on cached immediate transitions for current state.
    	//if (cachedEnabledTransitions.containsKey(state)) {
        //    return cachedEnabledTransitions.get(state);
        //}
    	// First: get current enabled immediate transitions.
        Set<Transition> enabledTransitions = timedState.getEnabledImmediateTransitions();
        //boolean hasImmediate = areAnyTransitionsImmediate(enabledTransitions);
        int maxPriority = getMaxPriority(enabledTransitions);
        if (maxPriority > 1) {
        	removePrioritiesLessThan(maxPriority, enabledTransitions);
        }
        cachedEnabledImmediateTransitions.put(timedState, enabledTransitions);
//<<<<<<< 0e5245da7684e9b205c7f7b8cd2102aa8601f94b
//        enabledTransitions = getEnabledTimedTransitionsForCurrentTime(timedState, enabledTransitions);
//SJDclean=======
        // Second: Checking timed transitions which should fire by now when there are no
        // immediate transitions left.
        if (enabledTransitions.isEmpty()) {
        	enabledTransitions = timedState.getCurrentlyEnabledTimedTransitions(); 
        }
        return enabledTransitions;
	}
	
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
   public Transition getRandomEnabledTransition(TimedState timedState) {
	   // TODO: Turn on cached immediate transitions for current state.
	   //if (cachedEnabledTransitions.containsKey(state)) {
       //    return cachedEnabledTransitions.get(state);
       //}
	   // First: get current enabled immediate transitions.
       Set<Transition> enabledTransitions = timedState.getEnabledImmediateTransitions();
       int maxPriority = getMaxPriority(enabledTransitions);
       if (maxPriority > 1) {
    	   removePrioritiesLessThan(maxPriority, enabledTransitions);
       }
       cachedEnabledImmediateTransitions.put(timedState, enabledTransitions);
       // Second: Checking timed transitions which should fire by now when there are no
       // immediate transitions left.
       
       // TODO: Check if it is still enabled 
       // and whenever a place is loosing tokens it should be checked if
       // a timed transitions becomes disabled again.
       // APPROACH: for enabled timed transitions put 
       // observed places in a map and for every changed place do a lookup if this might affect
       // a timed transition.
//<<<<<<< 0e5245da7684e9b205c7f7b8cd2102aa8601f94b
//       enabledTransitions = getEnabledTimedTransitionsForCurrentTime(timedState,
//			enabledTransitions);
//SJDclean=======
       if (enabledTransitions.isEmpty()) {
       		enabledTransitions = timedState.getCurrentlyEnabledTimedTransitions();
       }
//>>>>>>> Added TimedPetriNetRunner and sorted out timed transitions in the PNAnimationLogic and PNAnimator.
       //logger.debug("enabled transitions count: "+enabledTransitions.size());
       if (enabledTransitions.isEmpty()) {
    	   return null;
       }
       Transition[] enabledTransitionsArray = enabledTransitions.toArray(new Transition[]{}); 
       int index = getRandom().nextInt(enabledTransitions.size());
       return enabledTransitionsArray[index]; 
   }

	protected Set<Transition> getEnabledTimedTransitionsForCurrentTime(TimedState timedState, 
			Set<Transition> enabledTransitions) {
		if (enabledTransitions.isEmpty()) {
		   		if (timedState.getEnabledTimedTransitions().containsKey(timedState.getCurrentTime())) {
		   			enabledTransitions = timedState.getEnabledTimedTransitions().get(timedState.getCurrentTime());
		   		}
		   }
		return enabledTransitions;
	}

    /**
     * @param state to be evaluated
     * @return all successors of this state
     */
    @Override
    public Map<TimedState, Collection<Transition>> getSuccessors(TimedState timedState) {
    	TimedState startState = timedState.makeCopy();
    	
    	//Set<Transition> enabledTransitions = startState.getEnabledTimedTransitionsNew();
    	//startState.registerEnabledTimedTransitions(enabledTransitions);
    	// TODO: successor has to be adapted.
//<<<<<<< 0e5245da7684e9b205c7f7b8cd2102aa8601f94b
////<<<<<<< 384394601450967788e197ab312eae581d637c80
////        Collection<Transition> enabled = getEnabledTransitions(timedState);
//////        logger.debug("Get Succ: " + timedState);
////=======
        Collection<Transition> enabled = getEnabledImmediateOrTimedTransitions(timedState);
        logger.debug("Get Succ: " + timedState);
//>>>>>>> refactor to stop using PetriNetAnimationLogic.getEnabledTransitions
        if (enabled.size() > 0) {
        	logger.debug("Enabled : " + enabled.iterator().next());
        }
//SJDclean=======
//        Collection<Transition> enabled = getEnabledTransitions(startState);
//>>>>>>> Added TimedPetriNetRunner and sorted out timed transitions in the PNAnimationLogic and PNAnimator.
        Map<TimedState, Collection<Transition>> successors = new HashMap<>();
        for (Transition transition : enabled) {
            TimedState successor = getFiredState( startState, transition);
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
    public TimedState getFiredState(TimedState timedState, Transition transition) {
    	// TODO: Turn on HashedStateBuilder
        /*HashedStateBuilder builder = new HashedStateBuilder();
        for (String placeId : timedState.getState().getPlaces()) {
            //Copy tokens
            builder.placeWithTokens(placeId, timedState.getState().getTokens(placeId));
<<<<<<< 0e5245da7684e9b205c7f7b8cd2102aa8601f94b
        }

        Set<Transition> enabled = getEnabledImmediateOrTimedTransitions(timedState);
=======
        }*/
    	TimedState returnState = timedState.makeCopy();
    	
//        Set<Transition> enabled = getEnabledTransitions(returnState); 
        Set<Transition> enabled = getEnabledImmediateOrTimedTransitions(returnState);
        
//>>>>>>> Added TimedPetriNetRunner and sorted out timed transitions in the PNAnimationLogic and PNAnimator.
        if (enabled.contains(transition)) {
        	// Has to be guaranteed!
        	this.executablePetriNet.setTimedState(timedState);
        	this.executablePetriNet.fireTransition(transition, returnState);
        	//TODO keep refactoring....
        	//builder = ((AbstractTransition) transition).fire(executablePetriNet, timedState.getState(), builder);
//            fireTransition(state, transition, builder);
        	
        }
//<<<<<<< 0e5245da7684e9b205c7f7b8cd2102aa8601f94b	
//<<<<<<< 9b197a93f9333000d7fddd936f4cbd1d5ce13fae
//        // NEW - the Fired Timed Transitions have to be removed from the enabled map.
//        State returnState = builder.build();
////        logger.debug("Fired State: " + returnState);
//SJDclean=======
//        TimedState returnTimedState = removeTimedTransitionsFromEnabledMap(timedState, transition, builder);
//        updateAffectedTransitionsStatus(returnTimedState);
//        return returnTimedState;
//    }
//
//	protected TimedState removeTimedTransitionsFromEnabledMap(TimedState timedState, Transition transition, 
//			HashedStateBuilder builder) {
//		State returnState = builder.build();
//        logger.debug("Fired State: " + returnState);
////>>>>>>> refactor to stop using getEnabledTransitions in animator and animatorLogic
//        TimedState returnTimedState = new TimedState (returnState, timedState.getEnabledTimedTransitions(), timedState.getCurrentTime() );
//        // TODO: Cloning the old enabled timed transitions - could be done more efficient!
//        if (transition.isTimed()) {
////        	logger.debug("Was a timed transition " + transition);
//        	Set<Transition> currentTrans = returnTimedState.getEnabledTimedTransitions().get( timedState.getCurrentTime() );
//        	currentTrans.remove(transition);
//        	if (currentTrans.isEmpty()) {
//        		returnTimedState.getEnabledTimedTransitions().remove(timedState.getCurrentTime());
//=======
        // Check all timed and waiting transitions, if they are still active.
        Iterator<Long> nextFiringTimes = returnState.getNextFiringTimes().iterator();
        while (nextFiringTimes.hasNext()) {
        	long nextFiringTime = nextFiringTimes.next();
        	Iterator<Transition> checkStillEnabled = returnState.getEnabledTransitionsAtTime(nextFiringTime).iterator();	
        	while (checkStillEnabled.hasNext()) {
        		Transition nextChecked = checkStillEnabled.next();
        		if (!(returnState.isEnabled( nextChecked ) )) {
        			//System.out.println(nextChecked);
        			returnState.unregisterTimedTransition(nextChecked, nextFiringTime);
        		}
//>>>>>>> Added TimedPetriNetRunner and sorted out timed transitions in the PNAnimationLogic and PNAnimator.
        	}
        }
//<<<<<<< 0e5245da7684e9b205c7f7b8cd2102aa8601f94b
        updateAffectedTransitionsStatus(returnState);
//		return returnTimedState;
//	}
//=======
        return ( returnState );
    }
//>>>>>>> Added TimedPetriNetRunner and sorted out timed transitions in the PNAnimationLogic and PNAnimator.

    /**
     * Computes transitions which need to be disabled because they are no longer enabled and
     * those that need to be enabled because they have been newly enabled.
     */
    protected void updateAffectedTransitionsStatus(TimedState state) {
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
     * @param state  petri net state to evaluate weight against
     * @param weight a functional weight
     * @return the evaluated weight for the given state
     */
    @Override
    public double getArcWeight(TimedState timedState, String weight) {
    	double result =  executablePetriNet.evaluateExpression(timedState.getState(), weight); 
        if (result == -1.0) {
            //TODO:
            throw new RuntimeException("Could not parse arc weight");
        }
        return result; 
    }

    /**
     * Clears cached transitions
     */
    @Override
    public void clear() {
        cachedEnabledImmediateTransitions.clear();
    }

//<<<<<<< 0e5245da7684e9b205c7f7b8cd2102aa8601f94b
//
//    /**
//     * @return all the currently enabled immediate transitions in the petri net
//     */
//    //TODO make public and add to interface if getRandomEnabledTransition is insufficient
//    protected Set<Transition> getEnabledImmediateTransitions(State state) {
//
//        Set<Transition> enabledTransitions = new HashSet<>();
//        for (Transition transition : executablePetriNet.getTransitions()) {
//            if (isEnabled(transition, state) && !transition.isTimed()) {
//                enabledTransitions.add(transition);
//            }
//        }
//        return enabledTransitions;
//    }
//    
//    /**
//     * @return all the currently enabled timed transitions in the petri net
//     */
//    //TODO make public and add to interface if getRandomEnabledTransition is insufficient
//    protected Set<Transition> getEnabledTimedTransitions(State state) {
//
//        Set<Transition> enabledTransitions = new HashSet<>();
//        for (Transition transition : executablePetriNet.getTransitions()) {
//            if (isEnabled(transition, state) & transition.isTimed()) {
//                enabledTransitions.add(transition);
//            }
//        }
//        return enabledTransitions;
//    }
//
//    /**
//     * Works out if an transition is enabled. This means that it checks if
//     * a) places connected by an incoming arc to this transition have enough tokens to fire
//     * b) places connected by an outgoing arc to this transition have enough space to fit the
//     * new tokens (that is enough capacity).
//     *
//     * @param transition to see if it is enabled
//     * @return true if transition is enabled
//     */
//    private boolean isEnabled(Transition transition, State state) {
//        for (Arc<Place, Transition> arc : executablePetriNet.inboundArcs(transition)) {
//            if (!arc.canFire(executablePetriNet, state)) {
//                return false;
//            }
//        }
//        for (Arc<Transition, Place> arc : executablePetriNet.outboundArcs(transition)) {
//            if (!arc.canFire(executablePetriNet, state)) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//=======
//>>>>>>> Added TimedPetriNetRunner and sorted out timed transitions in the PNAnimationLogic and PNAnimator.
    /**
     * @param transitions to check if any are timed
     * @return true if any of the transitions are immediate
     */

    /**
     * @param transitions to find max priority of
     * @return the maximum priority of immediate transitions in the collection
     */
    private int getMaxPriority(Iterable<Transition> transitions) {
        int maxPriority = 0;
        for (Transition transition : transitions) {
            if (!transition.isTimed()) {
                maxPriority = Math.max(maxPriority, transition.getPriority());
            }
        }
        return maxPriority;
    }


    /**
     * Performs in place removal transitions whose priority is less than the specified value
     * <p/>
     * Note we must use an iterator in order to ensure save removal
     * whilst looping
     *
     * @param priority    minimum priority of transitions allowed to remain in the Collection
     * @param transitions to remove if their priority is less than the specified value
     */
    private void removePrioritiesLessThan(int priority, Iterable<Transition> transitions) {
        Iterator<Transition> transitionIterator = transitions.iterator();
        while (transitionIterator.hasNext()) {
            Transition transition = transitionIterator.next();
            if (!transition.isTimed() && transition.getPriority() < priority) {
                transitionIterator.remove();
            }
        }
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
//<<<<<<< 0e5245da7684e9b205c7f7b8cd2102aa8601f94b
//	
//    /**
//     * For the current time all enabled timed transitions are 
//     * put in the timing queue = when time is advanced they can get activated when 
//     * the delay is gone.
//     * 
//     * @param state  petri net state to evaluate weight against
//     */
//    protected void registerEnabledTimedTransitions(TimedState timedState) {
//    	Set<Transition> enabledTransitions = getEnabledTimedTransitions(timedState.getState());
//    	Iterator<Transition> transitionIterator = enabledTransitions.iterator();
//    	while (transitionIterator.hasNext()) {
//    		Transition transition = transitionIterator.next();
//    		if (transition.getNextFiringTime() <= timedState.getCurrentTime()) {
//    			// Set in the transition the next firing time 
//        		// (this is only used to keep track of the firing inside the transitions)
//    			long nextFiringTime = timedState.getCurrentTime() + transition.getDelay();
//    			transition.setNextFiringTime(nextFiringTime);
//    			// Put transition into timing table to become fired when
//    			// the specified time is reached.
//    			if (timedState.getEnabledTimedTransitions().containsKey(nextFiringTime)) {
//    				Set<Transition> registeredTransitions = timedState.getEnabledTimedTransitions().get(nextFiringTime);
//    				registeredTransitions.add(transition);
//    				timedState.getEnabledTimedTransitions().put(nextFiringTime, registeredTransitions);
//    			} else {
//    				Set<Transition> registerTransitionSet = new HashSet<>();
//    			    registerTransitionSet.add(transition);
//    				timedState.getEnabledTimedTransitions().put(nextFiringTime, registerTransitionSet);
//    			}
//    		}
//    	}
//    }

//<<<<<<< dee58112d6125e9b0180ebe481f8aabe72c303f4
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		clear();
	}
//	protected void setCurrentTimeForTesting(long currentTime) {
//		this.currentTime =  currentTime; 
//	}
//SJDclean =======
////    /**
////     * Get the internal current time of the animated Petri network.
////     */
////    public long getCurrentTime() {
////    	return this.currentTime;
////    }
////    
////    /**
////     * Set the internal time step of the animated Petri network.
////     */
////    public void setTimeStep(long newStep) {
////    	this.timeStep = newStep;
////    }
////    
////    /**
////     * Get the internal time step of the animated Petri network.
////     */
////    public long getTimeStep() {
////    	return this.timeStep;
////    }
//    
//    /**
//     * Advance current time one time step.
//     */
//    public void advanceSingleTimeStep() {
//    	registerEnabledTimedTransitions(executablePetriNet.getTimedState());
//    	this.currentTime += this.timeStep;
//    }
//    
//    /**
//     * Advance current time.
//     */
//    protected void advanceToTime(long newTime) {
//    	registerEnabledTimedTransitions(executablePetriNet.getTimedState());
//    	this.currentTime = newTime;
//    }
    
	@Override
	public void stopAnimation() {
		for (Transition transition : markedEnabledTransitions) {
			transition.disable(); 
		}
	}

	@Override
	public void startAnimation() {
		initMarkedEnabledTransitions();  
    	updateAffectedTransitionsStatus(executablePetriNet.getTimedState()); 
	}

	protected void initMarkedEnabledTransitions() {
		markedEnabledTransitions = new HashSet<Transition>();
	}

//	protected void setCurrentTimeForTesting(long currentTime) {
//		this.currentTime =  currentTime; 
//	}
//>>>>>>> Added the timed behavior to transitions. And included this in the PN-AnimationLogic.
//=======
//    
//>>>>>>> Added TimedPetriNetRunner and sorted out timed transitions in the PNAnimationLogic and PNAnimator.
}
