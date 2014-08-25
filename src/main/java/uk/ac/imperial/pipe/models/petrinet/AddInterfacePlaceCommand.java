package uk.ac.imperial.pipe.models.petrinet;


public class AddInterfacePlaceCommand<T> extends
		AbstractIncludeHierarchyCommand<T> {

	private Place place;
	private InterfacePlaceStatusEnum status;

	public AddInterfacePlaceCommand(Place place, InterfacePlaceStatusEnum status) {
		super(); 
		this.place = place; 
		this.status = status; 
	}


	@Override
	public Result<T> execute(IncludeHierarchy includeHierarchy) {
		InterfacePlace interfacePlace = place.buildInterfacePlace(); 
		interfacePlace.setStatus(status.buildStatus()); 
		interfacePlace.setFullyQualifiedName(includeHierarchy.getFullyQualifiedName()); 
		boolean added = includeHierarchy.addInterfacePlaceToMap(interfacePlace); 
		if (!added) {
			result.addMessage("Unable to add InterfacePlace "+interfacePlace.getId()+" to Include Hierarchy "+
					includeHierarchy.getFullyQualifiedName()+" because it already exists."); 
		}
		return result;
	}

}
