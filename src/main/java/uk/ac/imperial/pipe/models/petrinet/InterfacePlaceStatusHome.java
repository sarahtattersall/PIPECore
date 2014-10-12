package uk.ac.imperial.pipe.models.petrinet;

public class InterfacePlaceStatusHome extends AbstractInterfacePlaceStatus  implements InterfacePlaceStatus {

	public InterfacePlaceStatusHome(IncludeHierarchy includeHierarchy) {
		super(includeHierarchy);
	}
	public InterfacePlaceStatusHome(InterfacePlace interfacePlace, IncludeHierarchy includeHierarchy) {
		super(interfacePlace, includeHierarchy);
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
	public void setInterfacePlace(InterfacePlace interfacePlace) {
		super.setInterfacePlace(interfacePlace); 
		getInterfacePlace().getPlace().setInterfacePlace(interfacePlace); 
	}
	
	@Override
	public String buildId(String id, String homeName, String awayName) {
		return homeName+"."+id;
	}

}
