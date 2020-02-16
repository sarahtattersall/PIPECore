package uk.ac.imperial.pipe.models.petrinet;

import java.util.Map;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.visitor.PlaceBuilder;

public class MergeInterfaceStatusAvailable extends AbstractMergeInterfaceStatus {

    public MergeInterfaceStatusAvailable(Place homePlace, PlaceStatus placeStatus, String awayId) {
        super(homePlace, placeStatus, awayId);

    }

    @Override
    public MergeInterfaceStatus copy(PlaceStatus placeStatus) {
        return new MergeInterfaceStatusAvailable(homePlace, placeStatus, awayId);
    }

    @Override
    public Result<InterfacePlaceAction> add(IncludeHierarchy includeHierarchy) {
        return new Result<>();
    }

    @Override
    public Result<InterfacePlaceAction> remove(IncludeHierarchy includeHierarchy) {
        return buildNotSupportedResult("remove", "Available");
    }

    @Override
    public Result<InterfacePlaceAction> add(PetriNet petriNet) {
        Result<InterfacePlaceAction> result = new Result<>();
        petriNet.addPlace(placeStatus.getPlace());
        PlaceBuilder builder = new PlaceBuilder(placeStatus);
        try {
            homePlace.accept(builder);
        } catch (PetriNetComponentException e) {
            //TODO test exception
            result.addEntry("Unable to add available place " + awayId + " to Petri net: " + petriNet.getName() +
                    "; probable logic error.  Details: " + e.getMessage(), null);
        }
        return result;
    }

    @Override
    public boolean canRemove() {
        return false;
    }

    @Override
    public String getXmlType() {
        return null;
    }

    @Override
    public void prefixIdWithQualifiedName(IncludeHierarchy currentIncludeHierarchy) {
        currentIncludeHierarchy.prefixComponentIdWithQualifiedName(placeStatus.getPlace());
    }

    @Override
    public void updateHomePlace(Map<String, Place> pendingNewHomePlaces) {
    }

}
