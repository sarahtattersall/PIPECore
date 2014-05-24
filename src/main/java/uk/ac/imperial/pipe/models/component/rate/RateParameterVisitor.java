package uk.ac.imperial.pipe.models.component.rate;

import uk.ac.imperial.pipe.exceptions.InvalidRateException;
import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

public interface RateParameterVisitor extends PetriNetComponentVisitor {
    void visit(FunctionalRateParameter rate) throws InvalidRateException;
}
