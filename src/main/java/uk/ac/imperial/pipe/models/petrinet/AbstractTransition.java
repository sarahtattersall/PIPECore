package uk.ac.imperial.pipe.models.petrinet;

import java.util.Map;

import uk.ac.imperial.state.HashedStateBuilder;
import uk.ac.imperial.state.State;

public abstract class AbstractTransition extends AbstractConnectable implements Transition {

	private ExecutablePetriNet executablePetriNet;
	private State state;
	private HashedStateBuilder builder;
	public AbstractTransition(String id, String name) {
		super(id, name);
	}

	public AbstractTransition(AbstractConnectable connectable) {
		super(connectable);
	}
	public HashedStateBuilder fire(ExecutablePetriNet executablePetriNet, State state,
			HashedStateBuilder builder) {
		this.executablePetriNet = executablePetriNet; 
		this.state = state; 
		this.builder = builder; 
		fire(); 
		consumeInboundTokens();

		State temporaryState = builder.build();

		produceOutboundTokens(temporaryState);
		return builder; 
	}


	protected void produceOutboundTokens(State temporaryState) {
		for (Arc<Transition, Place> arc : executablePetriNet.outboundArcs(this)) {
		    String placeId = arc.getTarget().getId();
		    Map<String, String> arcWeights = arc.getTokenWeights();
		    for (Map.Entry<String, String> entry : arcWeights.entrySet()) {
		        String tokenId = entry.getKey();
		        int currentCount = temporaryState.getTokens(placeId).get(tokenId);
		        int arcWeight = (int) getArcWeight(executablePetriNet, state, entry.getValue());
		        builder.placeWithToken(placeId, tokenId, addWeight(currentCount, arcWeight));
		    }
		}
	}

	protected void consumeInboundTokens() {
		for (Arc<Place, Transition> arc : executablePetriNet.inboundArcs(this)) {
		    String placeId = arc.getSource().getId();
		    Map<String, String> arcWeights = arc.getTokenWeights();
		    for (Map.Entry<String, Integer> entry : state.getTokens(placeId).entrySet()) {
		        String tokenId = entry.getKey();
		        if (arcWeights.containsKey(tokenId)) {
		            int currentCount = entry.getValue();
		            int arcWeight = (int) getArcWeight(executablePetriNet, state, arcWeights.get(tokenId));
		            builder.placeWithToken(placeId, tokenId, subtractWeight(currentCount, arcWeight));
		        }
		    }
		}
	}
    /**
     * Treats Integer.MAX_VALUE as infinity and so will not subtract the weight
     * from it if this is the case
     *
     * @param currentWeight
     * @param arcWeight
     * @return subtracted weight
     */
    protected int subtractWeight(int currentWeight, int arcWeight) {
        if (currentWeight == Integer.MAX_VALUE) {
            return currentWeight;
        }
        return currentWeight - arcWeight;
    }

    /**
     * Treats Integer.MAX_VALUE as infinity and so will not add the weight
     * to it if this is the case
     *
     * @param currentWeight
     * @param arcWeight
     * @return added weight
     */
    protected int addWeight(int currentWeight, int arcWeight) {
        if (currentWeight == Integer.MAX_VALUE) {
            return currentWeight;
        }
        return currentWeight + arcWeight;
    }
    /**
     * @param state  petri net state to evaluate weight against
     * @param weight a functional weight
     * @return the evaluated weight for the given state
     */
    protected double getArcWeight(ExecutablePetriNet executablePetriNet, State state, String weight) {
    	double result =  executablePetriNet.evaluateExpression(state, weight); 
        if (result == -1.0) {
            //TODO:
            throw new RuntimeException("Could not parse arc weight");
        }
        return result; 
    }

}
