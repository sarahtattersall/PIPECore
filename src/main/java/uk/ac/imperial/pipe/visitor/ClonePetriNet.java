package uk.ac.imperial.pipe.visitor;

import uk.ac.imperial.pipe.exceptions.InvalidRateException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.*;
import uk.ac.imperial.pipe.models.petrinet.name.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for cloning exactly a Petri net
 */
public final class ClonePetriNet {
    /**
     * Class logger
     */
    private static final Logger LOGGER = Logger.getLogger(ClonePetriNet.class.getName());

    /**
     * Original Petri net to clone
     */
    private final PetriNet petriNet;

    /**
     * Closed Petri net
     */
    private final PetriNet newPetriNet;

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
     * private constructor
     * @param petriNet petri net to clone
     */
    private ClonePetriNet(PetriNet petriNet) {
        this.petriNet = petriNet;
        newPetriNet = new PetriNet();
    }

    /**
     *
     * @param petriNet to be cloned 
     * @return  cloned Petri net
     */
    public static PetriNet clone(PetriNet petriNet) {
        ClonePetriNet clone = new ClonePetriNet(petriNet);
        return clone.clonePetriNet();
    }

    /**
     *
     * Clones the petri net by visiting all its components and adding them to the new Petri net
     *
     * @return cloned Petri net
     */
    private PetriNet clonePetriNet() {
        visit(petriNet.getName());

        for (Token token : petriNet.getTokens()) {
            visit(token);
        }

        for (Annotation annotation : petriNet.getAnnotations()) {
            visit(annotation);
        }

        for (Place place : petriNet.getPlaces()) {
            visit(place);
        }

        for (RateParameter rateParameter : petriNet.getRateParameters()) {
        	visit(rateParameter);
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
        return newPetriNet;
    }

    /**
     * Clone a Petri net name
     * @param name of the Petri net 
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
        for (Map.Entry<String, Integer> entry : place.getTokenCounts().entrySet()) {
            newPlace.setTokenCount(entry.getKey(), entry.getValue());
        }
        newPetriNet.addPlace(newPlace);
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
        newPetriNet.addArc(newArc);
    }

    /**
     * Used to clone a name into the new Petri net
     */
    private class NameCloner implements NormalNameVisitor, FileNameVisitor {

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

}
