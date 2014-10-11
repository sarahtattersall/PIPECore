package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.exceptions.IncludeException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;

@RunWith(MockitoJUnitRunner.class)
public class InterfacePlaceStatusTest {

	private InterfacePlaceStatus home;
	private InterfacePlaceStatus available;
	private InterfacePlaceStatus inUse;
	private IncludeHierarchy includes;

//	@Mock
	private PetriNet net;
	private DiscretePlace place;
	private PetriNet net2;
	private Place placeTop;
	private Place placeA;
	private Place placeTopIP;
	private IncludeHierarchy includeA;
	private InterfacePlaceStatus status; 

	@Before
	public void setUp() throws Exception {
		net = createSimpleNet(1);  
		net2 = createSimpleNet(2);  
		buildHierarchyWithInterfacePlaces(); 
		home = InterfacePlaceStatusEnum.HOME.buildStatus(includes);
		available = InterfacePlaceStatusEnum.AVAILABLE.buildStatus(includes);
		inUse = InterfacePlaceStatusEnum.IN_USE.buildStatus(includes);
	}
	private void buildHierarchyWithInterfacePlaces()
			throws PetriNetComponentNotFoundException, IncludeException {
		includes = new IncludeHierarchy(net, "top"); 
    	includeA = includes.include(net2, "a");  
    	placeTop = net.getComponent("P0", Place.class); 
    	placeA = net2.getComponent("P0", Place.class); 
    	includes.getChildInclude("a").addToInterface(placeA);
    	placeTopIP = includes.getInterfacePlace("top..a.P0"); 
	}
	@Test
	public void addedToPlacesOnlyWhenInUse() throws Exception {
		available = placeTopIP.getInterfacePlace().getStatus(); 
		assertTrue(available instanceof InterfacePlaceStatusAvailable); 
		assertEquals(2, net.getPlaces().size()); 
		assertEquals("IP's place is in the home include",
				placeTopIP.getInterfacePlace().getPlace(), includeA.getPetriNet().getComponent("P0", Place.class)); 
		
		placeTopIP.getInterfacePlace().use(); 
		assertTrue(placeTopIP.getInterfacePlace().getStatus() instanceof InterfacePlaceStatusInUse); 
		assertEquals(3, net.getPlaces().size()); 
		assertEquals(placeTopIP.getInterfacePlace(), includes.getPetriNet().getComponent("top..a.P0",  Place.class)); 
	}
	@Test
	public void canRemoveIfNoDependentComponentsForInuse() throws Exception {
		status = placeTopIP.getInterfacePlace().getStatus(); 
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
	@Test
	public void canNotRemoveIfDependentComponentsForInuseAndComponentIdsListed() throws Exception {
		status = placeTopIP.getInterfacePlace().getStatus(); 
		status.use(); 
		status = status.nextStatus(); 
		assertTrue(status instanceof InterfacePlaceStatusInUse); 
		assertEquals(3, net.getPlaces().size()); 
		
		assertEquals(placeTopIP.getInterfacePlace(), includes.getPetriNet().getComponent("top..a.P0",  Place.class)); 
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
		Result<InterfacePlaceAction> result = status.remove(); 
		status = status.nextStatus(); 
		assertTrue(result.hasResult()); 
		assertTrue("status unchanged",status instanceof InterfacePlaceStatusInUse); 
		assertEquals(3, result.getEntries().size()); 
		Iterator<ResultEntry<InterfacePlaceAction>> iterator = result.getEntries().iterator();
		ResultEntry<InterfacePlaceAction> entry = iterator.next(); 
		
		assertEquals("InterfacePlace top..a.P0 cannot be removed from IncludeHierarchy top" +
				" because it is referenced in a functional expression in component T0 TO top..a.P0", entry.message);
		entry = iterator.next(); 
		assertEquals("InterfacePlace top..a.P0 cannot be removed from IncludeHierarchy top" +
				" because it is referenced in a functional expression in component frp1", entry.message);
		entry = iterator.next(); 
		assertEquals("InterfacePlace top..a.P0 cannot be removed from IncludeHierarchy top" +
				" because it is referenced in a functional expression in component T2", entry.message);
		assertEquals("T2",entry.value.getComponentId()); 
		
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

}
