package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.state.State;

import java.util.HashMap;
import java.util.Map;

/**
 * A test arc maps from places to tokens and is allowed to fire
 * if and only if its source place has any tokens whatsoever.
 */
public class InboundTestArc extends InboundArc {
    /**
     * Constructor
     * @param source of the arc
     * @param target of the arc
     */
    public InboundTestArc(Place source, Transition target) {
        super(source, target, new HashMap<String, String>(), ArcType.TEST);
    }

    /**
     * Analyses the state to see if the arcs source has any tokens
     *
     * @param executablePetriNet to be evaluated
     * @param state to be evaluated 
     * @return true if the arc can fire
     */
    //FIXME  this won't work as anticipated if one color has non-zero and another has zero.  
    @Override
    public boolean canFire(ExecutablePetriNet executablePetriNet, State state) {
        Map<String, Integer> tokens = state.getTokens(getSource().getId());
        for (Integer tokenCount : tokens.values()) {
            if (tokenCount == 0) {
                return false;
            }
        }
        return true;
    }
}
