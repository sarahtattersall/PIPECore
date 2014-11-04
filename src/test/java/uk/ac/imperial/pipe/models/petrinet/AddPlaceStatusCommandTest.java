package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

public class AddPlaceStatusCommandTest {

	private IncludeHierarchy includes;
	private PetriNet net;
	private Place place;
	private PlaceStatusInterface status;
	private IncludeHierarchyCommand<Place> command;
	
	@Before
	public void setUp() throws Exception {
		net = new PetriNet(); 
		includes = new IncludeHierarchy(net, "top"); 
		place = new DiscretePlace("P0");
		net.addPlace(place); 
		status = new PlaceStatusInterface(place); 
	}
//	@Test
	public void interfacePlaceAddedToInitialHierarchy() throws Exception {
		status.setStatusForTesting(new MergeInterfaceStatusHome(place)); 
//		command = new AddPlaceStatusCommand(status, includes);
		assertEquals(1, net.getPlaces().size());
		assertEquals(0, includes.getPlacesInInterface().size());
		command.execute(includes);
		assertEquals(1, includes.getPlacesInInterface().size());
	}
//	@Test
	//FIXME
	public void interfacePlaceAddedToHomeNetAndHierarchyReplacingExistingPlace() throws Exception {
		Place p0 = new DiscretePlace("P0");
		net.addPlace(p0); 
		assertEquals(1, net.getPlaces().size());
		assertEquals(0, includes.getInterfacePlaces().size());
		command = new AddInterfacePlaceCommand<Place>(p0, new IncludeHierarchy(new PetriNet(), "top")); 
		command.execute(includes);
		assertEquals(1, net.getPlaces().size());
		Place newp0 = net.getComponent("top.P0", Place.class); 
		assertEquals(1, includes.getInterfacePlaces().size());
		InterfacePlace ip0 = includes.getInterfacePlaces().iterator().next(); 
		assertEquals(ip0, newp0);
		assertTrue(ip0.getInterfacePlaceStatus() instanceof InterfacePlaceStatusHome); 
	}
	//TODO interfacePlaceAddedToPetriNetPlacesWhenNotHome
//	@Test
	public void interfacePlaceAddedToPetriNetPlacesWhenNotHome() throws Exception {

	}
	//TODO namedWithUniqueNamesOfSourceAndTargetIncludesPlusPlace
//	@Test
	public void namedWithUniqueNamesOfSourceAndTargetIncludesPlusPlace() throws Exception {
		
	}
	//TODO hasHomeStatusInSourceIncludeAndAvailableStatusElsewhere
//	@Test
	public void hasHomeStatusInSourceIncludeAndAvailableStatusElsewhere() throws Exception {

	}
	//TODO addedToOtherIncludesAsDefinedByAccessScope
//	@Test
	public void addedToOtherIncludesAsDefinedByAccessScope() throws Exception {

	}
}
