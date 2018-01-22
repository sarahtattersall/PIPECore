package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.state.HashedState;
import uk.ac.imperial.state.State;

public class TimingQueueTest {

    private TimingQueue timing;
    private TestingExecutablePetriNet executablePetriNet;
    private Map<String, Map<String, Integer>> stateMap;
    private Transition t1;
    private Transition t2;
    private Transition t3;

    @Before
    public void setUp() throws Exception {
        // use of this TestingExecutable... fake limits testing of rebuild() and dequeueAndRebuild()
        // see ExecutablePetriNetTest 
        executablePetriNet = new TestingExecutablePetriNet(new PetriNet());
        buildState();
    }

    protected void buildState() {
        stateMap = new HashMap<>();
        Map<String, Integer> tokenMap = new HashMap<>();
        tokenMap.put("default", 1);
        stateMap.put("P0", tokenMap);
    }

    @Test
    public void testNoTransitionsGivesEmptyCache() {
        timing = new TimingQueue(executablePetriNet, 3);
        assertEquals(0, timing.enabledTimedTransitions.size());
    }

    @Test
    public void enabledTransitionsAreCachedByNextFiringTime() {
        executablePetriNet.testTransitions.add(new DiscreteTransition("T1"));
        executablePetriNet.testTransitions.add(new DiscreteTransition("T2"));
        timing = new TimingQueue(executablePetriNet, 3);
        assertEquals("one entry, because both transitions fire at same time", 1, timing.enabledTimedTransitions.size());
        assertEquals("transitions fire at 3 ms", 3, (long) timing.enabledTimedTransitions.keySet().first());
        assertEquals(2, timing.enabledTimedTransitions.get(3l).size());
        assertEquals("T1", timing.enabledTimedTransitions.get(3l).iterator().next().getId());
    }

    @Test
    public void differentTransitionDelaysGiveDifferentFiringTimes() {
        executablePetriNet.testTransitions.add(buildTransition("T1", 0));
        executablePetriNet.testTransitions.add(buildTransition("T2", 4));
        timing = new TimingQueue(executablePetriNet, 3);
        assertEquals("two entries, one for each delay", 2, timing.enabledTimedTransitions.size());
        assertEquals(1, timing.enabledTimedTransitions.get(3l).size());
        assertEquals(1, timing.enabledTimedTransitions.get(7l).size());
        assertEquals("T1", timing.enabledTimedTransitions.get(3l).iterator().next().getId());
        assertEquals("T2", timing.enabledTimedTransitions.get(7l).iterator().next().getId());
    }

    @Test
    public void onceQueuedTransitionsCanBeDequeued() {
        Transition transition = buildTransition("T1", 0);
        executablePetriNet.testTransitions.add(transition);
        executablePetriNet.testTransitions.add(buildTransition("T2", 4));
        timing = new TimingQueue(executablePetriNet, 3);
        assertEquals(2, timing.enabledTimedTransitions.size());
        timing.dequeue(transition, executablePetriNet.getState());
        // for timing.dequeueAndRebuild, see ExecutablePetriNetTest
        assertEquals(1, timing.enabledTimedTransitions.size());
        assertEquals(1, timing.enabledTimedTransitions.get(7l).size());
        assertEquals("T2", timing.enabledTimedTransitions.get(7l).iterator().next().getId());
    }

    @Test
    public void attemptingToDequeueTransitionThatDoesntExistReturnsFalse() {
        Transition transition = buildTransition("T1", 0);
        Transition transitionNotAdded = buildTransition("T99", 0);
        executablePetriNet.testTransitions.add(transition);
        timing = new TimingQueue(executablePetriNet, 3);
        assertTrue(timing.dequeueAndRebuild(transition, executablePetriNet.getState()));
        assertFalse("not in the EPN, so not dequeued", timing
                .dequeueAndRebuild(transitionNotAdded, executablePetriNet.getState()));
    }

    @Test
    public void onceTransitionIsAddedLaterChangesToDelayAreIgnored() {
        Transition transition = buildTransition("T1", 5);
        executablePetriNet.testTransitions.add(transition);
        timing = new TimingQueue(executablePetriNet, 3);
        assertEquals("T1", timing.enabledTimedTransitions.get(8l).iterator().next().getId());
        assertFalse("transition is in the timing map", timing.checkIfTransitionNotRegistered(transition));
        transition.setDelay(2);
        assertFalse("...still in the map; delay change ignored", timing.checkIfTransitionNotRegistered(transition));
    }

    @Test
    public void returnsTransitionsForTheCurrentAbsoluteTime() {
        buildThreeTransitions(4, 4, 0);
        timing = new TimingQueue(executablePetriNet, 2);
        assertEquals("current time is 2, with one entry", 1, timing.getCurrentlyEnabledTimedTransitions().size());
        assertTrue(timing.getCurrentlyEnabledTimedTransitions().contains(t3));
        timing.setCurrentTime(3);
        assertEquals("current time is 3, with no entries", 0, timing.getCurrentlyEnabledTimedTransitions().size());
        timing.setCurrentTime(6);
        assertEquals("current time is 6, with two entries", 2, timing.getCurrentlyEnabledTimedTransitions().size());
        assertTrue(timing.getCurrentlyEnabledTimedTransitions().contains(t1));
        assertTrue(timing.getCurrentlyEnabledTimedTransitions().contains(t2));
    }

