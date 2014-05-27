package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

/**
 * Visits discrete places which is a concrete implementation of the {@link Place} interface.
 */
public interface DiscretePlaceVisitor extends PetriNetComponentVisitor {
    void visit(DiscretePlace discretePlace);
}
