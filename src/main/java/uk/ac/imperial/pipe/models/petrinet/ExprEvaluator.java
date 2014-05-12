package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.parsers.FunctionalResults;
import uk.ac.imperial.pipe.parsers.FunctionalWeightParser;
import uk.ac.imperial.pipe.parsers.PetriNetWeightParser;

public class ExprEvaluator {

    private final PetriNet petriNet;

    public ExprEvaluator(PetriNet petriNet) {
        this.petriNet = petriNet;
    }

    /**
     * Parses expression against petri net and returns the result
     * @param expr functional expression
     * @return result of evaluating the expression. E.g
     * @throws FunctionalEvaluationException if expression cannot be parsed due to components not being in
     *         Petri net
     */
    public Double evaluateExpression(String expr) throws FunctionalEvaluationException {
        FunctionalWeightParser<Double> transitionWeightParser = new PetriNetWeightParser(petriNet);
        FunctionalResults<Double> result = transitionWeightParser.evaluateExpression(expr);
        if (result.hasErrors()) {
            throw new FunctionalEvaluationException(result.getErrors());
        }
        return result.getResult();

    }
}