    @Test
    public void resetsTimeAndRebuildTransitionsBasedOnTheNewTime() {
        executablePetriNet.testTransitions.add(buildTransition("T1", 0));
        executablePetriNet.testTransitions.add(buildTransition("T2", 4));
        timing = new TimingQueue(executablePetriNet, 3);
        assertEquals(3, timing.getCurrentTime());
        assertEquals(2, timing.enabledTimedTransitions.size());
        timing.resetTimeAndRebuildTimedTransitions(1l);
        //	should be rebuild(state); limitation of the fake.  		
        timing.queueEnabledTimedTransitions(executablePetriNet.getEnabledTimedTransitions());
        assertEquals(1, timing.getCurrentTime());
        assertEquals("new time, but still two transitions", 2, timing.enabledTimedTransitions.size());
        assertEquals("T1", timing.enabledTimedTransitions.get(1l).iterator().next().getId());
        assertEquals("T2", timing.enabledTimedTransitions.get(5l).iterator().next().getId());
    }

    @Test
    public void keepsTrackOfNextFiringTime() {
        executablePetriNet.testTransitions.add(buildTransition("T1", 0));
        executablePetriNet.testTransitions.add(buildTransition("T2", 4));
        executablePetriNet.testTransitions.add(buildTransition("T2", 8));
        timing = new TimingQueue(executablePetriNet, 3);
        assertEquals(3, timing.getCurrentTime());
        assertEquals(3, timing.getNextFiringTime());
        timing.setCurrentTime(5);
        assertEquals("advanced past first firing time", 7, timing.getNextFiringTime());
        timing.setCurrentTime(10);
        assertEquals(11, timing.getNextFiringTime());
    }

    @Test
    public void knowsWhetherAnyFiringTimesRemain() {
        executablePetriNet.testTransitions.add(buildTransition("T1", 2));
        timing = new TimingQueue(executablePetriNet, 3);
        assertTrue(timing.hasUpcomingTimedTransition());
        timing.setCurrentTime(5);
        assertTrue("transition at current time is still upcoming", timing.hasUpcomingTimedTransition());
        timing.setCurrentTime(6);
        assertFalse(timing.hasUpcomingTimedTransition());
    }

    @Test
    public void emptyTimingQueueReturnsAppropriateResponses() {
        timing = new TimingQueue(executablePetriNet, 3);
        assertEquals(-1l, timing.getNextFiringTime());
        assertEquals(0, timing.getAllFiringTimes().size());
        assertFalse(timing.hasUpcomingTimedTransition());
    }

    @Test
    public void tracksAllFiringTimesAcrossDequeue() {
        Transition transition = buildTransition("T1", 2);
        executablePetriNet.testTransitions.add(transition);
        executablePetriNet.testTransitions.add(buildTransition("T2", 4));
        executablePetriNet.testTransitions.add(buildTransition("T3", 8));
        timing = new TimingQueue(executablePetriNet, 3);
        assertEquals(3, timing.getAllFiringTimes().size());
        assertEquals(5l, (long) timing.getAllFiringTimes().iterator().next());
        timing.dequeue(transition, executablePetriNet.getState());
        // for timing.dequeueAndRebuild, see ExecutablePetriNetTest
        assertEquals(2, timing.getAllFiringTimes().size());
        assertEquals(7l, (long) timing.getAllFiringTimes().iterator().next());
    }

    @Test
    public void returnsTheTransitionsThatWillFireAtGivenTime() throws PetriNetComponentNotFoundException {
        buildThreeTransitions(4, 0, 4);
        timing = new TimingQueue(executablePetriNet, 3);
        assertEquals(1, timing.getEnabledTransitionsAtTime(3).size());
        assertTrue(timing.getEnabledTransitionsAtTime(3).contains(t2));
        assertEquals(2, timing.getEnabledTransitionsAtTime(7).size());
        assertTrue(timing.getEnabledTransitionsAtTime(7).contains(t1));
        assertTrue(timing.getEnabledTransitionsAtTime(7).contains(t3));
        assertEquals("empty set returned for nonexistent time", 0, timing.getEnabledTransitionsAtTime(5).size());
    }

    private void buildThreeTransitions(int delay1, int delay2, int delay3) {
        t1 = buildTransition("T1", delay1);
        t2 = buildTransition("T2", delay2);
        t3 = buildTransition("T3", delay3);
        executablePetriNet.testTransitions.add(t1);
        executablePetriNet.testTransitions.add(t2);
        executablePetriNet.testTransitions.add(t3);
    }

    protected DiscreteTransition buildTransition(String id, int delay) {
        DiscreteTransition transition = new DiscreteTransition(id);
        transition.setTimed(true);
        transition.setDelay(delay);
        return transition;
    }

    private class TestingExecutablePetriNet extends ExecutablePetriNet {

        public Set<Transition> testTransitions = new HashSet<>();;

        public TestingExecutablePetriNet(PetriNet petriNet) {
            super(petriNet);
        }

        @Override
        public Set<Transition> getEnabledTimedTransitions(State state) {
            return testTransitions;
        }

        @Override
        public Set<Transition> getEnabledTimedTransitions() {
            return testTransitions;
        }

        @Override
        public boolean isEnabled(Transition transition, State state) {
            return true;
        }

        //skip recursive call in the EPN constructor
        @Override
        protected TimingQueue buildTimingQueue(long initTime) {
            return null;
        }

    }
}
