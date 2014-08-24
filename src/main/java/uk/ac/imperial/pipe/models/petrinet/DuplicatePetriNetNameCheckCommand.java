package uk.ac.imperial.pipe.models.petrinet;

import java.util.List;

import uk.ac.imperial.pipe.models.petrinet.name.PetriNetName;

public class DuplicatePetriNetNameCheckCommand extends
		AbstractIncludeHierarchyCommand {

	private PetriNetName petriNetName;

	public DuplicatePetriNetNameCheckCommand(PetriNetName petriNetName) {
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
