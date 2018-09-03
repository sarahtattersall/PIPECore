package uk.ac.imperial.pipe.models.petrinet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * May be useful when a PNML file is loaded by itself that is normally part of an include hierarchy.
 * If it references away places for which no home place is found, this command converts the away place
 * to a non-merge place (either in the interface if isExternal is true, or removed from the interface.)
 * @author steve
 *
 */
public class RemoveOrphanedAwayPlacesFromInterfaceCommand
        extends AbstractIncludeHierarchyCommand<InterfacePlaceAction> {

    protected static Logger logger = LogManager.getLogger(RemoveOrphanedAwayPlacesFromInterfaceCommand.class);

    @Override
    public Result<InterfacePlaceAction> execute(IncludeHierarchy includeHierarchy) {
        MergeInterfaceStatus mergeStatus;
        PlaceStatus status;
        for (Place place : includeHierarchy.getPetriNet().getPlaces()) {
            mergeStatus = place.getStatus().getMergeInterfaceStatus();
            if ((mergeStatus instanceof MergeInterfaceStatusAway) && (mergeStatus.getHomePlace() == null)) {
                status = place.getStatus();
                if (!status.isExternal()) {
                    place.setStatus(new PlaceStatusNormal(place));
                    includeHierarchy.removeFromInterface(place.getId());
                } else {
                    place.getStatus().setMergeStatus(false);
                    place.getStatus().setMergeInterfaceStatus(new NoOpInterfaceStatus(place.getStatus()));
                }
                String message = "Place " + place.getId() + " in PetriNet " +
                        includeHierarchy.getPetriNet().getNameValue() +
                        " is an Away place without a corresponding Home place, " +
                        "possibly due to errors in the pnml file or opening an incomplete interface hierarchy.  " +
                        "Place is no longer a merge place; impact to PetriNet execution is undefined.";
                result.addMessage(message);
                logger.warn(message);
            }
        }
        return result;
    }

}
