package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.parsers.*;
import uk.ac.imperial.state.State;

import java.util.Map;

/**
 * Represents a normal arc from places to transitions.
 * A normal arc requires the number of tokens in its source place to be the same or greater than
 * its specified weight and on firing it should remove these from the place
 */
public class InboundNormalArc extends InboundArc {
    /**
     * Constructor
     * @param source connectable of the arc
     * @param target connectable of the arc
     * @param tokenWeights of the arc 
     */
    public InboundNormalArc(Place source, Transition target, Map<String, String> tokenWeights) {
        super(source, target, tokenWeights, ArcType.NORMAL);
    }

    /**
     *
     * @param petriNet to be evaluated 
     * @param state current state of the Petri net
     * @return true if the arcs place (source) has the same number of tokens or greater than the specified weight on the arc
     *         false otherwise, or if counts for all the tokens on the arc are zero. 
     */
    @Override
    public final boolean canFire(PetriNet petriNet, State state) {
        Place place = getSource();
        Map<String, Integer> tokenCounts = state.getTokens(place.getId());
        if (allTokenCountsAreZero(tokenCounts)) {
        	return false;
        }
        Map<String, String> tokenWeights = getTokenWeights();
        StateEvalVisitor stateEvalVisitor = new StateEvalVisitor(petriNet, state);
        FunctionalWeightParser<Double> functionalWeightParser = new PetriNetWeightParser(stateEvalVisitor, petriNet);


        for (Map.Entry<String, String> entry : tokenWeights.entrySet()) {
            FunctionalResults<Double> results = functionalWeightParser.evaluateExpression(entry.getValue());
            if (results.hasErrors()) {
                //TODO: test when results has errors 
                throw new RuntimeException("Errors evaluating arc weight against Petri net. Needs handling in code");
            }

            double tokenWeight = results.getResult();

            String tokenId = entry.getKey();
            int currentCount = tokenCounts.get(tokenId);
            if (currentCount < tokenWeight) {  
                return false;
            }
        }
        return true;
    }

	private boolean allTokenCountsAreZero(Map<String, Integer> tokenCounts) {
		boolean allCountsAreZero = true;
		for (Integer count : tokenCounts.values()) {
			if (count > 0) {
				allCountsAreZero = false; 
			}
		}
		return allCountsAreZero;
	}
}
