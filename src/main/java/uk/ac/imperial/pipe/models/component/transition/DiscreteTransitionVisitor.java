package uk.ac.imperial.pipe.models.component.transition;

import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

public interface DiscreteTransitionVisitor extends PetriNetComponentVisitor {
    void visit(DiscreteTransition transition);
}
