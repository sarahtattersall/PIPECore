package uk.ac.imperial.pipe.models.petrinet;

public class InterfacePlaceStatusHome implements InterfacePlaceStatus {

	private static final String CANT_USE_HOME_INTERFACE_PLACE = "InterfacePlaceStatusHome: interface place cannot be used in the petri net that is the home of its underlying place.";
	private IncludeHierarchy includeHierarchy;
	private InterfacePlace interfacePlace;

	public InterfacePlaceStatusHome(IncludeHierarchy includeHierarchy) {
		this.includeHierarchy = includeHierarchy;
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
	public InterfacePlaceStatus use() {
		throw new IllegalStateException(CANT_USE_HOME_INTERFACE_PLACE);
	}

	@Override
	public InterfacePlaceStatus remove() {
		return this;
	}


	@Override
	public Result<InterfacePlaceAction> use1() {
		return null;
	}

	@Override
	public Result<InterfacePlaceAction> remove1() {
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
		return homeName+"."+id;
	}

}
