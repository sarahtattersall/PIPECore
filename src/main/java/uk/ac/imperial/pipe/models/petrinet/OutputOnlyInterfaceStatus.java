package uk.ac.imperial.pipe.models.petrinet;

public class OutputOnlyInterfaceStatus implements InterfaceStatus {

	private IncludeHierarchy includes;

	public OutputOnlyInterfaceStatus(IncludeHierarchy includes) {
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
