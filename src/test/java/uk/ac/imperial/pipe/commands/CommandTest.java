package uk.ac.imperial.pipe.commands;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;

public class CommandTest {

	private List<String> messages;

	@Test
	public void commandExecutesPossiblyReturningMessages() {
		messages = new ArrayList<String>(); 
		IncludeHierarchyCommand command = new DummyCommand(messages); 
		messages = command.execute(new IncludeHierarchy(new PetriNet(), "fred")); 
		assertEquals(1, messages.size());
	}
	private class DummyCommand implements IncludeHierarchyCommand {

		private List<String> messages;

		public DummyCommand(List<String> messages) {
			this.messages = messages; 
		}


		@Override
		public List<String> execute(IncludeHierarchy includeHierarchy) {
			messages.add("dummy message"); 
			return messages;
		}
		
	}
}
