package uk.ac.imperial.pipe.models.petrinet;

import java.util.Map;

public class PlaceStatusInterface implements PlaceStatus {

    private static final String ARC_CONSTRAINT_MAY_NOT_BE_BOTH_INPUT_ONLY_AND_OUTPUT_ONLY = "arc constraint may not be both input only and output only.";
    private static final String SET_INPUT_ONLY_ARC_CONSTRAINT = "setInputOnlyArcConstraint: ";
    private static final String SET_OUTPUT_ONLY_CONSTRAINT = "setOutputOnlyArcConstraint: ";
    private static final String PLACE_STATUS = "PlaceStatus.";
    private Place place;
    private boolean merge;
    private boolean external;
    private boolean inputOnly;
    private boolean outputOnly;
    private MergeInterfaceStatus mergeStatus;
    private IncludeHierarchy includeHierarchy;
    private boolean mergeChanged;
    private boolean inputOnlyChanged;
    private boolean outputOnlyChanged;
    private Result<InterfacePlaceAction> result;
    private ArcConstraint arcConstraint;
    private boolean externalChanged;

    public PlaceStatusInterface(Place place) {
        this(place, null); //TODO delete
    }

    //TODO determine whether constructor should create new arc constraint
    public PlaceStatusInterface(PlaceStatus placeStatus, Place place) {
        this.place = place;
        this.includeHierarchy = placeStatus.getIncludeHierarchy();
        mergeStatus = new NoOpInterfaceStatus(this);
        setMergeStatus(placeStatus.isMergeStatus());
        setExternal(placeStatus.isExternal());
        setInputOnlyArcConstraint(placeStatus.isInputOnlyArcConstraint());
        setOutputOnlyArcConstraint(placeStatus.isOutputOnlyArcConstraint());
        setMergeInterfaceStatus(placeStatus.getMergeInterfaceStatus().copy(this));
        setArcConstraint(placeStatus.getArcConstraint());
        resetUpdate();
    }

    public PlaceStatusInterface(Place place, IncludeHierarchy includeHierarchy) {
        this.place = place;
        this.includeHierarchy = includeHierarchy;
        setMergeStatus(false);
        mergeStatus = new NoOpInterfaceStatus(this);
        setExternal(false);
        setInputOnlyArcConstraint(false);
        setOutputOnlyArcConstraint(false);
        update();
    }

    /**
     * Null constructor used by XML marshalling
     */
    public PlaceStatusInterface() {
        mergeStatus = new NoOpInterfaceStatus(this);
    }

    private void resetUpdate() {
        mergeChanged = false;
        inputOnlyChanged = false;
        outputOnlyChanged = false;
        externalChanged = false;
        result = new Result<>();
    }

    @Override
    public Place getPlace() {
        return place;
    }

    @Override
    public MergeInterfaceStatus getMergeInterfaceStatus() {
        return mergeStatus;
    }

    @Override
    public Result<InterfacePlaceAction> update() {
        if (mergeChanged)
            buildMergeStatus();
        if (inputOnlyChanged)
            buildInputOnlyArcConstraint();
        if (outputOnlyChanged)
            buildOutputOnlyArcConstraint();
        Result<InterfacePlaceAction> tempResult = result;
        resetUpdate();
        return tempResult;
    }

    @Override
    public void setMergeStatus(boolean merge) {
        if (merge != this.merge) {
            this.merge = merge;
            this.mergeChanged = true;
        }
    }

    @Override
    public void setExternal(boolean external) {
        if (external != this.external) {
            this.external = external;
            this.externalChanged = true;
        }
    }

    //TODO neither arc costraint can be true unless external is also true
    @Override
    public void setInputOnlyArcConstraint(boolean inputOnly) {
        if (inputOnly != this.inputOnly) {
            this.inputOnly = inputOnly;
            this.inputOnlyChanged = true;
        }
    }

    @Override
    public void setOutputOnlyArcConstraint(boolean outputOnly) {
        if (outputOnly != this.outputOnly) {
            this.outputOnly = outputOnly;
            this.outputOnlyChanged = true;
        }
    }

