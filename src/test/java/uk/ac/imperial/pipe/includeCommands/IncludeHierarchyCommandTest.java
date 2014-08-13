package uk.ac.imperial.pipe.includeCommands;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;

public class IncludeHierarchyCommandTest {

	@Test
	public void commandExecutesPossiblyReturningMessages() {
		IncludeHierarchyCommand command = new DummyCommand(); 
		assertEquals(0, command.getMessages().size());
		List<String> messages = command.execute(new IncludeHierarchy(
				new PetriNet(new NormalPetriNetName("net1")), "fred")); 
		assertEquals(1, messages.size());
		assertEquals(1, command.getMessages().size());
	}
}
