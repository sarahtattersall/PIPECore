package uk.ac.imperial.pipe.models.petrinet;

import java.beans.PropertyChangeListener;
import java.util.Map;

import uk.ac.imperial.pipe.exceptions.IncludeException;


public interface Place extends Connectable, PropertyChangeListener {
    /**
     * Place diameter
     */
    int DIAMETER = 30;

    /**
     * Message fired when the places tokens change in any way
     */
    String TOKEN_CHANGE_MESSAGE = "tokens";

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
     * @param token
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
     * @param token
     */
    void decrementTokenCount(String token);

    /**
     *
     * Removes all tokens with the given id from this place
     *
     * @param token
     */
    void removeAllTokens(String token);

    /**
     * 
     * @return whether this place is in the interface for the Petri net.  
     */
	public  boolean isInInterface();

	public abstract InterfacePlace getInterfacePlace();

	public abstract void setInterfacePlace(InterfacePlace interfacePlace);

	public abstract void setInInterface(boolean inInterface);

	public PlaceStatus getStatus();
	
	public void addToInterface(IncludeHierarchy includeHierarchy); 


}
