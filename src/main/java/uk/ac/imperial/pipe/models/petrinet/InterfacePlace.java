package uk.ac.imperial.pipe.models.petrinet;


public interface InterfacePlace extends Place {

	/**
	 * 
	 * @return the {@link uk.ac.imperial.pipe.models.Place} corresponding to this InterfacePlace
	 */
	public Place getPlace();
	/**
	 * Set the status of this InterfacePlace 
	 * @see InterfacePlaceStatusEnum 
	 * @param status
	 */
	public void setStatus(InterfacePlaceStatus status);
	public InterfacePlaceStatus getStatus();

	public void setAwayName(String alias);

	public void setHomeName(String alias);
	
	/**
	 * When true, this InterfacePlace has been added to the current PetriNet, and has status {@link InterfacePlaceStatusInUse} 
	 * When false, this InterfacePlace is not part of the current PetriNet, and has status {@link InterfacePlaceStatusAvailable}
	 * Throws {@link IllegalStateException} if current status is {@link InterfacePlaceStatusHome}; this indicates a logic error. If in doubt,
	 * test the status using {@link #canUse()}. 
	 * @see InterfacePlaceStatus 
	 * @param inUse
	 */
	public boolean canUse();
	public boolean isInUse();
	public boolean isHome();

	public boolean use();
	public boolean remove();


}