package uk.ac.imperial.pipe.animation;

import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.state.HashedStateBuilder;
import uk.ac.imperial.state.State;

/**
 * Utility class for useful static methods for animation
 */
public final class AnimationUtils {
    /**
     * Private utility constructor
     */
    private AnimationUtils() {}

    /**
     *
     * @param petriNet current Petri net
     * @return Markov chain state representing the current placing of tokens in the Petri net
     */
    public static State getState(PetriNet petriNet) {
        HashedStateBuilder builder = new HashedStateBuilder();
        for (Place place : petriNet.getPlaces()) {
            for (Token token : petriNet.getTokens()) {
                builder.placeWithToken(place.getId(), token.getId(), place.getTokenCount(token.getId()));
            }
        }
        return builder.build();
    }

}
