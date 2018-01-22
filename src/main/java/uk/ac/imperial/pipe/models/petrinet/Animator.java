package uk.ac.imperial.pipe.models.petrinet;

import java.util.Random;

/**
 * Class responsible for calculating transitions that can fire etc.
 */
public interface Animator {
    public static final String ERROR_NO_TRANSITIONS_TO_FIRE = "Error - no transitions to fire!";

    /**
     * @return a random transition that can fire, or null if no transitions are enabled
     */
    public Transition getRandomEnabledTransition();

    /**
     * Removes the relevant number tokens from places into the transition
     * Adds tokens to the places out of the transition according to the arc weight
     *
     * <p> Handles functional weights e.g. removing all of a places tokens and adding them
     * to the receiving place by calculating all incidence matrices before setting any token counts
     * </p>
     * <p>Recalculates enabled transitions
     *</p>
     * @param transition transition to fire
     */
    public void fireTransition(Transition transition);

    /**
     * Removes tokens from places out of the transition
     * Adds tokens to the places into the transition according to the arc weight
     * Enables fired transition
     *
     * @param transition transition to fire backwards
     */
    // TODO: NOT SURE IF BETTER TO JUST HAVE UNDO/REDO IN ANIMATION HISTORY? HAVE TO STORE ENTIRE PETRI
    //       NET STATES SO MAYBE NOT?
    public void fireTransitionBackwards(Transition transition);

    /**
     * Gets the animation logic that determines what transitions are eligible for animation
     * @return AnimationLogic
     */
    public AnimationLogic getAnimationLogic();

    /**
     * Prepare to animate:  save state, and mark initially enabled transitions
     */
    public void startAnimation();

    /**
     * Resets the state to the last saved net
     */
    public void reset();

    /**
     * Saves the current state of the Petri net, allowing it to
     * be reset at any point
     */
    public void saveState();

    /**
     * Generate predictable results for repeated testing of a given Petri net by providing a Random built from the same long seed for each run.
     * Otherwise, a new Random will be used on each execution, leading to different firing patterns.
     * @param random to use for pseudo-random operations
     */
    public void setRandom(Random random);
}
