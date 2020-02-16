package uk.ac.imperial.pipe.models.petrinet;

public class RemovePlaceFromInterfaceCommand extends AbstractIncludeHierarchyCommand<InterfacePlaceAction> {

    private Place homePlace;

    public RemovePlaceFromInterfaceCommand(Place homePlace) {
        this.homePlace = homePlace;
    }

    @Override
    public Result<InterfacePlaceAction> execute(IncludeHierarchy includeHierarchy) {
        if (this.homePlace.getStatus().getIncludeHierarchy().equals(includeHierarchy)) {
            removeFromHomeIncludeHierarchy(includeHierarchy);
        } else {
            removeFromAwayIncludeHierarchy(includeHierarchy);
        }
        return result;
    }

    protected void removeFromHomeIncludeHierarchy(
            IncludeHierarchy includeHierarchy) {
        includeHierarchy.removeFromInterface(homePlace.getId());
    }

    protected void removeFromAwayIncludeHierarchy(IncludeHierarchy includeHierarchy) {
        String awayId = homePlace.getStatus().getMergeInterfaceStatus().getAwayId();
        if (includeHierarchy.getInterfacePlace(awayId) != null) {
            includeHierarchy.removeFromInterface(awayId);
            deletePlaceFromPetriNetIfExists(includeHierarchy, awayId);
        } else {
            //TODO test logic error for awayId
            result.addMessage("RemovePlaceFromInterfaceCommand.execute: Interface place not found for AwayId " +
                    awayId + " in include hierarchy " + includeHierarchy.getName() + ".  Probable logic error.");
        }
    }

    protected void deletePlaceFromPetriNetIfExists(
            IncludeHierarchy includeHierarchy, String awayId) {
        try {
            Place place = includeHierarchy.getPetriNet().getComponent(awayId, Place.class);
            includeHierarchy.getPetriNet().removePlaceBare(place);
        } catch (Exception e) {
            // PetriNetComponentNotFoundException if place doesn't exist,
            // i.e., if MergeInterfaceStatusAvailable
        }
    }

}
