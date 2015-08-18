package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class DiscreteTimedTransitionTest {

	private Transition transition;
	@Before
	public void setUp() throws Exception {
		transition = new DiscreteTimedTransition("T0"); 
	}
	@Test
	public void createTimedTransition() {
		assertTrue(transition.isTimed());
		assertEquals("T0", transition.getId()); 
	}
	@Test
	public void delayCanBeSet() throws Exception {
		assertEquals("default",0, transition.getDelay()); 
		transition.setDelay(1000);
		assertEquals(1000, transition.getDelay()); 
	}
	@Test
	public void waitsDelayMillisecondsBeforeFiring() throws Exception {
		transition.setDelay(1000);
		// current time
		int fakeTime = 40000; 
		((DiscreteTimedTransition) transition).setCurrentTimeForTesting(fakeTime); 
		transition.fire(); 
		// assert current time is at least 1000 ms later.
		assertTrue(41000 <= ((DiscreteTimedTransition) transition).getCurrentTimeForTesting()); 
	}

}
