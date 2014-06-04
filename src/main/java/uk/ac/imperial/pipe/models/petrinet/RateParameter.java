package uk.ac.imperial.pipe.models.petrinet;

/**
 * Rate parameter, this is equivalent to declaring a variable
 * and having transitions reference this. When it is updated all transitions
 * referencing a rate parameter will be updated and so for transitions which should
 * all have the same rate a rate parameter should be used.
 */
public interface RateParameter extends PetriNetComponent, Rate {
    /**
     * Message fired when the places tokens change in any way
     */
    String EXPRESSION_CHANGE_MESSAGE = "expression";

    /**
     *
     * @param expression new expression for the rate paramter
     */
    void setExpression(String expression);
}
