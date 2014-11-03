package uk.ac.imperial.pipe.models.petrinet;

public class PlaceStatusInterface implements PlaceStatus {

	private Place place;
	private IncludeHierarchy includes;
	private boolean merge;
	private boolean external;
	private boolean inputOnly;
	private boolean outputOnly;
	private InterfaceStatus mergeStatus;
	private InterfaceStatus externalStatus;
	private InterfaceStatus inputOnlyStatus;
	private InterfaceStatus outputOnlyStatus;

	public PlaceStatusInterface(Place place, IncludeHierarchy includes) {
		this.place = place; 
		this.includes = includes; 
		setMergeStatus(false); 
		setExternalStatus(false); 
		setInputOnlyStatus(false); 
		setOutputOnlyStatus(false); 
	}

	public PlaceStatusInterface(PlaceStatus placeStatus, Place newPlace) {
		this.place = newPlace; 
		this.includes = placeStatus.getIncludeHierarchy(); 
		setMergeStatus(placeStatus.isMergeStatus());
		setExternalStatus(placeStatus.isExternalStatus());
		setInputOnlyStatus(placeStatus.isInputOnlyStatus());
		setOutputOnlyStatus(placeStatus.isOutputOnlyStatus());
	}

	@Override
	public Place getPlace() {
		return place;
	}

	@Override
	public InterfaceStatus getMergeInterfaceStatus() {
		return mergeStatus;  
	}

	@Override
	public InterfaceStatus getExternalInterfaceStatus() {
		return externalStatus;
	}

	@Override
	public InterfaceStatus getInputOnlyInterfaceStatus() {
		return inputOnlyStatus;
	}

	@Override
	public InterfaceStatus getOutputOnlyInterfaceStatus() {
		return outputOnlyStatus;
	}

	@Override
	public IncludeHierarchy getIncludeHierarchy() {
		return includes;
	}

	@Override
	public void setMergeStatus(boolean merge) {
		this.merge = merge; 
		if (merge) {
			mergeStatus = new MergeInterfaceStatus(includes); 
		}
		else {
			mergeStatus = new NoOpInterfaceStatus();
		}
	}

	@Override
	public void setExternalStatus(boolean external) {
		this.external = external;
		if (external) {
			externalStatus = new ExternalInterfaceStatus(includes); 
		}
		else {
			externalStatus = new NoOpInterfaceStatus(); 
		}
	}

	@Override
	public void setInputOnlyStatus(boolean inputOnly) {
		this.inputOnly = inputOnly;
		if (inputOnly) {
			inputOnlyStatus = new InputOnlyInterfaceStatus(includes); 
		}
		else {
			inputOnlyStatus = new NoOpInterfaceStatus();
		}
	}

	@Override
	public void setOutputOnlyStatus(boolean outputOnly) {
		this.outputOnly = outputOnly; 
		if (outputOnly) {
			outputOnlyStatus = new OutputOnlyInterfaceStatus(includes);
		}
		else {
			outputOnlyStatus = new NoOpInterfaceStatus();
		}
	}

	@Override
	public boolean isMergeStatus() {
		return merge;
	}

	@Override
	public boolean isExternalStatus() {
		return external;
	}

	@Override
	public boolean isInputOnlyStatus() {
		return inputOnly;
	}

	@Override
	public boolean isOutputOnlyStatus() {
		return outputOnly;
	}

	@Override
	public PlaceStatus copy(Place place) {
		return new PlaceStatusInterface(this, place);
	}

}
