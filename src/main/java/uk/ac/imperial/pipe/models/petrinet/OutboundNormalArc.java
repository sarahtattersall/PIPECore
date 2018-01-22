package uk.ac.imperial.pipe.models.petrinet;

import java.util.Map;

import uk.ac.imperial.state.State;

/**
 * This is a normal arc that is from transitions to places.
 *
 * It is allowed to fire if its target place will have enough capacity for the number of tokens
 * this arc will produce
 */
public class OutboundNormalArc extends OutboundArc {
    /**
     * Constructor
     * @param source connectable of the arc
     * @param target connectable of the arc
     * @param tokenWeights of the arc
     */
    public OutboundNormalArc(Transition source, Place target, Map<String, String> tokenWeights) {
        super(source, target, tokenWeights, ArcType.NORMAL);
    }

    /**
     *
     * @param executablePetriNet to be evaluated 
     * @param state of the Petri net
     * @return true if there is no capacity restriction on the target or firing will
     *         not cause capacity overflow
     */
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
    private int getNumberOfTokensLeavingPlace(State state, ExecutablePetriNet executablePetriNet) {
        Place place = getTarget();
        int count = 0;
        for (InboundArc arc : executablePetriNet.outboundArcs(place)) {
            if (arc.getSource().equals(getTarget()) && arc.getTarget().equals(getSource())) {
                count += getTokenCounts(executablePetriNet, state, arc);
            }
        }
        return count;
    }

    /**
     *
     * @param petriNet to be evaluated 
     * @param state of the Petri net
     * @param arc for which counts are to be retrieved 
     * @return the sum of total number of tokens that the specified arc needs for its weight
     */
    private int getTokenCounts(ExecutablePetriNet executablePetriNet, State state,
            AbstractArc<? extends Connectable, ? extends Connectable> arc) {
        int count = 0;
        double weight = 0;
        for (Map.Entry<String, String> entry : arc.tokenWeights.entrySet()) {
            weight = executablePetriNet.evaluateExpression(state, entry.getValue());
            if (weight == -1.0) {
                throw new RuntimeException("Cannot parse outbound arc weight");
            }
            count += weight;
        }
        return count;
    }

    /**
     *
     * @param state of the Petri net
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
