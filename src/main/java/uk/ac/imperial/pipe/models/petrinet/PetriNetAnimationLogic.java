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

import uk.ac.imperial.pipe.runner.Runner;
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
     * Returns the set of enabled transitions for the given State, i.e.,  
     * the set of enabled transitions that could fire at the current time, following the normal Petri net firing rules.
     * Specifically, this means that the highest priority immediate transitions are returned.  
     * If there are no enabled immediate transitions, then the timed transitions that can fire 
     * at the current time ({@link ExecutablePetriNet.#getCurrentTime()}) are returned.  This method is 
     * primarily useful for analysis purposes.
     * </p><p>
     * To retrieve a random transition that is eligible for firing under the normal Petri net firing rules, use
     * {@link AnimationLogic#getRandomEnabledTransition()} or {@link AnimationLogic#getRandomEnabledTransition(State)} 
     * This is the typical use case for executing a Petri net, either by the PIPE GUI, or through a {@link Runner}.
     * </p><p>Alternatively, and only for analysis purposes, the entire set of transitions that are enabled for the given 
     * State can be returned by using {@link SimpleAnimationLogic}. (Note that this class is not suitable for animation.) 
     * </p> 
     * @see SimpleAnimationLogic
     * @param state Must be a valid state for the executable Petri net
     * @return all enabled transitions that are eligible for firing under normal Petri net firing rules
     */    
    @Override
    public Set<Transition> getEnabledTransitions(State state) {
    	return getEnabledImmediateOrTimedTransitions(state, new HashedTimingQueue(executablePetriNet, state, 0), false);
    }
    
	protected Set<Transition> getEnabledImmediateOrTimedTransitions(State state) {
		return getEnabledImmediateOrTimedTransitions(state, null, true); 
	}
	protected Set<Transition> getEnabledImmediateOrTimedTransitions() {
		return getEnabledImmediateOrTimedTransitions(executablePetriNet.getState()); 
	}
    private Set<Transition> getEnabledImmediateOrTimedTransitions(State state,
			HashedTimingQueue timingQueue, boolean updateState) {
    	// TODO: Turn on cached immediate transitions for current state.
    	//if (cachedEnabledTransitions.containsKey(state)) {
    	//    return cachedEnabledTransitions.get(state);
    	//}
		Set<Transition> enabledTransitions = getOnlyMaximumPriorityEnabledImmediateTransitions(state); 
		cachedEnabledImmediateTransitionsState.put(state, enabledTransitions);
		enabledTransitions = getCurrentTimedTransitionsIfImmediateTransitionsEmpty(
				timingQueue, updateState, enabledTransitions);
		// getSuccessors iterates through this collection, but if its executablePetriNet.getCurrentlyEnabledTimedTransitions()
		// the timedQueue.dequeue process removes elements from the collection, so make a copy to avoid
		// ConcurrentModificationException.
		Set<Transition> copyEnabledTransitions = new HashSet<Transition>(enabledTransitions); 
		return copyEnabledTransitions;
	}

	protected Set<Transition> getOnlyMaximumPriorityEnabledImmediateTransitions(
			State state) {
		Set<Transition> enabledTransitions = 
				executablePetriNet.maximumPriorityTransitions(
				executablePetriNet.getEnabledImmediateTransitions(state));
		return enabledTransitions;
	}

	protected Set<Transition> getCurrentTimedTransitionsIfImmediateTransitionsEmpty(
			HashedTimingQueue timingQueue, boolean updateState,
			Set<Transition> enabledTransitions) {
		if (enabledTransitions.isEmpty()) {
			if (updateState) {
				enabledTransitions = executablePetriNet.getCurrentlyEnabledTimedTransitions(); // delegates to EPN timingQ 
			} else {
				if (timingQueue.hasUpcomingTimedTransition()) {
					timingQueue.setCurrentTime(timingQueue.getNextFiringTime()); 
					enabledTransitions  = timingQueue.getCurrentlyEnabledTimedTransitions();
				}
			}
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
    * Calculates successor states of a given state of the executable Petri net.  This will both calculate 
    * the successors, as well as update the current State of the executable Petri net to the first successor found.  
    * This is equivalent to {@link #getSuccessors(State, true)}.  Although useful for testing in cases where 
    * there is expected to be 0 or 1 successor, in general this 
    * is unlikely to be the desired behavior; more likely is {@link #getSuccessors(State, false)}. 
    * 
    * @param state to be evaluated
    * @return successors of the given state 
    */
    @Override
    public Map<State, Collection<Transition>> getSuccessors(State state) {
    	return getSuccessors(state, true);
    }

    /**
     * Calculates successor states of a given state.  When updateState is false (the normal case), the successor
     * states will be calculated, but the State of the executable Petri net is left unchanged.  
     * When updateState is true, this is equivalent to {@link #getSuccessors(State)}. 
     *
     * @param state to be evaluated
     * @param updateState whether the State of the executable Petri net should be updated or left unchanged
     * @return successors of the given state
     */
	public Map<State, Collection<Transition>> getSuccessors(State state, boolean updateState) {
		HashedTimingQueue timingQueue = new HashedTimingQueue(executablePetriNet, state, 0); 
		State startState = (new HashedStateBuilder(state)).build(); 
    	Collection<Transition> enabled = (updateState)  
    			? getEnabledImmediateOrTimedTransitions()
    			: getEnabledImmediateOrTimedTransitions(state, timingQueue, false);
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
    @Override
    public State getFiredState(Transition transition, State state) {
    	return getFiredState(transition, state, false);
    }
    /**
     * @param state to be evaluated
     * @param transition  to be fired 
     * @return Map of places whose token counts differ from those in the initial state
     */
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
