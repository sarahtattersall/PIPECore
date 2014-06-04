package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

/**
 * Visits arc points
 */
public interface ArcPointVisitor extends PetriNetComponentVisitor {
    void visit(ArcPoint arcPoint);
}
