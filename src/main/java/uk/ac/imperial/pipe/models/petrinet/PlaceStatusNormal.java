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
	public InterfaceStatus getMergeInterfaceStatus() {
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

	@Override
	public IncludeHierarchy getIncludeHierarchy() {
		return null;
	}
	//TODO set...status should throw unsupported operation exception
	@Override
	public void setMergeStatus(boolean merge) {
	}

	@Override
	public void setExternalStatus(boolean external) {
	}

	@Override
	public void setInputOnlyStatus(boolean inputOnly) {
	}

	@Override
	public void setOutputOnlyStatus(boolean outputOnly) {
		
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

	@Override
	public PlaceStatus copy(Place place) {
		return new PlaceStatusNormal(place);
	}

}
