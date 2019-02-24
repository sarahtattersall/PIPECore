package uk.ac.imperial.pipe.runner;

import java.beans.PropertyChangeListener;

import uk.ac.imperial.pipe.models.petrinet.Place;

public interface Runner extends PlaceMarker {

    public void run();

    public void setFiringLimit(int firingLimit);

    public void setSeed(long seed);

    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Registers a listener for any token changes to the {@link Place}.  The listener will be called both when a token is produced
     * and when a token is consumed.
     *
     * Execution of the Runner may continue immediately upon listener notification.  The order in which the Runner and any listener
     * execute is dependent on the listener's system.  If the listener must complete some
     * processing before Runner continues execution, use {@link #listenForTokenChanges(PropertyChangeListener, String, boolean)},
     * and call {@link #acknowledge(String)} when the required execution is complete.
     * @param listener to be notified when the token count has changed for Place with id placeId
     * @param placeId the id of the {@link Place} to be monitored for token changes
     * @throws InterfaceException if placeId does not exist in the Petri net or is not externally accessible
     */

    public void listenForTokenChanges(PropertyChangeListener listener, String placeId) throws InterfaceException;

    /**
     * Same as {@link #listenForTokenChanges(PropertyChangeListener, String)}, except that if acknowledgement is true,
     * Runner will not proceed (will not fire additional transitions) until {@link #acknowledge(String)} is called.
     *
     * This is intended to enable running a Petri net in lock step with an external system.
     *
     * @param listener to be notified when the token count has changed for Place with id placeId
     * @param placeId the id of the {@link Place} to be monitored for token changes
     * @param acknowledgement  waits if true; if false, identical to {@link #listenForTokenChanges(PropertyChangeListener, String)}.
     * @throws InterfaceException if placeId does not exist in the Petri net or is not externally accessible
     */
    public void listenForTokenChanges(PropertyChangeListener listener, String placeId, boolean acknowledgement)
            throws InterfaceException;

    public void setTransitionContext(String transitionId, Object object);

    public void setWaitForExternalInput(boolean wait);

    /**
     * Impose a delay before firing each transition.  Useful when interacting asynchronously
     * with an external system, to avoid spending CPU resources on tight loops.  May be changed
     * multiple times during execution of a Petri net.  Defaults to no delay.
     * <p>
     * This is not an implementation of timed transitions.  Semantics of the Petri net are unchanged,
     * except that overall execution time is slowed by milliseconds x number of fired transitions
     * @param milliseconds to wait between each firing
     */
    public void setFiringDelay(int milliseconds);

    /**
     * Called by an external system to indicate that processing is complete following the notification of the listening
     * system that a place has been marked {@link #listenForTokenChanges(PropertyChangeListener, String, boolean)}
     */
    public void acknowledge();

    /**
     * Called by an external system to indicate that processing is complete following the notification of the listening
     * system that the marking for a place has changed (received an event for property name {@link uk.ac.imperial.pipe.models.petrinet.Place#TOKEN_CHANGE_MESSAGE})
     *
     * Used in conjunction with {@link #listenForTokenChanges(PropertyChangeListener, String, boolean)}
     * to explicitly synchronize the processing of the external system with the firing of the Petri net.
     * For example, Matlab defaults to processing listener events in batches, which may lead to unexpected behavior.
     * @param placeId for which acknowledgement is being made
     */
    public void acknowledge(String placeId);

    /**
     * returns a list of places in the executable Petri net with their current token counts
     * @param markedPlaces if true, list of places only includes those with at least one non-zero token count
     * @return sorted list of places with the counts for each token
     */
    public String getPlacesReport(boolean markedPlaces);

}
