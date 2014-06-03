package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.parsers.FunctionalResults;
import uk.ac.imperial.state.State;

import java.util.Map;

public class InboundNormalArc extends InboundArc {
    public InboundNormalArc(Place source, Transition target, Map<String, String> tokenWeights) {
        super(source, target, tokenWeights, ArcType.NORMAL);
    }

    @Override
    public final boolean canFire(PetriNet petriNet, State state) {
        Place place = getSource();

        Map<String, Integer> tokenCounts = state.getTokens(place.getId());

        Map<String, String> tokenWeights = getTokenWeights();
        for (Map.Entry<String, String> entry : tokenWeights.entrySet()) {
            FunctionalResults<Double> results = petriNet.parseExpression(entry.getValue());
            if (results.hasErrors()) {
                //TODO:
                throw new RuntimeException("Errors evaluating arc weight against Petri net. Needs handling in code");
            }

            double tokenWeight = results.getResult();

            String tokenId = entry.getKey();
            int currentCount = tokenCounts.get(tokenId);
            if (currentCount < tokenWeight && currentCount != -1) {
                return false;
            }
        }
        return true;
    }
}
