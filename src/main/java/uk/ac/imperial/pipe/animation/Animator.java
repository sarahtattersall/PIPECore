package uk.ac.imperial.pipe.animation;

import uk.ac.imperial.pipe.models.petrinet.Transition;

import java.util.Random;
import java.util.Set;

/**
 * Class responsible for calculating transitions that can fire etc.
 */
public interface Animator {
    public static final String ERROR_NO_TRANSITIONS_TO_FIRE = "Error - no transitions to fire!";

	/**
     * Saves the current state of the Petri net, allowing it to
     * be reset at any point
     */
    void saveState();

    /**
     * Resets the state to the last saved net
     */
    void reset();

    /**
     * @return a random transition that can fire
     */
// TODO: Should be given by animation logic.
    Transition getRandomEnabledTransition();

    /**
     * Finds all of the transitions which are enabled
     * If there are any immediate transitions then these take priority
     * and timed transactions are not counted as enabled
     * 
     * <p> It also disables any immediate transitions with a lower
     * priority than the highest available priority. </p>
     * 
     *
     * @deprecated use {@link #getRandomEnabledTransition()}
     * 
     * @return all transitions that can be enabled
     */
    @Deprecated
    Set<Transition> getEnabledTransitions();


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
    void fireTransition(Transition transition);

    /**
     * Removes tokens from places out of the transition
     * Adds tokens to the places into the transition according to the arc weight
     * Enables fired transition
     *
     * @param transition transition to fire backwards
     */
    // TODO: NOT SURE IF BETTER TO JUST HAVE UNDO/REDO IN ANIMATION HISTORY? HAVE TO STORE ENTIRE PETRI
    //       NET STATES SO MAYBE NOT?
    void fireTransitionBackwards(Transition transition);

    /**
	 * Generate predictable results for repeated testing of a given Petri net by providing a Random built from the same long seed for each run.  
	 * Otherwise, a new Random will be used on each execution, leading to different firing patterns. 
	 * @param random to use for pseudo-random operations
	 */
	public void setRandom(Random random);
	/**
	 * Gets the animation logic that determines what transitions are eligible for animation 
	 * @return AnimationLogic
	 */
	public AnimationLogic getAnimationLogic();

	/**
	 * Prepare to animate:  save state, and mark initially enabled transitions 
	 */
	public void startAnimation();
}
