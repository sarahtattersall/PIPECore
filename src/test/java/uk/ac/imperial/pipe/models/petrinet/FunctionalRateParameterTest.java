package uk.ac.imperial.pipe.models.petrinet;

import org.junit.Before;
import org.junit.Test;
import uk.ac.imperial.pipe.exceptions.InvalidRateException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class FunctionalRateParameterTest {

    FunctionalRateParameter rateParameter;

    @Before
    public void setUp() {
        rateParameter = new FunctionalRateParameter("5*2", "R1", "R1");
    }

    @Test
    public void visitsRateParameterVisitor() throws InvalidRateException, PetriNetComponentException {
        RateParameterVisitor visitor = mock(RateParameterVisitor.class);
        rateParameter.accept(visitor);
        verify(visitor).visit(rateParameter);
    }

    @Test
    public void visitsFunctionalRateParamterVisitor() throws PetriNetComponentException {
        FunctionalRateParameterVisitor visitor = mock(FunctionalRateParameterVisitor.class);
        rateParameter.accept(visitor);
        verify(visitor).visit(rateParameter);
    }
}