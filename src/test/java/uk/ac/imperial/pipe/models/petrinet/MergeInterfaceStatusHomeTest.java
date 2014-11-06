package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import java.awt.Color;

import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;

//TODO break out tests for AddPlaceStatusCommand
public class MergeInterfaceStatusHomeTest {

	private IncludeHierarchy includes;
	private PetriNet net;
	private Place place;
	private PlaceStatus status;
	private IncludeHierarchyCommand<Place> command;
	
	@Before
	public void setUp() throws Exception {
		net = new PetriNet(); 
		includes = new IncludeHierarchy(net, "top"); 
		place = new DiscretePlace("P10");
		status = new PlaceStatusInterface(place, includes); 
		place.setStatus(status); 
	}
	@Test
	public void interfacePlaceAddedToInitialHierarchy() throws Exception {
		net.addPlace(place); 
		MergeInterfaceStatusHome mergeStatus = new MergeInterfaceStatusHome(place, status); 
		assertEquals(1, net.getPlaces().size());
		assertEquals(0, includes.getInterfacePlaceMap().size());
		Result<InterfacePlaceAction> result = mergeStatus.add(includes); 
		assertFalse(result.hasResult()); 
		assertEquals(1, includes.getInterfacePlaceMap().size());
		Place addedPlace = includes.getInterfacePlace("P10"); 
		assertEquals("P10", addedPlace.getId()); 
	}
	@Test
	public void interfacePlaceAddedToIncludeHierarchiesInAccessScopeOnlyWithAvailableMergeStatusAndListensForTokenChanges() throws Exception {
		PetriNet net2 = buildNet(2); 
		PetriNet net3 = buildNet(3); 
		includes.include(net2, "a"); 
		IncludeHierarchy include2 = includes.getInclude("a"); 
		include2.include(net3, "b");
		IncludeHierarchy include3 = includes.getInclude("b"); 
		net2.addPlace(place); 
		MergeInterfaceStatusHome mergeStatus = new MergeInterfaceStatusHome(place, status); 
		status.setMergeInterfaceStatus(mergeStatus); // normally automatic
		assertEquals(3, net2.getPlaces().size());
		assertEquals(0, includes.getInterfacePlaceMap().size());
		assertEquals(0, include2.getInterfacePlaceMap().size());
		assertEquals(0, include3.getInterfacePlaceMap().size());
		Result<InterfacePlaceAction> result = mergeStatus.add(include2); 
		assertFalse(result.hasResult()); 
		assertEquals(1, includes.getInterfacePlaceMap().size());
		assertEquals(1, include2.getInterfacePlaceMap().size());
		assertEquals("child not updated",0, include3.getInterfacePlaceMap().size());
		Place homePlace = include2.getInterfacePlace("P10"); 
		Place availablePlace = includes.getInterfacePlace("a.P10"); 
		assertTrue(homePlace.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusHome);
		assertTrue(availablePlace.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAvailable);
    	homePlace.setTokenCount("Default", 3); 
    	assertEquals(3, availablePlace.getTokenCount("Default")); 
	}
	
	protected PetriNet buildNet(int i) throws PetriNetComponentNotFoundException {
		PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
    			APlace.withId("P1")).and(AnImmediateTransition.withId("T0")).andFinally(
    			ANormalArc.withSource("P0").andTarget("T0"));
		net.setName(new NormalPetriNetName("net"+i));
		return net;
	}

}
