package uk.ac.imperial.pipe.dsl;

import org.junit.Before;
import org.junit.Test;
import uk.ac.imperial.pipe.models.petrinet.Connectable;
import uk.ac.imperial.pipe.models.petrinet.Arc;
import uk.ac.imperial.pipe.models.petrinet.InboundInhibitorArc;
import uk.ac.imperial.pipe.models.petrinet.DiscretePlace;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.DiscreteTransition;
import uk.ac.imperial.pipe.models.petrinet.Transition;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class AnInhibitorArcTest {
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
    public void createsArcWithSourceAndTarget() {
        places.put("P0", new DiscretePlace("P0", "P0"));
        transitions.put("T1", new DiscreteTransition("T1", "T1"));
        Arc<? extends Connectable, ? extends Connectable> arc = AnInhibitorArc.withSource("P0").andTarget("T1")
                .create(tokens, places, transitions, rateParameters);

        Arc<? extends Connectable, ? extends Connectable> expected = new InboundInhibitorArc(places.get("P0"),
                transitions.get("T1"));

        assertEquals(expected, arc);
    }
}
