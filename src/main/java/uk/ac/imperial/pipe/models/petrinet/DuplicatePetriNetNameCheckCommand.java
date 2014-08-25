package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.models.petrinet.name.PetriNetName;

public class DuplicatePetriNetNameCheckCommand<T> extends
		AbstractIncludeHierarchyCommand<T> {

	private PetriNetName petriNetName;

	public DuplicatePetriNetNameCheckCommand(PetriNetName petriNetName) {
		super(); 
		this.petriNetName = petriNetName; 
	}


	@Override
	public Result<T> execute(IncludeHierarchy includeHierarchy) {
		if (includeHierarchy.getPetriNet().getName().equals(petriNetName)) {
			result.addMessage("Duplicate name "+petriNetName.getName()+" at alias level "+includeHierarchy.getFullyQualifiedName()); 
		}
		return result;
	}

}
