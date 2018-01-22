package uk.ac.imperial.pipe.models.petrinet;

public abstract class AbstractMergeInterfaceStatus implements MergeInterfaceStatus {

    protected Place homePlace;
    protected PlaceStatus placeStatus;
    protected String awayId;
    private ArcConstraint arcConstraint;

    public AbstractMergeInterfaceStatus(Place homePlace, PlaceStatus placeStatus) {
        this.homePlace = homePlace;
        this.placeStatus = placeStatus;
        this.arcConstraint = new NoArcConstraint();
    }

    public AbstractMergeInterfaceStatus(Place homePlace,
            PlaceStatus placeStatus, String awayId) {
        this(homePlace, placeStatus);
        this.awayId = awayId;
    }

    @Override
    public abstract boolean canRemove();

    @Override
    public Place getHomePlace() {
        return homePlace;
    }

    @Override
    public void setHomePlace(Place homePlace) {
        this.homePlace = homePlace;
    }

    protected void buildAwayId(String uniqueNameAsPrefix) {
        this.awayId = uniqueNameAsPrefix + homePlace.getId();
    }

    @Override
    public String getAwayId() {
        return awayId;
    }

    @Override
    public void setAwayId(String awayId) {
        this.awayId = awayId;
    }

    @Override
    public Result<InterfacePlaceAction> add(PetriNet petriNet) {
        Result<InterfacePlaceAction> result = new Result<>();
        result.addMessage("place " + placeStatus.getPlace().getId() + " cannot be added to Petri net " +
                petriNet.getNameValue() + " because it is already present.");
        return result;
    }

    @Override
    public abstract Result<InterfacePlaceAction> add(IncludeHierarchy includeHierarchy);

    @Override
    public abstract Result<InterfacePlaceAction> remove(IncludeHierarchy includeHierarchy);

    protected Result<InterfacePlaceAction> buildNotSupportedResult(String method, String status) {
        Result<InterfacePlaceAction> result = new Result<>();
        result.addMessage("MergeInterfaceStatus" + status + "." + method + ": not supported for " +
                status + " status.  Must be issued by MergeInterfaceStatusHome against the home include hierarchy.");
        return result;
    }

    @Override
    public void setArcConstraint(ArcConstraint arcConstraint) {
        this.arcConstraint = arcConstraint;
    }

    @Override
    public ArcConstraint getArcConstraint() {
        return arcConstraint;
    }

}
