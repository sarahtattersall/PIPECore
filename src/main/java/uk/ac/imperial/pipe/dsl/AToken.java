package uk.ac.imperial.pipe.dsl;

import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.petrinet.ColoredToken;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.Transition;

import java.awt.Color;
import java.util.Map;

public final class AToken implements DSLCreator<Token> {
    private String name;
    private Color color = Color.BLACK;

    private AToken(String name) { this.name = name; }

    public static AToken called(String name) {
        return new AToken(name);
    }

    public AToken withColor(Color color) {
        this.color = color;
        return this;
    }

    @Override
    public Token create(Map<String, Token> tokens, Map<String, Place> places, Map<String, Transition> transitions, Map<String, FunctionalRateParameter> rateParameters) {
        Token token = new ColoredToken(name, color);
        tokens.put(name, token);
        return token;
    }
}
