package uk.ac.imperial.pipe.models.petrinet;

public class InterfacePlaceStatusAvailable extends AbstractInterfacePlaceStatus implements InterfacePlaceStatus {

	public InterfacePlaceStatusAvailable(IncludeHierarchy includeHierarchy) {
		super(includeHierarchy); 
	}
	public InterfacePlaceStatusAvailable(InterfacePlace interfacePlace, IncludeHierarchy includeHierarchy) {
		super(interfacePlace, includeHierarchy); 
	}
	
	@Override
	public boolean canUse() {
		return true;
	}

	@Override
	public Result<InterfacePlaceAction> use() {
		includeHierarchy.getPetriNet().addPlace(interfacePlace); 
		nextStatus = new InterfacePlaceStatusInUse(interfacePlace,includeHierarchy);
		return new Result<InterfacePlaceAction>();
	}

	@Override
	public Result<InterfacePlaceAction> remove() {
		return null;
	}


}
