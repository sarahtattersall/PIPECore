package uk.ac.imperial.pipe.models.petrinet;

public interface PlaceStatus {

    public Place getPlace();

	public InterfaceStatus getMergeInterfaceStatus();

	public InterfaceStatus getExternalInterfaceStatus();

	public InterfaceStatus getInputOnlyInterfaceStatus();

	public InterfaceStatus getOutputOnlyInterfaceStatus();

	public IncludeHierarchy getIncludeHierarchy();

	public void setMergeStatus(boolean merge);

	public void setExternalStatus(boolean external);

	public void setInputOnlyStatus(boolean inputOnly);

	public void setOutputOnlyStatus(boolean outputOnly);

	public boolean isMergeStatus();

	public boolean isExternalStatus();

	public boolean isInputOnlyStatus();

	public boolean isOutputOnlyStatus();

	public PlaceStatus copy(Place Place);


	//TODO implement paintComponent from PlaceView, or equivalent 
	
}
