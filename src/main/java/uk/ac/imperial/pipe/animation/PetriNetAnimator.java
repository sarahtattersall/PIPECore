package uk.ac.imperial.pipe.animation;

import java.util.HashSet;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.pipe.models.petrinet.Arc;
import uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.state.State;
import uk.ac.imperial.pipe.models.petrinet.TimedState;

/**
 * Contains methods to help with animating the Petri net and performs
 * in place modifications to the Petri net.
 */
public final class PetriNetAnimator implements Animator {

	private static Logger logger = LogManager.getLogger(PetriNetAnimator.class);  
    /**
     * Executable Petri net to animate
     */
    private ExecutablePetriNet executablePetriNet;
    /**
     * Underlying animation logic, which returns logic as Markov Chain states
     */
    //TODO: getEnabledTimedTransitions is needed here but should go into the AL interface?
    private final PetriNetAnimationLogic animationLogic;

    /**
     * Saved State of the underlying
     * Petri net so that it can be reapplied to the Petri net at any time
     */
    private TimedState savedState;
    /**
     * Random for use in random firing.   
     */

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
    	savedState = executablePetriNet.getTimedState();
    }

    /**
     * Reset the Petri net by applying the saved state back onto the Petri net
     */
    @Override
    public void reset() {
    	executablePetriNet.setTimedState(savedState);
    }

    /**
     *
     * @return a random transition which is enabled given the Petri nets current state
     */
// TODO: Clean up – Moved to AL
    @Override
    public Transition getRandomEnabledTransition() {
    	return animationLogic.getRandomEnabledTransition(executablePetriNet.getTimedState());
    }

    /**
     *
     * @return all enabled transitions for the Petri nets current underlying state
     */
// TODO: Remove
    @Override
    public Set<Transition> getEnabledTransitions() {
        return animationLogic.getEnabledTransitions(executablePetriNet.getTimedState());
    }

    /**
     *
     * Fires the transition if it is enabled in the Petri net for the current underlying state
     *
     * @param transition transition to fire
     */
    //TODO move state logic to Executable PN
   /* @Override
    public void fireTransition(Transition transition, TimedState timedState) {
        //TimedState newState = animationLogic.getFiredState( timedState, transition);
        this.executablePetriNet.fireTransition(transition, timedState);
        // TODO: A problem is that time is not part of the state - therefore must be handed additionally.
//        if (transition.isTimed()) {
//        	(newState.getEnabledTimedTransitions().get(newState.getCurrentTime())).remove(transition);
//        	System.out.println("SET OF TRANS: " + newState.getEnabledTimedTransitions().get(newState.getCurrentTime()));
//        }
//        for (Place place : executablePetriNet.getPlaces()) {
//            place.setTokenCounts(newState.getState().getTokens(place.getId()));
//        }
    }*/
    
   /**
    *
    * Fires the transition if it is enabled in the Petri net for the current underlying state
    *
    * @param transition transition to fire
    */
    // TODO: Clean-up
    public void fireTransition(Transition transition) {
    	this.executablePetriNet.fireTransition(transition, this.executablePetriNet.getTimedState() );
    }

    /**
     * Undo the firing of the transition
     * @param transition transition to fire backwards
     */
    // TODO: Has to be moved to ExecutablePetriNet - or later better TimedState?.
    @Override
    public void fireTransitionBackwards(Transition transition) {
        TimedState timedState = executablePetriNet.getTimedState();
        // TODO: Move time backward!? = put transition back onto stack
        //Increment previous places
        for (Arc<Place, Transition> arc : executablePetriNet.inboundArcs(transition)) {
            Place place = arc.getSource();
            for (Map.Entry<String, String> entry : arc.getTokenWeights().entrySet()) {
                String tokenId = entry.getKey();
                String functionalWeight = entry.getValue();
                double weight = animationLogic.getArcWeight(timedState, functionalWeight);
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
                double weight = animationLogic.getArcWeight(timedState, functionalWeight);
                int oldCount = place.getTokenCount(tokenId);
                int newCount = oldCount - (int) weight;
                place.setTokenCount(tokenId, newCount);
            }
        }
    }

    /**
     * Fire all currently enabled immediate transitions
     * and afterwards the enabled timed transitions which are due to fire.
     * 
     * @param TimedState timedState
     */
    public void fireAllCurrentEnabledTransitions(TimedState timedState) {
    	Transition nextTransition = animationLogic.getRandomEnabledTransition( timedState );
    	if (nextTransition != null) {
    		this.executablePetriNet.fireTransition(nextTransition, timedState);
    		// TODO: Removing from timed transition table has to go somewhere else
    		if (nextTransition.isTimed()) {
    			timedState.unregisterTimedTransition(nextTransition, timedState.getCurrentTime() );
    		}
    		Set<Transition> enabledTransitions = timedState.getEnabledTimedTransitions();
        	timedState.registerEnabledTimedTransitions(enabledTransitions);
    		fireAllCurrentEnabledTransitions(timedState);
    	}
    }

    /**
     * Fire a single enabled transition (immediate - or a timed one which is due when
     * there is no immediate transition left).
     * 
     * @param TimedState timedState
     */
    public boolean fireOneEnabledTransition(TimedState timedState) {
    	Transition nextTransition = animationLogic.getRandomEnabledTransition( timedState );
    	if (nextTransition != null) {
    		this.executablePetriNet.fireTransition(nextTransition, timedState);
    		if (nextTransition.isTimed()) {
    			timedState.unregisterTimedTransition(nextTransition, timedState.getCurrentTime() );
    		}
    		Set<Transition> enabledTransitions = timedState.getEnabledTimedTransitions();
        	timedState.registerEnabledTimedTransitions(enabledTransitions);
    		return true;
    	} else {
    		return false;
    	}
    }
    
    /**
     * Advance current time of the Petri Network.
     * Fire all immediate transitions and afterwards step through time
     * always firing the timed transitions that become due to fire.
     */
    public void advanceNetToTime(TimedState timedState, long newTime) {
    	if (newTime > timedState.getCurrentTime() ) {
        	fireAllCurrentEnabledTransitions(timedState);
        	while ( timedState.hasUpcomingTimedTransition() ) {
        		long nextFiringTime = timedState.getNextFiringTime();
        		if (nextFiringTime < newTime) {
        			timedState.setCurrentTime(timedState.getNextFiringTime());
        			fireAllCurrentEnabledTransitions(timedState);
        		} else {
        			timedState.setCurrentTime(newTime);
        			break;
        		}
        	}
        	timedState.setCurrentTime(newTime);
    	}
    }
    
    
    @Override
    public void setRandom(Random random) {
		animationLogic.setRandom(random);
	}
}
