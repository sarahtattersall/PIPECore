package uk.ac.imperial.pipe.models.petrinet;

public class InterfacePlaceStatusInUse implements InterfacePlaceStatus {

	@Override
	public boolean canRemove() {
		return false;
	}

}
