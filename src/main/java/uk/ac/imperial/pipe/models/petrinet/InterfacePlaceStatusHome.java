package uk.ac.imperial.pipe.models.petrinet;

public class InterfacePlaceStatusHome implements InterfacePlaceStatus {

	private static final String CANT_USE_HOME_INTERFACE_PLACE = "InterfacePlaceStatusHome: interface place cannot be used in the petri net that is the home of its underlying place.";
	private IncludeHierarchy includeHierarchy;
	private InterfacePlace interfacePlace;
	private InterfacePlaceStatus nextStatus;

	public InterfacePlaceStatusHome(IncludeHierarchy includeHierarchy) {
		this(null, includeHierarchy); 
	}
	public InterfacePlaceStatusHome(InterfacePlace interfacePlace, IncludeHierarchy includeHierarchy) {
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
		return false;
	}
	@Override
	public boolean isHome() {
		return true;
	}
	
	@Override
	public Result<InterfacePlaceAction> use() {
		return null;
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
		getInterfacePlace().getPlace().setInterfacePlace(interfacePlace); 
	}

	@Override
	public IncludeHierarchy getIncludeHierarchy() {
		return includeHierarchy;
	}

	@Override
	public String buildId(String id, String homeName, String awayName) {
		return homeName+"."+id;
	}

	@Override
	public InterfacePlaceStatus nextStatus() {
		return nextStatus;
	}

}
