package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

public interface DiscreteExternalTransitionVisitor extends PetriNetComponentVisitor {
    void visit(DiscreteExternalTransition transition);
}
