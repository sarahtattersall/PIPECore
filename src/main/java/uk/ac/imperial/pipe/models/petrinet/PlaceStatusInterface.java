package uk.ac.imperial.pipe.models.petrinet;


public class PlaceStatusInterface implements PlaceStatus {

	private static final String STATUS_MAY_NOT_BE_BOTH_INPUT_ONLY_AND_OUTPUT_ONLY = "status may not be both input only and output only.";
	private static final String SET_INPUT_ONLY_STATUS = "setInputOnlyStatus: ";
	private static final String SET_OUTPUT_ONLY_STATUS = "setOutputOnlyStatus: ";
	private static final String PLACE_STATUS = "PlaceStatus.";
	private Place place;
	private boolean merge;
	private boolean external;
	private boolean inputOnly;
	private boolean outputOnly;
	private MergeInterfaceStatus mergeStatus;
	private InterfaceStatus externalStatus;
	private InterfaceStatus inputOnlyStatus;
	private InterfaceStatus outputOnlyStatus;
	private IncludeHierarchy includeHierarchy;
	private boolean mergeChanged;
	private boolean externalChanged;
	private boolean inputOnlyChanged;
	private boolean outputOnlyChanged;
	private Result<InterfacePlaceAction> result;

	public PlaceStatusInterface(Place place) {
		this(place, null); //TODO delete 
	}

	public PlaceStatusInterface(PlaceStatus placeStatus, Place place) {
		this.place = place; 
		this.includeHierarchy = placeStatus.getIncludeHierarchy(); 
		setMergeStatus(placeStatus.isMergeStatus());
		setExternalStatus(placeStatus.isExternalStatus());
		setInputOnlyStatus(placeStatus.isInputOnlyStatus());
		setOutputOnlyStatus(placeStatus.isOutputOnlyStatus());
		setMergeInterfaceStatus(placeStatus.getMergeInterfaceStatus()); 
		setExternalInterfaceStatus(placeStatus.getExternalInterfaceStatus()); 
		setInputOnlyInterfaceStatus(placeStatus.getInputOnlyInterfaceStatus()); 
		setOutputOnlyInterfaceStatus(placeStatus.getOutputOnlyInterfaceStatus()); 
		resetUpdate(); 
	}

	public PlaceStatusInterface(Place place, IncludeHierarchy includeHierarchy) {
		this.place = place; 
		this.includeHierarchy = includeHierarchy; 
		setMergeStatus(false); 
		setExternalStatus(false); 
		setInputOnlyStatus(false); 
		setOutputOnlyStatus(false); 
		update(); 
	}
	private void resetUpdate() {
		mergeChanged = false; 
		externalChanged = false; 
		inputOnlyChanged = false; 
		outputOnlyChanged = false; 
		result = new Result<>(); 
	}

	@Override
	public Place getPlace() {
		return place;
	}

	@Override
	public MergeInterfaceStatus getMergeInterfaceStatus() {
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
	public Result<InterfacePlaceAction> update() {
		if (mergeChanged) buildMergeStatus(); 
		if (externalChanged) buildExternalStatus(); 
		if (inputOnlyChanged) buildInputOnlyStatus(); 
		if (outputOnlyChanged) buildOutputOnlyStatus(); 
		Result<InterfacePlaceAction> tempResult = result; 
		resetUpdate(); 
		return tempResult;
	}

	@Override
	public void setMergeStatus(boolean merge) {
		this.merge = merge; 
		this.mergeChanged = true; 
	}
	@Override
	public void setExternalStatus(boolean external) {
		this.external = external;
		this.externalChanged = true; 
	}
	@Override
	public void setInputOnlyStatus(boolean inputOnly) {
		this.inputOnly = inputOnly;
		this.inputOnlyChanged = true; 
	}
	@Override
	public void setOutputOnlyStatus(boolean outputOnly) {
		this.outputOnly = outputOnly; 
		this.outputOnlyChanged = true; 
	}

	protected Result<InterfacePlaceAction> buildMergeStatus() {
		if (merge) {
			mergeStatus = new MergeInterfaceStatusHome(place, this); 
			result = mergeStatus.add(includeHierarchy); 
		}
		else {
			mergeStatus = new NoOpInterfaceStatus();
		}
		return result;
	}


	protected Result<InterfacePlaceAction> buildExternalStatus() {
		if (external) {
			externalStatus = new ExternalInterfaceStatus(); 
		}
		else {
			externalStatus = new NoOpInterfaceStatus(); 
		}
		return result;
	}


	protected Result<InterfacePlaceAction> buildInputOnlyStatus() {
		if (inputOnly) {
			if (outputOnly) {
				result.addMessage(PLACE_STATUS+SET_INPUT_ONLY_STATUS+STATUS_MAY_NOT_BE_BOTH_INPUT_ONLY_AND_OUTPUT_ONLY); 
				this.inputOnly = false; 
			}
			else {
//				this.inputOnly = inputOnly;
				inputOnlyStatus = new InputOnlyInterfaceStatus(); 
			}
		}
		else {
//			this.inputOnly = inputOnly;
			inputOnlyStatus = new NoOpInterfaceStatus();
		}
		return result;
	}


	protected Result<InterfacePlaceAction> buildOutputOnlyStatus() {
		if (outputOnly) {
			if (inputOnly) {
				result.addMessage(PLACE_STATUS+SET_OUTPUT_ONLY_STATUS+STATUS_MAY_NOT_BE_BOTH_INPUT_ONLY_AND_OUTPUT_ONLY); 
				this.outputOnly = false; 
			} 
			else {
//				this.outputOnly = outputOnly; 
				outputOnlyStatus = new OutputOnlyInterfaceStatus();
			}
		}
		else {
//			this.outputOnly = outputOnly; 
			outputOnlyStatus = new NoOpInterfaceStatus();
		}
		return result;
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
	public PlaceStatus copyStatus(Place place) {
		return new PlaceStatusInterface(this, place);
	}

	@Override
	public void setMergeInterfaceStatus(MergeInterfaceStatus interfaceStatus) {
		this.mergeStatus = interfaceStatus; 
	}
	@Override
	public void setOutputOnlyInterfaceStatus(InterfaceStatus outputOnlyInterfaceStatus) {
		this.outputOnlyStatus = outputOnlyInterfaceStatus; 
	}

	@Override
	public void setInputOnlyInterfaceStatus(InterfaceStatus inputOnlyInterfaceStatus) {
		this.inputOnlyStatus = inputOnlyInterfaceStatus; 
	}

	@Override
	public void setExternalInterfaceStatus(InterfaceStatus externalInterfaceStatus) {
		this.externalStatus = externalInterfaceStatus; 
	}

	@Override
	public IncludeHierarchy getIncludeHierarchy() {
		return includeHierarchy;
	}



}
