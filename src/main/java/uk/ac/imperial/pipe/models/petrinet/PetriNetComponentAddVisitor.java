package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.exceptions.InvalidRateException;

/**
 * Adds a component to the petri net by implementing the visitor design pattern
 */
public final class PetriNetComponentAddVisitor
        implements PlaceVisitor, ArcVisitor, TransitionVisitor, TokenVisitor, AnnotationVisitor, RateParameterVisitor {
    private final PetriNet petriNet;

    /**
     *
     * @param petriNet petri net to add components to
     */
    public PetriNetComponentAddVisitor(PetriNet petriNet) {
        this.petriNet = petriNet;
    }

    /**
     * Add a place to the Petri net
     * @param place to be added
     */
    @Override
    public void visit(Place place) {
        petriNet.addPlace(place);
    }

    /**
     * Add a transition to the Petri net
     * @param transition to be added
     */
    @Override
    public void visit(Transition transition) {
        petriNet.addTransition(transition);
    }

    /**
     *
     * Adds the token to the Petri net
     * @param token to be added 
     */
    @Override
    public void visit(Token token) {
        petriNet.addToken(token);
    }

    /**
     * Adds the annotation to the Petri net
     * @param annotation to be added 
     */
    @Override
    public void visit(Annotation annotation) {
        petriNet.addAnnotation(annotation);
    }

    /**
     * Adds the rate paramter to the Petri net
     * @param rate to be added 
     * @throws InvalidRateException if the rate cannot be added 
     */
    @Override
    public void visit(FunctionalRateParameter rate) throws InvalidRateException {
        petriNet.addRateParameter(rate);
    }

    /**
     * Adds the inbound arc to the Petri net
     * @param inboundArc to be added
     */
    @Override
    public void visit(InboundArc inboundArc) {
        petriNet.addArc(inboundArc);
    }

    /**
     * Adds the outbound arc to the Petri net
     * @param outboundArc to be added
     */
    @Override
    public void visit(OutboundArc outboundArc) {
        petriNet.addArc(outboundArc);
    }
}
