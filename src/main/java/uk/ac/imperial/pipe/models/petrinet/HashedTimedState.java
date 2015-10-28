package uk.ac.imperial.pipe.models.petrinet;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Set;
import java.util.Collections;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;

import uk.ac.imperial.state.HashedStateBuilder;
import uk.ac.imperial.state.State;

public class HashedTimedState extends TimedState {
	
	private ConcurrentSkipListMap<Long, Set<Transition>> enabledTimedTransitions;
	private ExecutablePetriNet executablePetriNet;
	
	public HashedTimedState(ExecutablePetriNet epn, State state, ConcurrentSkipListMap<Long, Set<Transition>> timedTrans, long time) {
		// TODO: Both copying is probably very expensive!
	    this(epn, state, time);
	    this.enabledTimedTransitions = new ConcurrentSkipListMap<Long, Set<Transition>>();
	    Iterator<Entry<Long, Set<Transition>>> entryIterator = timedTrans.entrySet().iterator();
	    while (entryIterator.hasNext() ) {
	    	Entry<Long, Set<Transition>> nextEntry = entryIterator.next();
	    	this.enabledTimedTransitions.put(nextEntry.getKey(), 
	    			new HashSet<Transition>(nextEntry.getValue()) );	    	
	    }
	}
	
	public HashedTimedState(ExecutablePetriNet epn, State state, long time) {
		super(state, time);
		this.executablePetriNet = epn;
    	this.enabledTimedTransitions = new ConcurrentSkipListMap<>();
    	setCurrentTime(time);
	}

	public String toString() { 
		return "(" + this.state + ", " + this.enabledTimedTransitions + ", " + this.currentTime + ")"; 
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public Set<Transition> getCurrentlyEnabledTimedTransitions() {
		if (this.enabledTimedTransitions.containsKey(this.currentTime)) {
			return this.enabledTimedTransitions.get(this.currentTime);
		} else {
			return ( Collections.emptySet() );
		}
	}
	
	public ConcurrentSkipListMap<Long, Set<Transition>> getHashedTimedStateMap() {
		return this.enabledTimedTransitions;
	}

//	public void setSecond(ConcurrentSkipListMap<Long, Set<Transition>> transitions) {
//		this.enabledTimedTransitions = transitions;
//	}
	    
	public long getCurrentTime() {
		return this.currentTime;
	}
	    
	public void resetTimeAndTimedTransitions(long newInitTime) {
		this.enabledTimedTransitions = new ConcurrentSkipListMap<Long, Set<Transition>>();
		setCurrentTime(newInitTime);
	}
	

    /**
     * @return all the currently enabled immediate transitions in the petri net
     */
    public Set<Transition> getEnabledImmediateTransitions() {

        Set<Transition> enabledTransitions = new HashSet<>();
        for (Transition transition : executablePetriNet.getTransitions()) {
            if (isEnabled(transition) && !transition.isTimed()) {
                enabledTransitions.add(transition);
            }
        }
        return enabledTransitions;
    }
    
    /**
     * @return all the currently enabled timed transitions in the petri net
     */
    public Set<Transition> getEnabledTimedTransitions() {
        Set<Transition> enabledTransitions = new HashSet<>();
        for (Transition transition : executablePetriNet.getTransitions()) {
            if (isEnabled(transition) & transition.isTimed()) {
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
    public boolean isEnabled(Transition transition) {
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
	
	/*public void advanceTime(long newTime) {
		
	}*/
	
	public long getNextFiringTime() {
		return this.enabledTimedTransitions.ceilingKey( this.currentTime );
	}
	
	public Set<Long> getNextFiringTimes() {
		return this.enabledTimedTransitions.keySet();
	}
	
	public Set<Transition> getEnabledTransitionsAtTime(long nextTime) {
		return this.enabledTimedTransitions.get(nextTime);
	}
	
	public boolean hasUpcomingTimedTransition() {
		return (this.enabledTimedTransitions.ceilingKey( this.currentTime ) != null);
	}
	
	public TimedState makeCopy() {
		HashedStateBuilder builder = new HashedStateBuilder();
        for (String placeId : this.state.getPlaces()) {
            //Copy tokens
            builder.placeWithTokens(placeId, this.state.getTokens(placeId));
        }
	    return (new HashedTimedState(executablePetriNet, builder.build(), this.enabledTimedTransitions, this.currentTime));
	}
	
	private boolean checkIfValueNotRegistered(Transition transition) {
		boolean transitionNotRegistered = true;
		Collection<Set<Transition>> transitionSets = this.enabledTimedTransitions.values();
	    Iterator itr = transitionSets.iterator();
	    while (itr.hasNext()) {
	    	Set<Transition> nextSet = (Set<Transition>) itr.next();
	    	if (nextSet.contains(transition)) {
	    		transitionNotRegistered = false;
	    		break;
	    	}
	    }
	    return transitionNotRegistered;
	}
	
	public void unregisterTimedTransition(Transition transition, long atTime) {
		this.enabledTimedTransitions.get(atTime).remove(transition);
		if (this.enabledTimedTransitions.get(atTime).isEmpty()) {
			this.enabledTimedTransitions.remove(atTime);
		}
	}
	
    /**
     * For the current time all enabled timed transitions are 
     * put in the timing queue = when time is advanced they can get activated when 
     * the delay is gone.
     * 
     * @param state  petri net state to evaluate weight against
     */
    public void registerEnabledTimedTransitions(Set<Transition> enabledTransitions) {
    	//Set<Transition> enabledTransitions = getEnabledTimedTransitions(timedState.getState());
    	Iterator<Transition> transitionIterator = enabledTransitions.iterator();
    	while (transitionIterator.hasNext()) {
    		Transition transition = transitionIterator.next();
    		if (checkIfValueNotRegistered(transition)) {
    		//if (transition.getNextFiringTime() <= this.currentTime) {
    			// Set in the transition the next firing time 
        		// (this is only used to keep track of the firing inside the transitions)
    			long nextFiringTime = this.currentTime + transition.getDelay();
    			
    			// TODO: This information has to be removed from the transition - it is
    			// only part of the state
    			// Put transition into timing table to become fired when
    			// the specified time is reached.
    			if (this.enabledTimedTransitions.containsKey(nextFiringTime)) {
    				Set<Transition> registeredTransitions = this.enabledTimedTransitions.get(nextFiringTime);
    				registeredTransitions.add(transition);
    				this.enabledTimedTransitions.put(nextFiringTime, registeredTransitions);
    			} else {
    				Set<Transition> registerTransitionSet = new HashSet<>();
    			    registerTransitionSet.add(transition);
    				this.enabledTimedTransitions.put(nextFiringTime, registerTransitionSet);
    			}
    		}
    	}
    }

}
