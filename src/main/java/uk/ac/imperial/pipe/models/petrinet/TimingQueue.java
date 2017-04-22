package uk.ac.imperial.pipe.models.petrinet;

import java.util.Set;

import uk.ac.imperial.state.HashedStateBuilder;
import uk.ac.imperial.state.State;

public abstract class TimingQueue {
	
	protected State state;
	protected long currentTime;
	
	protected TimingQueue() {
		super();
	}
	public TimingQueue(long time) {
		this.currentTime = time;
	}
	
	public TimingQueue(State state, long time) {
		this(time);
		HashedStateBuilder builder = new HashedStateBuilder();
        for (String placeId : state.getPlaces()) {
            builder.placeWithTokens(placeId, state.getTokens(placeId));
        }
    	this.state = builder.build();
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}
	    
	public long getCurrentTime() {
		return this.currentTime;
	}
	    
	public abstract void resetTimeAndRebuildTimedTransitions(long newInitTime);
	
	public abstract TimingQueue makeCopy();
	
	public abstract long getNextFiringTime();
	
	public abstract boolean hasUpcomingTimedTransition();
	
	public abstract Set<Transition> getCurrentlyEnabledTimedTransitions();
	
    public abstract void registerEnabledTimedTransitions(Set<Transition> enabledTransitions);
    
    public abstract boolean unregisterTimedTransition(Transition transition, long atTime);
    
	public abstract Set<Long> getAllFiringTimes();
	
	public abstract Set<Transition> getEnabledTransitionsAtTime(long nextTime);
	
    public abstract void setCurrentTime(long newTime);
}
