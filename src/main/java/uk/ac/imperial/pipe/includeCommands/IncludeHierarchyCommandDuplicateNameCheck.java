package uk.ac.imperial.pipe.includeCommands;

import java.util.List;

import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.name.PetriNetName;

public class IncludeHierarchyCommandDuplicateNameCheck extends
		AbstractIncludeHierarchyCommand {

	private PetriNetName petriNetName;

	public IncludeHierarchyCommandDuplicateNameCheck(PetriNetName petriNetName) {
		super(); 
		this.petriNetName = petriNetName; 
	}

	@Override
	public List<String> execute(IncludeHierarchy includeHierarchy) {
		if (includeHierarchy.getPetriNet().getName().equals(petriNetName)) {
			messages.add("Duplicate name "+petriNetName.getName()+" at alias level "+includeHierarchy.getFullyQualifiedName()); 
		}
		return messages;
	}

}
