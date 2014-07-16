package uk.ac.imperial.pipe.models.petrinet;

public enum InterfacePlaceStatusEnum {
	HOME { public InterfacePlaceStatus buildStatus() { return new InterfacePlaceStatusHome();}},
	AVAILABLE { public InterfacePlaceStatus buildStatus() { return new InterfacePlaceStatusAvailable();}},
	IN_USE { public InterfacePlaceStatus buildStatus() { return new InterfacePlaceStatusInUse();}};

	public abstract InterfacePlaceStatus buildStatus();

}
