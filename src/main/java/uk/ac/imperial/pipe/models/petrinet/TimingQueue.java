package uk.ac.imperial.pipe.models.petrinet;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

import uk.ac.imperial.state.State;

public class TimingQueue {

    protected State state;
    protected long currentTime;
    protected ConcurrentSkipListMap<Long, Set<Transition>> enabledTimedTransitions;
    protected ExecutablePetriNet executablePetriNet;

    protected TimingQueue() {
        super();
    }

    public TimingQueue(long time) {
        this.currentTime = time;
    }

    public TimingQueue(ExecutablePetriNet epn, ConcurrentSkipListMap<Long, Set<Transition>> timedTrans, long time) {
        this(epn, time);
        this.enabledTimedTransitions = new ConcurrentSkipListMap<>();
        Iterator<Entry<Long, Set<Transition>>> entryIterator = timedTrans.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Entry<Long, Set<Transition>> nextEntry = entryIterator.next();
            this.enabledTimedTransitions.put(nextEntry.getKey(), new HashSet<>(nextEntry.getValue()));
        }
    }

    public TimingQueue(ExecutablePetriNet epn, long time) {
        this(epn, epn.getState(), time);
    }

    public TimingQueue(ExecutablePetriNet epn, State state, long time) {
        this(time);
        this.executablePetriNet = epn;
        this.enabledTimedTransitions = new ConcurrentSkipListMap<>();
        setCurrentTime(time);
        rebuild(state);
    }

    @Override
    public String toString() {
        return "(" + this.state + ", " + this.enabledTimedTransitions + ", " + this.currentTime + ")";
    }

    /**
     * For the current time, returns all timed transitions that are enabled to fire
     * @return set of transitions that are enabled to fire for the current time
     */
    public Set<Transition> getCurrentlyEnabledTimedTransitions() {
        return getEnabledTransitionsAtTime(currentTime);
    }

    public ConcurrentSkipListMap<Long, Set<Transition>> getTimingQueueMap() {
        return this.enabledTimedTransitions;
    }

    public long getCurrentTime() {
        return this.currentTime;
    }

    /**
     * Sets the current time to a new time, and then rebuilds the timing queue using the new current
     * time as the initial offset.
     * @param newInitTime new time to use the base for building the timing queue
     */
    public void resetTimeAndRebuildTimedTransitions(long newInitTime) {
        this.enabledTimedTransitions = new ConcurrentSkipListMap<>();
        setCurrentTime(newInitTime);
    }

    /**
     * If the current time has at least one transition, the current time will be returned.
     * If the timing queue is empty, returns -1.
     * @return the next time at which at least one transition will fire.
     */
    public long getNextFiringTime() {
        if (enabledTimedTransitions.size() > 0) {
            return this.enabledTimedTransitions.ceilingKey(this.currentTime);
        } else {
            return -1l;
        }
    }

    /**
     * that have already past.
     * @return set of all times at which transitions are scheduled to fire, including times
     */
    protected Set<Long> getAllFiringTimes() {
        return this.enabledTimedTransitions.keySet();
    }

    /**
     * if the time does not exist in the timing queue.
     * @param nextTime the next time to evaluate
     * @return set of all transitions scheduled to fire at the given time, or an empty set,
     */
    protected Set<Transition> getEnabledTransitionsAtTime(long nextTime) {
        if (this.enabledTimedTransitions.containsKey(nextTime)) {
            return this.enabledTimedTransitions.get(nextTime);
        } else {
            return Collections.emptySet();
        }
    }

    /**
     * @return true if there is at least one transition at the current or later time
     */
    public boolean hasUpcomingTimedTransition() {
        return (this.enabledTimedTransitions.ceilingKey(this.currentTime) != null);
    }

    /**
     * verifies whether a given transition does not exist anywhere in the timing queue
     * @param transition to be verified
     * @return true if the transition does not exist in the timing queue
     */
    protected boolean checkIfTransitionNotRegistered(Transition transition) {
        boolean transitionNotRegistered = true;
        Collection<Set<Transition>> transitionSets = this.enabledTimedTransitions.values();
        Iterator<Set<Transition>> it = transitionSets.iterator();
        while (it.hasNext()) {
            Set<Transition> nextSet = it.next();
            if (nextSet.contains(transition)) {
                transitionNotRegistered = false;
                break;
            }
        }
        return transitionNotRegistered;
    }

    /**
     * If a transition exists in the timing queue, remove it.
     * @param transition to be removed
     * @param atTime time at which transition was to be fired
     * @return true if transition was removed at the specified time; false if atTime did not
     *   exist in the timing queue or if the transition was not at that time
     *   (in which case transition might exist at another time -- logic error)
     */
    public boolean unregisterTimedTransition(Transition transition, long atTime) {
        boolean unregistered = false;
        if (this.enabledTimedTransitions.containsKey(atTime)) {
            unregistered = this.enabledTimedTransitions.get(atTime).remove(transition);
            removeFiringTimeIfEmpty(atTime);
        } else {
            unregistered = false;
        }
        return unregistered;
    }

    /**
     * If a transition exists in the timing queue, remove it.  Then, rebuild
     * the timing queue based on the state that was produced by the firing of the transition.
     * @param transition to be removed
     * @param state of the executable Petri net produced when the transition fired
     * @return true if transition was removed; false if transition did not exist
     */
    public boolean dequeueAndRebuild(Transition transition, State state) {
        boolean dequeued = dequeue(transition, state);
        rebuild(state, false); // pending already verified in dequeue
        return dequeued;
    }

    protected boolean dequeue(Transition transition, State state) {
        boolean unregistered = false;
        for (Set<Transition> transitions : enabledTimedTransitions.values()) {
            if (transitions.remove(transition)) {
                unregistered = true;
            }
        }
        verifyPendingTransitionsStillActive(state);
        return unregistered;
    }

    /**
     * Rebuild the timing queue based on the given state and the current time
     * @param state of the executable Petri net
     */
    public void rebuild(State state) {
        rebuild(state, true);
    }

    private void rebuild(State state, boolean verifyPending) {
        if (verifyPending) {
            verifyPendingTransitionsStillActive(state);
        }
        queueEnabledTimedTransitions(this.executablePetriNet.getEnabledTimedTransitions(state));
    }

    public void verifyPendingTransitionsStillActive(State state) {
        Iterator<Long> timeIterator = getAllFiringTimes().iterator();
        while (timeIterator.hasNext()) {
            Long nextFiringTime = timeIterator.next();
            Set<Transition> enabledTransitions = removeEmptyTimeSlots(timeIterator, nextFiringTime);
            Iterator<Transition> transitionIterator = enabledTransitions.iterator();
            while (transitionIterator.hasNext()) {
                Transition nextChecked = transitionIterator.next();
                if (!(executablePetriNet.isEnabled(nextChecked, state))) {
                    transitionIterator.remove();
                    removeEmptyTimeSlots(timeIterator, nextFiringTime);
                }
            }
        }
    }

    private Set<Transition> removeEmptyTimeSlots(Iterator<Long> timeIterator, Long nextFiringTime) {
        Set<Transition> enabledTimedTransitions = getEnabledTransitionsAtTime(nextFiringTime);
        if (enabledTimedTransitions.isEmpty()) {
            timeIterator.remove();
        }
        return enabledTimedTransitions;
    }

    protected void removeFiringTimeIfEmpty(long atTime) {
        if (this.enabledTimedTransitions.get(atTime).isEmpty()) {
            this.enabledTimedTransitions.remove(atTime);
        }
    }

    /**
     * For the current time all enabled timed transitions are
     * put in the timing queue = when time is advanced they can get activated when
     * the delay is gone.
     *
     * @param enabledTransitions  set of enabled timed transitions to be queued for timed firing
     */
    public void queueEnabledTimedTransitions(Set<Transition> enabledTransitions) {
        Iterator<Transition> transitionIterator = enabledTransitions.iterator();
        while (transitionIterator.hasNext()) {
            Transition transition = transitionIterator.next();
            if (checkIfTransitionNotRegistered(transition)) {
                queueTransition(transition);
            }
        }
    }

    protected void queueTransition(Transition transition) {
        long nextFiringTime = this.currentTime + transition.getDelay();
        Set<Transition> queuedTransitions = null;
        if (this.enabledTimedTransitions.containsKey(nextFiringTime)) {
            queuedTransitions = this.enabledTimedTransitions.get(nextFiringTime);
            addTransition(transition, nextFiringTime, queuedTransitions);
        } else {
            queuedTransitions = new HashSet<>();
            addTransition(transition, nextFiringTime, queuedTransitions);
        }
    }

    protected void addTransition(Transition transition, long nextFiringTime, Set<Transition> queuedTransitions) {
        queuedTransitions.add(transition);
        this.enabledTimedTransitions.put(nextFiringTime, queuedTransitions);
    }

    public void setCurrentTime(long newTime) {
        this.currentTime = newTime;
    }

}
