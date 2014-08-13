package uk.ac.imperial.pipe.includeCommands;

import java.util.List;

import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;

public class DummyCommand extends AbstractIncludeHierarchyCommand  {
	
	public DummyCommand() {
		super(); 
	}
	@Override
	public List<String> execute(IncludeHierarchy includeHierarchy) {
		messages.add("dummy message for "+includeHierarchy.getPetriNet().getNameValue()); 
		return messages;
	}
}
