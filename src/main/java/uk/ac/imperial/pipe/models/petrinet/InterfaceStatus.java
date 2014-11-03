package uk.ac.imperial.pipe.models.petrinet;

public interface InterfaceStatus {

	public Result<InterfacePlaceAction> add();

	public Result<InterfacePlaceAction> remove();
}
