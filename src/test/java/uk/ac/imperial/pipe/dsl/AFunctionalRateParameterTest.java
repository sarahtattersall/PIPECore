package uk.ac.imperial.pipe.dsl;

import org.junit.Before;
import org.junit.Test;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.Transition;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class AFunctionalRateParameterTest {
    private Map<String, Token> tokens;

    private Map<String, Place> places;

    private Map<String, FunctionalRateParameter> rateParameters;

    private Map<String, Transition> transitions;

    @Before
    public void setUp() {
        tokens = new HashMap<>();
        places = new HashMap<>();
        transitions = new HashMap<>();
        rateParameters = new HashMap<>();
    }

    @Test
    public void createsRateParameter() {
        FunctionalRateParameter rateParameter = ARateParameter.withId("Foo").andExpression("5.0")
                .create(tokens, places, transitions, rateParameters);
        FunctionalRateParameter expected = new FunctionalRateParameter("5.0", "Foo", "Foo");

        assertEquals(expected, rateParameter);
    }

    @Test
    public void addsRateParameterToRateParameters() {
        FunctionalRateParameter rateParameter = ARateParameter.withId("Foo").andExpression("5.0")
                .create(tokens, places, transitions, rateParameters);
        assertThat(rateParameters).containsEntry("Foo", rateParameter);
    }

}
