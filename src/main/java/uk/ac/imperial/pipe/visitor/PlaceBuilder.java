package uk.ac.imperial.pipe.visitor;

import uk.ac.imperial.pipe.models.petrinet.DiscretePlace;
import uk.ac.imperial.pipe.models.petrinet.DiscretePlaceVisitor;
import uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet;
import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatus;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatusAvailable;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatusHome;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.PlaceStatusInterface;

/**
 * Clones a place by visiting the place and calling the correct
 * constructor for each concrete implementation of {@link uk.ac.imperial.pipe.models.petrinet.Place}
 */
public final class PlaceBuilder implements DiscretePlaceVisitor {
    /**
     * Cloned place, null before visit is called
     */
    public Place cloned = null;
	private IncludeHierarchy includeHierarchy;
	private Build build;
	private ExecutablePetriNet executablePetriNet;
	private CloneExecutablePetriNet cloneInstance; 

    /**
     * Constructor to build a Place where Place.getStatus().getMergeInterfaceStatus()
     * is {@link MergeInterfaceStatusAvailable}, based on a place that has {@link MergeInterfaceStatusHome}
     * @param homePlace  home place to clone 
     * @param includeHierarchy  where this place will be Available
     */
    public PlaceBuilder(IncludeHierarchy includeHierarchy) {
    	this.includeHierarchy = includeHierarchy; 
    	build = Build.AVAILABLE; 
	}

    public PlaceBuilder() {
    	build = Build.SIMPLE; 
	}
    
	public PlaceBuilder(ExecutablePetriNet executablePetriNet, CloneExecutablePetriNet cloneInstance) {
		this.executablePetriNet = executablePetriNet; 
		this.cloneInstance = cloneInstance;
		build = Build.CLONE; 
	}

	/**
     * Clones a discrete place
     * @param discretePlace to be visited 
     */
    @Override
    public void visit(DiscretePlace discretePlace) {
    	switch (build) {
		case AVAILABLE: buildAvailable(discretePlace); break;
		case CLONE:  buildClone(discretePlace); break;
		case SIMPLE: buildSimple(discretePlace);	break;
		}
    }
    private void buildAvailable(DiscretePlace discretePlace) {
    	cloned = new DiscretePlace(discretePlace, false);
    	cloned.getStatus().setIncludeHierarchy(includeHierarchy);
		MergeInterfaceStatus mergeStatus = new MergeInterfaceStatusAvailable(
				discretePlace, cloned.getStatus(),  
				cloned.getStatus().getMergeInterfaceStatus().getAwayId());  
		cloned.getStatus().setMergeInterfaceStatus(mergeStatus); 
		cloned.getStatus().setExternal(false); 
		if (discretePlace.getStatus().isInputOnlyArcConstraint()) { 
			cloned.getStatus().setInputOnlyArcConstraint(true); 
			((PlaceStatusInterface) cloned.getStatus()).buildInputOnlyArcConstraint();  
		}
		else if (discretePlace.getStatus().isOutputOnlyArcConstraint()) {
			cloned.getStatus().setOutputOnlyArcConstraint(true); 
			((PlaceStatusInterface) cloned.getStatus()).buildOutputOnlyArcConstraint();  
		}
		cloned.setId(mergeStatus.getAwayId()); 
		addEachPlaceAsListenerForTokenChanges(discretePlace); 
	}

    private void buildClone(DiscretePlace discretePlace) {
    	cloned = new DiscretePlace(discretePlace, true);
    	addEachPlaceAsListenerForTokenChanges(discretePlace); 
	    cloneInstance.prepareExecutablePetriNetPlaceProcessing(discretePlace, cloned); 
	    cloneInstance.updatePlace(discretePlace, cloned);
    }

    
    private void buildSimple(DiscretePlace discretePlace) {
    	cloned = new DiscretePlace(discretePlace, true);
    }
    private void addEachPlaceAsListenerForTokenChanges(DiscretePlace discretePlace) {
    	discretePlace.addPropertyChangeListener(cloned); 
    	cloned.addPropertyChangeListener(discretePlace);
    }
	private enum Build {
    	SIMPLE,
    	CLONE,
    	AVAILABLE;
    	
    }
}
//listenForTokenCountChanges(newPlace);

