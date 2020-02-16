package uk.ac.imperial.pipe.models.petrinet;

import java.util.Map;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;

public class MergeInterfaceStatusAway extends AbstractMergeInterfaceStatus implements MergeInterfaceStatus {

    public MergeInterfaceStatusAway(Place homePlace, PlaceStatus placeStatus, String awayId) {
        super(homePlace, placeStatus, awayId);
    }

    @Override
    public MergeInterfaceStatus copy(PlaceStatus placeStatus) {
        return new MergeInterfaceStatusAway(homePlace, placeStatus, awayId);
    }

    @Override
    public Result<InterfacePlaceAction> add(IncludeHierarchy includeHierarchy) {
        return new Result<InterfacePlaceAction>();
    }

    @Override
    public boolean canRemove() {
        return false;
    }

    @Override
    public Result<InterfacePlaceAction> remove(IncludeHierarchy includeHierarchy) {
        Result<InterfacePlaceAction> result = new Result<>();
        Place place = null;
        try {
            place = includeHierarchy.getPetriNet().getComponent(awayId, Place.class);
            includeHierarchy.getPetriNet().removePlaceBare(place);
        } catch (PetriNetComponentNotFoundException e) {
            result.addMessage(e.getMessage());
        } catch (PetriNetComponentException e) {
            result.addMessage(e.getMessage());
        }
        if (!result.hasResult()) {
            MergeInterfaceStatus mergeStatus = new MergeInterfaceStatusAvailable(homePlace, placeStatus, awayId);
            place.getStatus().setMergeInterfaceStatus(mergeStatus);
        }
        return result;
    }

    @Override
    public String getXmlType() {
        return AWAY;
    }

    @Override
    public void prefixIdWithQualifiedName(IncludeHierarchy currentIncludeHierarchy) {
    }

    @Override
    public void updateHomePlace(Map<String, Place> pendingNewHomePlaces) {
    }
}
