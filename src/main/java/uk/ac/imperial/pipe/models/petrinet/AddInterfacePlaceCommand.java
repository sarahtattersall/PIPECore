package uk.ac.imperial.pipe.models.petrinet;


public class AddInterfacePlaceCommand<T> extends AbstractIncludeHierarchyCommand<T> {

	private Place place;
	private IncludeHierarchy home;

	//TODO enforce:  only accepts DiscretePlace to avoid bad cast in execute 
	public AddInterfacePlaceCommand(Place place, IncludeHierarchy home) {
		super(); 
		this.place = place; 
		this.home = home; ; 
	}

	@SuppressWarnings("unchecked")
	@Override
	public Result<T> execute(IncludeHierarchy includeHierarchy) {
		InterfacePlace interfacePlace = null; 
		if (includeHierarchy.equals(home)) {
			interfacePlace = new DiscreteInterfacePlace((DiscretePlace) place, InterfacePlaceStatusEnum.HOME, includeHierarchy.getUniqueName()); 
		}
		else {
			interfacePlace = new DiscreteInterfacePlace((DiscretePlace) place, InterfacePlaceStatusEnum.AVAILABLE, home.getUniqueName(), includeHierarchy.getUniqueName()); 
		}
		boolean added = includeHierarchy.addInterfacePlaceToMap(interfacePlace); 
		if (!added) {
			result.addEntry("Unable to add InterfacePlace "+interfacePlace.getId()+" to Include Hierarchy "+
					includeHierarchy.getFullyQualifiedName()+" because it already exists.", (T) place); 
		}
		return result;
	}
}
