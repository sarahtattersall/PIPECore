package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

/**
 * Visit inbound and outbound arcs
 */
public interface ArcVisitor extends PetriNetComponentVisitor {
    void visit(InboundArc inboundArc);

    void visit(OutboundArc outboundArc);
}
