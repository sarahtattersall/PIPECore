package uk.ac.imperial.pipe.models.petrinet;

public interface InterfaceStatus {

	public Result<InterfacePlaceAction> addTo(IncludeHierarchy includeHierarchy);

	public Result<InterfacePlaceAction> removeFrom(IncludeHierarchy includeHierarchy);
}
