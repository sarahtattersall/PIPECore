package uk.ac.imperial.pipe.visitor;

import uk.ac.imperial.pipe.models.component.transition.DiscreteTransition;
import uk.ac.imperial.pipe.models.component.transition.DiscreteTransitionVisitor;
import uk.ac.imperial.pipe.models.component.transition.Transition;

public class TransitionCloner implements DiscreteTransitionVisitor {
    public Transition cloned;
    @Override
    public void visit(DiscreteTransition transition) {
        cloned = new DiscreteTransition(transition);
    }
}
