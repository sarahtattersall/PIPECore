package uk.ac.imperial.pipe.visitor;

import uk.ac.imperial.pipe.models.petrinet.DiscreteTransition;
import uk.ac.imperial.pipe.models.petrinet.DiscreteTransitionVisitor;
import uk.ac.imperial.pipe.models.petrinet.Transition;

public class TransitionCloner implements DiscreteTransitionVisitor {
    public Transition cloned;
    @Override
    public void visit(DiscreteTransition transition) {
        cloned = new DiscreteTransition(transition);
    }
}
