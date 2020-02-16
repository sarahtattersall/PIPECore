package uk.ac.imperial.pipe.models.petrinet;

import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import uk.ac.imperial.pipe.runner.Runner;
import uk.ac.imperial.state.State;

/**
 * Contains methods which deal with the logic of animation or running an {@link ExecutablePetriNet},
 * for instance, which transitions are enabled for a given state.  This may be used both for analysis purposes
 * and for execution purposes (animation or running an executable Petri net).  When animating or running
 * an executable Petri net, the state of the executable Petri net may be changed.  When performing analysis,
 * multiple different States may be processed, other than the current State.  Therefore, many methods have two flavors:
 * <ul>
 * <li> someProcessing():  performs some processing against the current State of the ExecutablePetriNet.
 * This may include updating the current State.
 * <li> someProcessing(State state):  performs some processing against the given state, as if it were the state of
 * the ExecutablePetriNet.  The current State of the ExecutablePetriNet, however, is neither referenced nor updated.
 * </ul>
 * <p>This interface has two implementations.  For most use cases, including normal analysis and execution, use
 * {@link PetriNetAnimationLogic}.  For some limited analysis purposes, {@link SimpleAnimationLogic} may be appropriate.</p>
 *
 * @see PetriNet
 * @see ExecutablePetriNet
 * @see State
 * @see Runner
 * @see Animator
 * @see PetriNetAnimationLogic
 * @see SimpleAnimationLogic
 */
public interface AnimationLogic {
    /**
     * Returns the set of enabled transitions for the given State,
     * as determined by the particular implementation of AnimationLogic used.
     * <p>For most purposes, {@link PetriNetAnimationLogic} returns appropriate results, i.e.,
     * the set of enabled transitions that could fire at the current time, following the normal Petri net firing rules.
     * Specifically, this means that the highest priority immediate transitions are returned.
     * If there are no enabled immediate transitions, then the timed transitions that can fire
     * at the current time ({@link ExecutablePetriNet#getCurrentTime()}) are returned.
     * To retrieve a random transition that is eligible for firing under these rules, use
     * {@link AnimationLogic#getRandomEnabledTransition()} or {@link AnimationLogic#getRandomEnabledTransition(State)}
     * </p><p>Only for analysis purposes, the entire set of transitions that are enabled for the given State
     * can be returned by using {@link SimpleAnimationLogic#getEnabledTransitions(State)}.
     * This method is not suitable for animation.
     * </p>
     *
     * @see SimpleAnimationLogic
     * @see PetriNetAnimationLogic
     * @param state Must be a valid state for the executable Petri net
     * @return all enabled transitions, as determined by the AnimationLogic implementation
     */
    public Set<Transition> getEnabledTransitions(State state);

    /**
     * From the enabled transitions, returns one, given the provided State of the Executable Petri Net.
     * <p>
     * The transition which is returned is controlled pseudo-randomly, and can be made deterministic by
     * providing a seed:  {@link #setRandom(Random)}. </p>
     * @see #getRandomEnabledTransition()
     * @param state the State of the executable Petri net against which transitions are evaluated
     * @return a random transition that can fire
     */
    public Transition getRandomEnabledTransition(State state);

    /**
     * From the enabled transitions, returns one from the current State of the Executable Petri Net
     * ({@link ExecutablePetriNet#getState()})
     * <p>
     * The transition which is returned is controlled pseudo-randomly, and can be made deterministic by
     * providing a seed:  {@link #setRandom(Random)}. </p>
     * @see #getRandomEnabledTransition(State)
     * @return a random transition that can fire
     */
    public Transition getRandomEnabledTransition();

    /**
     * Calculates successor states of a given state of the executable Petri net.  This will both calculate
     * the successors, as well as update the current State of the executable Petri net to the first successor found.
     * This is equivalent to {@link #getSuccessors(State, boolean)} where updateState is true.
     * Although useful for testing in cases where
     * there is expected to be 0 or 1 successor, in general this
     * is unlikely to be the desired behavior; more likely is {@link #getSuccessors(State, boolean)},
     * where updateState is false
     *
     * @param state to be evaluated
     * @return successors of the given state
     */
    Map<State, Collection<Transition>> getSuccessors(State state);

    /**
     * Calculates successor states of a given state.  When updateState is false (the normal case), the successor
     * states will be calculated, but the State of the executable Petri net is left unchanged.
     * When updateState is true, this is equivalent to {@link #getSuccessors(State)}
     *
     * @param state to be evaluated
     * @param updateState whether the State of the executable Petri net should be updated or left unchanged
     * @return successors of the given state
     */
    Map<State, Collection<Transition>> getSuccessors(State state, boolean updateState);

    /**
     * Get the State that results from firing the specified Transition, given the provided State
     * of the Executable Petri Net.
     * @see #getFiredState(Transition)
     * @param state to be evaluated
     * @param transition to be fired
     * @return the successor state after firing the transition
     */
    //TODO consider throwing if the specified Transition is not eligible to be fired under the firing rules
    public State getFiredState(Transition transition, State state);

    /**
     * Get the State that results from firing the specified Transition in the current State of the
     * Executable Petri Net.  ({@link ExecutablePetriNet#getState()})
     * @see #getFiredState(Transition, State)
     * @param transition to be fired
     * @return the successor state after firing the transition
     */
    //TODO consider throwing if the specified Transition is not eligible to be fired under the firing rules
    public State getFiredState(Transition transition);

    /**
     * Get the State that generated the current state of executable Petri net through the firing
     * of the specified Transition  ({@link ExecutablePetriNet#getState()})
     * This is the reverse of {@link #getFiredState(Transition)}, i.e., executing these two operations in
     * either order from an arbitrary starting point should return the Executable Petri Net to that
     * starting point (if there is a valid preceding state).
     * @param transition to be fired
     * @return the predecessor state prior to firing the transition
     */
    public State getBackwardsFiredState(Transition transition);

    /**
     * Clears any caching done in the animation logic
     * This method helps with memory usage once you know
     * a state will no longer be visited etc.
     */
    void clear();

    /**
     * Generate predictable results for repeated testing of a given Petri net by providing a Random built from the same long seed for each run.
     * Otherwise, a new Random will be used on each execution, leading to different firing patterns.
     * @param random to generate deterministic pseudo-random firing pattern
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
