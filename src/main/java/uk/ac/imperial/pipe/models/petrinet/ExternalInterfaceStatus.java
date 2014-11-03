package uk.ac.imperial.pipe.models.petrinet;

public class ExternalInterfaceStatus implements InterfaceStatus {

	private IncludeHierarchy includes;

	public ExternalInterfaceStatus(IncludeHierarchy includes) {
		this.includes = includes; 
	}

	@Override
	public Result<InterfacePlaceAction> add() {
		return null;
	}

	@Override
	public Result<InterfacePlaceAction> remove() {
		return null;
	}

}
