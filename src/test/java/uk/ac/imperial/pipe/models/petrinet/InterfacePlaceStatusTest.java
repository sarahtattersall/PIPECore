package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class InterfacePlaceStatusTest {

	private InterfacePlaceStatus home;
	private InterfacePlaceStatus available;
	private InterfacePlaceStatus inUse;
	private IncludeHierarchy includes;
	@Before
	public void setUp() throws Exception {
		includes = new IncludeHierarchy(new PetriNet(), "top"); 
		home = InterfacePlaceStatusEnum.HOME.buildStatus(includes);
		available = InterfacePlaceStatusEnum.AVAILABLE.buildStatus(includes);
		inUse = InterfacePlaceStatusEnum.IN_USE.buildStatus(includes);
	}
	
	@Test
	public void testEnumReturnsStatus() {
		InterfacePlaceStatus status = InterfacePlaceStatusEnum.HOME.buildStatus(includes); 
		assertTrue(status instanceof InterfacePlaceStatusHome); 
		status = InterfacePlaceStatusEnum.AVAILABLE.buildStatus(includes); 
		assertTrue(status instanceof InterfacePlaceStatusAvailable); 
		status = InterfacePlaceStatusEnum.IN_USE.buildStatus(includes); 
		assertTrue(status instanceof InterfacePlaceStatusInUse); 
	}
	//FIXME:  does remove imply there are arcs to / from this IP or not? 
	@Test
	public void isInUseOnlyTrueForInUseStatus() throws Exception {
		assertFalse(home.isInUse()); 
		assertFalse(available.isInUse()); 
		assertTrue(inUse.isInUse());  // was False 
	}
	@Test
	public void isHomeOnlyTrueForHomeStatus() throws Exception {
		assertTrue(home.isHome()); 
		assertFalse(available.isHome()); 
		assertFalse(inUse.isHome());  // was False 
	}
	@Test
	public void canUseOnlyTrueForAvailableStatus() throws Exception {
		assertFalse(home.canUse()); 
		assertTrue(available.canUse()); 
		assertFalse(inUse.canUse());  // was False 
	}

}
