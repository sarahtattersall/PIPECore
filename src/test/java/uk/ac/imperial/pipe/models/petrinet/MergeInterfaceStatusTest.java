package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.exceptions.IncludeException;
import uk.ac.imperial.pipe.exceptions.InvalidRateException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;

public class MergeInterfaceStatusTest {

	private MergeInterfaceStatus home;
	private MergeInterfaceStatus available;
	private MergeInterfaceStatus away;
	private IncludeHierarchy includes;

	private PetriNet net;
	private DiscretePlace place;
	private PetriNet net2;
	private Place placeTop;
	private Place placeA;
	private Place placeTopIP;
	private IncludeHierarchy includeA;
	private InterfacePlaceStatus status;
	private PetriNet net3;
	private Place placeB;
	private IncludeHierarchy includeB; 

	@Before
	public void setUp() throws Exception {
		net = createSimpleNet(1);  
		net2 = createSimpleNet(2);  
		net3 = createSimpleNet(3);  
//		buildHierarchyWithInterfacePlaces(); 
//		home = InterfacePlaceStatusEnum.HOME.buildStatus(includes);
//		available = InterfacePlaceStatusEnum.AVAILABLE.buildStatus(includes);
//		inUse = InterfacePlaceStatusEnum.IN_USE.buildStatus(includes);
	}
	private void buildHierarchyWithInterfacePlaces()
			throws PetriNetComponentNotFoundException, IncludeException {
		includes = new IncludeHierarchy(net, "top"); 
    	includeA = includes.include(net2, "a");  
    	includeB = includeA.include(net3, "b");  
    	placeTop = net.getComponent("P0", Place.class); 
    	placeA = net2.getComponent("P0", Place.class); 
    	placeB = net2.getComponent("P0", Place.class); 
    	includes.getChildInclude("a").addToInterfaceOld(placeA);
    	placeTopIP = includes.getInterfacePlaceOld("top..a.P0"); 
	}
//	@Test
	public void addedToPlacesOnlyWhenInUse() throws Exception {
//		available = placeTopIP.getInterfacePlace().getInterfacePlaceStatus(); 
//		assertTrue(available instanceof InterfacePlaceStatusAvailable); 
//		assertEquals(2, net.getPlaces().size()); 
//		assertEquals("IP's place is in the home include",
//				placeTopIP.getInterfacePlace().getPlace(), includeA.getPetriNet().getComponent("P0", Place.class)); 
//		
//		placeTopIP.getInterfacePlace().use(); 
//		assertTrue(placeTopIP.getInterfacePlace().getInterfacePlaceStatus() instanceof InterfacePlaceStatusInUse); 
//		assertEquals(3, net.getPlaces().size()); 
//		assertEquals(placeTopIP.getInterfacePlace(), includes.getPetriNet().getComponent("top..a.P0",  Place.class)); 
	}
//	@Test
	public void canRemoveIfNoDependentComponentsForInuse() throws Exception {
		status = placeTopIP.getInterfacePlace().getInterfacePlaceStatus(); 
		assertTrue(status instanceof InterfacePlaceStatusAvailable); 
		assertEquals(2, net.getPlaces().size()); 
		status.use(); 
		status = status.nextStatus(); 
		assertTrue(status instanceof InterfacePlaceStatusInUse); 
		assertEquals(3, net.getPlaces().size()); 
		assertEquals(placeTopIP.getInterfacePlace(), includes.getPetriNet().getComponent("top..a.P0",  Place.class)); 

		Result<InterfacePlaceAction> result = status.remove(); 
		status = status.nextStatus(); 
		assertFalse(result.hasResult()); 
		assertTrue(status instanceof InterfacePlaceStatusAvailable); 
		assertEquals(2, net.getPlaces().size()); 
	}
//	@Test
	public void canNotRemoveIfDependentComponentsForInuseAndAffectedComponentIdsListed() throws Exception {
		status = placeTopIP.getInterfacePlace().getInterfacePlaceStatus(); 
		status.use(); 
		status = status.nextStatus(); 
		assertTrue(status instanceof InterfacePlaceStatusInUse); 
		assertEquals(3, net.getPlaces().size()); 
		
		assertEquals(placeTopIP.getInterfacePlace(), includes.getPetriNet().getComponent("top..a.P0",  Place.class)); 
		addRateParameterAndTransitionAndArcReferencingInterfacePlace(); 
		Result<InterfacePlaceAction> result = status.remove(); 
		status = status.nextStatus(); 
		assertTrue(result.hasResult()); 
		assertTrue("status unchanged, because remove wasn't successful",status instanceof InterfacePlaceStatusInUse); 
		assertEquals(3, result.getEntries().size()); 
	}
	private void addRateParameterAndTransitionAndArcReferencingInterfacePlace()
			throws InvalidRateException, PetriNetComponentNotFoundException {
		FunctionalRateParameter rateParameter = new FunctionalRateParameter("#(top..a.P0)", "frp1", "frp1");
        net.addRateParameter(rateParameter);
        Transition t2 = new DiscreteTransition("T2", "T2");
        t2.setTimed(true);
        t2.setRate(rateParameter);
        net.addTransition(t2);
        Map<String, String> tokenWeights = new HashMap<>(); 
        tokenWeights.put("Default", "#(top..a.P0)");
        Transition t0 = net.getComponent("T0", Transition.class); 
        OutboundArc newArc = new OutboundNormalArc(t0, placeTopIP, tokenWeights);
        net.addArc(newArc);
	}
//	@Test
	public void removeForHomeInterfacePropagatesUsingAccessScope() throws Exception {

	}
//	@Test
	public void removeOkForSomeNetsButNotOthersGivesResultForFailingNets() throws Exception {
    	includes.getInclude("b").addToInterfaceOld(placeB);
    	InterfacePlace placeTopIPb = includes.getInterfacePlaceOld("top..b.P0");
    	InterfacePlaceStatus statusTop = placeTopIPb.getInterfacePlace().getInterfacePlaceStatus(); 
    	statusTop.use();
    	statusTop = statusTop.nextStatus(); 
    	assertTrue(statusTop instanceof InterfacePlaceStatusInUse); 
    	
    	InterfacePlace placeAIPb = includes.getInterfacePlaceOld("a..b.P0"); 
    	InterfacePlaceStatus statusA = placeAIPb.getInterfacePlace().getInterfacePlaceStatus(); 
    	statusA.use(); 
    	statusA = statusA.nextStatus(); 
    	assertTrue(statusA instanceof InterfacePlaceStatusInUse); 
    	
		FunctionalRateParameter rateParameter = new FunctionalRateParameter("#(top..a.P0)", "frp1", "frp1");
        net.addRateParameter(rateParameter);
        
    	
	}
//	@Test
	public void testEnumReturnsStatus() {
		InterfacePlaceStatus status = InterfacePlaceStatusEnum.HOME.buildStatus(includes); 
		assertTrue(status instanceof InterfacePlaceStatusHome); 
		status = InterfacePlaceStatusEnum.AVAILABLE.buildStatus(includes); 
		assertTrue(status instanceof InterfacePlaceStatusAvailable); 
		status = InterfacePlaceStatusEnum.IN_USE.buildStatus(includes); 
		assertTrue(status instanceof InterfacePlaceStatusInUse); 
	}
//	@Test
//	public void isInUseOnlyTrueForInUseStatus() throws Exception {
//		assertFalse(home.isInUse()); 
//		assertFalse(available.isInUse()); 
//		assertTrue(inUse.isInUse());   
//	}
//	@Test
//	public void isHomeOnlyTrueForHomeStatus() throws Exception {
//		assertTrue(home.isHome()); 
//		assertFalse(available.isHome()); 
//		assertFalse(inUse.isHome());   
//	}
//	@Test
//	public void canUseOnlyTrueForAvailableStatus() throws Exception {
//		assertFalse(home.canUse()); 
//		assertTrue(available.canUse()); 
//		assertFalse(inUse.canUse());   
//	}
	private PetriNet createSimpleNet(int i) {
		PetriNet net = 
				APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
				APlace.withId("P1")).and(AnImmediateTransition.withId("T0")).and(
				AnImmediateTransition.withId("T1")).and(
				ANormalArc.withSource("T1").andTarget("P1")).andFinally(
				ANormalArc.withSource("T0").andTarget("P0").with("#(P0)", "Default").token());
		net.setName(new NormalPetriNetName("net"+i));
		return net; 
	}
//	//TODO consider having HomeIP replace the original Place, and 
	//TODO any impact from renames? 
	//TODO one net ok, but the other not for remove 
	//TODO call use and remove on the wrong Status
	//TODO remove for home calls all; but for others, only affects local hierarchy
}
