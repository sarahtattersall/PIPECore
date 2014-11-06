package uk.ac.imperial.pipe.models.petrinet;



public class PlaceStatusNormal implements PlaceStatus {

	private Place place;

	public PlaceStatusNormal(Place place) {
		this.place = place;
		
	}

	@Override
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
	public void setMergeStatus(boolean merge) {
		throw new UnsupportedOperationException(buildUnsupportedMessage("setMergeStatus")); 
	}
	@Override
	public void setExternalStatus(boolean external) {
		throw new UnsupportedOperationException(buildUnsupportedMessage("setExternalStatus")); 
	}

	@Override
	public void setInputOnlyStatus(boolean inputOnly) {
		throw new UnsupportedOperationException(buildUnsupportedMessage("setInputOnlyStatus")); 
	}

	@Override
	public void setOutputOnlyStatus(boolean outputOnly) {
		throw new UnsupportedOperationException(buildUnsupportedMessage("setOutputOnlyStatus")); 
	}
	private String buildUnsupportedMessage(String method) {
		return "PlaceStatusNormal:  "+method+
				" not a valid request for place "+place.getId()+
				" until Place.addToInterface(IncludeHierarchy) has been requested";
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

	public void setMergeInterfaceStatus(MergeInterfaceStatus interfaceStatus) {
	}

	@Override
	public Result<InterfacePlaceAction> update() {
		return null;
	}

	public void setExternalInterfaceStatus(InterfaceStatus externalInterfaceStatus) {
	}

	public void setInputOnlyInterfaceStatus(InterfaceStatus inputOnlyInterfaceStatus) {
	}

	public void setOutputOnlyInterfaceStatus(InterfaceStatus outputOnlyInterfaceStatus) {
	}


}
