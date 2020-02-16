package uk.ac.imperial.pipe.models.petrinet;

import java.util.Map;

import uk.ac.imperial.pipe.exceptions.IncludeException;

public class MergeInterfaceStatusHome extends AbstractMergeInterfaceStatus implements MergeInterfaceStatus {

    public MergeInterfaceStatusHome(Place homePlace, PlaceStatus placeStatus) {
        super(homePlace, placeStatus);
    }

    @Override
    public MergeInterfaceStatus copy(PlaceStatus placeStatus) {
        MergeInterfaceStatusHome mergeInterfaceStatus = new MergeInterfaceStatusHome(homePlace, placeStatus);
        mergeInterfaceStatus.awayId = this.awayId;
        return mergeInterfaceStatus;
    }

    @Override
    public boolean canRemove() {
        return true;
    }

    @Override
    public Result<InterfacePlaceAction> add(IncludeHierarchy includeHierarchy) {
        buildAwayId(includeHierarchy.getUniqueNameAsPrefix());
        IncludeHierarchyCommand<InterfacePlaceAction> command = new ConvertPlaceToMergeStatusHomeCommand(homePlace,
                includeHierarchy);
        Result<InterfacePlaceAction> result = includeHierarchy.self(command);
        try {
            result = includeHierarchy.getInterfacePlaceAccessScope().execute(command);
        } catch (IncludeException e) {
            result.addMessage(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Result<InterfacePlaceAction> remove(IncludeHierarchy includeHierarchy) {
        Result<InterfacePlaceAction> result = checkIfPlaceEligibleForRemovalFromInterface(includeHierarchy);
        if (!result.hasResult()) {
            result = removePlaceFromInterfaceAndResetToNormalStatus(includeHierarchy, result);
        }
        return result;

    }

    protected Result<InterfacePlaceAction> checkIfPlaceEligibleForRemovalFromInterface(
            IncludeHierarchy includeHierarchy) {
        PlaceRemovalEligibilityCommand eligibilityCommand = new PlaceRemovalEligibilityCommand(homePlace);
        Result<InterfacePlaceAction> result = new Result<>();
        try {
            result = includeHierarchy.getInterfacePlaceAccessScope().execute(eligibilityCommand);
        } catch (IncludeException e) {
            result.addMessage(e.getMessage());
            e.printStackTrace(); // logic error; this command doesn't throw
        }
        result = includeHierarchy.self(eligibilityCommand);
        return result;
    }

    protected Result<InterfacePlaceAction> removePlaceFromInterfaceAndResetToNormalStatus(
            IncludeHierarchy includeHierarchy,
            Result<InterfacePlaceAction> result) {
        RemovePlaceFromInterfaceCommand removalCommand = new RemovePlaceFromInterfaceCommand(homePlace);
        try {
            result = includeHierarchy.getInterfacePlaceAccessScope().execute(removalCommand);
        } catch (IncludeException e) {
            result.addMessage(e.getMessage());
            e.printStackTrace(); // logic error; this command doesn't throw
        }
        result = includeHierarchy.self(removalCommand);
        if (!result.hasResult()) {
            resetStatusToNormal();
        }
        return result;
    }

    private void resetStatusToNormal() {
        homePlace.setStatus(new PlaceStatusNormal(homePlace));
    }

    @Override
    public String getXmlType() {
        return HOME;
    }

    @Override
    public void setArcConstraint(ArcConstraint arcConstraint) {
    }

    @Override
    public ArcConstraint getArcConstraint() {
        return new NoArcConstraint();
    }

    @Override
    public void prefixIdWithQualifiedName(IncludeHierarchy currentIncludeHierarchy) {
        currentIncludeHierarchy.prefixComponentIdWithQualifiedName(placeStatus.getPlace());
    }

    @Override
    public void updateHomePlace(Map<String, Place> pendingNewHomePlaces) {
        Place newPlace = placeStatus.getPlace();
        setHomePlace(newPlace);
        pendingNewHomePlaces.put(getAwayId(), newPlace);
    }
}
