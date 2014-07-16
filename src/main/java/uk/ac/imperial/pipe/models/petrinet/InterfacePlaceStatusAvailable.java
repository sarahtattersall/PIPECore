package uk.ac.imperial.pipe.models.petrinet;

public class InterfacePlaceStatusAvailable implements InterfacePlaceStatus {

	@Override
	public boolean canRemove() {
		return true;
	}

}
