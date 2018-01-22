package uk.ac.imperial.pipe.models.petrinet;

public class RemovePlaceFunctionalExpressionAction implements
        InterfacePlaceAction {

    private String componentId;
    private Place place;
    private IncludeHierarchy includeHierarchy;

    public RemovePlaceFunctionalExpressionAction(
            IncludeHierarchy includeHierarchy, Place place,
            String componentId) {
        this.includeHierarchy = includeHierarchy;
        this.place = place;
        this.componentId = componentId;
    }

    @Override
    public String getComponentId() {
        return componentId;
    }

    @Override
    public Place getInterfacePlace() {
        return place;
    }

    @Override
    public IncludeHierarchy getIncludeHierarchy() {
        return includeHierarchy;
    }

}
