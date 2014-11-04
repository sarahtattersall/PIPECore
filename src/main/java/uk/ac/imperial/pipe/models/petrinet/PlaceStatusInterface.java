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

	public PlaceStatusInterface(Place place) {
		this(place, null); //TODO delete 
	}

	public PlaceStatusInterface(PlaceStatus placeStatus, Place newPlace) {
		this.place = newPlace; 
		setMergeStatus(placeStatus.isMergeStatus());
		setExternalStatus(placeStatus.isExternalStatus());
		setInputOnlyStatus(placeStatus.isInputOnlyStatus());
		setOutputOnlyStatus(placeStatus.isOutputOnlyStatus());
	}

	public PlaceStatusInterface(Place place, IncludeHierarchy includeHierarchy) {
		this.place = place; 
		this.includeHierarchy = includeHierarchy; 
		setMergeStatus(false); 
		setExternalStatus(false); 
		setInputOnlyStatus(false); 
		setOutputOnlyStatus(false); 
		
	}

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
	public Result<InterfacePlaceAction> setMergeStatus(boolean merge) {
		this.merge = merge; 
		Result<InterfacePlaceAction> result = new Result<>(); 
		if (merge) {
			mergeStatus = new MergeInterfaceStatusHome(place); 
			result = mergeStatus.addTo(includeHierarchy); 
		}
		else {
			mergeStatus = new NoOpInterfaceStatus();
		}
		return result;
	}

	@Override
	public Result<InterfacePlaceAction> setExternalStatus(boolean external) {
		this.external = external;
		if (external) {
			externalStatus = new ExternalInterfaceStatus(); 
		}
		else {
			externalStatus = new NoOpInterfaceStatus(); 
		}
		return new Result<InterfacePlaceAction>();
	}

	@Override
	public Result<InterfacePlaceAction> setInputOnlyStatus(boolean inputOnly) {
		Result<InterfacePlaceAction> result = new Result<InterfacePlaceAction>();
		if (inputOnly) {
			if (outputOnly) {
				result.addMessage(PLACE_STATUS+SET_INPUT_ONLY_STATUS+STATUS_MAY_NOT_BE_BOTH_INPUT_ONLY_AND_OUTPUT_ONLY); 
			}
			else {
				this.inputOnly = inputOnly;
				inputOnlyStatus = new InputOnlyInterfaceStatus(); 
			}
		}
		else {
			this.inputOnly = inputOnly;
			inputOnlyStatus = new NoOpInterfaceStatus();
		}
		return result;
	}

	@Override
	public Result<InterfacePlaceAction> setOutputOnlyStatus(boolean outputOnly) {
		Result<InterfacePlaceAction> result = new Result<InterfacePlaceAction>();
		if (outputOnly) {
			if (inputOnly) {
				result.addMessage(PLACE_STATUS+SET_OUTPUT_ONLY_STATUS+STATUS_MAY_NOT_BE_BOTH_INPUT_ONLY_AND_OUTPUT_ONLY); 
			} 
			else {
				this.outputOnly = outputOnly; 
				outputOnlyStatus = new OutputOnlyInterfaceStatus();
			}
		}
		else {
			this.outputOnly = outputOnly; 
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

	protected void setStatusForTesting(InterfaceStatus interfaceStatus) {
		if (interfaceStatus instanceof MergeInterfaceStatus) mergeStatus = (MergeInterfaceStatus) interfaceStatus;  
	}


}
