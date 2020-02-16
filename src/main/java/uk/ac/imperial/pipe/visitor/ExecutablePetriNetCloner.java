package uk.ac.imperial.pipe.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.imperial.pipe.exceptions.IncludeException;
import uk.ac.imperial.pipe.models.petrinet.AbstractPetriNet;
import uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet;
import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.IncludeIterator;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatusAway;
import uk.ac.imperial.pipe.models.petrinet.NoOpInterfaceStatus;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.PetriNetComponent;
import uk.ac.imperial.pipe.models.petrinet.Place;

/**
 * Class for cloning exactly a Petri net, or for refreshing an existing {@link ExecutablePetriNet} from the Petri nets of its {@link IncludeHierarchy}
 */
public final class ExecutablePetriNetCloner extends AbstractPetriNetCloner {

    //	/**
    //	 * Class logger
    //	 */
    //	private static final Logger LOGGER = Logger.getLogger(CloneExecutablePetriNet.class.getName());

    /**
     * as components are visited, some modifications are required when refreshing an {@link ExecutablePetriNet}:
     */
    boolean refreshingExecutablePetriNet = false;
    /**
     * The {@link IncludeHierarchy} of a target {@link ExecutablePetriNet} during {@link ExecutablePetriNetCloner#refreshFromIncludeHierarchy(ExecutablePetriNet)}
     */
    private IncludeHierarchy includeHierarchy;

    /**
     * The {@link IncludeHierarchy} of the {@link PetriNet} currently being processed during {@link ExecutablePetriNetCloner#refreshFromIncludeHierarchy(ExecutablePetriNet)}
     */
    private IncludeHierarchy currentIncludeHierarchy;

    private Map<String, Place> pendingAwayPlaces = new HashMap<>();

    private List<Place> pendingPlacesToDelete = new ArrayList<>();

    Map<String, Place> pendingNewHomePlaces = new HashMap<>();

    protected ExecutablePetriNet newPetriNet;

    public static ExecutablePetriNetCloner cloneInstance;

    /**
     * Rebuilds an {@link ExecutablePetriNet} from the set of {@link PetriNet} defined in its {@link IncludeHierarchy}.
     * The following collections are refreshed, by cloning each element in the PetriNet collection, and adding the cloned element to
     * the corresponding collection in the ExecutablePetriNet:
     * <ul>
     * <li>tokens
     * <li>annotations
     * <li>places
     * <li>rateParameters
     * <li>transitions
     * <li>inboundArcs
     * <li>outboundArcs
     * </ul>
     * <p>
     * As each element is cloned, it is assigned an ID that is unique in the ExecutablePetriNet, using the prefix logic of {@link IncludeHierarchy}
     * </p><p>
     * Each {@link Place} in the source {@link PetriNet} will listen for changes to the token counts in the corresponding Place in the refreshed ExecutablePetriNet.
     * </p>
     * @param targetExecutablePetriNet to be refreshed
     * @throws IncludeException for problems processing the IncludeHierarchy
     */
    public static void refreshFromIncludeHierarchy(ExecutablePetriNet targetExecutablePetriNet)
            throws IncludeException {
        cloneInstance = new ExecutablePetriNetCloner(targetExecutablePetriNet);
        cloneInstance.clonePetriNetToExecutablePetriNet();
    }

    /**
     * private constructor
     * @param targetExecutablePetriNet to be refreshed from the PetriNets of its IncludeHierarchy
     * @return  cloned Petri net
     */
    private ExecutablePetriNetCloner(ExecutablePetriNet targetExecutablePetriNet) {
        this.newPetriNet = targetExecutablePetriNet;
        this.includeHierarchy = targetExecutablePetriNet.getIncludeHierarchy();
        this.refreshingExecutablePetriNet = true;
    }

    /**
     * Refreshes the target ExecutablePetriNet by re-initializing its collections,
     * then visiting the components of each PetriNet in its IncludeHierarchy,
     * modifying each new component as controlled by {@link #refreshingExecutablePetriNet},
     * and adding each component to the new collection in the ExecutablePetriNet.
     * @throws IncludeException for problems processing the IncludeHierarchy
     */
    private void clonePetriNetToExecutablePetriNet() throws IncludeException {
        buildPendingAwayPlacesForInterfacePlaceConversion();
        IncludeIterator iterator = includeHierarchy.iterator();
        currentIncludeHierarchy = null;
        while (iterator.hasNext()) {
            currentIncludeHierarchy = iterator.next();
            this.petriNet = currentIncludeHierarchy.getPetriNet();
            visitAllComponents();
        }
        replaceInterfacePlacesWithOriginalPlaces();
    }

