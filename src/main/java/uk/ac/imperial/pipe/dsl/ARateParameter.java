package uk.ac.imperial.pipe.dsl;

import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.Transition;

import java.util.Map;

/**
 * Rate parameter DSL for use with {@link uk.ac.imperial.pipe.dsl.APetriNet}
 * Usage:
 * ARateParameter.withId("R1").andExpression("#(P0)")
 */
public final class ARateParameter implements DSLCreator<FunctionalRateParameter> {


    /**
     * Rate paramter id
     */
    private final String id;

    /**
     * Rate parameter functional expression
     */
    private String expression;

    /**
     * Private constructor
     * @param id
     */
    private ARateParameter(String id) {this.id = id;}

    /**
     * Create an ARateParameter instance with id
     * @param id name of rate parameter
     * @return instantiated ARateParameter
     */
    public static ARateParameter withId(String id) {
        return new ARateParameter(id);
    }

    /**
     * Functional expression to add to the rate parameter on creation
     * @param expression
     * @return builder for chaining
     */
    public ARateParameter andExpression(String expression) {
        this.expression = expression;
        return this;
    }


    /**
     *
     * @param tokens map of created tokens with id -> Token
     * @param places map of created places with id -> Connectable
     * @param transitions
     * @param rateParameters
     * @return FunctionalRateParameter
     */
    @Override
    public FunctionalRateParameter create(Map<String, Token> tokens, Map<String, Place> places,
                                Map<String, Transition> transitions, Map<String, FunctionalRateParameter> rateParameters) {
        FunctionalRateParameter rateParameter = new FunctionalRateParameter(expression, id, id);
        rateParameters.put(id, rateParameter);
        return rateParameter;
    }
}
