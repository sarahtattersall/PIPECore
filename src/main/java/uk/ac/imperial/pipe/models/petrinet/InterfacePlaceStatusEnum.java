package uk.ac.imperial.pipe.models.petrinet;

public enum InterfacePlaceStatusEnum {
	HOME { public InterfacePlaceStatus buildStatus(IncludeHierarchy includeHierarchy) { return new InterfacePlaceStatusHome(includeHierarchy);}},
	AVAILABLE { public InterfacePlaceStatus buildStatus(IncludeHierarchy includeHierarchy) { return new InterfacePlaceStatusAvailable(includeHierarchy);}},
	IN_USE { public InterfacePlaceStatus buildStatus(IncludeHierarchy includeHierarchy) { return new InterfacePlaceStatusInUse(includeHierarchy);}};

	public abstract InterfacePlaceStatus buildStatus(IncludeHierarchy includeHierarchy);

}
