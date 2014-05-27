package uk.ac.imperial.pipe.visitor;

import uk.ac.imperial.pipe.models.petrinet.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.petrinet.FunctionalRateParameterVisitor;
import uk.ac.imperial.pipe.models.petrinet.RateParameter;

public class RateParameterCloner implements FunctionalRateParameterVisitor {
    public RateParameter cloned;
    @Override
    public void visit(FunctionalRateParameter rateParameter) {
        cloned = new FunctionalRateParameter(rateParameter);
    }
}
