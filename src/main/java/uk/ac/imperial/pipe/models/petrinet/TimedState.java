package uk.ac.imperial.pipe.models.petrinet;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

import uk.ac.imperial.state.State;

public class TimedState {
	
	private State state;
	private ConcurrentSkipListMap<Long, Set<Transition>> enabledTimedTransitions;
	private long currentTime, initialTime;

	public TimedState(State state, ConcurrentSkipListMap<Long, Set<Transition>> timedTrans, long time) {
	    this(state, time);
	    this.enabledTimedTransitions = new ConcurrentSkipListMap<Long, Set<Transition>>(timedTrans);
	}
	
	public TimedState(State state, long time) {
    	super();
    	this.state = state;
    	this.enabledTimedTransitions = new ConcurrentSkipListMap<>();
    	this.initialTime = time;
    	this.currentTime = time;
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

	public ConcurrentSkipListMap<Long, Set<Transition>> getEnabledTimedTransitions() {
		return this.enabledTimedTransitions;
	}

	public void setSecond(ConcurrentSkipListMap<Long, Set<Transition>> transitions) {
		this.enabledTimedTransitions = transitions;
	}
	    
	public long getCurrentTime() {
		return this.currentTime;
	}
	    
	public void setCurrentTime(long newTime) {
		this.currentTime = newTime;
	}

}
