package uk.ac.imperial.pipe.models.petrinet;

public interface InterfacePlaceStatus {
	/**
	 * 
	 * @return whether the InterfacePlace can be removed from the interface.  false if status is InUse 
	 * @see uk.ac.imperial.pipe.models.petrinet.InterfacePlaceStatusInUse
	 * @see uk.ac.imperial.pipe.models.petrinet.InterfacePlaceStatusHome
	 * @see uk.ac.imperial.pipe.models.petrinet.InterfacePlaceStatusAvailable
	 */
	public boolean canRemove();	

}
