package uk.ac.imperial.pipe.models.petrinet;

/**
 * Placeholder for future function, so operations on a {@link Place} can be presented to the user
 * depending on the {@link PlaceStatus} 
 * @see PlaceStatus
 */
public interface InterfacePlaceAction {

    public abstract IncludeHierarchy getIncludeHierarchy();

    public abstract Place getInterfacePlace();

    public abstract String getComponentId();

}
