package uk.ac.imperial.pipe.animation;

import uk.ac.imperial.pipe.models.petrinet.Arc;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.state.State;

import java.util.*;

/**
 * Contains methods to help with animating the Petri net and performs
 * in place modifications to the Petri net.
 */
public final class PetriNetAnimator implements Animator {
    private static final String ERROR_NO_TRANSITIONS_TO_FIRE = "Error - no transitions to fire!";

	/**
     * Petri net to animate
     */
    private final PetriNet petriNet;

    /**
     * Underlying animation logic, which returns logic as Markov Chain states
     */
    private final AnimationLogic animationLogic;

    /**
     * map of place id -> {token id -> count} and is used to save the underlying
     * Petri net state so that it can be reapplied to the Petri net at any time
     */
    private Map<String, Map<String, Integer>> savedStateTokens = new HashMap<>();

    /**
     * Constructor
     * @param petriNet petri net to modify the structure for for animaiton
     */
    public PetriNetAnimator(PetriNet petriNet) {
        this.petriNet = petriNet;
        animationLogic = new PetriNetAnimationLogic(petriNet);
        saveState();
    }

    /**
     * Save the Petri net state into the saved state map
     */
    @Override
    public void saveState() {
        savedStateTokens.clear();
        for (Place place : petriNet.getPlaces()) {
            savedStateTokens.put(place.getId(), new HashMap<>(place.getTokenCounts()));
        }
    }

    /**
     * Reset the Petri net by applying the saved state back onto the Petri net
     */
    @Override
    public void reset() {
        for (Place place : petriNet.getPlaces()) {
            Map<String, Integer> originalTokens = savedStateTokens.get(place.getId());
            place.setTokenCounts(originalTokens);
        }
    }

    /**
     *
     * @return a random transition which is enabled given the Petri nets current state
     */
    @Override
    public Transition getRandomEnabledTransition() {
        Set<Transition> enabledTransitions = getEnabledTransitions();
        if (enabledTransitions.isEmpty()) {
            throw new RuntimeException(ERROR_NO_TRANSITIONS_TO_FIRE);
        }
        Transition[] enabledTransitionsArray = enabledTransitions.toArray(new Transition[]{}); 
        int index = new Random().nextInt(enabledTransitions.size());
        return enabledTransitionsArray[index]; 
    }

    /**
     *
     * @return all enabled transitions for the Petri nets current underlying state
     */
    @Override
    public Set<Transition> getEnabledTransitions() {
        return animationLogic.getEnabledTransitions(AnimationUtils.getState(petriNet));
    }

    /**
     *
     * Fires the transition if it is enabled in the Petri net for the current underlying state
     *
     * @param transition transition to fire
     */
    @Override
    public void fireTransition(Transition transition) {
        State newState = animationLogic.getFiredState(AnimationUtils.getState(petriNet), transition);

        //Set all counts
        for (Place place : petriNet.getPlaces()) {
            place.setTokenCounts(newState.getTokens(place.getId()));
        }
    }

    /**
     * Undo the firing of the transition
     * @param transition transition to fire backwards
     */
    @Override
    public void fireTransitionBackwards(Transition transition) {
        State state = AnimationUtils.getState(petriNet);
        //Increment previous places
        for (Arc<Place, Transition> arc : petriNet.inboundArcs(transition)) {
            Place place = arc.getSource();
            for (Map.Entry<String, String> entry : arc.getTokenWeights().entrySet()) {
                String tokenId = entry.getKey();
                String functionalWeight = entry.getValue();
                double weight = animationLogic.getArcWeight(state, functionalWeight);
                int currentCount = place.getTokenCount(tokenId);
                int newCount = currentCount + (int) weight;
                place.setTokenCount(tokenId, newCount);
            }
        }

        //Decrement new places
        for (Arc<Transition, Place> arc : petriNet.outboundArcs(transition)) {
            Place place = arc.getTarget(); for (Map.Entry<String, String> entry : arc.getTokenWeights().entrySet()) {
                String tokenId = entry.getKey();
                String functionalWeight = entry.getValue();
                double weight = animationLogic.getArcWeight(state, functionalWeight);
                int oldCount = place.getTokenCount(tokenId);
                int newCount = oldCount - (int) weight;
                place.setTokenCount(tokenId, newCount);
            }
        }
    }
}
