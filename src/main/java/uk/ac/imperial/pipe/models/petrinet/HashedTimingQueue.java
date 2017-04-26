package uk.ac.imperial.pipe.models.petrinet;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

import uk.ac.imperial.state.HashedStateBuilder;
import uk.ac.imperial.state.State;

public class HashedTimingQueue extends TimingQueue {
	
	protected ConcurrentSkipListMap<Long, Set<Transition>> enabledTimedTransitions;
	private ExecutablePetriNet executablePetriNet;
	
	public HashedTimingQueue(ExecutablePetriNet epn, State state, ConcurrentSkipListMap<Long, Set<Transition>> timedTrans, long time) {
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
	
	public HashedTimingQueue(ExecutablePetriNet epn, State state, long time) {
		super(state, time);
		this.executablePetriNet = epn;
    	this.enabledTimedTransitions = new ConcurrentSkipListMap<>();
    	setCurrentTime(time);
	}
	public HashedTimingQueue(ExecutablePetriNet epn, long time) {
		super(time);
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
	/**
	 * For the current time, returns all timed transitions that are enabled to fire
	 * @return set of transitions that are enabled to fire for the current time
	 */
	public Set<Transition> getCurrentlyEnabledTimedTransitions() {
		if (this.enabledTimedTransitions.containsKey(this.currentTime)) {
			return this.enabledTimedTransitions.get(this.currentTime);
		} else {
			return Collections.emptySet() ;
		}
	}
	
	public ConcurrentSkipListMap<Long, Set<Transition>> getTimingQueueMap() {
		return this.enabledTimedTransitions;
	}
	    
	public long getCurrentTime() {
		return this.currentTime;
	}
	/**
	 * Sets the current time to a new time, and then rebuilds the timing queue using the new current
	 * time as the initial offset.
	 * @param newInitTime new time to use the base for building the timing queue
	 */
	public void resetTimeAndRebuildTimedTransitions(long newInitTime) {
		this.enabledTimedTransitions = new ConcurrentSkipListMap<Long, Set<Transition>>();
		setCurrentTime(newInitTime);
	}
	/**
	 * @return the next time at which at least one transition will fire.  
	 * If the current time has at least one transition, the current time will be returned. 
	 * If the timing queue is empty, returns -1.   
	 */
	public long getNextFiringTime() {
		if (enabledTimedTransitions.size() > 0) {
			return this.enabledTimedTransitions.ceilingKey( this.currentTime );
		} else {
			return -1l; 
		}
	}
	/**
	 * @return set of all times at which transitions are scheduled to fire, including times 
	 * that have already past.   
	 */
	//TODO make private 
	public Set<Long> getAllFiringTimes() {
		return this.enabledTimedTransitions.keySet();
	}
	/**
	 * @return set of all transitions scheduled to fire at the given time, or an empty set,
	 * if the time does not exist in the timing queue.  
	 */
	//TODO:  make private 
	public Set<Transition> getEnabledTransitionsAtTime(long nextTime) {
		if (this.enabledTimedTransitions.containsKey(nextTime)) {
			return this.enabledTimedTransitions.get(nextTime);
		} else {
			return  Collections.emptySet(); 
		}
	}
	/**
	 * @return true if there is at least one transition at the current or later time
	 */
	public boolean hasUpcomingTimedTransition() {
		return (this.enabledTimedTransitions.ceilingKey( this.currentTime ) != null);
	}
	
	public TimingQueue makeCopy() {
		HashedStateBuilder builder = new HashedStateBuilder();
        for (String placeId : this.state.getPlaces()) {
            //Copy tokens
            builder.placeWithTokens(placeId, this.state.getTokens(placeId));
        }
	    return (new HashedTimingQueue(executablePetriNet, builder.build(), this.enabledTimedTransitions, this.currentTime));
	}
	/**
	 * verifies whether a given transition does not exist anywhere in the timing queue 
	 * @param transition to be verified
	 * @return true if the transition does not exist in the timing queue
	 */
	protected boolean checkIfTransitionNotRegistered(Transition transition) {
		boolean transitionNotRegistered = true;
		Collection<Set<Transition>> transitionSets = this.enabledTimedTransitions.values();
	    Iterator<Set<Transition>> it = transitionSets.iterator();
	    while (it.hasNext()) {
	    	Set<Transition> nextSet = it.next();
	    	if (nextSet.contains(transition)) {
	    		transitionNotRegistered = false;
	    		break;
	    	}
	    }
	    return transitionNotRegistered;
	}
	/**
	 * If a transition exists in the timing queue, remove it.  
	 * @param transition to be removed
	 * @param atTime time at which transition was to be fired
	 * @return true if transition was removed at the specified time; false if atTime did not 
	 *   exist in the timing queue or if the transition was not at that time 
	 *   (in which case transition might exist at another time -- logic error) 
	 */
	public boolean unregisterTimedTransition(Transition transition, long atTime) {
		boolean unregistered = false;
		if (this.enabledTimedTransitions.containsKey(atTime)) {
			unregistered = this.enabledTimedTransitions.get(atTime).remove(transition);
			removeFiringTimeIfEmpty(atTime);
		} else {
			unregistered = false; 
		}
		return unregistered; 
	}
	public boolean dequeue(Transition transition, State state) {
		boolean unregistered = false;
		for (Set<Transition> transitions : enabledTimedTransitions.values()) {
			if (transitions.remove(transition)) {
				unregistered = true; 
			}
		}
		verifyPendingTransitionsStillActive(state);
		registerEnabledTimedTransitions( this.executablePetriNet.getEnabledTimedTransitions(state));
		return unregistered; 
	}

	
	@Override
	public void verifyPendingTransitionsStillActive(State state) {
		Iterator<Long> timeIterator = getAllFiringTimes().iterator();  
		while (timeIterator.hasNext()) {
			Long nextFiringTime = timeIterator.next();
			Set<Transition> enabledTransitions = removeEmptyTimeSlots(timeIterator, nextFiringTime);
			Iterator<Transition> transitionIterator = enabledTransitions.iterator();	
			while (transitionIterator.hasNext()) {
				Transition nextChecked = transitionIterator.next(); 
				if (!(executablePetriNet.isEnabled( nextChecked, state ) )) { 
					transitionIterator.remove(); 
					removeEmptyTimeSlots(timeIterator, nextFiringTime);
				}
			}
        }
	}

	private Set<Transition> removeEmptyTimeSlots(Iterator<Long> timeIterator,
			Long nextFiringTime) {
		Set<Transition> enabledTimedTransitions = getEnabledTransitionsAtTime(nextFiringTime); 
		if (enabledTimedTransitions.isEmpty()) {
			timeIterator.remove(); 
		}
		return enabledTimedTransitions; 
	}

	
	@Override
	public boolean unregisterTimedTransition(Transition nextChecked,
			Long nextFiringTime, Iterator<Transition> transitionIterator,
			Iterator<Long> timeIterator) {
		transitionIterator.remove();
		return false;
	}

	
	protected void removeFiringTimeIfEmpty(long atTime) {
		if (this.enabledTimedTransitions.get(atTime).isEmpty()) {
			this.enabledTimedTransitions.remove(atTime);
		}
	}
	
    /**
     * For the current time all enabled timed transitions are 
     * put in the timing queue = when time is advanced they can get activated when 
     * the delay is gone.
     * 
     * @param enabledTransitions  set of enabled timed transitions to be registered for timed firing
     */
    public void registerEnabledTimedTransitions(Set<Transition> enabledTransitions) {
    	Iterator<Transition> transitionIterator = enabledTransitions.iterator();
    	while (transitionIterator.hasNext()) {
    		Transition transition = transitionIterator.next();
    		if (checkIfTransitionNotRegistered(transition)) {
    			registerTransition(transition);
    		}
    	}
    }

	protected void registerTransition(Transition transition) {
		long nextFiringTime = this.currentTime + transition.getDelay();
		Set<Transition> registeredTransitions = null; 
		if (this.enabledTimedTransitions.containsKey(nextFiringTime)) {
			registeredTransitions = this.enabledTimedTransitions.get(nextFiringTime);
			addTransition(transition, nextFiringTime, registeredTransitions);
		} else {
			registeredTransitions = new HashSet<>();
			addTransition(transition, nextFiringTime, registeredTransitions);
		}
	}

	protected void addTransition(Transition transition, long nextFiringTime,
			Set<Transition> registeredTransitions) {
		registeredTransitions.add(transition);
		this.enabledTimedTransitions.put(nextFiringTime, registeredTransitions);
	}

	public void setCurrentTime(long newTime) {
		this.currentTime = newTime;
		registerEnabledTimedTransitions( this.executablePetriNet.getEnabledTimedTransitions() );
	}


}
