package uk.ac.imperial.pipe.dsl;

import uk.ac.imperial.pipe.models.petrinet.DiscretePlace;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.petrinet.PlaceStatusInterface;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.Transition;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * APlace DSL to be used in conjunction with {@link uk.ac.imperial.pipe.dsl.APetriNet}
 * Usage:
 * APlace.withId("P0").andCapacity(5).containing(5, "Red).tokens();
 */
public final class APlace implements DSLCreator<Place> {
    /**
     * Place id
     */
    private String id;

    /**
     * Place capacity (defaults to 0)
     */
    private int capacity = 0;

    /**
     * Place token counts (defaults to no tokens)
     */
    private Map<String, Integer> tokenCounts = new HashMap<>();

    /**
     * Place x location
     */
    private int x = 0;

    /**
     * Place y location
     */
    private int y = 0;

    /**
     * Flag indicating whether Place is in Interface, i.e. has {@link PlaceStatusInterface}
     */
    private boolean interfaceStatus = false;

    /**
     * Place is externally accessible, i.e. {@link PlaceStatusInterface#isExternal()} is true
     */
    private boolean external = false;

    /**
     * Hidden constructor
     * @param id place id
     */
    private APlace(String id) {
        this.id = id;
    }

    /**
     *
     * Factory for the place
     * @param id of the place
     * @return builder for chaining
     */
    public static APlace withId(String id) {
        return new APlace(id);
    }

    /**
     * Sets the place capacity
     * @param capacity of the place
     * @return builder for chaining
     */
    public APlace andCapacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    /**
     * Sets the places token count for the specified type of tokens
     * @param count for token
     * @param tokenId of the token
     * @return builder for chaining
     */
    public APlace containing(int count, String tokenId) {
        tokenCounts.put(tokenId, count);
        return this;
    }

    /**
     * Added for readability
     * E.g. containing(5, "Default).tokens()
     * @return builder for chaining
     */
    public APlace tokens() {
        return this;
    }

    /**
     * Added for readability
     * E.g. containing(1, "Default).token()
     * @return builder for chaining
     */
    public APlace token() {
        return this;
    }

    /**
     *
     * Creates a discrete place
     *
     * @param tokens map of created tokens with id of Token
     * @param places map of created places with id of Connectable
     * @param transitions map of created transitions with id of Transition
     * @param rateParameters map of created rateParameters with id of rateParameter
     * @return created place
     */
    @Override
    public Place create(Map<String, Token> tokens, Map<String, Place> places, Map<String, Transition> transitions,
            Map<String, FunctionalRateParameter> rateParameters) {
        Place place = new DiscretePlace(id, id);
        place.setX(x);
        place.setY(y);

        place.setCapacity(capacity);
        place.setTokenCounts(tokenCounts);

        if (interfaceStatus) {
            PlaceStatusInterface status = new PlaceStatusInterface(place);
            status.setExternal(external);
            place.setStatus(status);
        }

        places.put(id, place);
        return place;
    }

    /**
     * Chains adding tokens
     * E.g.
     * contains(1, "Red").token().and(2, "Blue").tokens();
     * @param count token count
     * @param tokenName token name
     * @return instance of APlace for chaining
     */
    public APlace and(int count, String tokenName) {
        tokenCounts.put(tokenName, count);
        return this;
    }

    /**
     * Set the place net x, y locations
     * @param x coordinate
     * @param y coordinate
     * @return builder for chaining
     */
    public APlace locatedAt(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * This place will be externally accessible, i.e., {@link PlaceStatusInterface#isExternal()} is true
     * @return builder for chaining 
     */
    public APlace externallyAccessible() {
        interfaceStatus = true;
        external = true;
        return this;
    }
}
