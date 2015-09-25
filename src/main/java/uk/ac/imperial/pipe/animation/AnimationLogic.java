package uk.ac.imperial.pipe.animation;

import uk.ac.imperial.pipe.models.petrinet.Transition;

import uk.ac.imperial.state.State;
import uk.ac.imperial.pipe.models.petrinet.TimedState;

import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Contains methods which deal with the logic of animation, for instance
 * which transitions are enabled for a given state.
 */
public interface AnimationLogic {
    /**
     * @param state Must be a valid state for the Petri net this class represents
     * @return all enabled transitions
     */
	//STEVE: I propose to remove this method (at least from the interface)
	// In my opinion, the AnimationLogic is following the right sequence of which transitions can
	// be fired. The animator only accesses it through the getNextRandomTransition.
	// Or if you really want to mess around you can getImmediateTransitions and get getTimedTransitions 
	// and do whatever you want with it. But putting them all in one place will surely lead
	// to someone using it the wrong way.
    Set<Transition> getEnabledTransitions(TimedState state);
	
    /**
     * @return a random transition that can fire
     */
    Transition getRandomEnabledTransition(TimedState state);

    /**
     * Calculates successor states of a given state
     *
     * @param state
     * @return successors of the given state
     */
    Map<TimedState, Collection<Transition>> getSuccessors(TimedState state);

    /**
     *
     * @param state
     * @param transition
     * @return the successor state after firing the transition
     */
    TimedState getFiredState(TimedState state, Transition transition);

    /**
     *
     * @param state  petri net state to evaluate weight against
     * @param weight a functional weight
     * @return the evaluated weight for the given state
     */
    double getArcWeight(TimedState state, String weight);

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
}
