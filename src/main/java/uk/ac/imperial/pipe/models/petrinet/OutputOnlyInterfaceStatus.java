package uk.ac.imperial.pipe.models.petrinet;

public class OutputOnlyInterfaceStatus implements InterfaceStatus {


	public OutputOnlyInterfaceStatus() {
	}


	@Override
	public Result<InterfacePlaceAction> add(IncludeHierarchy includeHierarchy) {
		return null;
	}

	@Override
	public Result<InterfacePlaceAction> remove(
			IncludeHierarchy includeHierarchy) {
		return null;
	}


	public InterfaceStatus copy() {
		return null;
	}

}
