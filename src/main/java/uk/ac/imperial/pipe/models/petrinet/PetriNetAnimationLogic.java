package uk.ac.imperial.pipe.models.petrinet;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.state.HashedStateBuilder;
import uk.ac.imperial.state.State;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

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
    public Map<State, Set<Transition>> cachedEnabledImmediateTransitionsState = new ConcurrentHashMap<>();
	
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
     * @deprecated  can lead to confusion between immediate and timed transitions.  Use {@link #getRandomEnabledTransition(State)}
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
    public Set<Transition> getEnabledTransitions(State state) {
    	return getEnabledImmediateOrTimedTransitions(state, new HashedTimingQueue(executablePetriNet, state, 0));
    }
    
    /**
     * Replaces getEnabledTransitions; intended for internal use and testing
     * @param state against which transitions are to be evaluated 
     * @return enabled immediate transitions, if any; else enabled timed transitions 
     */
	public Set<Transition> getEnabledImmediateOrTimedTransitions(State state) {
		// TODO: Turn on cached immediate transitions for current state.
		//if (cachedEnabledTransitions.containsKey(state)) {
		//    return cachedEnabledTransitions.get(state);
		//}
		Set<Transition> enabledTransitions = 
				executablePetriNet.maximumPriorityTransitions(
				executablePetriNet.getEnabledImmediateTransitions(state)); 
		cachedEnabledImmediateTransitionsState.put(state, enabledTransitions);
		if (enabledTransitions.isEmpty()) {
			enabledTransitions = executablePetriNet.getCurrentlyEnabledTimedTransitions(); // delegates to timedQ 
		}
		// getSuccessors iterates through this collection, but if its executablePetriNet.getCurrentlyEnabledTimedTransitions()
		// the timedQueue.dequeue process removes elements from the collection, so make a copy to avoid
		// ConcurrentModificationException.
		Set<Transition> copyEnabledTransitions = new HashSet<Transition>(enabledTransitions); 
		return copyEnabledTransitions;
//		return enabledTransitions;
	}
	public Set<Transition> getEnabledImmediateOrTimedTransitions() {
		return getEnabledImmediateOrTimedTransitions(executablePetriNet.getState()); 
	}
    private Set<Transition> getEnabledImmediateOrTimedTransitions(State state,
			HashedTimingQueue timingQueue) {
		Set<Transition> enabledTransitions = 
				executablePetriNet.maximumPriorityTransitions(
				executablePetriNet.getEnabledImmediateTransitions(state)); 
		cachedEnabledImmediateTransitionsState.put(state, enabledTransitions);
		if (enabledTransitions.isEmpty()) {
			if (timingQueue.hasUpcomingTimedTransition()) {
				timingQueue.setCurrentTime(timingQueue.getNextFiringTime()); 
				enabledTransitions  = timingQueue.getCurrentlyEnabledTimedTransitions();
			}
		}
		// getSuccessors iterates through this collection, but if its executablePetriNet.getCurrentlyEnabledTimedTransitions()
		// the timedQueue.dequeue process removes elements from the collection, so make a copy to avoid
		// ConcurrentModificationException.
		Set<Transition> copyEnabledTransitions = new HashSet<Transition>(enabledTransitions); 
		return copyEnabledTransitions;
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
   public Transition getRandomEnabledTransition(State state) {
	   Set<Transition> enabledTransitions = getEnabledImmediateOrTimedTransitions(state);
	   if (enabledTransitions.isEmpty()) {
		   return null;
	   }
	   Transition[] enabledTransitionsArray = enabledTransitions.toArray(new Transition[]{}); 
	   int index = getRandom().nextInt(enabledTransitions.size());
	   return enabledTransitionsArray[index]; 
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
   public Transition getRandomEnabledTransition() {
	   Set<Transition> enabledTransitions = getEnabledImmediateOrTimedTransitions();
	   if (enabledTransitions.isEmpty()) {
		   return null;
	   }
	   Transition[] enabledTransitionsArray = enabledTransitions.toArray(new Transition[]{}); 
	   int index = getRandom().nextInt(enabledTransitions.size());
	   return enabledTransitionsArray[index]; 
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
     * <p>This method invokes {@link #getSuccessors(State, true)} </p>
     * @param state to be evaluated
     * @return all successors of this state
     */
    @Override
    public Map<State, Collection<Transition>> getSuccessors(State state) {
    	return getSuccessors(state, true);
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
     * @param updateState whether the State of the executable Petri net will be updated or left unchanged
     * @return all successors of this state
     */
	public Map<State, Collection<Transition>> getSuccessors(State state, boolean updateState) {
		HashedTimingQueue timingQueue = new HashedTimingQueue(executablePetriNet, state, 0); 
		State startState = (new HashedStateBuilder(state)).build(); 
    	Collection<Transition> enabled = (updateState)  
    			? getEnabledImmediateOrTimedTransitions()
    			: getEnabledImmediateOrTimedTransitions(state, timingQueue);
    	Map<State, Collection<Transition>> successors = new HashMap<>();
    	for (Transition transition : enabled) {
    		State successor = getFiredState( transition, startState, updateState);
    		if (!updateState) {
    			timingQueue.dequeueAndRebuild(transition, successor); 
    		}
    		if (!successors.containsKey(successor)) {
    			successors.put(successor, new LinkedList<Transition>());
    		}
    		successors.get(successor).add(transition);
    	}
    	return successors;
	}

	/**
     * @param transition  to be fired 
     * @param state to be evaluated
     * @return Map of places whose token counts differ from those in the initial state
     */
    //TODO:  refactor to ExecutablePetriNet 
    @Override
    public State getFiredState(Transition transition, State state) {
    	return getFiredState(transition, state, false);
    }
    /**
     * @param state to be evaluated
     * @param transition  to be fired 
     * @return Map of places whose token counts differ from those in the initial state
     */
    //TODO:  refactor to ExecutablePetriNet 
    @Override
    public State getFiredState(Transition transition) {
    	return getFiredState(transition, executablePetriNet.getState(), true);
    }
    
    private State getFiredState(Transition transition, State state, boolean updateState) {
    	State returnState = (new HashedStateBuilder(state)).build();
		if (updateState) {
			returnState = this.executablePetriNet.fireTransition(transition);
		} else {
			returnState = this.executablePetriNet.fireTransition(transition, state);
		}
    	updateAffectedTransitionsStatus(returnState);
    	return ( returnState );
    }

    /**
     * Computes transitions which need to be disabled because they are no longer enabled and
     * those that need to be enabled because they have been newly enabled.
     */
    protected void updateAffectedTransitionsStatus(State state) {
    	Set<Transition> enabled = getEnabledImmediateOrTimedTransitions(state);
    	//Sets.difference returns a SetView, which perhaps maintains iterators over each set, and gives
    	// ConcurrentModificationException when we remove something from markedEnabledTransitions, so copy it first
    	for (Transition transition : ((Sets.difference(markedEnabledTransitions, enabled)).copyInto(new HashSet<Transition>()))) {
    		transition.disable();
    		markedEnabledTransitions.remove(transition);
    	}
    	for (Transition transition : ((Sets.difference(enabled, markedEnabledTransitions)).copyInto(new HashSet<Transition>()))) {
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
    	updateAffectedTransitionsStatus(executablePetriNet.getState()); 
	}

	protected void initMarkedEnabledTransitions() {
		markedEnabledTransitions = new HashSet<Transition>();
	}

}
