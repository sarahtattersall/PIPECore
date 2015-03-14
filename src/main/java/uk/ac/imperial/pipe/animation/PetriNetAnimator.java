package uk.ac.imperial.pipe.animation;

import java.util.Collection;
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
     * Saved State of the underlying
     * Petri net so that it can be reapplied to the Petri net at any time
     */
    private State savedState;
    /**
     * Random for use in random firing.   
     */
	private Random random; 


    public PetriNetAnimator(ExecutablePetriNet executablePetriNet) {
    	this.executablePetriNet = executablePetriNet;
    	animationLogic = new PetriNetAnimationLogic(executablePetriNet);
    	saveState();
    	
	}

	/**
     * Save the Petri net state into the saved state map
     */
    @Override
    public void saveState() {
    	savedState = executablePetriNet.getState(); 
    }

    /**
     * Reset the Petri net by applying the saved state back onto the Petri net
     */
    @Override
    public void reset() {
    	executablePetriNet.setState(savedState);
    }

    /**
     *
     * @return a random transition which is enabled given the Petri nets current state
     */
    //TODO rather than throwing, should return null when no more transitions, and caller should check for that
    @Override
    public Transition getRandomEnabledTransition() {
        Set<Transition> enabledTransitions = getEnabledTransitions();
        if (enabledTransitions.isEmpty()) {
            throw new RuntimeException(ERROR_NO_TRANSITIONS_TO_FIRE);
        }
        Transition[] enabledTransitionsArray = enabledTransitions.toArray(new Transition[]{}); 
        

//        int index = getRandom().nextInt(enabledTransitions.size());
//        
//        Iterator<Transition> iter = enabledTransitions.iterator();
//        Transition transition = iter.next();
//        for (int i = 1; i < index; i++) {
//        	transition = iter.next();
//        }
//        return transition;
        int index = getRandom().nextInt(enabledTransitions.size());
        return enabledTransitionsArray[index]; 
    }

    /**
     *
     * @return all enabled transitions for the Petri nets current underlying state
     */
    @Override
    public Set<Transition> getEnabledTransitions() {
        return animationLogic.getEnabledTransitions(executablePetriNet.getState());
    }

    /**
     *
     * Fires the transition if it is enabled in the Petri net for the current underlying state
     *
     * @param transition transition to fire
     */
    //TODO move state logic to Executable PN
    @Override
    public void fireTransition(Transition transition) {
        State newState = animationLogic.getFiredState(executablePetriNet.getState(), transition);
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
        State state = executablePetriNet.getState();
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

	private Random getRandom() {
		if (random == null) {
			random = new Random(); 
		}
		return random;
	}
	/**
	 * Generate predictable results for repeated testing of a given Petri net by providing a Random built from the same long seed for each run.  
	 * Otherwise, a new Random will be used on each execution, leading to different firing patterns. 
	 * @param random
	 */
	@Override
	public void setRandom(Random random) {
		this.random = random;
	}
}
