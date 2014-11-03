package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DiscreteInterfacePlaceTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    DiscretePlace place;

	private InterfacePlace discreteInterfacePlace;

	private InterfacePlace discreteInterfacePlace2;

	private IncludeHierarchy includes;

    @Before
    public void setUp() {
        place = new DiscretePlace("test", "test");
        includes = new IncludeHierarchy(new PetriNet(), "top"); 
        discreteInterfacePlace = new DiscreteInterfacePlace(place, InterfacePlaceStatusEnum.HOME.buildStatus(includes), "home", "away");
    }
    //TODO should look different in GUI
    @Test
	public void placeKnowsItsHomeInterfacePlace() throws Exception {
    	InterfacePlaceStatus home = InterfacePlaceStatusEnum.HOME.buildStatus(includes);
    	discreteInterfacePlace = new DiscreteInterfacePlace(place, home, "home");
    	assertTrue(discreteInterfacePlace.getInterfacePlaceStatus() instanceof InterfacePlaceStatusHome); 
    	assertEquals(home, place.getInterfacePlace().getInterfacePlaceStatus()); 
    	assertEquals(discreteInterfacePlace, place.getInterfacePlace()); 
    	assertEquals(discreteInterfacePlace, discreteInterfacePlace.getInterfacePlaceStatus().getInterfacePlace()); 
    	assertEquals(includes, discreteInterfacePlace.getInterfacePlaceStatus().getIncludeHierarchy()); 
    	InterfacePlaceStatus available = InterfacePlaceStatusEnum.AVAILABLE.buildStatus(includes); 
    	DiscreteInterfacePlace discreteInterfacePlaceAvailable = new DiscreteInterfacePlace(place, available, "home", "away");
    	assertEquals("place still points home",home, place.getInterfacePlace().getInterfacePlaceStatus()); 
    	assertTrue(discreteInterfacePlaceAvailable.getInterfacePlaceStatus() instanceof InterfacePlaceStatusAvailable); 
    	assertEquals(discreteInterfacePlaceAvailable, discreteInterfacePlaceAvailable.getInterfacePlaceStatus().getInterfacePlace()); 
    	InterfacePlaceStatus inUse = InterfacePlaceStatusEnum.IN_USE.buildStatus(includes); 
    	DiscreteInterfacePlace discreteInterfacePlaceInUse = new DiscreteInterfacePlace(place, inUse, "home", "away");
    	assertEquals("place still points home",home, place.getInterfacePlace().getInterfacePlaceStatus()); 
    	assertTrue(discreteInterfacePlaceInUse.getInterfacePlaceStatus() instanceof InterfacePlaceStatusInUse); 
    	assertEquals(discreteInterfacePlaceInUse, discreteInterfacePlaceInUse.getInterfacePlaceStatus().getInterfacePlace()); 
    	
	}
    @Test
	public void mirrorsTokenCountOfSourcePlace() throws Exception {
    	place.setTokenCount("Default", 3); 
    	assertEquals(3, discreteInterfacePlace.getTokenCount("Default")); 
	}
    @Test
    public void sourceMirrorsTokenCountOfInterfacePlace() throws Exception {
    	discreteInterfacePlace.setTokenCount("Default", 2); 
    	assertEquals(2, place.getTokenCount("Default")); 
    }
    //TODO consider whether only In_Use should mirror
    @Test
    public void multipleInterfacePlacesMirrorSource() throws Exception {
    	discreteInterfacePlace2 = new DiscreteInterfacePlace(place,InterfacePlaceStatusEnum.HOME.buildStatus(includes),"","");
    	place.setTokenCount("Default", 4); 
    	assertEquals(4, discreteInterfacePlace.getTokenCount("Default")); 
    	assertEquals(4, discreteInterfacePlace2.getTokenCount("Default")); 
    }
    @Test
    public void oneInterfacePlaceSendsCountsToSourceAndOtherInterfacePlaces() throws Exception {
    	discreteInterfacePlace2 = new DiscreteInterfacePlace(place,InterfacePlaceStatusEnum.AVAILABLE.buildStatus(includes), "","");
    	discreteInterfacePlace2.setTokenCount("Default", 2); 
    	assertEquals(2, place.getTokenCount("Default")); 
    	assertEquals(2, discreteInterfacePlace.getTokenCount("Default")); 
    }
    @Test
	public void interfacePlaceCantBeBuiltFromAnotherInterfacePlace() throws Exception {
    	exception.expect(IllegalArgumentException.class);
    	exception.expectMessage("InterfaceDiscretePlace:  an InterfacePlace cannot be constructed from another InterfacePlace, only from a DiscretePlace.");
    	DiscreteInterfacePlace discreteInterfacePlace = new DiscreteInterfacePlace(place, InterfacePlaceStatusEnum.AVAILABLE.buildStatus(includes), "home", "away");
    	discreteInterfacePlace2 = new DiscreteInterfacePlace(discreteInterfacePlace,InterfacePlaceStatusEnum.AVAILABLE.buildStatus(includes),"","");
	}
    @Test
	public void placeAndInterfacePlaceBothReturnSameInterfacePlace() throws Exception {
    	place = new DiscretePlace("test", "test");
    	assertNull(place.getInterfacePlace()); 
    	discreteInterfacePlace = new DiscreteInterfacePlace(place, InterfacePlaceStatusEnum.HOME.buildStatus(includes), "home");
    	assertEquals(discreteInterfacePlace, place.getInterfacePlace()); 
    	assertEquals(discreteInterfacePlace, discreteInterfacePlace.getInterfacePlace()); 
    	discreteInterfacePlace = new DiscreteInterfacePlace(place, InterfacePlaceStatusEnum.AVAILABLE.buildStatus(includes), "home", "away");
    	assertEquals(discreteInterfacePlace, discreteInterfacePlace.getInterfacePlace()); 
    	discreteInterfacePlace = new DiscreteInterfacePlace(place, InterfacePlaceStatusEnum.IN_USE.buildStatus(includes), "home", "away");
    	assertEquals(discreteInterfacePlace, discreteInterfacePlace.getInterfacePlace()); 
    	
	}
    //TODO consider whether use(false) is same as remove()
