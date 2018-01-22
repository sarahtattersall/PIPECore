package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.exceptions.InvalidRateException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;

public final class PetriNetComponentRemovalVisitor
        implements PlaceVisitor, TransitionVisitor, ArcVisitor, TokenVisitor, AnnotationVisitor, RateParameterVisitor {
    /**
     * Petri net to remove components from
     */
    private final PetriNet net;

    /**
     *
     * @param net Petri net to remove components from
     */
    public PetriNetComponentRemovalVisitor(PetriNet net) {
        this.net = net;
    }

    /**
     * Remove the place from the Petri net
     * @param place to be removed
     * @throws PetriNetComponentException if place does not exist in the Petri net 
     */
    @Override
    public void visit(Place place) throws PetriNetComponentException {
        net.removePlace(place);

    }

    /**
     * Remove the transition from the Petri net
     * @param transition to be removed
     */
    @Override
    public void visit(Transition transition) {
        net.removeTransition(transition);

    }

    /**
     * Remove the token from the Petri net
     * @param token to be removed
     * @throws PetriNetComponentException if component not found 
     */
    @Override
    public void visit(Token token) throws PetriNetComponentException {
        net.removeToken(token);
    }

    /**
     * Remove the annotation from the Petri net
     * @param annotation to be removed
     */
    @Override
    public void visit(Annotation annotation) {
        net.removeAnnotation(annotation);
    }

    /**
     * Remove the functional rate paramter from the Petri net
     * @param rate to be removed
     * @throws InvalidRateException if rate is invalid
     */
    @Override
    public void visit(FunctionalRateParameter rate) throws InvalidRateException {
        net.removeRateParameter(rate);
    }

    /**
     * Remove the inbound arc from the Petri net
     * @param inboundArc to be removed
     */
    @Override
    public void visit(InboundArc inboundArc) {
        net.removeArc(inboundArc);
    }

    /**
     * Remove the outbound arc from the Petri net
     *
     * @param outboundArc to be removed
     */
    @Override
    public void visit(OutboundArc outboundArc) {
        net.removeArc(outboundArc);
    }
}
