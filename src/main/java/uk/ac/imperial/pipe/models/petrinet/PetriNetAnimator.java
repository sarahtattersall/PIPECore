package uk.ac.imperial.pipe.models.petrinet;

import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


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
    private TimingQueue savedState;

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
    	savedState = executablePetriNet.getTimingQueue().makeCopy();
    }

    /**
     * Reset the Petri net by applying the saved state back onto the Petri net
     */
    @Override
    public void reset() {
    	executablePetriNet.setTimedState(savedState);
    	animationLogic.stopAnimation();  
    }

    /**
     *
     * @return a random transition which is enabled given the Petri nets current state
     */
// TODO: Clean up – Moved to AL
    @Override
    public Transition getRandomEnabledTransition() {
    	return animationLogic.getRandomEnabledTransition(executablePetriNet.getTimingQueue());
    }

    /**
     * @deprecated use {@link #getRandomEnabledTransition()}
     * @return all enabled transitions for the Petri nets current underlying state
     */
    @Deprecated
    @Override
    public Set<Transition> getEnabledTransitions() {
        return animationLogic.getEnabledImmediateOrTimedTransitions(executablePetriNet.getTimingQueue());
    }

   /**
    *
    * Fires the transition if it is enabled in the Petri net for the current underlying state
    *
    * @param transition transition to fire
    */
    // TODO: Clean-up
    public void fireTransition(Transition transition) {
    	animationLogic.getFiredState(this.executablePetriNet.getTimingQueue(), transition);  // don't call directly, so affected transitions are updated ? 
//    	this.executablePetriNet.fireTransition(transition, this.executablePetriNet.getTimedState() );
    }

    /**
     * Undo the firing of the transition
     * @param transition transition to fire backwards
     */
    // TODO: Has to be moved to ExecutablePetriNet - or later better TimedState?.
    @Override
    public void fireTransitionBackwards(Transition transition) {
        TimingQueue timedState = executablePetriNet.getTimingQueue();
        // TODO: Move time backward!? = put transition back onto stack
        //Increment previous places
        for (Arc<Place, Transition> arc : executablePetriNet.inboundArcs(transition)) {
            Place place = arc.getSource();
            adjustCount(timedState, arc, place, false);
        }
        //Decrement new places
        for (Arc<Transition, Place> arc : executablePetriNet.outboundArcs(transition)) {
            Place place = arc.getTarget(); 
            adjustCount(timedState, arc, place, true);
        }
    }
	protected void adjustCount(TimingQueue timedState,
			Arc<? extends Connectable,? extends Connectable> arc, Place place, boolean decrement) {
		for (Map.Entry<String, String> entry : arc.getTokenWeights().entrySet()) {
		    String tokenId = entry.getKey();
		    double weight = getWeight(timedState, entry);
		    int currentCount = place.getTokenCount(tokenId);
		    int adjust = (decrement) ? -1 : 1; 
		    int newCount = currentCount + adjust * ((int) weight);
		    place.setTokenCount(tokenId, newCount);
		}
	}

	protected double getWeight(TimingQueue timedState,
			Map.Entry<String, String> entry) {
		String functionalWeight = entry.getValue();
		return executablePetriNet.getArcWeight(functionalWeight, timedState );
	}


    /**
     * Fire all currently enabled immediate transitions
     * and afterwards the enabled timed transitions which are due to fire.
     * 
     * @param TimingQueue timedState
     */
	//FIXME:  halting problem :/
    public void fireAllCurrentEnabledTransitions(TimingQueue timedState) {
    	Transition nextTransition = animationLogic.getRandomEnabledTransition( timedState );
    	if (nextTransition != null) {
    		fireTransitionPotentiallyTimed(timedState, nextTransition);
    		Set<Transition> enabledTransitions = this.executablePetriNet.getEnabledTimedTransitions();
//    		Set<Transition> enabledTransitions = timedState.getEnabledTimedTransitions();
        	timedState.registerEnabledTimedTransitions(enabledTransitions);
    		fireAllCurrentEnabledTransitions(timedState);
    	}
    }
    //FIXME:  move this, and collapse firing with unregistering 
	private void fireTransitionPotentiallyTimed(TimingQueue timedState,
			Transition nextTransition) {
		this.executablePetriNet.fireTransition(nextTransition, timedState);
		// TODO: Removing from timed transition table has to go somewhere else
		if (nextTransition.isTimed()) {
			timedState.unregisterTimedTransition(nextTransition, timedState.getCurrentTime() );
		}
	}

    /**
     * Fire a single enabled transition (immediate - or a timed one which is due when
     * there is no immediate transition left).
     * 
     * @param TimingQueue timedState
     */
    public boolean fireOneEnabledTransition(TimingQueue timedState) {
    	Transition nextTransition = animationLogic.getRandomEnabledTransition( timedState );
    	if (nextTransition != null) {
    		fireTransitionPotentiallyTimed(timedState, nextTransition);
    		Set<Transition> enabledTransitions = this.executablePetriNet.getEnabledTimedTransitions();
//    		Set<Transition> enabledTransitions = timedState.getEnabledTimedTransitions();
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
    public void advanceNetToTime(TimingQueue timedState, long newTime) {
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

	@Override
	public AnimationLogic getAnimationLogic() {
		return animationLogic	;
	}

	@Override
	public void startAnimation() {
		saveState();
		animationLogic.startAnimation(); 
	}
}
