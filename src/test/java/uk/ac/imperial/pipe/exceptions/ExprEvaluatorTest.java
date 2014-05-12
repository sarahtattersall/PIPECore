package uk.ac.imperial.pipe.exceptions;

import org.junit.Before;
import uk.ac.imperial.pipe.models.petrinet.ExprEvaluator;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;

public class ExprEvaluatorTest {

    ExprEvaluator exprEvaluator;

    PetriNet net;

    @Before
    public void setUp() {
        net = new PetriNet();
        exprEvaluator = new ExprEvaluator(net);
    }


     boolean isTrue(double value) {
        return value == 1.0;
    }

    boolean isFalse(double value) {
        return value == 0.0;
    }
}
