package uk.ac.imperial.pipe.models.petrinet;

public class InterfacePlaceStatusInUse implements InterfacePlaceStatus {

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
		return this; 
	}

	@Override
	public InterfacePlaceStatus remove() {
		return InterfacePlaceStatusEnum.AVAILABLE.buildStatus(); 
	}

}
