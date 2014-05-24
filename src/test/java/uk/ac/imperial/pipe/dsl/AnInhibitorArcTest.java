package uk.ac.imperial.pipe.dsl;

import org.junit.Before;
import org.junit.Test;
import uk.ac.imperial.pipe.models.component.Connectable;
import uk.ac.imperial.pipe.models.component.arc.Arc;
import uk.ac.imperial.pipe.models.component.arc.InboundInhibitorArc;
import uk.ac.imperial.pipe.models.component.place.DiscretePlace;
import uk.ac.imperial.pipe.models.component.place.Place;
import uk.ac.imperial.pipe.models.component.rate.RateParameter;
import uk.ac.imperial.pipe.models.component.token.Token;
import uk.ac.imperial.pipe.models.component.transition.DiscreteTransition;
import uk.ac.imperial.pipe.models.component.transition.Transition;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class AnInhibitorArcTest {
    private Map<String, Token> tokens;

    private Map<String, Place> places;

    private Map<String, RateParameter> rateParameters;

    private Map<String, Transition> transitions;

    @Before
    public void setUp() {
        tokens = new HashMap<>();
        places = new HashMap<>();
        transitions = new HashMap<>();
        rateParameters = new HashMap<>();
    }

    @Test
    public void createsArcWithSourceAndTarget() {
        places.put("P0", new DiscretePlace("P0", "P0"));
        transitions.put("T1", new DiscreteTransition("T1", "T1"));
        Arc<? extends Connectable, ? extends Connectable> arc =
                AnInhibitorArc.withSource("P0").andTarget("T1").create(tokens, places, transitions, rateParameters);

        Arc<? extends Connectable, ? extends Connectable> expected =
                new InboundInhibitorArc(places.get("P0"), transitions.get("T1"));

        assertEquals(expected, arc);
    }
}
