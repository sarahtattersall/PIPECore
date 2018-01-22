package uk.ac.imperial.pipe.runner;

import java.util.Set;

import uk.ac.imperial.pipe.runner.PetriNetRunner;
import uk.ac.imperial.pipe.models.petrinet.Animator;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.PetriNetAnimator;
import uk.ac.imperial.pipe.models.petrinet.TimingQueue;
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

    public TimedPetriNetRunner(PetriNet petriNet) {
        super(petriNet);
    }

    /**
     * Fires all immediate and due timed transitions as well as providing the next 
     * time a timed transition would be up for firing.
     * @param newTime to set as the current time
     * @return next time in milliseconds at which this net will fire
     */
    public long fireAllCurrentEnabledTransitionsAndGetNextFiringTime(long newTime) {
        TimingQueue currentState = this.executablePetriNet.getTimingQueue();
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
        this.executablePetriNet.getTimingQueue().setCurrentTime(newTime);
    }

    @Override
    public void run() {
        logger.info("run ExecutablePetriNet " + executablePetriNet.getName().getName());
        start();
        final long initialFiringTime = this.executablePetriNet.getTimingQueue().getCurrentTime();
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
                    exec_duration = (int) (endTime - startTime) / 1000000;
                    if ((exec_duration < (nextFiringTime - lastTime))) {
                        try {
                            Thread.sleep((nextFiringTime - lastTime) - exec_duration);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        timedRunnerThread.start();
        /*
         * while ((round < firingLimit) && transitionsToFire()) { round++;
         * fireOneTransition(); }
         */
        end();
    }

    public Thread getTimedRunnerThread() {
        return timedRunnerThread;
    }
}
