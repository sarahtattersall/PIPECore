package uk.ac.imperial.pipe.models.petrinet;



public class MergeInterfaceStatusAvailable extends AbstractMergeInterfaceStatus implements MergeInterfaceStatus {


	public MergeInterfaceStatusAvailable(Place homePlace, PlaceStatus placeStatus, String awayId) {
		super(homePlace, placeStatus, awayId);
		
	}

	@Override
	public Result<InterfacePlaceAction> add(IncludeHierarchy includeHierarchy) {
		return new Result<InterfacePlaceAction>();
	}
	@Override
	public Result<InterfacePlaceAction> remove(IncludeHierarchy includeHierarchy) {
		return buildNotSupportedResult("remove", "Available");
	}
	//TODO refactor...
	@Override
	public Result<InterfacePlaceAction> add(PetriNet petriNet) {
		Result<InterfacePlaceAction> result = new Result<>();  
		petriNet.addPlace(placeStatus.getPlace()); 
		MergeInterfaceStatus mergeStatus = new MergeInterfaceStatusAway(homePlace, placeStatus, awayId);  
        placeStatus.setMergeInterfaceStatus(mergeStatus); 
//		placeStatus.setExternal(false); 
		if (homePlace.getStatus().isInputOnlyArcConstraint()) { 
			placeStatus.setInputOnlyArcConstraint(true); 
			((PlaceStatusInterface) placeStatus).buildInputOnlyArcConstraint();  
		}
		else if (homePlace.getStatus().isOutputOnlyArcConstraint()) {
			placeStatus.setOutputOnlyArcConstraint(true); 
			((PlaceStatusInterface) placeStatus).buildOutputOnlyArcConstraint();  
		}

	 
		return result;
	}

	@Override
	public boolean canRemove() {
		return false;
	}

	@Override
	public String getXmlType() {
		return null;
	}



}
