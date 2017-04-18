package uk.ac.imperial.pipe.animation;

import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import uk.ac.imperial.pipe.models.petrinet.TimingQueue;
import uk.ac.imperial.pipe.models.petrinet.Transition;

/**
 * Contains methods which deal with the logic of animation, for instance
 * which transitions are enabled for a given state.
 */
public interface AnimationLogic {
    /**
     * @deprecated  can lead to confusion between immediate and timed transitions.  Use {@link #getRandomEnabledTransition(TimingQueue)}
     * @param state Must be a valid state for the Petri net this class represents
     * @return all enabled transitions
     */
	@Deprecated
    Set<Transition> getEnabledTransitions(TimingQueue state);
	
    /**
     * @return a random transition that can fire
     */
    Transition getRandomEnabledTransition(TimingQueue state);

    /**
     * Calculates successor states of a given state
     *
     * @param state to be evaluated
     * @return successors of the given state
     */
    Map<TimingQueue, Collection<Transition>> getSuccessors(TimingQueue state);

    /**
     *
     * @param state to be evaluated
     * @param transition to be fired
     * @return the successor state after firing the transition
     */
    TimingQueue getFiredState(TimingQueue state, Transition transition);


    /**
     * Clears any caching done in the animation logic
     * This method helps with memory usage once you know
     * a state will no longer be visited etc.
     */
    void clear();
    
    /**
	 * Generate predictable results for repeated testing of a given Petri net by providing a Random built from the same long seed for each run.  
	 * Otherwise, a new Random will be used on each execution, leading to different firing patterns. 
	 * @param random
	 */
	public void setRandom(Random random);
	/**
	 * Clients must call to indicate the end of animation
	 * All transitions will be marked as disabled
	 */
	public void stopAnimation();
	/**
	 * Clients must call to indicate the beginning of animation
	 * All initially enabled transitions will be marked as enabled
	 */
	public void startAnimation();
}
