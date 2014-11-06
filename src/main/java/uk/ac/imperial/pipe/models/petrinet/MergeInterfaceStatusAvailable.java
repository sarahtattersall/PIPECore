package uk.ac.imperial.pipe.models.petrinet;


public class MergeInterfaceStatusAvailable extends AbstractMergeInterfaceStatus implements MergeInterfaceStatus {


	public MergeInterfaceStatusAvailable(Place homePlace, PlaceStatus placeStatus, String awayId) {
		super(homePlace, placeStatus, awayId);
		
	}

	@Override
	public Result<InterfacePlaceAction> add(IncludeHierarchy includeHierarchy) {
		return null;
	}

	@Override
	public Result<InterfacePlaceAction> remove(IncludeHierarchy includeHierarchy) {
		return null;
	}

	@Override
	public Result<InterfacePlaceAction> add(PetriNet petriNet) {
		Result<InterfacePlaceAction> result = new Result<>();  
		petriNet.addPlace(placeStatus.getPlace()); 
		MergeInterfaceStatus mergeStatus = new MergeInterfaceStatusAway(homePlace, placeStatus, awayId);  
        placeStatus.setMergeInterfaceStatus(mergeStatus); 
	 
		return result;
	}



}
