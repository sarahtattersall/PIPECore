package uk.ac.imperial.pipe.dsl;

import org.junit.Before;
import org.junit.Test;
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

public class ATransitionTest {

    private Map<String, Token> tokens;

    private Map<String, Place> places;

    private Map<String, Transition> transitions;

    private Map<String, FunctionalRateParameter> rateParameters;

    @Before
    public void setUp() {
        tokens = new HashMap<>();
        places = new HashMap<>();
        transitions = new HashMap<>();
        rateParameters = new HashMap<>();
    }

    @Test
    public void createsTransitionWithId() {
        Transition transition = AnImmediateTransition.withId("T0").create(tokens, places, transitions, rateParameters);
        Transition expected = new DiscreteTransition("T0", "T0");
        assertEquals(expected, transition);
    }

    @Test
    public void addsTransitionToConnectables() {
        Transition transition = AnImmediateTransition.withId("T0").create(tokens, places, transitions, rateParameters);
        assertThat(transitions).containsEntry("T0", transition);
    }

    @Test
    public void createsTransitionWithPriority() {
        Transition transition = AnImmediateTransition.withId("T0").andPriority(5)
                .create(tokens, places, transitions, rateParameters);
        Transition expected = new DiscreteTransition("T0", "T0");
        expected.setPriority(5);
        assertEquals(expected, transition);
    }

    @Test
    public void createsTimedTransitionWithRate() {
        Transition transition = ATimedTransition.withId("T0").create(tokens, places, transitions, rateParameters);
        Transition expected = new DiscreteTransition("T0", "T0");
        expected.setTimed(true);
        assertEquals(expected, transition);
    }

    @Test
    public void createsTimedTransitionWithDelay() {
        Transition transition = ATimedTransition.withId("T0").andDelay(1000)
                .create(tokens, places, transitions, rateParameters);
        Transition expected = new DiscreteTransition("T0", "T0");
        expected.setTimed(true);
        expected.setDelay(1000);
        assertEquals(expected, transition);
    }
    // how to distinguish rates from delays

    @Test
    public void createsImmediateTransition() {
        Transition transition = AnImmediateTransition.withId("T0").create(tokens, places, transitions, rateParameters);
        Transition expected = new DiscreteTransition("T0", "T0");
        expected.setTimed(false);
        assertEquals(expected, transition);
    }

    @Test
    public void createsInfiniteServerTransition() {
        Transition transition = AnImmediateTransition.withId("T0").andIsAnInfinite().server()
                .create(tokens, places, transitions, rateParameters);
        Transition expected = new DiscreteTransition("T0", "T0");
        expected.setInfiniteServer(true);
        assertEquals(expected, transition);
    }

    @Test
    public void createsSingleServerTransition() {
        Transition transition = AnImmediateTransition.withId("T0").andIsASingle().server()
                .create(tokens, places, transitions, rateParameters);
        Transition expected = new DiscreteTransition("T0", "T0");
        expected.setInfiniteServer(false);
        assertEquals(expected, transition);
    }

    @Test
    public void createsNormalRateTransition() {
        Transition transition = AnImmediateTransition.withId("T0").andProbability("5")
                .create(tokens, places, transitions, rateParameters);
        Transition expected = new DiscreteTransition("T0", "T0");
        expected.setRate(new NormalRate("5"));
        assertEquals(expected, transition);
    }

    @Test
    public void createsTransitionWithARateParameter() {
        rateParameters.put("Foo", new FunctionalRateParameter("5", "Foo", "Foo"));
        Transition transition = ATimedTransition.withId("T0").withRateParameter("Foo")
                .create(tokens, places, transitions, rateParameters);
        Transition expected = new DiscreteTransition("T0", "T0");
        expected.setTimed(true);
        expected.setRate(rateParameters.get("Foo"));
        assertEquals(expected, transition);
    }

}
