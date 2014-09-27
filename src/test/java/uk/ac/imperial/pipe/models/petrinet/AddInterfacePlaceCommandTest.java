package uk.ac.imperial.pipe.models.petrinet;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AddInterfacePlaceCommandTest {

	@Mock
	private IncludeHierarchy mockHierarchy;
	private AddInterfacePlaceCommand<Place> command;

	@Test
	public void interfacePlaceAddedToInitialHierarchy() throws Exception {
		Place p0 = new DiscretePlace("P0"); 
		command = new AddInterfacePlaceCommand<Place>(p0, new IncludeHierarchy(new PetriNet(), "top")); 
		command.execute(mockHierarchy);
		verify(mockHierarchy).addInterfacePlaceToMap(any(InterfacePlace.class)); 
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
