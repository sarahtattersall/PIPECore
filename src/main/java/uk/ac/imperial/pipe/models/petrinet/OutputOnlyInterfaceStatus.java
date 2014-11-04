package uk.ac.imperial.pipe.models.petrinet;

public class OutputOnlyInterfaceStatus implements InterfaceStatus {


	public OutputOnlyInterfaceStatus() {
	}


	@Override
	public Result<InterfacePlaceAction> addTo(IncludeHierarchy includeHierarchy) {
		return null;
	}

	@Override
	public Result<InterfacePlaceAction> removeFrom(
			IncludeHierarchy includeHierarchy) {
		return null;
	}

}
