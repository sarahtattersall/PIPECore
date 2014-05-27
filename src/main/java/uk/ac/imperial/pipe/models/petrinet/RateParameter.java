package uk.ac.imperial.pipe.models.petrinet;

public interface RateParameter extends PetriNetComponent, Rate {
    /**
     * Message fired when the places tokens change in any way
     */
    String EXPRESSION_CHANGE_MESSAGE = "expression";

    void setExpression(String expression);
}
