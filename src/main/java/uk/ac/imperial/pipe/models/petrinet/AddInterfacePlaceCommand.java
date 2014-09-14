package uk.ac.imperial.pipe.models.petrinet;


public class AddInterfacePlaceCommand<T> extends AbstractIncludeHierarchyCommand<T> {

	private Place place;
	private InterfacePlaceStatusEnum status;

	public AddInterfacePlaceCommand(Place place, InterfacePlaceStatusEnum status) {
		super(); 
		this.place = place; 
		this.status = status; 
	}

	@SuppressWarnings("unchecked")
	@Override
	public Result<T> execute(IncludeHierarchy includeHierarchy) {
		InterfacePlace interfacePlace = place.buildInterfacePlace(); 
		interfacePlace.setStatus(status.buildStatus()); 
		interfacePlace.setFullyQualifiedName(includeHierarchy.getFullyQualifiedName()); 
		boolean added = includeHierarchy.addInterfacePlaceToMap(interfacePlace); 
		if (!added) {
			result.addEntry("Unable to add InterfacePlace "+interfacePlace.getId()+" to Include Hierarchy "+
					includeHierarchy.getFullyQualifiedName()+" because it already exists.", (T) place); 
		}
		return result;
	}
}
