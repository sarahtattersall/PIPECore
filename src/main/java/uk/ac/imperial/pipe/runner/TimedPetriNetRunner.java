package uk.ac.imperial.pipe.runner;

import java.util.Set;

import uk.ac.imperial.pipe.runner.PetriNetRunner;
import uk.ac.imperial.pipe.animation.Animator;
import uk.ac.imperial.pipe.animation.PetriNetAnimator;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.TimedState;
import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.pipe.runner.Firing;

/**
 * A PetriNetRunner that handles time as well.
 * 
 * Importantly, TimedPetriNetRunner is running synchronously.
 * Therefore, it creates its own thread and starts it, always coming
 * back to fire timed transitions when those are due - in between it simply sleeps.
 * 
 */
public class TimedPetriNetRunner extends PetriNetRunner {

	/**
	 * PetriNetRunner Thread - for timed networks the runner
	 * is sleeping in between firing times (waiting for the next timed transition to come up).
	 */
	private Thread timedRunnerThread;
	
	//TODO tell EPN it needs a TimedPetriNetAnimationLogic
	public TimedPetriNetRunner(PetriNet petriNet) {
		super(petriNet);
	}
	
	/**
	 * Fires all immediate and due timed transitions as well as providing the next 
	 * time a timed transition would be up for firing.
	 * @param newTime to set as current time  
	 * @return next firing time 
	 */
	//TODO consider epn.fireTransitionsAndGetNextFiringTime
	// alternatively:  EPN is not time-aware; it fires the next transition whether immediate or timed
	//  epn.fireTransition
	//  epn.fireTransition(long time) 
	//  general:  fireTransition(-1) 
	public long fireAllCurrentEnabledTransitionsAndGetNextFiringTime(long newTime) {
		TimedState currentState = this.executablePetriNet.getTimedState();
		currentState.setCurrentTime(newTime);
		//((PetriNetAnimator) animator).fireAllCurrentEnabledTransitions(currentState);
		boolean transitionToFire = true;
		while (transitionToFire) {
			transitionToFire = fireOneTransition();
		}
		if (currentState.hasUpcomingTimedTransition()) {
			return currentState.getNextFiringTime();
		} else {
			return -1;
		}
	}
	
	public void setCurrentTimeExecutablePetriNet(long newTime) {
		this.executablePetriNet.getTimedState().setCurrentTime(newTime);
	}
	
	@Override
	public void run() {
		logger.info("run ExecutablePetriNet "+executablePetriNet.getName().getName());
		start(); 
		final long initialFiringTime = this.executablePetriNet.getTimedState().getCurrentTime();
		timedRunnerThread = new Thread() {
			public void run() {
				long lastTime, startTime, endTime;
				long nextFiringTime = initialFiringTime;
				int exec_duration;
				while (nextFiringTime >= 0) {
					startTime = System.nanoTime();
					lastTime = nextFiringTime;
					nextFiringTime = fireAllCurrentEnabledTransitionsAndGetNextFiringTime(nextFiringTime);
					endTime = System.nanoTime();
					exec_duration = (int) (endTime - startTime)/1000000;
					if ( (exec_duration < (nextFiringTime - lastTime) ) ){
						try {
							Thread.sleep( (nextFiringTime - lastTime) - exec_duration);
						} catch(InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			} };
		timedRunnerThread.start();
		/*while ((round < firingLimit) && transitionsToFire()) {
			round++; 
			fireOneTransition();
		}*/
		end(); 
	}
	
	public Thread getTimedRunnerThread() {
		return timedRunnerThread;
	}
}
