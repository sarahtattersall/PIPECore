package uk.ac.imperial.pipe.models.petrinet;

public class MergeInterfaceStatusAway extends AbstractMergeInterfaceStatus implements MergeInterfaceStatus {

	public MergeInterfaceStatusAway(Place homePlace, PlaceStatus placeStatus, String awayId) {
		super(homePlace, placeStatus, awayId);
	}

	@Override
	public Result<InterfacePlaceAction> add(IncludeHierarchy includeHierarchy) {
		return null;
	}

	@Override
	public Result<InterfacePlaceAction> remove(IncludeHierarchy includeHierarchy) {
		return buildNotSupportedResult("remove", "Away");
	}

	@Override
	public boolean canRemove() {
		return false;
	}


}
