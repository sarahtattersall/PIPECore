package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

public interface DiscreteTransitionVisitor extends PetriNetComponentVisitor {
    void visit(DiscreteTransition transition);
}
