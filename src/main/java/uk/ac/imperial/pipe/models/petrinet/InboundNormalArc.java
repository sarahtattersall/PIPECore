package uk.ac.imperial.pipe.models.petrinet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import uk.ac.imperial.state.State;

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
     * @param executablePetriNet to be evaluated 
     * @param state current state of the Petri net
     * @return true if the arcs place (source) has the same number of tokens or greater than the specified weight on the arc
     *         false otherwise, or if counts for all the tokens on the arc are zero. 
     */
    //TODO refactor...
    @Override
    public final boolean canFire(ExecutablePetriNet executablePetriNet, State state) {
        Place place = getSource();
        Map<String, Integer> tokenCounts = state.getTokens(place.getId());
        if (allTokenCountsAreZero(tokenCounts)) {
            return false;
        }
        Map<String, String> tokenWeights = getTokenWeights();
        boolean allCanFire = true;
        Collection<Map.Entry<String, String>> entries = nonZeroWeightEntries(tokenWeights
                .entrySet(), executablePetriNet, state);
        if (entries.size() == 0) {
            allCanFire = false;
        } else {
            allCanFire = verifyAllNonZeroEntriesCanFire(executablePetriNet, state, tokenCounts, allCanFire, entries);
        }
        return allCanFire;
    }

    protected boolean verifyAllNonZeroEntriesCanFire(
            ExecutablePetriNet executablePetriNet, State state,
            Map<String, Integer> tokenCounts, boolean allCanFire,
            Collection<Map.Entry<String, String>> entries) {
        double tokenWeight;
        for (Entry<String, String> entry : entries) {
            tokenWeight = executablePetriNet.evaluateExpression(state, entry.getValue());
            if (!canFireForToken(tokenCounts, tokenWeight, entry)) {
                allCanFire = false;
            }
        }
        return allCanFire;
    }

    private Collection<Entry<String, String>> nonZeroWeightEntries(
            Set<Entry<String, String>> entrySet, ExecutablePetriNet epn, State state) {
        ArrayList<Entry<String, String>> entries = new ArrayList<Entry<String, String>>();
        double tokenWeight = 0;
        for (Map.Entry<String, String> entry : tokenWeights.entrySet()) {
            tokenWeight = epn.evaluateExpression(state, entry.getValue());
            if (tokenWeight == -1.0) {
                //TODO:
                throw new RuntimeException("Errors evaluating arc weight against Petri net. Needs handling in code");
            } else if (tokenWeight > 0) {
                entries.add(entry);
            }
        }
        return entries;
    }

    protected boolean canFireForToken(Map<String, Integer> tokenCounts, double tokenWeight,
            Map.Entry<String, String> entry) {
        String tokenId = entry.getKey();
        int currentCount = tokenCounts.get(tokenId);
        return !((currentCount < tokenWeight) || (currentCount == 0));
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
