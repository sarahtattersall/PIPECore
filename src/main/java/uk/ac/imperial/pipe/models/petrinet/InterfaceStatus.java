package uk.ac.imperial.pipe.models.petrinet;

public interface InterfaceStatus {

    public Result<InterfacePlaceAction> add(IncludeHierarchy includeHierarchy);

    public Result<InterfacePlaceAction> remove(IncludeHierarchy includeHierarchy);

}
