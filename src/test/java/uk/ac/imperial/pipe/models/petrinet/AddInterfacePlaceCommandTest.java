package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

public class AddInterfacePlaceCommandTest {

	private IncludeHierarchy includes;
	private AddInterfacePlaceCommand<Place> command;
	private PetriNet net;
	
	@Before
	public void setUp() throws Exception {
		net = new PetriNet(); 
		includes = new IncludeHierarchy(net, "top"); 
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
//	@Test
	public void interfacePlaceAddedToInitialHierarchy() throws Exception {
		Place p0 = new DiscretePlace("P0");
		assertEquals(0, net.getPlaces().size());
		assertEquals(0, includes.getInterfacePlaces().size());
		command = new AddInterfacePlaceCommand<Place>(p0, new IncludeHierarchy(new PetriNet(), "top")); 
		command.execute(includes);
		assertEquals("not added to PN places until InUse",
				0, net.getPlaces().size());
		assertEquals(1, includes.getInterfacePlaces().size());
	}
	//TODO interfacePlaceAddedToPetriNetPlacesWhenNotHome
	@Test
	public void interfacePlaceAddedToPetriNetPlacesWhenNotHome() throws Exception {

	}
	//TODO namedWithUniqueNamesOfSourceAndTargetIncludesPlusPlace
	@Test
	public void namedWithUniqueNamesOfSourceAndTargetIncludesPlusPlace() throws Exception {
		
	}
	//TODO hasHomeStatusInSourceIncludeAndAvailableStatusElsewhere
	@Test
	public void hasHomeStatusInSourceIncludeAndAvailableStatusElsewhere() throws Exception {

	}
	//TODO addedToOtherIncludesAsDefinedByAccessScope
	@Test
	public void addedToOtherIncludesAsDefinedByAccessScope() throws Exception {

	}
}
