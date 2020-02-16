package uk.ac.imperial.pipe.runner;

import uk.ac.imperial.state.State;

/**
 * The result of firing a single transition.
 * Round is the number of this firing in the sequence of the current execution.  
 * Round 0 represents the initial state, prior to any transitions being fired.  The transition will be "".  
 * <p>
 * Transition is the id of the {@link uk.ac.imperial.pipe.models.petrinet.Transition} that fired
 * <p>
 * State is the {@link uk.ac.imperial.state.State} that resulted from firing the transition
 */

public class Firing {

    public final State state;
    public final String transition;
    public final int round;

    public Firing(int round, String transition, State state) {
        this.round = round;
        this.transition = transition;
        this.state = state;
    }

}
