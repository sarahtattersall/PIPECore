package uk.ac.imperial.pipe.models.component.rate;

import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

public interface FunctionalRateParameterVisitor extends PetriNetComponentVisitor {
    void visit(FunctionalRateParameter rateParameter);
}