    @Override
    protected void prepareExecutablePetriNetPlaceProcessing(Place place, Place newPlace) {
        newPlace.getStatus().prefixIdWithQualifiedName(currentIncludeHierarchy);
        newPlace.getStatus().updateHomePlace(pendingNewHomePlaces);
        updatePendingAwayPlacesWithNewPlace(place, newPlace);
        updatePendingPlacesToDelete(place, newPlace);
    }

    @Override
    protected void addPlaceToNet(Place place, Place newPlace) {
        super.addPlaceToNet(place, newPlace);
        newPetriNet.getPlaceCloneMap().put(place, newPlace);
    }

    private void buildPendingAwayPlacesForInterfacePlaceConversion() {
        IncludeIterator iterator = includeHierarchy.iterator();
        IncludeHierarchy include = null;
        AbstractPetriNet net = null;
        while (iterator.hasNext()) {
            include = iterator.next();
            net = include.getPetriNet();
            for (Place place : net.getPlaces()) {
                if (place.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAway) {
                    pendingAwayPlaces.put(place.getStatus().getMergeInterfaceStatus().getAwayId(), place); // a.P1 / homePlace P1
                }
            }
        }
    }

    protected void updatePendingPlacesToDelete(Place place, Place newPlace) {
        if (place.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAway) {
            pendingPlacesToDelete.add(newPlace);
        }
    }

    private void updatePendingAwayPlacesWithNewPlace(Place place, Place newPlace) {
        for (Entry<String, Place> entry : pendingAwayPlaces.entrySet()) {
            if (entry.getValue().equals(place)) {
                pendingAwayPlaces.put(entry.getKey(), newPlace);
            }
        }
    }

    /**
     * Create a unique name for the {@link PetriNetComponent} by prefixing it with the
     * fully qualified name from the {@link IncludeHierarchy} being currently processed.
     * @param component to be prefixed
     */
    @Override
    protected void prefixIdWithQualifiedName(PetriNetComponent component) {
        currentIncludeHierarchy.prefixComponentIdWithQualifiedName(component);
    }

    private void replaceInterfacePlacesWithOriginalPlaces() throws IncludeException {
        convertAwayPlaceArcsToUseOriginalPlaces();
        Map<String, Place> newPlaceMap = newPetriNet.getMapForClass(Place.class);
        for (Place place : pendingPlacesToDelete) {
            place.removeSelfFromListeners();
            newPlaceMap.remove(place.getId());
        }
    }

    //TODO test suggests entry key may sometimes be null; figure out why.
    protected void convertAwayPlaceArcsToUseOriginalPlaces() throws IncludeException {
        Place newPlace = null;
        for (Entry<String, Place> entry : pendingAwayPlaces.entrySet()) {
            if (!(entry.getValue().getStatus().getMergeInterfaceStatus() instanceof NoOpInterfaceStatus)) {
                newPlace = pendingNewHomePlaces.get(entry.getKey());
                if (newPlace == null) {
                    throw new IncludeException(
                            "Away place " + entry.getValue().getId() + " does not have a corresponding Home place.  " +
                                    "Possible causes:\n" +
                                    "  petri net opened standalone, outside of its include hierarchy\n" +
                                    "  missing 'merge type=\"home\"' entry in the PNML for the place in its home petri net");
                }
                newPetriNet.convertArcsToUseNewPlace(entry.getValue(), newPlace);
            }
        }

    }

    protected Map<String, Place> getPendingAwayPlacesForInterfacePlaceConversion() {
        return pendingAwayPlaces;
    }

    protected Map<String, Place> getPendingNewHomePlaces() {
        return pendingNewHomePlaces;
    }

    @Override
    protected ExecutablePetriNetCloner getInstance() {
        return cloneInstance;
    }

    @Override
    protected AbstractPetriNet getNewPetriNet() {
        return newPetriNet;
    }
}
