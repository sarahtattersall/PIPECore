package uk.ac.imperial.pipe.models.component.arc;

import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

public interface ArcVisitor extends PetriNetComponentVisitor {
    void visit (InboundArc inboundArc);
    void visit (OutboundArc outboundArc);
}
