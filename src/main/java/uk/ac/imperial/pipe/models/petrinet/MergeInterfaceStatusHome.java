package uk.ac.imperial.pipe.models.petrinet;

public class MergeInterfaceStatusHome implements MergeInterfaceStatus {


	private Place homePlace;

	public MergeInterfaceStatusHome(Place place) {
		homePlace = place; 
	}


	@Override
	public Place getHomePlace() {
		return homePlace;
	}


	@Override
	public Result<InterfacePlaceAction> addTo(IncludeHierarchy includeHierarchy) {
		//TODO AddPlaceStatusCommand (w merge), return results
		return null;
	}


	@Override
	public Result<InterfacePlaceAction> removeFrom(
			IncludeHierarchy includeHierarchy) {
		return null;
	}

}
