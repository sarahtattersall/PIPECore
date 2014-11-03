package uk.ac.imperial.pipe.models.petrinet;

public class NoOpInterfaceStatus extends AbstractIncludeHierarchyCommand<InterfacePlaceAction>  implements InterfaceStatus {

	@Override
	public Result<InterfacePlaceAction> add() {
		return result;
	}

	@Override
	public Result<InterfacePlaceAction> remove() {
		return result;
	}

	@Override
	public Result<InterfacePlaceAction> execute(
			IncludeHierarchy includeHierarchy) {
		return result;
	}

}
