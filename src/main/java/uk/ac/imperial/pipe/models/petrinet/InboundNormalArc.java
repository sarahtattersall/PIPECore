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
     * @param source
     * @param target
     * @param tokenWeights
     */
    public InboundNormalArc(Place source, Transition target, Map<String, String> tokenWeights) {
        super(source, target, tokenWeights, ArcType.NORMAL);
    }

    /**
     *
     * @param petriNet
     * @param state current state of the Petri net
     * @return true if the arcs place (source) has the same number of tokens or greater than the specified weight on the arc
     */
    @Override
    public final boolean canFire(PetriNet petriNet, State state) {
        Place place = getSource();
        Map<String, Integer> tokenCounts = state.getTokens(place.getId());
        Map<String, String> tokenWeights = getTokenWeights();
        StateEvalVisitor stateEvalVisitor = new StateEvalVisitor(petriNet, state);
        FunctionalWeightParser<Double> functionalWeightParser = new PetriNetWeightParser(stateEvalVisitor, petriNet);


        for (Map.Entry<String, String> entry : tokenWeights.entrySet()) {
            FunctionalResults<Double> results = functionalWeightParser.evaluateExpression(entry.getValue());
            if (results.hasErrors()) {
                //TODO:
                throw new RuntimeException("Errors evaluating arc weight against Petri net. Needs handling in code");
            }

            double tokenWeight = results.getResult();

            String tokenId = entry.getKey();
            int currentCount = tokenCounts.get(tokenId);
            //TODO test:  currentCount = -1 can't mean that the arc can fire
//            if ((currentCount < tokenWeight) && (currentCount != -1)) {
            if ((currentCount < tokenWeight) || (currentCount == 0)) {
                return false;
            }
        }
        return true;
    }
}
