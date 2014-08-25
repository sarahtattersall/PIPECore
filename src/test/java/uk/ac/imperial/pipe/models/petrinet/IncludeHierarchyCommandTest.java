package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchyCommand;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;

public class IncludeHierarchyCommandTest {

	@Test
	public void commandExecutesPossiblyReturningMessages() {
		IncludeHierarchyCommand<Object> command = new DummyCommand<>(); 
		assertEquals(0, command.getResult().getEntries().size());
		Result<Object> result = command.execute(new IncludeHierarchy(
				new PetriNet(new NormalPetriNetName("net1")), "fred")); 
		assertEquals(1, result.getEntries().size());
		assertEquals(1, command.getResult().getEntries().size());
	}
}
