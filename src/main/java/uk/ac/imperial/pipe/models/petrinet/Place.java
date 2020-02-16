package uk.ac.imperial.pipe.models.petrinet;

import java.beans.PropertyChangeListener;
import java.util.Map;

public interface Place extends Connectable, PropertyChangeListener {

    /**
     * Place diameter
     */
    int DIAMETER = 30;

    /**
     * Message fired when place is being deleted so listeners stop listening
     */
    public static final String REMOVE_PLACE_MESSAGE = "remove place";

    /**
     * Message fired when the places tokens change in any way
     */
    public static final String TOKEN_CHANGE_MESSAGE = "tokens";

    /**
     * Message fired when mirroring token changes in another place 
     */
    public static final String TOKEN_CHANGE_MIRROR_MESSAGE = "tokens mirrored";
    /**
     * Message fired when the place capacity changes
     */
    public static final String CAPACITY_CHANGE_MESSAGE = "capacity";

    double getMarkingXOffset();

    void setMarkingXOffset(double markingXOffset);

    double getMarkingYOffset();

    void setMarkingYOffset(double markingYOffset);

    int getCapacity();

    void setCapacity(int capacity);

    Map<String, Integer> getTokenCounts();

    void setTokenCounts(Map<String, Integer> tokenCounts);

    boolean hasCapacityRestriction();

    /**
     * Increments the token count of the given token
     *
     * @param token to be incremented 
     */
    void incrementTokenCount(String token);

    void setTokenCount(String token, int count);

    /**
     * @return the number of tokens currently stored in this place
     */
    int getNumberOfTokensStored();

    int getTokenCount(String token);

    /**
     * Decrements the count of the token by one in this place
     * @param token to be decremented
     */
    void decrementTokenCount(String token);

    /**
     *
     * Removes all tokens with the given id from this place
     *
     * @param token whose count is to be zero
     */
    void removeAllTokens(String token);

    /**
     * 
     * @return whether this place is in the interface for the Petri net.  
     */
    public boolean isInInterface();

    public abstract void setInInterface(boolean inInterface);

    public void addToInterface(IncludeHierarchy includeHierarchy);

    public PlaceStatus getStatus();

    public void setStatus(PlaceStatus status);

    public void removeSelfFromListeners();

    boolean equalsState(Place place);

}
