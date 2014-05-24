package uk.ac.imperial.pipe.visitor;

import uk.ac.imperial.pipe.exceptions.InvalidRateException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.component.annotation.Annotation;
import uk.ac.imperial.pipe.models.component.arc.*;
import uk.ac.imperial.pipe.models.component.place.Place;
import uk.ac.imperial.pipe.models.component.rate.RateParameter;
import uk.ac.imperial.pipe.models.component.rate.RateType;
import uk.ac.imperial.pipe.models.component.token.Token;
import uk.ac.imperial.pipe.models.component.transition.Transition;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
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
        Annotation newAnnotation =  new Annotation(annotation);
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
       RateParameter rateParameter = new RateParameter(rate);
        try {
            newPetriNet.addRateParameter(rateParameter);
            rateParameters.put(rateParameter.getId(), rateParameter);
        } catch (InvalidRateException ignored) {
        }

    }

    public void visit(Token token) {
        Token newToken = new Token(token);
        newPetriNet.addToken(newToken);
    }

    public void visit(Transition transition) {
        Transition newTransition = new Transition(transition);
        if (transition.getRate().getRateType().equals(RateType.RATE_PARAMETER)) {
            RateParameter rateParameter = (RateParameter) transition.getRate();
            newTransition.setRate(rateParameters.get(rateParameter.getId()));
        }
        transitions.put(transition.getId(), newTransition);
        newPetriNet.addTransition(newTransition);
    }

}
