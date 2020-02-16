package uk.ac.imperial.pipe.dsl;

import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.petrinet.ColoredToken;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.Transition;

import java.awt.Color;
import java.util.Map;

/**
 * Token DSL to be used in conjunction with {@link uk.ac.imperial.pipe.dsl.APetriNet}
 */
public final class AToken implements DSLCreator<Token> {
    /**
     * Token id
     */
    private String name;

    /**
     * Token color, defaults to black
     */
    private Color color = Color.BLACK;

    /**
     * Private constructor
     * @param name token id
     */
    private AToken(String name) {
        this.name = name;
    }

    /**
     * Factory method
     * @param name token id
     * @return builder for chaining
     */
    public static AToken called(String name) {
        return new AToken(name);
    }

    /**
     *
     * @param color token color
     * @return builder for chaining
     */
    public AToken withColor(Color color) {
        this.color = color;
        return this;
    }

    /**
     *
     * @param tokens map of created tokens with id of Token
     * @param places map of created places with id of Connectable
     * @param transitions map of created transitions with id of Transition
     * @param rateParameters map of created rateParameters with id of rateParameter
     * @return Token with specified id and color.
     */
    @Override
    public Token create(Map<String, Token> tokens, Map<String, Place> places, Map<String, Transition> transitions,
            Map<String, FunctionalRateParameter> rateParameters) {
        Token token = new ColoredToken(name, color);
        tokens.put(name, token);
        return token;
    }
}
