package uk.ac.imperial.pipe.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.AbstractPetriNet;
import uk.ac.imperial.pipe.models.petrinet.Arc;
import uk.ac.imperial.pipe.models.petrinet.ArcPoint;
import uk.ac.imperial.pipe.models.petrinet.Connectable;
import uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet;
import uk.ac.imperial.pipe.models.petrinet.InboundArc;
import uk.ac.imperial.pipe.models.petrinet.InboundInhibitorArc;
import uk.ac.imperial.pipe.models.petrinet.InboundTestArc;
import uk.ac.imperial.pipe.models.petrinet.InboundNormalArc;
import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.IncludeIterator;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatusAway;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatusHome;
import uk.ac.imperial.pipe.models.petrinet.NoOpInterfaceStatus;
import uk.ac.imperial.pipe.models.petrinet.OutboundArc;
import uk.ac.imperial.pipe.models.petrinet.OutboundNormalArc;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.PetriNetComponent;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.pipe.models.petrinet.name.FileNameVisitor;
import uk.ac.imperial.pipe.models.petrinet.name.NormalNameVisitor;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;
import uk.ac.imperial.pipe.models.petrinet.name.PetriNetFileName;

/**
 * Class for cloning exactly a Petri net, or for refreshing an existing {@link ExecutablePetriNet} from the Petri nets of its {@link IncludeHierarchy} 
 */
public final class CloneExecutablePetriNet extends AbstractClonePetriNet {
	
	/**
	 * Class logger
	 */
	private static final Logger LOGGER = Logger.getLogger(CloneExecutablePetriNet.class.getName());

	
    /**
     * as components are visited, some modifications are required when refreshing an {@link ExecutablePetriNet}:
     */
	boolean refreshingExecutablePetriNet = false;
	/**
	 * The {@link IncludeHierarchy} of a target {@link ExecutablePetriNet} during {@link CloneExecutablePetriNet#refreshFromIncludeHierarchy(ExecutablePetriNet)}
	 */
	private IncludeHierarchy includeHierarchy;

	/**
	 * The {@link IncludeHierarchy} of the {@link PetriNet} currently being processed during {@link CloneExecutablePetriNet#refreshFromIncludeHierarchy(ExecutablePetriNet)}
	 */
	private IncludeHierarchy currentIncludeHierarchy;

//	private List<InterfacePlace> interfacePlaces;

	private Map<String, Place> pendingPlaces = new HashMap<>();
	
	

//	private List<InboundArc> convertedInboundArcs;
//
//	private List<OutboundArc> convertedOutboundArcs;

	private List<Place> pendingPlacesToDelete = new ArrayList<>();

	Map<String, Place> pendingNewHomePlaces = new HashMap<>(); 
	
	protected static CloneExecutablePetriNet cloneInstance;

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
	 */
	public static void refreshFromIncludeHierarchy(ExecutablePetriNet targetExecutablePetriNet) {
		cloneInstance = new CloneExecutablePetriNet(targetExecutablePetriNet);
		cloneInstance.clonePetriNetToExecutablePetriNet();
	}
    /**
     * private constructor 
     * @param targetExecutablePetriNet to be refreshed from the PetriNets of its IncludeHierarchy
     * @return  cloned Petri net
     */
    private CloneExecutablePetriNet(ExecutablePetriNet targetExecutablePetriNet) {
		this.newPetriNet = targetExecutablePetriNet; 
		this.includeHierarchy = targetExecutablePetriNet.getIncludeHierarchy(); 
		this.refreshingExecutablePetriNet = true;
	}

    /**
     * Refreshes the target ExecutablePetriNet by re-initializing its collections, 
     * then visiting the components of each PetriNet in its IncludeHierarchy, 
     * modifying each new component as controlled by {@link #refreshingExecutablePetriNet}, 
     * and adding each component to the new collection in the ExecutablePetriNet.   
     */
    private void clonePetriNetToExecutablePetriNet() {
    	buildPendingPlacesForInterfacePlaceConversion(); 
    	IncludeIterator iterator = includeHierarchy.iterator(); 
    	currentIncludeHierarchy = null; 
    	while (iterator.hasNext()) {
    		currentIncludeHierarchy = iterator.next();  
    		this.petriNet = currentIncludeHierarchy.getPetriNet(); 
    		visitAllComponents();
    	}
    	replaceInterfacePlacesWithOriginalPlaces();
	}
	/**
	 * Clones and adds the new place to the new Petri net
	 * 
	 * @param place original place
	 */
	public void visit(Place place) {
	    PlaceCloner cloner = new PlaceCloner();
	    try {
	        place.accept(cloner);
	    } catch (PetriNetComponentException e) {
	        LOGGER.log(Level.SEVERE, e.getMessage());
	    }
	    Place newPlace = cloner.cloned;
	    if (!(place.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAway)) {
	    	prefixIdWithQualifiedName(newPlace); 
	    }
	    for (Map.Entry<String, Integer> entry : place.getTokenCounts().entrySet()) {
	        newPlace.setTokenCount(entry.getKey(), entry.getValue());
	    }
        if (newPlace.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusHome) {
            newPlace.getStatus().getMergeInterfaceStatus().setHomePlace(newPlace); 
            pendingNewHomePlaces.put(newPlace.getStatus().getMergeInterfaceStatus().getAwayId(), newPlace); 
        }
        savePlacesForPostProcessing(place, newPlace);
	    newPetriNet.addPlace(newPlace);
	    newPlace.addPropertyChangeListener(place); 
	    place.addPropertyChangeListener(newPlace); 
	    places.put(place.getId(), newPlace);
	}

