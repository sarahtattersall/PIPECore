package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.parsers.FunctionalResults;
import uk.ac.imperial.pipe.parsers.FunctionalWeightParser;
import uk.ac.imperial.pipe.parsers.PetriNetWeightParser;
import uk.ac.imperial.pipe.parsers.StateEvalVisitor;
import uk.ac.imperial.state.State;

import java.util.Map;

/**
 * This is a normal arc that is from transitions to places.
 *
 * It is allowed to fire if its target place will have enough capacity for the number of tokens
 * this arc will produce
 */
public class OutboundNormalArc extends OutboundArc {
    /**
     * Constructor
     * @param source
     * @param target
     * @param tokenWeights
     */
    public OutboundNormalArc(Transition source, Place target, Map<String, String> tokenWeights) {
        super(source, target, tokenWeights, ArcType.NORMAL);
    }

    /**
     *
     * @param petriNet
     * @param state
     * @return true if there is no capacity restriction on the target or firing will
     *         not cause capacity overflow
     */
    @Override
    public final boolean canFire(PetriNet petriNet, State state) {
        Place place = getTarget();
        if (!place.hasCapacityRestriction()) {
            return true;
        }

        int totalTokensIn = getTokenCounts(petriNet, state, this);
        int totalTokensOut = getNumberOfTokensLeavingPlace(state, petriNet);
        int tokensInPlace = getTokensInPlace(state);

        return (tokensInPlace + totalTokensIn - totalTokensOut <= place.getCapacity());
    }
    @Override
    public boolean canFire(ExecutablePetriNet executablePetriNet, State state) {
    	Place place = getTarget();
    	if (!place.hasCapacityRestriction()) {
    		return true;
    	}
    	
    	int totalTokensIn = getTokenCounts(executablePetriNet, state, this);
    	int totalTokensOut = getNumberOfTokensLeavingPlace(state, executablePetriNet);
    	int tokensInPlace = getTokensInPlace(state);
    	
    	return (tokensInPlace + totalTokensIn - totalTokensOut <= place.getCapacity());
    }

    /**
     *
     * Calculates the number of tokens leaving the target due
     * to an arc loop
     *
     *
     * @return the number of tokens that leave the  place
     *         via this transition.
     *
     */
    private int getNumberOfTokensLeavingPlace(State state, PetriNet petriNet) {
        Place place = getTarget();
        int count = 0;
        for (InboundArc arc : petriNet.outboundArcs(place)) {
            if (arc.getSource().equals(getTarget())  && arc.getTarget().equals(getSource())) {
                count += getTokenCounts(petriNet, state, arc);
            }
        }
        return count;
    }
    private int getNumberOfTokensLeavingPlace(State state, ExecutablePetriNet executablePetriNet) {
    	Place place = getTarget();
    	int count = 0;
    	for (InboundArc arc : executablePetriNet.outboundArcs(place)) {
    		if (arc.getSource().equals(getTarget())  && arc.getTarget().equals(getSource())) {
    			count += getTokenCounts(executablePetriNet, state, arc);
    		}
    	}
    	return count;
    }

    /**
     *
     * @param petriNet
     * @param state
     *@param arc  @return the sum of total number of tokens that the specified arc needs for its weight
     */
    private int getTokenCounts(PetriNet petriNet, State state, AbstractArc<? extends Connectable, ? extends Connectable> arc) {
        StateEvalVisitor stateEvalVisitor = new StateEvalVisitor(petriNet, state);
        FunctionalWeightParser<Double> functionalWeightParser = new PetriNetWeightParser(stateEvalVisitor, petriNet);

        int count = 0;
        for (Map.Entry<String, String> entry : arc.tokenWeights.entrySet()) {
            FunctionalResults<Double> result =  functionalWeightParser.evaluateExpression(entry.getValue());
            if (result.hasErrors()) {
                throw new RuntimeException("Cannot parse outbound arc weight");
            }
            double weight = result.getResult();
            count += weight;
        }
        return count;
    }
    //TODO refactor, probably to ExecutablePetriNet
    private int getTokenCounts(ExecutablePetriNet executablePetriNet, State state, AbstractArc<? extends Connectable, ? extends Connectable> arc) {
    	StateEvalVisitor stateEvalVisitor = new StateEvalVisitor(executablePetriNet, state);
    	FunctionalWeightParser<Double> functionalWeightParser = new PetriNetWeightParser(stateEvalVisitor, executablePetriNet);
    	
    	int count = 0;
    	for (Map.Entry<String, String> entry : arc.tokenWeights.entrySet()) {
    		FunctionalResults<Double> result =  functionalWeightParser.evaluateExpression(entry.getValue());
    		if (result.hasErrors()) {
    			throw new RuntimeException("Cannot parse outbound arc weight");
    		}
    		double weight = result.getResult();
    		count += weight;
    	}
    	return count;
    }

    /**
     *
     * @param state
     * @return the number of tokens in this arcs target place for the given state
     */
    private int getTokensInPlace(State state) {
        Place place = getTarget();
        int count = 0;
        for (Integer value : state.getTokens(place.getId()).values()) {
            count += value;
        }
        return count;
    }

}
