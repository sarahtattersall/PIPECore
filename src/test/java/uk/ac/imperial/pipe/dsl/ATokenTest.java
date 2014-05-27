package uk.ac.imperial.pipe.dsl;

import org.junit.Before;
import org.junit.Test;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.petrinet.ColoredToken;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.Transition;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class ATokenTest {
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
    public void createsTokenWithNameAndDefaultColorBlack() {
        Token token = AToken.called("Default").create(tokens, places, transitions, rateParameters);
        Token expected = new ColoredToken("Default", Color.BLACK);
        assertEquals(expected, token);
    }

    @Test
    public void createsTokenWithSpecifiedColor() {
        Token token = AToken.called("Red").withColor(Color.RED).create(tokens, places, transitions, rateParameters);
        Token expected = new ColoredToken("Red", Color.RED);
        assertEquals(expected, token);
    }

    @Test
    public void addsToTokens() {
        Token token = AToken.called("Default").create(tokens, places, transitions, rateParameters);
        assertThat(tokens).containsEntry("Default", token);
    }
}
