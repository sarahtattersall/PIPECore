package uk.ac.imperial.pipe.models.component.arc;

import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

public interface ArcPointVisitor extends PetriNetComponentVisitor {
    void visit(ArcPoint arcPoint);
}
