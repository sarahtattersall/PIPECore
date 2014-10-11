package uk.ac.imperial.pipe.models.petrinet;

/**
 * Represents the status of the associated {@link InterfacePlace}.  
 * When a {@link Place} is initially added to the interface of a given Petri net, 
 * a corresponding {@link InterfacePlace} is created.  The Place is the "source Place", and 
 * the InterfacePlace is the "home InterfacePlace" ({@link InterfacePlaceStatusHome}).  
 * As determined by the {@link IncludeHierarchyCommandScope}, 
 * other InterfacePlaces are created in some or all of the other IncludeHierarchies that are part of
 * the {@link IncludeHierarchy} to which the Petri net belongs.  These 
 * interface places are given a status of "available" ({@link InterfacePlaceStatusAvailable}).  Subsequently, an available 
 * interface place may be added to the Petri net corresponding to an IncludeHierarchy, 
 * at which point its status is "inuse" ({@link InterfacePlaceStatusInUse}).  If it is determined
 * that a given in use interface place is no longer needed for a particular Petri net, it may be 
 * removed, in which case its status returns to available.  Finally, if the source Place is removed
 * from the interface of its Petri net, all associated interface places are removed across all include hierarchies.
 * If some of these interface places are in use, the remove request will fail.  An {@link InterfacePlaceAction} is returned for 
 * each component that is making use of the interface place, so that the components may be altered or removed 
 * to eliminate the dependency on the interface place.  Once all dependencies have been addressed, 
 * the remove request can be retried.              
 *    
 * @see uk.ac.imperial.pipe.models.petrinet.InterfacePlaceStatusInUse
 * @see uk.ac.imperial.pipe.models.petrinet.InterfacePlaceStatusHome
 * @see uk.ac.imperial.pipe.models.petrinet.InterfacePlaceStatusAvailable
 * @see uk.ac.imperial.pipe.models.petrinet.IncludeHierarchyCommandScope
 * @see uk.ac.imperial.pipe.models.petrinet.IncludeHierarchyCommandScopeEnum
 * @see uk.ac.imperial.pipe.models.petrinet.InterfacePlaceAction
 */
public interface InterfacePlaceStatus {
	/**
	 * An in use InterfacePlace can only be removed if there are no components (e.g., arcs, functional rate expressions)
	 * that depend on it.  
	 * @return whether the InterfacePlace is in use by the current Petri net.     
	 */
	public boolean isInUse();
	/**
	 * 
	 * @return whether the InterfacePlace can be added to the current Petri net
	 */
	public boolean canUse();
	/**
	 * 
	 * @return whether the source Place for this InterfacePlace is in the current Petri net
	 */
	public boolean isHome();

	public Result<InterfacePlaceAction> use();
	
	public Result<InterfacePlaceAction> remove();

	public InterfacePlace getInterfacePlace();
	
	public void setInterfacePlace(InterfacePlace interfacePlace);
	
	public IncludeHierarchy getIncludeHierarchy();

	public String buildId(String id, String homeName, String awayName);

	public InterfacePlaceStatus nextStatus(); 

}