//    @Test
	public void useInterfacePlaceTogglesItsStatus() throws Exception {
    	//TODO enum builds status:  build(IH), and passes the status, not the enum, to the constructor.  
    	// use mock IH to force return of empty collection for getComponents during remove
    	discreteInterfacePlace = new DiscreteInterfacePlace(place, InterfacePlaceStatusEnum.AVAILABLE.buildStatus(includes), "home", "away");
    	assertTrue(discreteInterfacePlace.getInterfacePlaceStatus() instanceof InterfacePlaceStatusAvailable); 
    	assertTrue(discreteInterfacePlace.canUse()); 
    	assertFalse(discreteInterfacePlace.isInUse()); 
    	assertFalse(discreteInterfacePlace.isHome()); 
//    	assertFalse(discreteInterfacePlace.use().hasResult()); 
    	assertTrue(discreteInterfacePlace.getInterfacePlaceStatus() instanceof InterfacePlaceStatusInUse); 
    	assertTrue(discreteInterfacePlace.isInUse()); 
    	assertFalse(discreteInterfacePlace.canUse()); 
    	assertFalse(discreteInterfacePlace.isHome()); 
    	discreteInterfacePlace.remove(); 
    	assertTrue(discreteInterfacePlace.getInterfacePlaceStatus() instanceof InterfacePlaceStatusAvailable); 
    	assertTrue(discreteInterfacePlace.canUse()); 
    	assertFalse(discreteInterfacePlace.isInUse()); 
    	assertFalse(discreteInterfacePlace.isHome()); 
	}
    @Test
	public void homeInterfacePlaceCantBeUsed() throws Exception {
    	//TODO throw or just return false? 
//    	exception.expect(IllegalStateException.class); 
//    	exception.expectMessage("InterfacePlaceStatusHome: interface place cannot be used in the petri net that is the home of its underlying place.");
    	discreteInterfacePlace = new DiscreteInterfacePlace(place, InterfacePlaceStatusEnum.HOME.buildStatus(includes), "home");
    	assertFalse("can't use home IP",discreteInterfacePlace.canUse()); 
    	assertFalse(discreteInterfacePlace.use()); 
	}
    @Test
	public void homeStatusImpliesNoAwayAlias() throws Exception {
    	discreteInterfacePlace = new DiscreteInterfacePlace(place, InterfacePlaceStatusEnum.HOME.buildStatus(includes), "home", "away");
    	assertTrue(discreteInterfacePlace.getInterfacePlaceStatus() instanceof InterfacePlaceStatusHome); 
    	assertEquals("home.test", discreteInterfacePlace.getId()); 
    	//TODO homeStatusImpliesNoAwayAlias
	}
    @Test
    public void awayStatusImpliesNotNullAwayAlias() throws Exception {
    	discreteInterfacePlace = new DiscreteInterfacePlace(place, InterfacePlaceStatusEnum.AVAILABLE.buildStatus(includes), "home");
    	assertTrue(discreteInterfacePlace.getInterfacePlaceStatus() instanceof InterfacePlaceStatusAvailable); 
    	assertEquals("..home.test", discreteInterfacePlace.getId()); 
    }
    @Test
	public void eitherHomeOrAwayIncludeNameCanBeChanged() throws Exception {
    	discreteInterfacePlace = new DiscreteInterfacePlace(place, InterfacePlaceStatusEnum.AVAILABLE.buildStatus(includes), "home", "away");
    	assertEquals("away..home.test", discreteInterfacePlace.getId()); 
    	assertEquals("away..home.test", discreteInterfacePlace.getName()); 
    	discreteInterfacePlace.setHomeName("x"); 
    	assertEquals("away..x.test", discreteInterfacePlace.getId()); 
    	discreteInterfacePlace.setAwayName("y"); 
    	assertEquals("y..x.test", discreteInterfacePlace.getId()); 
    	assertEquals("y..x.test", discreteInterfacePlace.getName()); 
	}
    // PN
    // soure place remove
    // home IP remove
    // 
	// interfaceplacetest:  added to a PN; marking follows other IP; participates in EPN 
//	@Test
//	public void listensForChangesToSourceAndTargetIncludesAndRenamesAccordingly() throws Exception {
//
//	}

}
