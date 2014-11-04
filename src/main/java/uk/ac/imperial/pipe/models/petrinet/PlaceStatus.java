package uk.ac.imperial.pipe.models.petrinet;


public interface PlaceStatus {

	public MergeInterfaceStatus getMergeInterfaceStatus();

	public InterfaceStatus getExternalInterfaceStatus();

	public InterfaceStatus getInputOnlyInterfaceStatus();

	public InterfaceStatus getOutputOnlyInterfaceStatus();

	public Result<InterfacePlaceAction> setMergeStatus(boolean merge);

	public Result<InterfacePlaceAction> setExternalStatus(boolean external);

	public Result<InterfacePlaceAction> setInputOnlyStatus(boolean inputOnly);

	public Result<InterfacePlaceAction> setOutputOnlyStatus(boolean outputOnly);

	public boolean isMergeStatus();

	public boolean isExternalStatus();

	public boolean isInputOnlyStatus();

	public boolean isOutputOnlyStatus();

	public abstract PlaceStatus copyStatus(Place place);


	//TODO implement paintComponent from PlaceView, or equivalent 
	
}
