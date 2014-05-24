package uk.ac.imperial.pipe.dsl;

import org.junit.Before;
import org.junit.Test;
import uk.ac.imperial.pipe.models.component.Connectable;
import uk.ac.imperial.pipe.models.component.arc.Arc;
import uk.ac.imperial.pipe.models.component.arc.InboundNormalArc;
import uk.ac.imperial.pipe.models.component.place.DiscretePlace;
import uk.ac.imperial.pipe.models.component.place.Place;
import uk.ac.imperial.pipe.models.component.rate.RateParameter;
import uk.ac.imperial.pipe.models.component.token.ColoredToken;
import uk.ac.imperial.pipe.models.component.token.Token;
import uk.ac.imperial.pipe.models.component.transition.DiscreteTransition;
import uk.ac.imperial.pipe.models.component.transition.Transition;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ANormalArcTest {
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
                ANormalArc.withSource("P0").andTarget("T1").create(tokens, places, transitions, rateParameters);

        Arc<? extends Connectable, ? extends Connectable> expected =
                new InboundNormalArc(places.get("P0"), transitions.get("T1"), new HashMap<String, String>());
        assertEquals(expected, arc);
    }

    @Test
    public void createsArcSingleToken() {
        tokens.put("Red", new ColoredToken("Red", Color.RED));
        places.put("P0", new DiscretePlace("P0", "P0"));
        transitions.put("T1", new DiscreteTransition("T1", "T1"));
        Arc<? extends Connectable, ? extends Connectable> arc =
                ANormalArc.withSource("P0").andTarget("T1").with("4", "Red").tokens().create(tokens, places, transitions,
                        rateParameters);

        HashMap<String, String> tokenWeights = new HashMap<>();
        tokenWeights.put("Red", "4");
        Arc<? extends Connectable, ? extends Connectable> expected =
                new InboundNormalArc(places.get("P0"), transitions.get("T1"), tokenWeights);

        assertEquals(expected, arc);
    }

    @Test
    public void createsArcWithMultipleTokens() {
        tokens.put("Red", new ColoredToken("Red", Color.RED));
        tokens.put("Default", new ColoredToken("Default", Color.BLACK));
        places.put("P0", new DiscretePlace("P0", "P0"));
        transitions.put("T1", new DiscreteTransition("T1", "T1"));
        Arc<? extends Connectable, ? extends Connectable> arc =
                ANormalArc.withSource("P0").andTarget("T1").with("4", "Red").tokens().and("1", "Default").token().create(tokens,
                        places, transitions,
                        rateParameters);

        HashMap<String, String> tokenWeights = new HashMap<>();
        tokenWeights.put("Red", "4");
        tokenWeights.put("Default", "1");
        Arc<? extends Connectable, ? extends Connectable> expected =
                new InboundNormalArc(places.get("P0"), transitions.get("T1"), tokenWeights);

        assertEquals(expected, arc);
    }

}

