package uk.ac.imperial.pipe.visitor;

import uk.ac.imperial.pipe.models.component.rate.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.component.rate.FunctionalRateParameterVisitor;
import uk.ac.imperial.pipe.models.component.rate.RateParameter;

public class RateParameterCloner implements FunctionalRateParameterVisitor {
    public RateParameter cloned;
    @Override
    public void visit(FunctionalRateParameter rateParameter) {
        cloned = new FunctionalRateParameter(rateParameter);
    }
}
