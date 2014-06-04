package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

/**
 * Visits the Rate interface
 */
public interface RateVisitor extends PetriNetComponentVisitor {
    void visit(Rate rate);
}