package uk.ac.imperial.pipe.models.petrinet;

public class ExternalInterfaceStatus implements InterfaceStatus {


	public ExternalInterfaceStatus() {
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
