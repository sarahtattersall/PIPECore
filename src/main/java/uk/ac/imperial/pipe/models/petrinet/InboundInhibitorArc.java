package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.state.State;

import java.util.HashMap;
import java.util.Map;

/**
 * An inhibitor arc maps from places to tokens and is allowed to fire
 * if and only if its source place has no tokens whatsoever.
 */
public class InboundInhibitorArc extends InboundArc {
    /**
     * Constructor
     * @param source
     * @param target
     */
    public InboundInhibitorArc(Place source, Transition target) {
        super(source, target, new HashMap<String, String>(), ArcType.INHIBITOR);
    }

    /**
     * Analyses the state to see if the arcs source has no tokens
     *
     * @param petriNet
     * @param state
     * @return true if the arc can fire
     */
	@Override
	public boolean canFire(ExecutablePetriNet executablePetriNet, State state) {
		Map<String, Integer> tokens = state.getTokens(getSource().getId());
		for (Integer tokenCount : tokens.values()) {
			if (tokenCount != 0) {
				return false;
			}
		}
		return true;
	}
}
