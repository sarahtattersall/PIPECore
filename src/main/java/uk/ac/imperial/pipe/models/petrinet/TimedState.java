package uk.ac.imperial.pipe.models.petrinet;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

import uk.ac.imperial.state.HashedStateBuilder;
import uk.ac.imperial.state.State;

public abstract class TimedState {
	
	protected State state;
	protected long currentTime;
	
	protected TimedState() {
		super();
	}
	
	public TimedState(State state, long time) {
		HashedStateBuilder builder = new HashedStateBuilder();
        for (String placeId : state.getPlaces()) {
            builder.placeWithTokens(placeId, state.getTokens(placeId));
        }
    	this.state = builder.build();
		this.currentTime = time;
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
	    
	public abstract void resetTimeAndTimedTransitions(long newInitTime);
	
	public abstract TimedState makeCopy();
	
	public abstract long getNextFiringTime();
	
	public abstract boolean hasUpcomingTimedTransition();
	
	public abstract Set<Transition> getCurrentlyEnabledTimedTransitions();
	
    public abstract void registerEnabledTimedTransitions(Set<Transition> enabledTransitions);
    
    public abstract void unregisterTimedTransition(Transition transition, long atTime);
    
	public abstract Set<Long> getNextFiringTimes();
	
	public abstract Set<Transition> getEnabledTransitionsAtTime(long nextTime);
	
    public abstract void setCurrentTime(long newTime);
}
