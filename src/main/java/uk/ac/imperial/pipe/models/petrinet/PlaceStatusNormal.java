package uk.ac.imperial.pipe.models.petrinet;



public class PlaceStatusNormal implements PlaceStatus {

	private Place place;

	public PlaceStatusNormal(Place place) {
		this.place = place;
		
	}

	public Place getPlace() {
		return place;
	}

	@Override
	public MergeInterfaceStatus getMergeInterfaceStatus() {
		return new NoOpInterfaceStatus();
	}

	@Override
	public InterfaceStatus getExternalInterfaceStatus() {
		return new NoOpInterfaceStatus();
	}

	@Override
	public InterfaceStatus getInputOnlyInterfaceStatus() {
		return new NoOpInterfaceStatus();
	}

	@Override
	public InterfaceStatus getOutputOnlyInterfaceStatus() {
		return new NoOpInterfaceStatus();
	}

	public IncludeHierarchy getIncludeHierarchy() {
		return null;
	}
	@Override
	public Result<InterfacePlaceAction> setMergeStatus(boolean merge) {
		throw new UnsupportedOperationException(buildUnsupportedMessage("setMergeStatus")); 
	}
	@Override
	public Result<InterfacePlaceAction> setExternalStatus(boolean external) {
		throw new UnsupportedOperationException(buildUnsupportedMessage("setExternalStatus")); 
	}

	@Override
	public Result<InterfacePlaceAction> setInputOnlyStatus(boolean inputOnly) {
		throw new UnsupportedOperationException(buildUnsupportedMessage("setInputOnlyStatus")); 
	}

	@Override
	public Result<InterfacePlaceAction> setOutputOnlyStatus(boolean outputOnly) {
		throw new UnsupportedOperationException(buildUnsupportedMessage("setOutputOnlyStatus")); 
	}
	private String buildUnsupportedMessage(String method) {
		return "PlaceStatusNormal:  "+method+
				" not a valid request for place "+place.getId()+
				" until Place.setInInterface(true) has been requested";
	}

	public boolean isOutputOnlyStatus() {
		return false;
	}

	public boolean isInputOnlyStatus() {
		return false;
	}

	public boolean isExternalStatus() {
		return false;
	}

	public boolean isMergeStatus() {
		return false;
	}

	public PlaceStatus copyStatus(Place place) {
		return new PlaceStatusNormal(place);
	}


}
