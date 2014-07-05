package uk.ac.imperial.pipe.animation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import uk.ac.imperial.pipe.models.petrinet.Arc;
import uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.state.State;

/**
 * Contains methods to help with animating the Petri net and performs
 * in place modifications to the Petri net.
 */
public final class PetriNetAnimator implements Animator {

    /**
     * Executable Petri net to animate
     */
    private ExecutablePetriNet executablePetriNet;
    /**
     * Underlying animation logic, which returns logic as Markov Chain states
     */
    private final AnimationLogic animationLogic;

    /**
     * map of place id -> {token id -> count} and is used to save the underlying
     * Petri net state so that it can be reapplied to the Petri net at any time
     */
    private Map<String, Map<String, Integer>> savedStateTokens = new HashMap<>();


    public PetriNetAnimator(ExecutablePetriNet executablePetriNet) {
    	this.executablePetriNet = executablePetriNet;
    	animationLogic = new PetriNetAnimationLogic(executablePetriNet);
    	saveState();
    	
	}

	/**
     * Save the Petri net state into the saved state map
     */
    //TODO save the state here, but have the ExecutablePetriNet create it
    @Override
    public void saveState() {
        savedStateTokens.clear();
        for (Place place : executablePetriNet.getPlaces()) {
            savedStateTokens.put(place.getId(), new HashMap<>(place.getTokenCounts()));
        }
    }

    /**
     * Reset the Petri net by applying the saved state back onto the Petri net
     */
    @Override
    public void reset() {
        for (Place place : executablePetriNet.getPlaces()) {
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
        Collection<Transition> enabledTransitions = getEnabledTransitions();
        if (enabledTransitions.isEmpty()) {
            throw new RuntimeException("Error - no transitions to fire!");
        }

        Random random = new Random();
        int index = random.nextInt(enabledTransitions.size());

        Iterator<Transition> iter = enabledTransitions.iterator();
        Transition transition = iter.next();
        for (int i = 1; i < index; i++) {
            transition = iter.next();
        }
        return transition;
    }

    /**
     *
     * @return all enabled transitions for the Petri nets current underlying state
     */
    @Override
    public Set<Transition> getEnabledTransitions() {
        return animationLogic.getEnabledTransitions(executablePetriNet.getCurrentState());
    }

    /**
     *
     * Fires the transition if it is enabled in the Petri net for the current underlying state
     *
     * @param transition transition to fire
     */
    @Override
    public void fireTransition(Transition transition) {
        State newState = animationLogic.getFiredState(executablePetriNet.getCurrentState(), transition);
        for (Place place : executablePetriNet.getPlaces()) {
            place.setTokenCounts(newState.getTokens(place.getId()));
        }
    }

    /**
     * Undo the firing of the transition
     * @param transition transition to fire backwards
     */
    @Override
    public void fireTransitionBackwards(Transition transition) {
        State state = executablePetriNet.getCurrentState();
        //Increment previous places
        for (Arc<Place, Transition> arc : executablePetriNet.inboundArcs(transition)) {
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
        for (Arc<Transition, Place> arc : executablePetriNet.outboundArcs(transition)) {
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
