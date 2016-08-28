package uk.ac.imperial.pipe.animation;

import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.state.State;

import java.util.Collection;
import java.util.Map;
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
    Set<Transition> getEnabledTransitions(State state);

    /**
     * Calculates successor states of a given state
     *
     * @param state to be evaluated
     * @return successors of the given state
     */
    Map<State, Collection<Transition>> getSuccessors(State state);

    /**
     *
     * @param state to be evaluated
     * @param transition to be fired
     * @return the successor state after firing the transition
     */
    State getFiredState(State state, Transition transition);

    /**
     *
     * @param state  petri net state to evaluate weight against
     * @param weight a functional weight
     * @return the evaluated weight for the given state
     */
    double getArcWeight(State state, String weight);

    /**
     * Clears any caching done in the animation logic
     * This method helps with memory usage once you know
     * a state will no longer be visited etc.
     */
    void clear();
}
