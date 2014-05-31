package uk.ac.imperial.pipe.visitor;

import uk.ac.imperial.pipe.models.petrinet.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.petrinet.FunctionalRateParameterVisitor;
import uk.ac.imperial.pipe.models.petrinet.RateParameter;

/**
 * Clones all implementations of {@link uk.ac.imperial.pipe.models.petrinet.RateParameter}
 */
public final class RateParameterCloner implements FunctionalRateParameterVisitor {
    public RateParameter cloned;
    @Override
    public void visit(FunctionalRateParameter rateParameter) {
        cloned = new FunctionalRateParameter(rateParameter);
    }
}
