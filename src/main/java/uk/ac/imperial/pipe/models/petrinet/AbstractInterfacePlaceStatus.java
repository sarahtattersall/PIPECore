package uk.ac.imperial.pipe.models.petrinet;

public abstract class AbstractInterfacePlaceStatus implements InterfacePlaceStatus {

	protected IncludeHierarchy includeHierarchy;
	protected InterfacePlace interfacePlace;
	protected InterfacePlaceStatus nextStatus;

	public AbstractInterfacePlaceStatus(IncludeHierarchy includeHierarchy) {
		this(null, includeHierarchy); 
	}
	public AbstractInterfacePlaceStatus(InterfacePlace interfacePlace, IncludeHierarchy includeHierarchy) {
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
		return false;
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
