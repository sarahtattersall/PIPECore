package uk.ac.imperial.pipe.includeCommands;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import uk.ac.imperial.pipe.includeCommands.AbstractIncludeHierarchyCommand;
import uk.ac.imperial.pipe.includeCommands.IncludeHierarchyCommand;
import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;

public class IncludeHierarchyCommandTest {

	private List<String> messages;

	@Test
	public void commandExecutesPossiblyReturningMessages() {
		IncludeHierarchyCommand command = new DummyCommand(); 
		assertEquals(0, command.getMessages().size());
		messages = command.execute(new IncludeHierarchy(buildNet(1), "fred")); 
		assertEquals(1, messages.size());
		assertEquals(1, command.getMessages().size());
	}
	@Test
	public void commandExecutesAtEachParentLevel() throws Exception {
		IncludeHierarchyCommand command = new DummyCommand(); 
		PetriNet net1 = buildNet(1); 
		net1.getIncludeHierarchy().include(buildNet(2), "2nd"); 
		net1.getIncludeHierarchy().getInclude("2nd").include(buildNet(3), "3rd"); 
		messages = net1.getIncludeHierarchy().getInclude("2nd").getInclude("3rd").parents(command); 
		assertEquals(2, messages.size());
		assertTrue(messages.get(0).endsWith("2")); 
		assertTrue(messages.get(1).endsWith("1")); 
	}
	protected PetriNet buildNet(int i) {
		return new PetriNet(new NormalPetriNetName("net"+i));
	}
	private class DummyCommand extends AbstractIncludeHierarchyCommand  {

		public DummyCommand() {
			super(); 
		}
		@Override
		public List<String> execute(IncludeHierarchy includeHierarchy) {
			messages.add("dummy message for "+includeHierarchy.getPetriNet().getNameValue()); 
			return messages;
		}
		
	}
}
