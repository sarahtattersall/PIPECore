package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.visitor.PlaceBuilder;

public class ConvertPlaceToMergeStatusHomeCommand extends AbstractIncludeHierarchyCommand<InterfacePlaceAction> {

    private IncludeHierarchy home;
    private Place homePlace;

    public ConvertPlaceToMergeStatusHomeCommand(Place homePlace, IncludeHierarchy home) {
        this.homePlace = homePlace;
        this.home = home;
    }

    @Override
    public Result<InterfacePlaceAction> execute(IncludeHierarchy includeHierarchy) {
        Place addedPlace;
        if (this.home.equals(includeHierarchy)) {
            addedPlace = homePlace;
        } else {
            Place place = buildPlaceWithMergeStatusAvailableOrUpdateAwayStatus(includeHierarchy);
            addedPlace = place;
        }
        addPlaceToInterfacePlaces(addedPlace, includeHierarchy);
        return result;
    }

    private Place buildPlaceWithMergeStatusAvailableOrUpdateAwayStatus(IncludeHierarchy includeHierarchy) {
        Place newPlace = updateAwayStatusIfExists(includeHierarchy);
        if (newPlace == null) {
            newPlace = buildPlaceWithAvailableStatus(includeHierarchy);
        }
        return newPlace;
    }

    private Place updateAwayStatusIfExists(IncludeHierarchy includeHierarchy) {
        Place awayPlace = null;
        String awayId = homePlace.getStatus().getMergeInterfaceStatus().getAwayId();
        if (awayId != null) {
            try {
                awayPlace = includeHierarchy.getPetriNet().getComponent(awayId, Place.class);
                awayPlace.getStatus().setIncludeHierarchy(includeHierarchy);
                MergeInterfaceStatus away = awayPlace.getStatus().getMergeInterfaceStatus();
                away.setHomePlace(homePlace);
                away.setAwayId(awayId);
                listenForTokenCountChanges(awayPlace);
            } catch (PetriNetComponentNotFoundException e) { // fine if doesn't exist
            }
        }
        return awayPlace;
    }

    protected Place buildPlaceWithAvailableStatus(IncludeHierarchy includeHierarchy) {
        PlaceBuilder cloner = new PlaceBuilder(includeHierarchy);
        try {
            homePlace.accept(cloner);
        } catch (PetriNetComponentException e) {
            e.printStackTrace();
        }
        return cloner.built;
    }

    private void listenForTokenCountChanges(Place place) {
        homePlace.addPropertyChangeListener(place);
        place.addPropertyChangeListener(homePlace);
    }

    private void addPlaceToInterfacePlaces(Place place, IncludeHierarchy includeHierarchy) {
        IncludeHierarchyCommand<UpdateResultEnum> addCommand = new UpdateMapEntryCommand<Place>(
                IncludeHierarchyMapEnum.INTERFACE_PLACES, place.getId(), place);
        Result<UpdateResultEnum> tempResult = addCommand.execute(includeHierarchy);
        if (tempResult.hasResult()) {
            for (String message : tempResult.getMessages()) {
                result.addMessage(message);
            }
        }
    }
}
