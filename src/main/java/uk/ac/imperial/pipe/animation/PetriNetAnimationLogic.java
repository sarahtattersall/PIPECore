package uk.ac.imperial.pipe.animation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import uk.ac.imperial.pipe.models.petrinet.AbstractTransition;
import uk.ac.imperial.pipe.models.petrinet.Arc;
import uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.state.HashedStateBuilder;
import uk.ac.imperial.state.State;

/**
 * This class has useful functions relevant for the animation
 * of a Petri net. 
 */
//TODO reallocate this logic; previously:  "It does not alter the state of the Petri net."
public final class PetriNetAnimationLogic implements AnimationLogic {

    /**
     * Executable Petri net this class represents the logic for
     */
    private final ExecutablePetriNet executablePetriNet;
    /**
     * Cache for storing a states enabled transitions
     * Needs to be concurrent thus to handle multiple calls to methods using this data structure
     * from different threads running in analysis modules
     */
    public Map<State, Set<Transition>> cachedEnabledImmediateTransitions = new ConcurrentHashMap<>();
    
    /**
     * A map providing for each time of the PN which timed transitions should fire
     * at that point in time (meaning: delay passed)
     */
    public Map<Long, Set<Transition>> timingEnabledTimedTransitions = new ConcurrentHashMap<>();
    
	private long timeStep = 10; // Is done in milliseconds.
	private long initialTime = 0;
	private long currentTime= this.initialTime;
	
    /**
     * Constructor
     * @param executablePetriNet executable Petri net to perform animation logic on
     */
    public PetriNetAnimationLogic(ExecutablePetriNet executablePetriNet) {
    	this.executablePetriNet = executablePetriNet; 
	}

	public PetriNetAnimationLogic(ExecutablePetriNet executablePetriNet, long initialTime) {
		this(executablePetriNet);
		this.initialTime = initialTime; 
		this.currentTime = this.initialTime;
	}

	/**
	 * First, is looking for all immediate transitions.
	 * Only if there are none, current timed transitions are used.
	 * 
     * @param state Must be a valid state for the Petri net this class represents
     * @return all transitions that are enabled in the given state
     */
    @Override
    public Set<Transition> getEnabledTransitions(State state) {
    	// TODO: Turn on cached immediate transitions for current state.
    	//if (cachedEnabledTransitions.containsKey(state)) {
        //    return cachedEnabledTransitions.get(state);
        //}
    	// First: get current enabled immediate transitions.
        Set<Transition> enabledTransitions = findEnabledImmediateTransitions(state);
        //boolean hasImmediate = areAnyTransitionsImmediate(enabledTransitions);
        int maxPriority = getMaxPriority(enabledTransitions);
        if (maxPriority > 1) {
        	removePrioritiesLessThan(maxPriority, enabledTransitions);
        }
        cachedEnabledImmediateTransitions.put(state, enabledTransitions);
        // Second: Checking timed transitions which should fire by now when there are no
        // immediate transitions left.
        if (enabledTransitions.isEmpty()) {
        	if (timingEnabledTimedTransitions.containsKey(this.currentTime)) {
        		enabledTransitions = timingEnabledTimedTransitions.get(this.currentTime);
        	}
        }
        return enabledTransitions;
    }

