package uk.ac.imperial.pipe.models.petrinet;


public interface PlaceStatus {

	public boolean isMergeStatus();
	public void setMergeStatus(boolean merge);
	
	public boolean isExternalStatus();
	public void setExternalStatus(boolean external);
	
	public boolean isInputOnlyStatus();
	public void setInputOnlyStatus(boolean inputOnly);
	
	public boolean isOutputOnlyStatus();
	public void setOutputOnlyStatus(boolean outputOnly);

	public MergeInterfaceStatus getMergeInterfaceStatus();
	public void setMergeInterfaceStatus(MergeInterfaceStatus interfaceStatus);

	public InterfaceStatus getExternalInterfaceStatus();
	public void setExternalInterfaceStatus(InterfaceStatus externalInterfaceStatus);
	

	public InterfaceStatus getInputOnlyInterfaceStatus();
	public void setInputOnlyInterfaceStatus(InterfaceStatus inputOnlyInterfaceStatus);
	
	public InterfaceStatus getOutputOnlyInterfaceStatus();
	public void setOutputOnlyInterfaceStatus(InterfaceStatus outputOnlyInterfaceStatus);
	
	public IncludeHierarchy getIncludeHierarchy();
	public void setIncludeHierarchy(IncludeHierarchy includeHierarchy);

	public  PlaceStatus copyStatus(Place place);

	public Result<InterfacePlaceAction> update();

	public Place getPlace();

	//TODO implement paintComponent from PlaceView, or equivalent 
	
}
