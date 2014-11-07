package uk.ac.imperial.pipe.models.petrinet;

/**
 * Placeholder for future function, so operations on an {@link InterfacePlace} can be presented to the user
 * depending on the {@link InterfacePlaceStatus} 
 * @see InterfacePlaceStatus
 */
public interface InterfacePlaceAction {

	public abstract IncludeHierarchy getIncludeHierarchy();

	public abstract Place getInterfacePlace();

	public abstract String getComponentId();
	

}
