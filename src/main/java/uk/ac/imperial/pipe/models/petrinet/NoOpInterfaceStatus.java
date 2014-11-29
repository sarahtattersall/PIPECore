package uk.ac.imperial.pipe.models.petrinet;

public class NoOpInterfaceStatus extends AbstractIncludeHierarchyCommand<InterfacePlaceAction>  
  	implements InterfaceStatus, MergeInterfaceStatus {

	@Override
	public Result<InterfacePlaceAction> add(IncludeHierarchy includeHierarchy) {
		return result;
	}

	@Override
	public Result<InterfacePlaceAction> remove(IncludeHierarchy includeHierarchy) {
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

	@Override
	public String getAwayId() {
		return null;
	}

	@Override
	public Result<InterfacePlaceAction> add(PetriNet petriNet) {
		return null;
	}

	@Override
	public boolean canRemove() {
		return false;
	}

	@Override
	public void setHomePlace(Place homePlace) {
	}

	@Override
	public String getXmlType() {
		return null;
	}

	public final void setAwayId(String awayId) {
	}


}
