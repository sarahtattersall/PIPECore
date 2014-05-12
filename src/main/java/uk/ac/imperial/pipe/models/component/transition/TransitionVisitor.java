package uk.ac.imperial.pipe.models.component.transition;

import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

public interface TransitionVisitor extends PetriNetComponentVisitor {
    void visit(Transition transition);
}
