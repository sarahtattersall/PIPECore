package uk.ac.imperial.pipe.visitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.imperial.pipe.exceptions.InvalidRateException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.AbstractPetriNet;
import uk.ac.imperial.pipe.models.petrinet.Annotation;
import uk.ac.imperial.pipe.models.petrinet.ArcPoint;
import uk.ac.imperial.pipe.models.petrinet.ColoredToken;
import uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet;
import uk.ac.imperial.pipe.models.petrinet.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.petrinet.InboundArc;
import uk.ac.imperial.pipe.models.petrinet.InboundInhibitorArc;
import uk.ac.imperial.pipe.models.petrinet.InboundNormalArc;
import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.IncludeIterator;
import uk.ac.imperial.pipe.models.petrinet.OutboundArc;
import uk.ac.imperial.pipe.models.petrinet.OutboundNormalArc;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.PetriNetComponent;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.RateParameter;
import uk.ac.imperial.pipe.models.petrinet.RateType;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.pipe.models.petrinet.name.FileNameVisitor;
import uk.ac.imperial.pipe.models.petrinet.name.NormalNameVisitor;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;
import uk.ac.imperial.pipe.models.petrinet.name.PetriNetFileName;
import uk.ac.imperial.pipe.models.petrinet.name.PetriNetName;

/**
 * Class for cloning exactly a Petri net, or for refreshing an existing {@link ExecutablePetriNet} from the Petri nets of its {@link IncludeHierarchy} 
 */
public final class ClonePetriNet {
    /**
     * Class logger
     */
    private static final Logger LOGGER = Logger.getLogger(ClonePetriNet.class.getName());

    /**
     * Original Petri net to clone
     */
    private  PetriNet petriNet;

    /**
     * Cloned Petri net 
     */
    private  AbstractPetriNet newPetriNet;

    /**
     * cloned rate parameters
     */
    private final Map<String, RateParameter> rateParameters = new HashMap<>();

    /**
     * cloned places
     */
    private final Map<String, Place> places = new HashMap<>();

    /**
     * cloned transitions
     */
    private final Map<String, Transition> transitions = new HashMap<>();
    /**
     * as components are visited, some modifications are required when refreshing an {@link ExecutablePetriNet}:
     */
	private boolean refreshingExecutablePetriNet = false;
	/**
	 * The {@link IncludeHierarchy} of a target {@link ExecutablePetriNet} during {@link ClonePetriNet#refreshFromIncludeHierarchy(ExecutablePetriNet)}
	 */
	private IncludeHierarchy includeHierarchy;

	/**
	 * The {@link IncludeHierarchy} of the {@link PetriNet} currently being processed during {@link ClonePetriNet#refreshFromIncludeHierarchy(ExecutablePetriNet)}
	 */
	private IncludeHierarchy currentIncludeHierarchy;

	/**
	 *
	 * @param petriNet
	 * @return  cloned Petri net
	 */
	public static PetriNet clone(PetriNet petriNet) {
		ClonePetriNet clone = new ClonePetriNet(petriNet);
		return clone.clonePetriNet();
	}
	/**
	 * Rebuilds an {@link ExecutablePetriNet} from the set of {@link PetriNet} defined in its {@link IncludeHierarchy}.
	 * The following collections are refreshed, by cloning each element in the PetriNet collection, and adding the cloned element to 
	 * the corresponding collection in the ExecutablePetriNet:  
	 * <ul>
	 * <li>tokens
	 * <li>rateParameters
	 * <li>annotations
	 * <li>places
	 * <li>transitions
	 * <li>inboundArcs
	 * <li>outboundArcs
	 * </ul>
	 * <p>
	 * As each element is cloned, it is assigned an ID that is unique in the ExecutablePetriNet, using the prefix logic of {@link IncludeHierarchy}
	 * <p>
	 * Each {@link Place} in the source {@link PetriNet} will listen for changes to the token counts in the corresponding Place in the refreshed ExecutablePetriNet.
	 * @param targetExecutablePetriNet
	 */
	public static void refreshFromIncludeHierarchy(ExecutablePetriNet targetExecutablePetriNet) {
		ClonePetriNet clone = new ClonePetriNet(targetExecutablePetriNet);
		clone.clonePetriNetToExecutablePetriNet();
	}
    /**
     * private constructor
     * @param petriNet petri net to clone
     */
    private ClonePetriNet(PetriNet petriNet) {
        this.petriNet = petriNet;
        newPetriNet = new PetriNet();
    }
    /**
     * private constructor 
     * @param targetExecutablePetriNet to be refreshed from the PetriNets of its IncludeHierarchy
     */
    private ClonePetriNet(ExecutablePetriNet targetExecutablePetriNet) {
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
    	IncludeIterator iterator = includeHierarchy.iterator(); 
    	currentIncludeHierarchy = null; 
    	while (iterator.hasNext()) {
    		currentIncludeHierarchy = iterator.next();  
    		this.petriNet = currentIncludeHierarchy.getPetriNet(); 
    		visitAllComponents();
    	}
	}
	/**
     *
     * Clones the petri net by visiting all its components and adding them to the new Petri net
     *
     * @return cloned Petri net
     */
    private PetriNet clonePetriNet() {
        visitAllComponents();
        return (PetriNet) newPetriNet;   
    }
    /**
     * Visit the components of the source PetriNet, and add a cloned version of the component, (possibly with modifications), 
     * to the new or refreshed AbstractPetriNet. 
     */
	private void visitAllComponents() {
		visit(petriNet.getName());

        for (Token token : petriNet.getTokens()) {
            visit(token);
        }

        for (RateParameter rateParameter : petriNet.getRateParameters()) {
            visit(rateParameter);
        }

        for (Annotation annotation : petriNet.getAnnotations()) {
            visit(annotation);
        }

        for (Place place : petriNet.getPlaces()) {
            visit(place);
        }

        for (Transition transition : petriNet.getTransitions()) {
            visit(transition);
        }

        for (InboundArc arc : petriNet.getInboundArcs()) {
            visit(arc);
        }

        for (OutboundArc arc : petriNet.getOutboundArcs()) {
            visit(arc);
        }
	}

