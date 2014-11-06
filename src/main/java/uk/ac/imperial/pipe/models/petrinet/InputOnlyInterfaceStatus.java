package uk.ac.imperial.pipe.models.petrinet;

public class InputOnlyInterfaceStatus implements InterfaceStatus {

	public InputOnlyInterfaceStatus() {
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

}
