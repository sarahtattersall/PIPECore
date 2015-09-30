package uk.ac.imperial.pipe.animation;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.pipe.models.petrinet.Arc;
import uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.TimedState;
import uk.ac.imperial.pipe.models.petrinet.Transition;

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
//Remove	private Random random; 


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
// Moved to AL
    @Override
    public Transition getRandomEnabledTransition() {
    	return animationLogic.getRandomEnabledTransition(executablePetriNet.getTimedState());
    }

    /**
     *
     * @return all enabled transitions for the Petri nets current underlying state
     */
// Remove
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
    @Override
    public void fireTransition(Transition transition) {
        TimedState newState = animationLogic.getFiredState(executablePetriNet.getTimedState(), transition);
        // TODO: A problem is that time is not part of the state - therefore must be handed additionally.
//        if (transition.isTimed()) {
//        	(newState.getEnabledTimedTransitions().get(newState.getCurrentTime())).remove(transition);
//        	System.out.println("SET OF TRANS: " + newState.getEnabledTimedTransitions().get(newState.getCurrentTime()));
//        }
        for (Place place : executablePetriNet.getPlaces()) {
            place.setTokenCounts(newState.getState().getTokens(place.getId()));
        }
    }

    /**
     * Undo the firing of the transition
     * @param transition transition to fire backwards
     */
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
     * Get the internal current time of the animated Petri network.
     */
    //public long getCurrentTime() {
    //	return this.executablePetriNet.currentTime;
    //}
    
    /**
     * Set the internal time step of the animated Petri network.
     */
   // public void setTimeStep(long newStep) {
   // 	this.timeStep = newStep;
   // }
    
    /**
     * Get the internal time step of the animated Petri network.
     */
    //public long getTimeStep() {
    //	return this.executablePetriNet.timeStep;
    //}
    
    // Move to Animator
    /**
     * Advance current time one time step.
     */
    //public void advanceSingleTimeStep() {
    //	registerEnabledTimedTransitions(executablePetriNet.getState());
    //	this.execucurrentTime += this.timeStep;
    //}
    
    public void fireAllCurrentEnabledTransitions() {
    	Transition nextTransition = animationLogic.getRandomEnabledTransition( executablePetriNet.getTimedState() );
    	logger.debug("Next fired trans " + nextTransition);
    	if (nextTransition != null) {
    		fireTransition(nextTransition);
    		fireAllCurrentEnabledTransitions();
    	}
    }
    
    /**
     * Advance current time.
     */
    // TODO: Should go in animator as it fires transitions!
    protected void advanceToTime(TimedState timedState, long newTime) {
    	fireAllCurrentEnabledTransitions();
    	animationLogic.registerEnabledTimedTransitions( timedState );
    	if ( timedState.getEnabledTimedTransitions().ceilingKey( timedState.getCurrentTime() ) != null) {
    		logger.debug("Timed transitions");
    		Map.Entry<Long,Set<Transition>> nextTimedTransitions = timedState.getEnabledTimedTransitions().ceilingEntry( timedState.getCurrentTime() );
    		while (nextTimedTransitions.getKey() < newTime) {
    			logger.debug("Found one " + newTime + " - " + nextTimedTransitions.getKey() );
    			Iterator<Transition> transitionIterator = nextTimedTransitions.getValue().iterator();
    			while (transitionIterator.hasNext()) {
    				logger.debug("Fire it");
    				Transition nextTransition = transitionIterator.next();
    				//TODO - remove firing from here!
    				fireAllCurrentEnabledTransitions();
    			} 
    			timedState.getEnabledTimedTransitions().remove( nextTimedTransitions.getKey() );
    			nextTimedTransitions = timedState.getEnabledTimedTransitions().higherEntry( nextTimedTransitions.getKey() );
    			logger.debug("NEXT TRANS: " + nextTimedTransitions);
    			if (nextTimedTransitions == null) {
    				logger.debug("Break");
    				break;
    			}
    			logger.debug("NEXT TRANS: " + nextTimedTransitions);
    		}
    	}
    	logger.debug("NEW TIME " + newTime);
    	timedState.setCurrentTime(newTime);
    }
    
    
    @Override
    public void setRandom(Random random) {
		animationLogic.setRandom(random);
	}
}
