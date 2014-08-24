package uk.ac.imperial.pipe.models.petrinet;

import java.util.List;

import uk.ac.imperial.pipe.models.petrinet.name.PetriNetName;

public class AddInterfacePlaceCommand extends
		AbstractIncludeHierarchyCommand {

	private Place place;
	private InterfacePlaceStatusEnum status;

	public AddInterfacePlaceCommand(Place place, InterfacePlaceStatusEnum status) {
		super(); 
		this.place = place; 
		this.status = status; 
	}

	@Override
	public List<String> execute(IncludeHierarchy includeHierarchy) {
		InterfacePlace interfacePlace = place.buildInterfacePlace(); 
		interfacePlace.setStatus(status.buildStatus()); 
		interfacePlace.setFullyQualifiedName(includeHierarchy.getFullyQualifiedName()); 
		boolean added = includeHierarchy.addInterfacePlaceToMap(interfacePlace); 
		if (!added) {
			messages.add("Unable to add InterfacePlace "+interfacePlace.getId()+" to Include Hierarchy "+
		includeHierarchy.getFullyQualifiedName()+" because it already exists."); 
		}
		return messages;
	}

}
