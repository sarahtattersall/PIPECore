package uk.ac.imperial.pipe.models.petrinet;

import java.util.Map;

public class PlaceStatusNormal implements PlaceStatus {

    private Place place;
    private MergeInterfaceStatus mergeInterfaceStatus;
    private ArcConstraint arcConstraint;

    public PlaceStatusNormal(Place place) {
        this.place = place;
        this.mergeInterfaceStatus = new NoOpInterfaceStatus(this);
        this.arcConstraint = new NoArcConstraint();
    }

    @Override
    public Place getPlace() {
        return place;
    }

    @Override
    public MergeInterfaceStatus getMergeInterfaceStatus() {
        return mergeInterfaceStatus;
    }

    public IncludeHierarchy getIncludeHierarchy() {
        return null;
    }

    @Override
    public void setMergeStatus(boolean merge) {
        throw new UnsupportedOperationException(buildUnsupportedMessage("setMergeStatus"));
    }

    @Override
    public void setExternal(boolean external) {
        throw new UnsupportedOperationException(buildUnsupportedMessage("setExternalStatus"));
    }

    @Override
    public void setInputOnlyArcConstraint(boolean inputOnly) {
        throw new UnsupportedOperationException(buildUnsupportedMessage("setInputOnlyStatus"));
    }

    @Override
    public void setOutputOnlyArcConstraint(boolean outputOnly) {
        throw new UnsupportedOperationException(buildUnsupportedMessage("setOutputOnlyStatus"));
    }

    private String buildUnsupportedMessage(String method) {
        return "PlaceStatusNormal:  " + method +
                " not a valid request for place " + place.getId() +
                " until Place.addToInterface(IncludeHierarchy) has been requested";
    }

    public boolean isOutputOnlyArcConstraint() {
        return false;
    }

    public boolean isInputOnlyArcConstraint() {
        return false;
    }

    public boolean isExternal() {
        return false;
    }

    public boolean isMergeStatus() {
        return false;
    }

    public PlaceStatus copyStatus(Place place) {
        return new PlaceStatusNormal(place);
    }

    public void setMergeInterfaceStatus(MergeInterfaceStatus interfaceStatus) {
        this.mergeInterfaceStatus = interfaceStatus;
    }

    @Override
    public Result<InterfacePlaceAction> update() {
        return null;
    }

    @Override
    public void setIncludeHierarchy(IncludeHierarchy includeHierarchy) {
    }

    @Override
    public void setPlace(Place place) {
        this.place = place;
    }

    @Override
    public String getMergeXmlType() {
        return null;
    }

    @Override
    public void buildMergeStatus(String type) {
    }

    public void setArcConstraint(ArcConstraint arcConstraint) {
        this.arcConstraint = arcConstraint;
    }

    public ArcConstraint getArcConstraint() {
        return arcConstraint;
    }

    @Override
    public void prefixIdWithQualifiedName(IncludeHierarchy currentIncludeHierarchy) {
        mergeInterfaceStatus.prefixIdWithQualifiedName(currentIncludeHierarchy);
    }

    @Override
    public void updateHomePlace(Map<String, Place> pendingNewHomePlaces) {
    }

}
