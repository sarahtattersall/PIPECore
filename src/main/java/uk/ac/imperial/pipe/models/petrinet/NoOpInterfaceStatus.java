package uk.ac.imperial.pipe.models.petrinet;

import java.util.Map;

public class NoOpInterfaceStatus extends AbstractIncludeHierarchyCommand<InterfacePlaceAction>
        implements InterfaceStatus, MergeInterfaceStatus {

    private ArcConstraint arcConstraint = new NoArcConstraint();
    private PlaceStatus placeStatus;

    public NoOpInterfaceStatus(PlaceStatus placeStatus) {
        this.placeStatus = placeStatus;

    }

    @Override
    public Result<InterfacePlaceAction> add(IncludeHierarchy includeHierarchy) {
        return result;
    }

    @Override
    public Result<InterfacePlaceAction> remove(IncludeHierarchy includeHierarchy) {
        return result;
    }

    @Override
    public Result<InterfacePlaceAction> execute(
            IncludeHierarchy includeHierarchy) {
        return result;
    }

    @Override
    public Place getHomePlace() {
        return null;
    }

    @Override
    public String getAwayId() {
        return null;
    }

    @Override
    public Result<InterfacePlaceAction> add(PetriNet petriNet) {
        return null;
    }

    @Override
    public boolean canRemove() {
        return false;
    }

    @Override
    public void setHomePlace(Place homePlace) {
    }

    @Override
    public String getXmlType() {
        return null;
    }

    public final void setAwayId(String awayId) {
    }

    @Override
    public void setArcConstraint(ArcConstraint arcConstraint) {
        this.arcConstraint = arcConstraint;
    }

    @Override
    public ArcConstraint getArcConstraint() {
        return arcConstraint;
    }

    @Override
    public void prefixIdWithQualifiedName(IncludeHierarchy currentIncludeHierarchy) {
        currentIncludeHierarchy.prefixComponentIdWithQualifiedName(placeStatus.getPlace());
    }

    @Override
    public MergeInterfaceStatus copy(PlaceStatus placeStatus) {
        return new NoOpInterfaceStatus(placeStatus);
    }

    @Override
    public void updateHomePlace(Map<String, Place> pendingNewHomePlaces) {
    }

}
