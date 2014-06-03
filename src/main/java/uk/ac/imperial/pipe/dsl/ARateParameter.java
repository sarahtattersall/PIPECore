package uk.ac.imperial.pipe.dsl;

import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.Transition;

import java.util.Map;

public final class ARateParameter implements DSLCreator<FunctionalRateParameter> {


    private final String id;

    private String expression;

    private ARateParameter(String id) {this.id = id;}

    /**
     * Create an ARateParameter instance with id
     * @param id name of rate parameter
     * @return instantiated ARateParameter
     */
    public static ARateParameter withId(String id) {
        return new ARateParameter(id);
    }

    public ARateParameter andExpression(String expression) {
        this.expression = expression;
        return this;
    }



    @Override
    public FunctionalRateParameter create(Map<String, Token> tokens, Map<String, Place> places,
                                Map<String, Transition> transitions, Map<String, FunctionalRateParameter> rateParameters) {
        FunctionalRateParameter rateParameter = new FunctionalRateParameter(expression, id, id);
        rateParameters.put(id, rateParameter);
        return rateParameter;
    }
}
