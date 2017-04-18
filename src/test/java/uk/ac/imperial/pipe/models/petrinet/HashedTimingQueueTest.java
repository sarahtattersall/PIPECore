package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.state.HashedState;
import uk.ac.imperial.state.State;

public class HashedTimingQueueTest {

	private HashedTimingQueue timing;
	private TestingExecutablePetriNet executablePetriNet;
	private Map<String, Map<String, Integer>> stateMap;
	private State state;

	@Before
	public void setUp() throws Exception {
		executablePetriNet = new TestingExecutablePetriNet(new PetriNet()); 
		buildState();  
	}

	protected void buildState() {
		stateMap = new HashMap<>();
		Map<String, Integer> tokenMap = new HashMap<>(); 
		tokenMap.put("default", 1); 
		stateMap.put("P0", tokenMap); 
		state = new HashedState(stateMap); 
	}
	
	@Test
	public void testNoTransitionsGivesEmptyCache() {
		timing = new HashedTimingQueue(executablePetriNet, state, 3); 
		assertEquals(0, timing.enabledTimedTransitions.size()); 
	}
	@Test
	public void enabledTransitionsAreCachedByNextFiringTime() {
		executablePetriNet.testTransitions.add(new DiscreteTransition("T1")); 
		executablePetriNet.testTransitions.add(new DiscreteTransition("T2")); 
		timing = new HashedTimingQueue(executablePetriNet, state, 3);
		assertEquals("one entry, because both transitions fire at same time",
				1, timing.enabledTimedTransitions.size()); 
		assertEquals("transitions fire at 3 ms",
				3, (long) timing.enabledTimedTransitions.keySet().first()); 
		assertEquals(2, timing.enabledTimedTransitions.get(3l).size()); 
		assertEquals("T1", timing.enabledTimedTransitions.get(3l).iterator().next().getId()); 
	}
	@Test
	public void differentTransitionDelaysGiveDifferentFiringTimes() {
		executablePetriNet.testTransitions.add(buildTransition("T1", 0)); 
		executablePetriNet.testTransitions.add(buildTransition("T2", 4)); 
		timing = new HashedTimingQueue(executablePetriNet, state, 3);
		assertEquals("two entries, one for each delay",
				2, timing.enabledTimedTransitions.size()); 
		assertEquals(1, timing.enabledTimedTransitions.get(3l).size()); 
		assertEquals(1, timing.enabledTimedTransitions.get(7l).size()); 
		assertEquals("T1", timing.enabledTimedTransitions.get(3l).iterator().next().getId()); 
		assertEquals("T2", timing.enabledTimedTransitions.get(7l).iterator().next().getId()); 
	}
	@Test
	public void onceRegisteredTransitionsCanBeUnRegistered() {
		Transition transition = buildTransition("T1", 0);
		executablePetriNet.testTransitions.add(transition); 
		executablePetriNet.testTransitions.add(buildTransition("T2", 4)); 
		timing = new HashedTimingQueue(executablePetriNet, state, 3);
		assertEquals(2, timing.enabledTimedTransitions.size()); 
		timing.unregisterTimedTransition(transition, 3l); 
		assertEquals(1, timing.enabledTimedTransitions.size()); 
		assertEquals(1, timing.enabledTimedTransitions.get(7l).size()); 
		assertEquals("T2", timing.enabledTimedTransitions.get(7l).iterator().next().getId()); 
	}
	@Test
	public void attemptingToUnregisterTransitionAtWrongTimeReturnsFalse() {
		Transition transition = buildTransition("T1", 0);
		executablePetriNet.testTransitions.add(transition); 
		timing = new HashedTimingQueue(executablePetriNet, state, 3);
		assertFalse("wrong time, so not unregistered",
				timing.unregisterTimedTransition(transition, 2l)); 
		assertTrue("...but transition still exists at a different time",
				timing.unregisterTimedTransition(transition, 3l)); 
	}
	@Test
	public void onceTransitionIsAddedLaterChangesToDelayAreIgnored() {
		Transition transition = buildTransition("T1", 5);
		executablePetriNet.testTransitions.add(transition); 
		timing = new HashedTimingQueue(executablePetriNet, state, 3);
		assertEquals("T1", timing.enabledTimedTransitions.get(8l).iterator().next().getId()); 
		assertFalse("transition is in the timing map",
				timing.checkIfTransitionNotRegistered(transition)); 
		transition.setDelay(2); 
		assertFalse("...still in the map; delay change ignored",
				timing.checkIfTransitionNotRegistered(transition)); 
	}
	@Test
	public void returnsTransitionsForTheCurrentAbsoluteTime() {
		executablePetriNet.testTransitions.add(buildTransition("T1", 4)); 
		executablePetriNet.testTransitions.add(buildTransition("T2", 4)); 
		executablePetriNet.testTransitions.add(buildTransition("T3", 0)); 
		timing = new HashedTimingQueue(executablePetriNet, state, 2);
		assertEquals("current time is 2, with one entry",
				1, timing.getCurrentlyEnabledTimedTransitions().size());
		assertEquals("T3", timing.getCurrentlyEnabledTimedTransitions().iterator().next().getId());
		timing.setCurrentTime(3); 
		assertEquals("current time is 3, with no entries",
				0, timing.getCurrentlyEnabledTimedTransitions().size());
		timing.setCurrentTime(6); 
		assertEquals("current time is 6, with two entries",
				2, timing.getCurrentlyEnabledTimedTransitions().size());
		assertEquals("T1", timing.getCurrentlyEnabledTimedTransitions().iterator().next().getId());
	}
	@Test
	public void resetsTimeAndRebuildTransitionsBasedOnTheNewTime() {
		executablePetriNet.testTransitions.add(buildTransition("T1", 0)); 
		executablePetriNet.testTransitions.add(buildTransition("T2", 4)); 
		timing = new HashedTimingQueue(executablePetriNet, state, 3);
		assertEquals(3, timing.getCurrentTime());
		assertEquals(2, timing.enabledTimedTransitions.size()); 
		timing.resetTimeAndRebuildTimedTransitions(1l);
		assertEquals(1, timing.getCurrentTime());
		assertEquals("new time, but still two transitions",
				2, timing.enabledTimedTransitions.size()); 
		assertEquals("T1", timing.enabledTimedTransitions.get(1l).iterator().next().getId()); 
		assertEquals("T2", timing.enabledTimedTransitions.get(5l).iterator().next().getId()); 
	}
	@Test
	public void keepsTrackOfNextFiringTime() {
		executablePetriNet.testTransitions.add(buildTransition("T1", 0)); 
		executablePetriNet.testTransitions.add(buildTransition("T2", 4)); 
		executablePetriNet.testTransitions.add(buildTransition("T2", 8)); 
		timing = new HashedTimingQueue(executablePetriNet, state, 3);
		assertEquals(3, timing.getCurrentTime());
		assertEquals(3, timing.getNextFiringTime());
		assertEquals(3, timing.getAllFiringTimes().size());
		assertEquals(3l, (long) timing.getAllFiringTimes().iterator().next());
		timing.setCurrentTime(5); 
		assertEquals("advanced past first firing time",7, timing.getNextFiringTime());
		assertEquals("times are preserved, even after moving past first current time",
				3, timing.getAllFiringTimes().size());
		timing.setCurrentTime(10); 
		assertEquals(11, timing.getNextFiringTime());
	}

	
	
	protected DiscreteTransition buildTransition(String id, int delay) {
		DiscreteTransition transition = new DiscreteTransition(id);
		transition.setTimed(true);
		transition.setDelay(delay);
		return transition;
	}
	
	private class TestingExecutablePetriNet extends ExecutablePetriNet  {

		public Set<Transition> testTransitions = new HashSet<>();; 
		
		public TestingExecutablePetriNet(PetriNet petriNet) {
			super(petriNet);
		}
		@Override
		public Set<Transition> getEnabledTimedTransitions() {
			return testTransitions;
		}
		//skip recursive call in the EPN constructor
		@Override
		protected HashedTimingQueue buildTiming(long initTime) {
			return null; 
		}
		
	}
}
