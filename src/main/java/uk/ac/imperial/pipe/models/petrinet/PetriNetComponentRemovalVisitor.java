package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.exceptions.InvalidRateException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;

public final class PetriNetComponentRemovalVisitor
        implements PlaceVisitor, TransitionVisitor, ArcVisitor, TokenVisitor, AnnotationVisitor, RateParameterVisitor {
    private final PetriNet net;

    public PetriNetComponentRemovalVisitor(PetriNet net) {
        this.net = net;
    }

    @Override
    public void visit(Place place) {
        net.removePlace(place);

    }

    @Override
    public void visit(Transition transition) {
        net.removeTransition(transition);

    }

    @Override
    public void visit(Token token) throws PetriNetComponentException {
        net.removeToken(token);
    }

    @Override
    public void visit(Annotation annotation) {
        net.removeAnnotation(annotation);
    }

    @Override
    public void visit(FunctionalRateParameter rate) throws InvalidRateException {
        net.removeRateParameter(rate);
    }

    @Override
    public void visit(InboundArc inboundArc) {
        net.removeArc(inboundArc);
    }

    @Override
    public void visit(OutboundArc outboundArc) {
        net.removeArc(outboundArc);
    }
}
