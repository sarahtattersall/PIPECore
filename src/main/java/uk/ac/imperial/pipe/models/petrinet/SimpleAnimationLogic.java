package uk.ac.imperial.pipe.models.petrinet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import uk.ac.imperial.pipe.tuple.Tuple;
import uk.ac.imperial.state.State;

/**
 * This is a limited implementation of {@link AnimationLogic}, suitable only for some types of analysis.
 * The only method supported is {@link #getEnabledTransitions(State)}.  All other methods
 * return {@link UnsupportedOperationException}.
 *
 * Normal Petri net firing logic is supported by {@link PetriNetAnimationLogic}.
 * @see AnimationLogic
 * @see PetriNetAnimationLogic
 */
public final class SimpleAnimationLogic implements AnimationLogic {

    /**
     * Executable Petri net this class represents the logic for
     */
    private final ExecutablePetriNet executablePetriNet;

    /**
     * Constructor
     * @param executablePetriNet executable Petri net to perform animation logic on
     */
    public SimpleAnimationLogic(ExecutablePetriNet executablePetriNet) {
        this.executablePetriNet = executablePetriNet;
    }

    /**
     * This is a limited implementation of {@link AnimationLogic#getEnabledTransitions(State)},
     * suitable only for some types of analysis.
     * Returns the entire set of enabled transitions for the given State, including both immediate transitions
     * (all priorities) and timed transitions (all possible firing times).
     * This method is not suitable for animation; i.e., firing a transition returned by this method may put the
     * Petri net in a state that would not normally be reachable.
     * <p>For the set of transitions enabled under normal Petri net firing rules, see
     * {@link PetriNetAnimationLogic#getEnabledTransitions(State)}.
     * To retrieve a random transition that is eligible for firing under the normal Petri net firing rules, use
     * {@link PetriNetAnimationLogic#getRandomEnabledTransition()} or
     * {@link PetriNetAnimationLogic#getRandomEnabledTransition(State)}
     * </p>
     *
     * @see AnimationLogic
     * @see PetriNetAnimationLogic
     * @param state Must be a valid state for the executable Petri net
     * @return all enabled transitions
     */
    @Override
    public Set<Transition> getEnabledTransitions(State state) {
        Set<Transition> alltransitions = new HashSet<>();
        Tuple<Set<Transition>, Set<Transition>> tuple = executablePetriNet.getEnabledImmediateAndTimedTransitions();
        alltransitions.addAll(tuple.tuple1);
        alltransitions.addAll(tuple.tuple2);
        return alltransitions;
    }

    /**
     * Not implemented for this class.  Use {@link PetriNetAnimationLogic} instead
     * @param state to be evaluated
     * @return set of transitions
     * @throws UnsupportedOperationException when called
     */
    public Set<Transition> getEnabledImmediateOrTimedTransitions(State state) {
        throw new UnsupportedOperationException(
                "SimpleAnimationLogic does not support this method.  Use PetriNetAnimationLogic instead.");
    }

    /**
     * Not implemented for this class.  Use {@link PetriNetAnimationLogic} instead
     * @return set of transitions
     * @throws UnsupportedOperationException when called
     */
    public Set<Transition> getEnabledImmediateOrTimedTransitions() {
        throw new UnsupportedOperationException(
                "SimpleAnimationLogic does not support this method.  Use PetriNetAnimationLogic instead.");
    }

    /**
    * Not implemented for this class.  Use {@link PetriNetAnimationLogic} instead
    * @param state to be evaluated
    * @return random transition
    * @throws UnsupportedOperationException when called
    */
    @Override
    public Transition getRandomEnabledTransition(State state) {
        throw new UnsupportedOperationException(
                "SimpleAnimationLogic does not support this method.  Use PetriNetAnimationLogic instead.");
    }

    /**
    * Not implemented for this class.  Use {@link PetriNetAnimationLogic} instead
    * @return random transition
    * @throws UnsupportedOperationException when called
    */
    @Override
    public Transition getRandomEnabledTransition() {
        throw new UnsupportedOperationException(
                "SimpleAnimationLogic does not support this method.  Use PetriNetAnimationLogic instead.");
    }

    /**
    * Not implemented for this class.  Use {@link PetriNetAnimationLogic} instead
    * @param state to be evaluated
    * @return map of state and transitions
    * @throws UnsupportedOperationException when called
    */
    @Override
    public Map<State, Collection<Transition>> getSuccessors(State state) {
        throw new UnsupportedOperationException(
                "SimpleAnimationLogic does not support this method.  Use PetriNetAnimationLogic instead.");
    }

    /**
     * Not implemented for this class.  Use {@link PetriNetAnimationLogic} instead
     * @param state to be evaluated
     * @param updateState if state should be updated
     * @return map of state and transitions
     * @throws UnsupportedOperationException when called
     */
    @Override
    public Map<State, Collection<Transition>> getSuccessors(State state, boolean updateState) {
        throw new UnsupportedOperationException(
                "SimpleAnimationLogic does not support this method.  Use PetriNetAnimationLogic instead.");
    }

    /**
     * Not implemented for this class.  Use {@link PetriNetAnimationLogic} instead
     * @param state to be evaluated
     * @return state after firing
     * @throws UnsupportedOperationException when called
     */
    @Override
    public State getFiredState(Transition transition, State state) {
        throw new UnsupportedOperationException(
                "SimpleAnimationLogic does not support this method.  Use PetriNetAnimationLogic instead.");
    }

    /**
     * Not implemented for this class.  Use {@link PetriNetAnimationLogic} instead
     * @param transition to be evaluated
     * @return state after firing
     * @throws UnsupportedOperationException when called
     */
    @Override
    public State getFiredState(Transition transition) {
        throw new UnsupportedOperationException(
                "SimpleAnimationLogic does not support this method.  Use PetriNetAnimationLogic instead.");
    }

    /**
     * Not implemented for this class.  Use {@link PetriNetAnimationLogic} instead
     * @throws UnsupportedOperationException when called
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException(
                "SimpleAnimationLogic does not support this method.  Use PetriNetAnimationLogic instead.");
    }

    /**
     * Not implemented for this class.  Use {@link PetriNetAnimationLogic} instead
     * @param random for pseudo-random firing
     * @throws UnsupportedOperationException when called
     */
    @Override
    public void setRandom(Random random) {
        throw new UnsupportedOperationException(
                "SimpleAnimationLogic does not support this method.  Use PetriNetAnimationLogic instead.");
    }

    /**
     * Not implemented for this class.  Use {@link PetriNetAnimationLogic} instead
     * @throws UnsupportedOperationException when called
     */
    @Override
    public void stopAnimation() {
        throw new UnsupportedOperationException(
                "SimpleAnimationLogic does not support this method.  Use PetriNetAnimationLogic instead.");
    }

    /**
     * Not implemented for this class.  Use {@link PetriNetAnimationLogic} instead
     * @throws UnsupportedOperationException when called
     */
    @Override
    public void startAnimation() {
        throw new UnsupportedOperationException(
                "SimpleAnimationLogic does not support this method.  Use PetriNetAnimationLogic instead.");
    }

    /**
     * Not implemented for this class.  Use {@link PetriNetAnimationLogic} instead
     * @param transition to be evaluated
     * @return state after firing
     * @throws UnsupportedOperationException when called
     */
    @Override
    public State getBackwardsFiredState(Transition transition) {
        throw new UnsupportedOperationException(
                "SimpleAnimationLogic does not support this method.  Use PetriNetAnimationLogic instead.");
    }

}
