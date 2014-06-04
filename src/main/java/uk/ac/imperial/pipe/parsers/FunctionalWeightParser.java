package uk.ac.imperial.pipe.parsers;

/**
 * Parses the functional weights procuding the result of type T
 * @param <T> format of the number result e.g. Double, Integer
 */
public interface FunctionalWeightParser<T extends Number> {
    /**
     *
     * Evaluates the functional expression to calculate a numerical
     * result for it based on components in the Petri net
     *
     * @param expression to evaluate
     * @return evaluated expression which will contain the result (if obtained) and information
     *         about any errors if it could not be parsed
     */
    FunctionalResults<T> evaluateExpression(String expression);
}
