package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.exceptions.InvalidRateException;

public class PetriNetComponentAddVisitor
        implements PlaceVisitor, ArcVisitor, TransitionVisitor, TokenVisitor, AnnotationVisitor, RateParameterVisitor {
    private final PetriNet petriNet;

    public PetriNetComponentAddVisitor(PetriNet petriNet) {
        this.petriNet = petriNet;
    }

    @Override
    public void visit(Place place) {
        petriNet.addPlace(place);
    }

    @Override
    public void visit(Transition transition) {
        petriNet.addTransition(transition);
    }

    @Override
    public void visit(Token token) {
        petriNet.addToken(token);
    }

    @Override
    public void visit(Annotation annotation) {
        petriNet.addAnnotation(annotation);
    }

    @Override
    public void visit(FunctionalRateParameter rate) throws InvalidRateException {
        petriNet.addRateParameter(rate);
    }

    @Override
    public void visit(InboundArc inboundArc) {
        petriNet.addArc(inboundArc);
    }

    @Override
    public void visit(OutboundArc outboundArc) {
        petriNet.addArc(outboundArc);
    }
}
