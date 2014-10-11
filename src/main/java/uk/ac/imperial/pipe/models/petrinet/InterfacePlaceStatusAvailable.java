package uk.ac.imperial.pipe.models.petrinet;

public class InterfacePlaceStatusAvailable implements InterfacePlaceStatus {

	private IncludeHierarchy includeHierarchy;
	private InterfacePlace interfacePlace;
	private InterfacePlaceStatus nextStatus;

	public InterfacePlaceStatusAvailable(IncludeHierarchy includeHierarchy) {
		this(null, includeHierarchy); 
	}
	public InterfacePlaceStatusAvailable(InterfacePlace interfacePlace, IncludeHierarchy includeHierarchy) {
		this.interfacePlace = interfacePlace; 
		this.includeHierarchy = includeHierarchy;
		nextStatus = this; 
	}
	
	@Override
	public boolean isInUse() {
		return false;
	}

	@Override
	public boolean canUse() {
		return true;
	}
	@Override
	public boolean isHome() {
		return false;
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

	@Override
	public InterfacePlace getInterfacePlace() {
		return interfacePlace;
	}

	@Override
	public void setInterfacePlace(InterfacePlace interfacePlace) {
		this.interfacePlace = interfacePlace; 
	}

	@Override
	public IncludeHierarchy getIncludeHierarchy() {
		return includeHierarchy;
	}

	@Override
	public String buildId(String id, String homeName, String awayName) {
		if (awayName == null) awayName = ""; 
		return awayName+".."+homeName+"."+id;
	}


	@Override
	public InterfacePlaceStatus nextStatus() {
		return nextStatus;
	}

}
