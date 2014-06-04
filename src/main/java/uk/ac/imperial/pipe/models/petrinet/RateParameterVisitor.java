package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.exceptions.InvalidRateException;
import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

/**
 * Rate paramter visitor
 */
public interface RateParameterVisitor extends PetriNetComponentVisitor {
    /**
     * Visit a functional rate paramter
     * @param rate
     * @throws InvalidRateException
     */
    void visit(FunctionalRateParameter rate) throws InvalidRateException;
}
