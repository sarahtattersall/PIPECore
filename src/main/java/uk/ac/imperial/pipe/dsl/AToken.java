package uk.ac.imperial.pipe.dsl;

import uk.ac.imperial.pipe.models.component.place.Place;
import uk.ac.imperial.pipe.models.component.rate.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.component.token.ColoredToken;
import uk.ac.imperial.pipe.models.component.token.Token;
import uk.ac.imperial.pipe.models.component.transition.Transition;

import java.awt.Color;
import java.util.Map;

public class AToken implements DSLCreator<Token> {
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
