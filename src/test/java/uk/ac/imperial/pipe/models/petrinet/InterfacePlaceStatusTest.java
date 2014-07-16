package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class InterfacePlaceStatusTest {

	private InterfacePlaceStatus home;
	private InterfacePlaceStatus available;
	private InterfacePlaceStatus inUse;
	@Before
	public void setUp() throws Exception {
		home = InterfacePlaceStatusEnum.HOME.buildStatus();
		available = InterfacePlaceStatusEnum.AVAILABLE.buildStatus();
		inUse = InterfacePlaceStatusEnum.IN_USE.buildStatus();
	}
	
	@Test
	public void testEnumReturnsStatus() {
		InterfacePlaceStatus status = InterfacePlaceStatusEnum.HOME.buildStatus(); 
		assertTrue(status instanceof InterfacePlaceStatusHome); 
		status = InterfacePlaceStatusEnum.AVAILABLE.buildStatus(); 
		assertTrue(status instanceof InterfacePlaceStatusAvailable); 
		status = InterfacePlaceStatusEnum.IN_USE.buildStatus(); 
		assertTrue(status instanceof InterfacePlaceStatusInUse); 
	}
	@Test
	public void canRemoveReturnsFalseForInUse() throws Exception {
		assertTrue(home.canRemove()); 
		assertTrue(available.canRemove()); 
		assertFalse(inUse.canRemove()); 
	}

}
