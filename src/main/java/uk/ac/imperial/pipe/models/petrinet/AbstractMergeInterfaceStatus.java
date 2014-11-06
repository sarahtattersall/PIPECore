package uk.ac.imperial.pipe.models.petrinet;

public class AbstractMergeInterfaceStatus implements MergeInterfaceStatus {

	protected Place homePlace;
	protected PlaceStatus placeStatus;
	protected String awayId;

	public AbstractMergeInterfaceStatus(Place homePlace, PlaceStatus placeStatus) {
		this.homePlace = homePlace; 
		this.placeStatus = placeStatus; 
	}

	public AbstractMergeInterfaceStatus(Place homePlace,
			PlaceStatus placeStatus, String awayId) {
		this(homePlace, placeStatus); 
		this.awayId = awayId; 
	}


	@Override
	public Place getHomePlace() {
		return homePlace;
	}

	protected void buildAwayId(String uniqueNameAsPrefix) {
		this.awayId = uniqueNameAsPrefix+homePlace.getId(); 
	}

	@Override
	public String getAwayId() {
		return awayId;
	}

	@Override
	public Result<InterfacePlaceAction> add(PetriNet petriNet) {
		Result<InterfacePlaceAction> result = new Result<>(); 
		result.addMessage("place "+placeStatus.getPlace().getId()+" cannot be added to Petri net "+petriNet.getNameValue()+" because it is already present.");
		return result;
	}


	@Override
	public Result<InterfacePlaceAction> add(IncludeHierarchy includeHierarchy) {
		return null;
	}


	@Override
	public Result<InterfacePlaceAction> remove(IncludeHierarchy includeHierarchy) {
		return null;
	}

}
