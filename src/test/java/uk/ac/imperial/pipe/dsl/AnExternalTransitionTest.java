package uk.ac.imperial.pipe.dsl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import uk.ac.imperial.pipe.models.petrinet.DiscreteExternalTransition;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.petrinet.NormalRate;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.DiscreteTransition;
import uk.ac.imperial.pipe.models.petrinet.Transition;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class AnExternalTransitionTest {

    private Map<String, Token> tokens;

    private Map<String, Place> places;

    private Map<String, Transition> transitions;

    private Map<String, FunctionalRateParameter> rateParameters;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        tokens = new HashMap<>();
        places = new HashMap<>();
        transitions = new HashMap<>();
        rateParameters = new HashMap<>();
    }

    @Test
    public void createsTransitionWithIdAndClass() {
        Transition transition = AnExternalTransition.withId("T0")
                .andExternalClass("uk.ac.imperial.pipe.models.petrinet.TestingExternalTransition")
                .create(tokens, places, transitions, rateParameters);
        Transition expected = new DiscreteExternalTransition("T0", "T0",
                "uk.ac.imperial.pipe.models.petrinet.TestingExternalTransition");
        assertEquals(expected, transition);
    }

    @Test
    public void throwsIfExternalClassNotSpecified() throws Exception {
        exception.expect(IllegalArgumentException.class);
        @SuppressWarnings("unused")
        Transition transition = AnExternalTransition.withId("T0").create(tokens, places, transitions, rateParameters);
    }

}
