package uk.ac.imperial.pipe.models.petrinet;

public class InterfacePlaceStatusAvailable implements InterfacePlaceStatus {

	private IncludeHierarchy includeHierarchy;
	private InterfacePlace interfacePlace;

	public InterfacePlaceStatusAvailable(IncludeHierarchy includeHierarchy) {
		this.includeHierarchy = includeHierarchy;
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
	public InterfacePlaceStatus use() {
		return InterfacePlaceStatusEnum.IN_USE.buildStatus(includeHierarchy); 
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
		if (awayName == null) awayName = ""; 
		return awayName+".."+homeName+"."+id;
	}

}