    protected Result<InterfacePlaceAction> buildMergeStatus() {
        if (merge) {
            mergeStatus = new MergeInterfaceStatusHome(place, this);
            result = mergeStatus.add(includeHierarchy);
        } else {
            mergeStatus = new NoOpInterfaceStatus(this);
        }
        return result;
    }

    @Override
    public void buildMergeStatus(String type) {
        merge = true;
        if (type.equals(MergeInterfaceStatus.HOME)) {
            mergeStatus = new MergeInterfaceStatusHome(null, this);
        } else {
            mergeStatus = new MergeInterfaceStatusAway(null, this, null);

        }
    }

    public Result<InterfacePlaceAction> buildInputOnlyArcConstraint() {
        if (inputOnly) {
            if (outputOnly) {
                result.addMessage(PLACE_STATUS + SET_INPUT_ONLY_ARC_CONSTRAINT +
                        ARC_CONSTRAINT_MAY_NOT_BE_BOTH_INPUT_ONLY_AND_OUTPUT_ONLY);
                this.inputOnly = false;
            } else {
                //				arcConstraint = new InputOnlyArcConstraint();
                mergeStatus.setArcConstraint(new InputOnlyArcConstraint());
            }
        } else {
            mergeStatus.setArcConstraint(new NoArcConstraint());
            //			arcConstraint = new NoOpInterfaceStatus();
        }
        return result;
    }

    public Result<InterfacePlaceAction> buildOutputOnlyArcConstraint() {
        if (outputOnly) {
            if (inputOnly) {
                result.addMessage(PLACE_STATUS + SET_OUTPUT_ONLY_CONSTRAINT +
                        ARC_CONSTRAINT_MAY_NOT_BE_BOTH_INPUT_ONLY_AND_OUTPUT_ONLY);
                this.outputOnly = false;
            } else {
                mergeStatus.setArcConstraint(new OutputOnlyArcConstraint());
                //				arcConstraint = new OutputOnlyArcConstraint();
            }
        } else {
            mergeStatus.setArcConstraint(new NoArcConstraint());
            //			arcConstraint = new NoArcConstraint();
        }
        return result;
    }

    @Override
    public boolean isMergeStatus() {
        return merge;
    }

    @Override
    public boolean isExternal() {
        return external;
    }

    @Override
    public boolean isInputOnlyArcConstraint() {
        return inputOnly;
    }

    @Override
    public boolean isOutputOnlyArcConstraint() {
        return outputOnly;
    }

    @Override
    public PlaceStatus copyStatus(Place place) {
        return new PlaceStatusInterface(this, place);
    }

    @Override
    public void setMergeInterfaceStatus(MergeInterfaceStatus interfaceStatus) {
        this.mergeStatus = interfaceStatus;
    }

    @Override
    public IncludeHierarchy getIncludeHierarchy() {
        return includeHierarchy;
    }

    @Override
    public void setIncludeHierarchy(IncludeHierarchy includeHierarchy) {
        this.includeHierarchy = includeHierarchy;
    }

    @Override
    public void setPlace(Place place) {
        if ((place.getStatus() != null) && (place.getStatus() != this)) {
            throw new IllegalArgumentException(
                    "PlaceStatus for Place " + place.getId() + " (if not null) must be same as this PlaceStatus");
        }
        this.place = place;
        mergeStatus.setHomePlace(place);
        //update MergeStatus
    }

    @Override
    public String getMergeXmlType() {
        return mergeStatus.getXmlType();
    }

    @Override
    public ArcConstraint getArcConstraint() {
        return mergeStatus.getArcConstraint();
        //		return arcConstraint;
    }

    @Override
    public void setArcConstraint(ArcConstraint arcConstraint) {
        mergeStatus.setArcConstraint(arcConstraint);
        //		this.arcConstraint = arcConstraint;
    }

    protected boolean hasExternalChanged() {
        return externalChanged;
    }

    @Override
    public void prefixIdWithQualifiedName(IncludeHierarchy currentIncludeHierarchy) {
        mergeStatus.prefixIdWithQualifiedName(currentIncludeHierarchy);
    }

    @Override
    public void updateHomePlace(Map<String, Place> pendingNewHomePlaces) {
        mergeStatus.updateHomePlace(pendingNewHomePlaces);
    }

}
