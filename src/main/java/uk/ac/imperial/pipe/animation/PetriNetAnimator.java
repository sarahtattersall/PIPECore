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
    /**
     * Petri net to animate
     */
    private final PetriNet petriNet;

    private final AnimationLogic animationLogic;

    private Map<String, Map<String, Integer>> savedStateTokens = new HashMap<>();

    public PetriNetAnimator(PetriNet petriNet) {
        this.petriNet = petriNet;
        animationLogic = new PetriNetAnimationLogic(petriNet);
        saveState();
    }


    @Override
    public void saveState() {
        savedStateTokens.clear();
        for (Place place : petriNet.getPlaces()) {
            savedStateTokens.put(place.getId(), new HashMap<>(place.getTokenCounts()));
        }
    }

    @Override
    public void reset() {
        for (Place place : petriNet.getPlaces()) {
            Map<String, Integer> originalTokens = savedStateTokens.get(place.getId());
            place.setTokenCounts(originalTokens);
        }
    }

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

    @Override
    public Set<Transition> getEnabledTransitions() {
        return animationLogic.getEnabledTransitions(AnimationUtils.getState(petriNet));
    }

    @Override
    public void fireTransition(Transition transition) {
        State newState = animationLogic.getFiredState(AnimationUtils.getState(petriNet), transition);

        //Set all counts
        for (Place place : petriNet.getPlaces()) {
            place.setTokenCounts(newState.getTokens(place.getId()));
        }
    }

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
