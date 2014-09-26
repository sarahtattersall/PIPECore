package uk.ac.imperial.pipe.models.petrinet;

public class InterfacePlaceStatusAvailable implements InterfacePlaceStatus {

	@Override
	public boolean canRemove() {
		return true;
	}

	@Override
	public boolean canUse() {
		return true;
	}
	
	@Override
	public InterfacePlaceStatus use() {
		return InterfacePlaceStatusEnum.IN_USE.buildStatus(); 
	}

	@Override
	public InterfacePlaceStatus remove() {
		return this;
	}


}
