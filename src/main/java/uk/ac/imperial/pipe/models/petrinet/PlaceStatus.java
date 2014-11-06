package uk.ac.imperial.pipe.models.petrinet;


public interface PlaceStatus {

	public MergeInterfaceStatus getMergeInterfaceStatus();

	public InterfaceStatus getExternalInterfaceStatus();

	public InterfaceStatus getInputOnlyInterfaceStatus();

	public InterfaceStatus getOutputOnlyInterfaceStatus();

	public void setMergeStatus(boolean merge);

	public void setExternalStatus(boolean external);

	public void setInputOnlyStatus(boolean inputOnly);

	public void setOutputOnlyStatus(boolean outputOnly);

	public boolean isMergeStatus();

	public boolean isExternalStatus();

	public boolean isInputOnlyStatus();

	public boolean isOutputOnlyStatus();

	public  PlaceStatus copyStatus(Place place);

	//TODO consider dropping this and changing to protected
	public void setMergeInterfaceStatus(MergeInterfaceStatus interfaceStatus);

	public IncludeHierarchy getIncludeHierarchy();

	public Result<InterfacePlaceAction> update();

	public abstract void setExternalInterfaceStatus(InterfaceStatus externalInterfaceStatus);

	public abstract void setInputOnlyInterfaceStatus(InterfaceStatus inputOnlyInterfaceStatus);

	public abstract void setOutputOnlyInterfaceStatus(InterfaceStatus outputOnlyInterfaceStatus);

	public Place getPlace();


	//TODO implement paintComponent from PlaceView, or equivalent 
	
}