	private void buildPendingPlacesForInterfacePlaceConversion() {
		IncludeIterator iterator = includeHierarchy.iterator(); 
		IncludeHierarchy include = null; 
		AbstractPetriNet net = null; 
		while (iterator.hasNext()) {
			include = iterator.next();  
			net = include.getPetriNet(); 
			for (Place place : net.getPlaces()) {
				if (place.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAway) {
					pendingPlaces.put(place.getStatus().getMergeInterfaceStatus().getAwayId(), place ); // a.P1 / homePlace P1
				}
			}
		}
	}
	/**
     *
     * Clones the petri net by visiting all its components and adding them to the new Petri net
     *
     * @return cloned Petri net
     */
    void savePlacesForPostProcessing(Place place, Place newPlace) {
		updatePendingPlaces(place, newPlace); 
		updatePendingPlacesToDelete(place, newPlace);
		newPetriNet.getPlaceCloneMap().put(place, newPlace);
	}
	protected void updatePendingPlacesToDelete(Place place, Place newPlace) {
		if (place.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAway) {
			pendingPlacesToDelete.add(newPlace); 
		}
	}

    private void updatePendingPlaces(Place place, Place newPlace) {
    	for (Entry<String, Place> entry : pendingPlaces.entrySet()) {
    		if (entry.getValue().equals(place)) {
    			pendingPlaces.put(entry.getKey(), newPlace); 
    		}
		}
	}

    /**
     * Create a unique name for the {@link PetriNetComponent} by prefixing it with the 
     * fully qualified name from the {@link IncludeHierarchy} being currently processed.  
     * <p>
     * This method is used as part of the process of refreshing an {@link ExecutablePetriNet}:  {@link #refreshFromIncludeHierarchy(ExecutablePetriNet)}
     * </p>
     * @param component to be prefixed 
     */
    @Override
    protected void prefixIdWithQualifiedName(PetriNetComponent component) {
    	component.setId(currentIncludeHierarchy.
    			getFullyQualifiedNameAsPrefix()+component.getId());
    }
	private void replaceInterfacePlacesWithOriginalPlaces() {
		convertAwayPlaceArcsToUseOriginalPlaces();
		Map<String, Place> newPlaceMap = newPetriNet.getMapForClass(Place.class);  
		for (Place place : pendingPlacesToDelete) {
			newPlaceMap.remove(place.getId());
		}
	}

	private void convertAwayPlaceArcsToUseOriginalPlaces() {
		Place newPlace = null;
		for (Entry<String, Place> entry : pendingPlaces.entrySet()) {
			if (!(entry.getValue().getStatus().getMergeInterfaceStatus() instanceof NoOpInterfaceStatus)) {
				newPlace = pendingNewHomePlaces.get(entry.getKey()); 
				newPetriNet.convertArcsToUseNewPlace(entry.getValue(), newPlace);
			}
		}
		
	}

    /**
     * Used to clone a name into the new Petri net
     */
    class NameCloner implements NormalNameVisitor, FileNameVisitor {

        /**
         * Clones a PetriNetFileName
         * @param name of the Petri net 
         */
        @Override
        public void visit(PetriNetFileName name) {
            newPetriNet.setName(new PetriNetFileName(name.getFile()));
        }

        /**
         * Clones a NormalPetriNetName
         * @param name name to visit
         */
        @Override
        public void visit(NormalPetriNetName name) {
            newPetriNet.setName(new NormalPetriNetName(name.getName()));
        }
    }

	protected Map<String, Place> getPendingPlacesForInterfacePlaceConversion() {
		return pendingPlaces;
	}
	protected static CloneExecutablePetriNet getInstanceForTesting() {
		return cloneInstance;
	}
}
