package uk.ac.imperial.pipe.visitor;

import uk.ac.imperial.pipe.models.petrinet.DiscretePlace;
import uk.ac.imperial.pipe.models.petrinet.DiscretePlaceVisitor;
import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatus;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatusAvailable;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatusAway;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatusHome;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.PlaceStatus;
import uk.ac.imperial.pipe.models.petrinet.PlaceStatusInterface;

/**
 * Builds a place by visiting the place and calling the correct
 * constructor for each concrete implementation of {@link uk.ac.imperial.pipe.models.petrinet.Place}
 * Some constructors modify an existing Place, rather than build a new one.  
 */
public final class PlaceBuilder implements DiscretePlaceVisitor {
    /**
     * Built place, null before visit is called
     */
    public Place built = null;
	private IncludeHierarchy includeHierarchy;
	private Build build;
	private AbstractClonePetriNet cloneInstance;
	private PlaceStatus placeStatus; 

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
    public PlaceBuilder(PlaceStatus placeStatus) {
    	this.placeStatus = placeStatus; 
    	build = Build.AWAY; 
    }

    public PlaceBuilder() {
    	build = Build.SIMPLE; 
	}
    
    
    
	public <T extends AbstractClonePetriNet> PlaceBuilder(T  cloneInstance) {
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
		case AWAY: convertAvailableToAway(discretePlace); break;
		case CLONE:  buildClone(discretePlace); break;
		case SIMPLE: buildSimple(discretePlace);	break;
		}
    }
	private void buildAvailable(DiscretePlace discretePlace) {
    	built = new DiscretePlace(discretePlace, false);
    	built.getStatus().setIncludeHierarchy(includeHierarchy);
    	built.getStatus().setExternal(false); 
		MergeInterfaceStatus mergeStatus = new MergeInterfaceStatusAvailable(
				discretePlace, built.getStatus(),  
				built.getStatus().getMergeInterfaceStatus().getAwayId());  
		built.getStatus().setMergeInterfaceStatus(mergeStatus); 
		buildArcConstraints(discretePlace, built.getStatus());
		built.setId(mergeStatus.getAwayId()); 
		addEachPlaceAsListenerForTokenChanges(discretePlace); 
	}
	private void convertAvailableToAway(DiscretePlace homePlace) {
		MergeInterfaceStatus mergeStatus = new MergeInterfaceStatusAway(homePlace, placeStatus, 
				homePlace.getStatus().getMergeInterfaceStatus().getAwayId());  
	    placeStatus.setMergeInterfaceStatus(mergeStatus); 
	    buildArcConstraints(homePlace, placeStatus);
	}


	private void buildArcConstraints(Place discretePlace, PlaceStatus placeStatus) {
		if (discretePlace.getStatus().isInputOnlyArcConstraint()) { 
			placeStatus.setInputOnlyArcConstraint(true); 
			((PlaceStatusInterface) placeStatus).buildInputOnlyArcConstraint();  
		}
		else if (discretePlace.getStatus().isOutputOnlyArcConstraint()) {
			placeStatus.setOutputOnlyArcConstraint(true); 
			((PlaceStatusInterface) placeStatus).buildOutputOnlyArcConstraint();  
		}
	}
    private void buildClone(DiscretePlace discretePlace) {
    	built = new DiscretePlace(discretePlace, true);
    	addEachPlaceAsListenerForTokenChanges(discretePlace); 
	    cloneInstance.prepareExecutablePetriNetPlaceProcessing(discretePlace, built); 
	    cloneInstance.updatePlace(discretePlace, built);
    }

    
    private void buildSimple(DiscretePlace discretePlace) {
    	built = new DiscretePlace(discretePlace, true);
    }
    private void addEachPlaceAsListenerForTokenChanges(DiscretePlace discretePlace) {
    	discretePlace.addPropertyChangeListener(built); 
    	built.addPropertyChangeListener(discretePlace);
    }
	private enum Build {
    	SIMPLE,
    	CLONE,
    	AVAILABLE,
    	AWAY;
    	
    }
}

