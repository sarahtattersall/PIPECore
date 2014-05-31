package uk.ac.imperial.pipe.visitor;

import uk.ac.imperial.pipe.exceptions.InvalidRateException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.*;
import uk.ac.imperial.pipe.models.petrinet.name.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClonePetriNet {
    private final PetriNet petriNet;
    private final PetriNet newPetriNet;
    private final Map<String, RateParameter> rateParameters = new HashMap<>();
    private final Map<String, Place> places = new HashMap<>();
    private final Map<String, Transition> transitions = new HashMap<>();

    private ClonePetriNet (PetriNet petriNet) {
        this.petriNet = petriNet;
        newPetriNet = new PetriNet();
    }

    private PetriNet clonePetriNet() {
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
        return newPetriNet;
    }

    private void visit(PetriNetName name) {
        if (name != null) {
            name.visit(new NameCloner());
        }
    }

    /**
     * Used to clone a name into the new Petri net
     */
    private class NameCloner implements NormalNameVisitor, FileNameVisitor {

        @Override
        public void visit(PetriNetFileName name) {
            newPetriNet.setName(new PetriNetFileName(name.getFile()));
        }

        @Override
        public void visit(NormalPetriNetName name) {
            newPetriNet.setName(new NormalPetriNetName(name.getName()));
        }
    }

    public static PetriNet clone(PetriNet petriNet) {
        ClonePetriNet clone = new ClonePetriNet(petriNet);
        return clone.clonePetriNet();
    }

    public void visit(Annotation annotation) {
        AnnotationCloner cloner = new AnnotationCloner();
        try {
            annotation.accept(cloner);
        } catch (PetriNetComponentException e) {
            e.printStackTrace();
        }
        Annotation newAnnotation = cloner.cloned;
        newPetriNet.addAnnotation(newAnnotation);

    }

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
        for (int i = 1; i < arcPoints.size() -1; i++) {
            newArc.addIntermediatePoint(arcPoints.get(i));
        }
        newArc.setId(arc.getId());
        newPetriNet.addArc(newArc);
    }


    public void visit(OutboundArc arc) {
        Place target = places.get(arc.getTarget().getId());
        Transition source = transitions.get(arc.getSource().getId());

        OutboundArc newArc = new OutboundNormalArc(source, target, arc.getTokenWeights());
        List<ArcPoint> arcPoints = arc.getArcPoints();
        for (int i = 1; i < arcPoints.size() -1; i++) {
            newArc.addIntermediatePoint(arcPoints.get(i));
        }
        newArc.setId(arc.getId());
        newPetriNet.addArc(newArc);
    }

    public void visit(Place place) {
        PlaceCloner cloner = new PlaceCloner();
        try {
            place.accept(cloner);
        } catch (PetriNetComponentException e) {
            e.printStackTrace();
        }
        Place newPlace = cloner.cloned;
        for (Map.Entry<String, Integer> entry : place.getTokenCounts().entrySet()) {
            newPlace.setTokenCount(entry.getKey(), entry.getValue());
        }
        newPetriNet.addPlace(newPlace);
        places.put(place.getId(), newPlace);
    }

    public void visit(RateParameter rate) {
        RateParameterCloner cloner = new RateParameterCloner();
        try {
            rate.accept(cloner);
        } catch (PetriNetComponentException e) {
            e.printStackTrace();
        }
        RateParameter rateParameter = cloner.cloned;
        try {
            newPetriNet.addRateParameter(rateParameter);
            rateParameters.put(rateParameter.getId(), rateParameter);
        } catch (InvalidRateException ignored) {
        }

    }

    public void visit(Token token) {
        Token newToken = new ColoredToken(token);
        newPetriNet.addToken(newToken);
    }

    public void visit(Transition transition) {
        TransitionCloner cloner = new TransitionCloner();
        try {
            transition.accept(cloner);
        } catch (PetriNetComponentException e) {
            e.printStackTrace();
        }
        Transition newTransition = cloner.cloned;
        if (transition.getRate().getRateType().equals(RateType.RATE_PARAMETER)) {
            FunctionalRateParameter rateParameter = (FunctionalRateParameter) transition.getRate();
            newTransition.setRate(rateParameters.get(rateParameter.getId()));
        }
        transitions.put(transition.getId(), newTransition);
        newPetriNet.addTransition(newTransition);
    }

}
