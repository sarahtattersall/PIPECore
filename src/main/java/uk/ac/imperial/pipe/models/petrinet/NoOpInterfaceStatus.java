package uk.ac.imperial.pipe.models.petrinet;

public class NoOpInterfaceStatus extends AbstractIncludeHierarchyCommand<InterfacePlaceAction>  
  	implements InterfaceStatus, MergeInterfaceStatus {

	@Override
	public Result<InterfacePlaceAction> addTo(IncludeHierarchy includeHierarchy) {
		return result;
	}

	@Override
	public Result<InterfacePlaceAction> removeFrom(IncludeHierarchy includeHierarchy) {
		return result;
	}

	@Override
	public Result<InterfacePlaceAction> execute(
			IncludeHierarchy includeHierarchy) {
		return result;
	}

	@Override
	public Place getHomePlace() {
		return null;
	}

}
