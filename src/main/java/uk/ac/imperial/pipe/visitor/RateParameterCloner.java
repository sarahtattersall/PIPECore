package uk.ac.imperial.pipe.visitor;

import uk.ac.imperial.pipe.models.petrinet.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.petrinet.FunctionalRateParameterVisitor;
import uk.ac.imperial.pipe.models.petrinet.RateParameter;

/**
 * Clones all implementations of {@link uk.ac.imperial.pipe.models.petrinet.RateParameter}
 */
public final class RateParameterCloner implements FunctionalRateParameterVisitor {
    /**
     * Cloned rate paramter, null before visit is called
     */
    public RateParameter cloned;

    /**
     * Clones a functional rate paramter
     * @param rateParameter to be visited 
     */
    @Override
    public void visit(FunctionalRateParameter rateParameter) {
        cloned = new FunctionalRateParameter(rateParameter);
    }
}
