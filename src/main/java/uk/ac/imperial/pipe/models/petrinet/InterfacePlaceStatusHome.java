package uk.ac.imperial.pipe.models.petrinet;

public class InterfacePlaceStatusHome implements InterfacePlaceStatus {

	private static final String CANT_USE_HOME_INTERFACE_PLACE = "InterfacePlaceStatusHome: interface place cannot be used in the petri net that is the home of its underlying place.";

	@Override
	public boolean canRemove() {
		return true;
	}

	@Override
	public boolean canUse() {
		return false;
	}
	
	@Override
	public InterfacePlaceStatus use() {
		throw new IllegalStateException(CANT_USE_HOME_INTERFACE_PLACE);
	}

	@Override
	public InterfacePlaceStatus remove() {
		return this;
	}

}