    /**
     * Clone a Petri net name
     * @param name
     */
    private void visit(PetriNetName name) {
        if (name != null) {
            name.visit(new NameCloner());
        }
    }

    /**
     * Clones and adds a token to the new Petri net
     * @param token original token
     */
    public void visit(Token token) {
        Token newToken = new ColoredToken(token);
        newPetriNet.addToken(newToken);
    }

    /**
     * Clones and adds a rate parameter to the new Petri net
     * @param rate original rate
     */
    public void visit(RateParameter rate) {
        RateParameterCloner cloner = new RateParameterCloner();
        try {
            rate.accept(cloner);
        } catch (PetriNetComponentException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
        RateParameter rateParameter = cloner.cloned;
        try {
            newPetriNet.addRateParameter(rateParameter);
            rateParameters.put(rateParameter.getId(), rateParameter);
        } catch (InvalidRateException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }

    }

    /**
     * Clones and adds an annotation to the new Petri net
     * @param annotation original annotation
     */
    public void visit(Annotation annotation) {
        AnnotationCloner cloner = new AnnotationCloner();
        try {
            annotation.accept(cloner);
        } catch (PetriNetComponentException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
        Annotation newAnnotation = cloner.cloned;
        if (refreshingExecutablePetriNet) prefixIdWithQualifiedName(newAnnotation); 
        newPetriNet.addAnnotation(newAnnotation);

    }

    /**
     * Clones and adds the new place to the new Petri net
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
        if (refreshingExecutablePetriNet) prefixIdWithQualifiedName(newPlace); 
        for (Map.Entry<String, Integer> entry : place.getTokenCounts().entrySet()) {
            newPlace.setTokenCount(entry.getKey(), entry.getValue());
        }
        newPetriNet.addPlace(newPlace);
        if (refreshingExecutablePetriNet) newPlace.addPropertyChangeListener(place); 
        places.put(place.getId(), newPlace);
    }

    /**
     * Clones and adds the new transition to the new Petri net
     * @param transition original transition
     */
    public void visit(Transition transition) {
        TransitionCloner cloner = new TransitionCloner();
        try {
            transition.accept(cloner);
        } catch (PetriNetComponentException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
        Transition newTransition = cloner.cloned;
        if (refreshingExecutablePetriNet) prefixIdWithQualifiedName(newTransition); 
        if (transition.getRate().getRateType().equals(RateType.RATE_PARAMETER)) {
            FunctionalRateParameter rateParameter = (FunctionalRateParameter) transition.getRate();
            newTransition.setRate(rateParameters.get(rateParameter.getId()));
        }
        transitions.put(transition.getId(), newTransition);
        newPetriNet.addTransition(newTransition);
    }

    /**
     * Clones and adds the new inbound arc to the new Petri net
     * @param arc original inbound arc
     */
    public void visit(InboundArc arc) {
        Place source = places.get(arc.getSource().getId());
        Transition target = transitions.get(arc.getTarget().getId());
        InboundArc newArc;
        switch (arc.getType()) {
            case INHIBITOR:
                newArc = new InboundInhibitorArc(source, target);
                break;
            default:
                newArc = new InboundNormalArc(source, target, arc.getTokenWeights());
        }
        List<ArcPoint> arcPoints = arc.getArcPoints();
        for (int i = 1; i < arcPoints.size() - 1; i++) {
            newArc.addIntermediatePoint(arcPoints.get(i));
        }
        newArc.setId(arc.getId());
        if (refreshingExecutablePetriNet) prefixIdWithQualifiedName(newArc); 
        newPetriNet.addArc(newArc);
    }

    /**
     * Clones and adds the new outbound arc to the new Petri net
     * @param arc original outbound arc
     */
    public void visit(OutboundArc arc) {
        Place target = places.get(arc.getTarget().getId());
        Transition source = transitions.get(arc.getSource().getId());

        OutboundArc newArc = new OutboundNormalArc(source, target, arc.getTokenWeights());
        List<ArcPoint> arcPoints = arc.getArcPoints();
        for (int i = 1; i < arcPoints.size() - 1; i++) {
            newArc.addIntermediatePoint(arcPoints.get(i));
        }
        newArc.setId(arc.getId());
        if (refreshingExecutablePetriNet) prefixIdWithQualifiedName(newArc); 
        newPetriNet.addArc(newArc);
    }
    /**
     * Create a unique name for the {@link PetriNetComponent} by prefixing it with the 
     * fully qualified name from the {@link IncludeHierarchy} being currently processed.  
     * <p>
     * This method is used as part of the process of refreshing an {@link ExecutablePetriNet}:  {@link #refreshFromIncludeHierarchy(ExecutablePetriNet)}
     * @param component
     */
    private void prefixIdWithQualifiedName(PetriNetComponent component) {
    	component.setId(currentIncludeHierarchy.
    			getFullyQualifiedNameAsPrefix()+component.getId());
    }

    /**
     * Used to clone a name into the new Petri net
     */
    private class NameCloner implements NormalNameVisitor, FileNameVisitor {

        /**
         * Clones a PetriNetFileName
         * @param name
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
}
