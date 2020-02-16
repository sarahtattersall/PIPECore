package uk.ac.imperial.pipe.models.petrinet;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.state.State;

/**
 * An inhibitor arc maps from places to tokens and is allowed to fire
 * if and only if its source place has no tokens whatsoever.
 */
public class InboundInhibitorArc extends InboundArc {
    protected static Logger logger = LogManager.getLogger(InboundInhibitorArc.class);

    /**
     * Constructor
     * @param source connectable of the arc
     * @param target connectable of the arc
     */
    public InboundInhibitorArc(Place source, Transition target) {
        super(source, target, new HashMap<String, String>(), ArcType.INHIBITOR);
    }

    /**
     * Analyses the state to see if the arcs source has no tokens
     *
     * @param executablePetriNet to be evaluated
     * @param state of the Petri net
     * @return true if the arc can fire
     */
    //TODO:  should this behave differently if there are multiple colors, and some colors have counts while others don't?
    @Override
    public boolean canFire(ExecutablePetriNet executablePetriNet, State state) {
        Map<String, Integer> tokens = state.getTokens(getSource().getId());
        if (tokens == null) {
            logger.error("No tokens found for inbound inhibitor arc with source: " + getSource().getId() +
                    "; likely logic error.");
            return true;
        }
        for (Integer tokenCount : tokens.values()) {
            if (tokenCount != 0) {
                return false;
            }
        }
        return true;
    }
}
