package uk.ac.imperial.pipe.runner;

import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.TimingQueue;
import java.util.concurrent.Semaphore;

/**
 * A PetriNetRunner that handles time as well.
 * 
 * Importantly, RealTimePetriNetRunner is running in real time.
 * Therefore, it creates its own thread and starts it and runs in the background.
 * It can be used to synchronize access to external places ..., but access
 * is guarded by semaphores. Only when the runner is not working actively on the 
 * PN it allows to change PN places or fire externally transitions. 
 * 
 */
public class RealTimePetriNetRunner extends TimedPetriNetRunner {

    //private double time_step_duration = 1.;
    /**
     * As the Runner shall keep up with time, it is always 
     * comparing the current time to the internal Petri Net clock.
     * The Petri Net clock is allowed to fall behind as long as no
     * timed transition should be fired by now (this would be late in this case).
     * The Petri Net is internally advanced to the next firing time and waits until
     * that time comes up or another process is externally changing something in the network.
     */
    private long realStartTime, pnStartTime, realCurrentTime, pnCurrentTime;
    private long initialFiringTime, nextFiringTime, lastFiringTime;

    /**
     * Controlling access to the PN structure:
     * Places and transitions are only allowed to change when the Runner is not actively
     * changing the state of the network.
     */
    static Semaphore semaphore = new Semaphore(0);

    public RealTimePetriNetRunner(PetriNet petriNet) {
        super(petriNet);
        // TODO Auto-generated constructor stub
    }

    /**
     * Initialize the PetriNetRunner - sets the starting of the PetriNetwork 
     * and synchronizes it with the real time.
     */
    public void startRealTimeClock() {
        pnStartTime = this.executablePetriNet.getTimingQueue().getCurrentTime();
        pnCurrentTime = pnStartTime;
        realStartTime = System.nanoTime() / 1000000;
        realCurrentTime = realStartTime;
    }

    /**
     * Fires all immediate and due timed transitions as well as providing the next 
     * time a timed transition would be up for firing.
     * @param newTime to be set as the current time
     */
    public long fireAllCurrentEnabledTransitionsAndGetNextFiringTime(long newTime) {
        TimingQueue currentState = this.executablePetriNet.getTimingQueue();
        currentState.setCurrentTime(newTime);
        //((PetriNetAnimator) animator).fireAllCurrentEnabledTransitions(currentState);
        boolean transitionToFire = true;
        System.out.println("Real: " + (System.nanoTime() / 1000000 - realStartTime));
        //System.out.println("PN: " + (this.executablePetriNet.getTimedState().getCurrentTime() - pnStartTime) );
        while (transitionToFire) {
            transitionToFire = fireOneTransition();
            //if (transitionToFire) {
            //	System.out.println("FIRED A TRANSITION");
            //}
        }
        if (currentState.hasUpcomingTimedTransition()) {
            return currentState.getNextFiringTime();
        } else {
            return -1;
        }
    }

    /**
     * Run the ExecutablePetriNet for a given duration in real time.
     * @param duration for the EPN to be executed
     */
    public void stepPetriNetSynchronized(int duration) {
        logger.info("run ExecutablePetriNet " + executablePetriNet.getName().getName());
        start();
        realCurrentTime = System.nanoTime() / 1000000;
        pnCurrentTime = this.executablePetriNet.getTimingQueue().getCurrentTime();
        // Wait for realtime to catch up =
        // for the stepping function the PN can go ahead of realtime, but
        // importantly no timed transition can be fired before real time.
        // Advantage: sleeping is only inserted directly before new evaluation
        // and really only if there is any time left.
        //System.out.println("Real: " + (System.nanoTime()/1000000 - realStartTime) );
        //System.out.println("PN: " + (this.executablePetriNet.getTimedState().getCurrentTime() - pnStartTime) );
        if ((realCurrentTime - realStartTime) < (pnCurrentTime - pnStartTime)) {
            try {
                semaphore.release();
                //System.out.println("Semaphore: " + semaphore);
                Thread.sleep((pnCurrentTime - pnStartTime) - (realCurrentTime - realStartTime));
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            if ((realCurrentTime - realStartTime) > (duration + (pnCurrentTime - pnStartTime))) {
                System.out.println("Not running real-time anymore. " + (realCurrentTime - realStartTime) + " > " +
                        duration);
            }
        }
        // Afterwards both clocks are synchronized again and 
        // PN is advanced parallel to realtime
        initialFiringTime = pnCurrentTime;
        nextFiringTime = initialFiringTime;
        do {
            lastFiringTime = nextFiringTime;
            nextFiringTime = fireAllCurrentEnabledTransitionsAndGetNextFiringTime(nextFiringTime);

            // To keep in between timed transitions timed, the thread sleeps here.
            // If this happens very often, maybe a different duration as a time  step 
            // should be used.
            //System.out.println("Before sleep: " + nextFiringTime+ " - " + lastFiringTime );
            if (((initialFiringTime + duration) >= nextFiringTime) & (nextFiringTime > 0)) {
                try {
                    semaphore.release();
                    //System.out.println("Semaphore: " + semaphore);
                    Thread.sleep((nextFiringTime - lastFiringTime));
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } while ((nextFiringTime >= 0) & ((initialFiringTime + duration) >= nextFiringTime));
        if ((initialFiringTime + duration) > lastFiringTime) {
            setCurrentTimeExecutablePetriNet(initialFiringTime + duration);
        }
        end();
    }

    /**
     * Get the semaphore that controls access to the PN structure:
     * Places and transitions are only allowed to change when the Runner is not actively
     * changing the state of the network.
     * @return semaphore that contrls access to PN structure 
     */
    public static Semaphore getPetriNetRunnerSemaphore() {
        return semaphore;
    }

    public long getRealTimeSinceStart() {
        return (System.nanoTime() / 1000000 - realStartTime);
    }

    public long getPNTimeSinceStart() {
        return (this.executablePetriNet.getTimingQueue().getCurrentTime() - pnStartTime);
    }
}