    /**
     * @param state
     * @return all successors of this state
     */
    @Override
    public Map<State, Collection<Transition>> getSuccessors(State state) {
        Collection<Transition> enabled = getEnabledTransitions(state);
        Map<State, Collection<Transition>> successors = new HashMap<>();
        for (Transition transition : enabled) {

            State successor = getFiredState(state, transition);
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
     * <p/>
     * We cannot set the token counts in the decrement phase in case an increment
     * depends on this value.
     * <p/>
     * E.g. if P0 -> T0 -> P1 and T0 -> P1 has a weight of #(P0) then we expect
     * #(P0) to refer to the number of tokens before firing.
     *
     * @param state
     * @param transition
     * @return Map of places whose token counts differ from those in the initial state
     */
    //TODO:  refactor to ExecutablePetriNet 
    @Override
    public State getFiredState(State state, Transition transition) {
        HashedStateBuilder builder = new HashedStateBuilder();
        for (String placeId : state.getPlaces()) {
            //Copy tokens
            builder.placeWithTokens(placeId, state.getTokens(placeId));
        }

        Set<Transition> enabled = getEnabledTransitions(state);
        if (enabled.contains(transition)) {
        	//TODO keep refactoring....
        	builder = ((AbstractTransition) transition).fire(executablePetriNet, state, builder);
//            fireTransition(state, transition, builder);
        }

        return builder.build();
    }

    /**
     * @param state  petri net state to evaluate weight against
     * @param weight a functional weight
     * @return the evaluated weight for the given state
     */
    @Override
    public double getArcWeight(State state, String weight) {
    	double result =  executablePetriNet.evaluateExpression(state, weight); 
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


    /**
     * @return all the currently enabled immediate transitions in the petri net
     */
    private Set<Transition> findEnabledImmediateTransitions(State state) {

        Set<Transition> enabledTransitions = new HashSet<>();
        for (Transition transition : executablePetriNet.getTransitions()) {
            if (isEnabled(transition, state) && !transition.isTimed()) {
                enabledTransitions.add(transition);
            }
        }
        return enabledTransitions;
    }
    
    /**
     * @return all the currently enabled timed transitions in the petri net
     */
    private Set<Transition> findEnabledTimedTransitions(State state) {

        Set<Transition> enabledTransitions = new HashSet<>();
        for (Transition transition : executablePetriNet.getTransitions()) {
            if (isEnabled(transition, state) & transition.isTimed()) {
                enabledTransitions.add(transition);
            }
        }
        return enabledTransitions;
    }

    /**
     * Works out if an transition is enabled. This means that it checks if
     * a) places connected by an incoming arc to this transition have enough tokens to fire
     * b) places connected by an outgoing arc to this transition have enough space to fit the
     * new tokens (that is enough capacity).
     *
     * @param transition to see if it is enabled
     * @return true if transition is enabled
     */
    private boolean isEnabled(Transition transition, State state) {
        for (Arc<Place, Transition> arc : executablePetriNet.inboundArcs(transition)) {
            if (!arc.canFire(executablePetriNet, state)) {
                return false;
            }
        }
        for (Arc<Transition, Place> arc : executablePetriNet.outboundArcs(transition)) {
            if (!arc.canFire(executablePetriNet, state)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param transitions to check if any are timed
     * @return true if any of the transitions are immediate
     */
    private boolean areAnyTransitionsImmediate(Iterable<Transition> transitions) {
        for (Transition transition : transitions) {
            if (!transition.isTimed()) {
                return true;
            }
        }
        return false;
    }

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

    /**
     * In place removal of timed transitions from transitions
     * <p/>
     * Note have to use an iterator for save deletions whilst
     * iterating through the list
     *
     * @param transitions to remove timed transitions from
     */
    private void removeTimedTransitions(Iterable<Transition> transitions) {
        Iterator<Transition> transitionIterator = transitions.iterator();
        while (transitionIterator.hasNext()) {
            Transition transition = transitionIterator.next();
            if (transition.isTimed()) {
                transitionIterator.remove();
            }
        }
    }
    
    /**
     * For the current time all enabled timed transitions are 
     * put in the timing queue = when time is advanced they can get activated when 
     * the delay is gone.
     * 
     * @param state  petri net state to evaluate weight against
     */
    private void registerEnabledTimedTransitions(State state) {
    	Set<Transition> enabledTransitions = findEnabledTimedTransitions(state);
    	Iterator<Transition> transitionIterator = enabledTransitions.iterator();
    	while (transitionIterator.hasNext()) {
    		Transition transition = transitionIterator.next();
    		if (transition.getNextFiringTime() < this.currentTime) {
    			// Set in the transition the next firing time 
        		// (this is only used to keep track of the firing inside the transitions)
    			long nextFiringTime = this.currentTime + transition.getDelay();
    			transition.setNextFiringTime(nextFiringTime);
    			// Put transition into timing table to become fired when
    			// the specified time is reached.
    			if (timingEnabledTimedTransitions.containsKey(nextFiringTime)) {
    				Set<Transition> registeredTransitions = timingEnabledTimedTransitions.get(nextFiringTime);
    				registeredTransitions.add(transition);
    				timingEnabledTimedTransitions.put(nextFiringTime, registeredTransitions);
    			} else {
    				Set<Transition> registerTransitionSet = new HashSet<>();
    			    registerTransitionSet.add(transition);
    				timingEnabledTimedTransitions.put(nextFiringTime, registerTransitionSet);
    			}
    		}
    	}
    }

    /**
     * Get the internal current time of the animated Petri network.
     */
    public long getCurrentTime() {
    	return this.currentTime;
    }
    
    /**
     * Set the internal time step of the animated Petri network.
     */
    public void setTimeStep(long newStep) {
    	this.timeStep = newStep;
    }
    
    /**
     * Get the internal time step of the animated Petri network.
     */
    public long getTimeStep() {
    	return this.timeStep;
    }
    
    /**
     * Advance current time one time step.
     */
    public void advanceSingleTimeStep() {
    	registerEnabledTimedTransitions(executablePetriNet.getState());
    	this.currentTime += this.timeStep;
    }
    
    /**
     * Advance current time.
     */
    protected void advanceToTime(long newTime) {
    	registerEnabledTimedTransitions(executablePetriNet.getState());
    	this.currentTime = newTime;
    }
    
//	protected void setCurrentTimeForTesting(long currentTime) {
//		this.currentTime =  currentTime; 
//	}
}
