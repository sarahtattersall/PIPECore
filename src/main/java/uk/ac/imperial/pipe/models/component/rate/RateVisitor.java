package uk.ac.imperial.pipe.models.component.rate;

import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

public interface RateVisitor extends PetriNetComponentVisitor {
    void visit(Rate rate);
}