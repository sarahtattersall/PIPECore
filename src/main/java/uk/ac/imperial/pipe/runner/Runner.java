package uk.ac.imperial.pipe.runner;

import java.beans.PropertyChangeListener;

public interface Runner extends PlaceMarker {

    public void run();

    public void setFiringLimit(int firingLimit);

    public void setSeed(long seed);

    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void listenForTokenChanges(PropertyChangeListener listener, String placeId) throws InterfaceException;

    public void setTransitionContext(String transitionId, Object object);

    public void setWaitForExternalInput(boolean wait);

    /**
     * Impose a delay before firing each transition.  Useful when interacting asynchronously
     * with an external system, to avoid spending CPU resources on tight loops.  May be changed
     * multiple times during execution of a Petri net.  Defaults to no delay.
     * <p>
     * This is not an implementation of timed transitions.  Semantics of the Petri net are unchanged,
     * except that overall execution time is slowed by milliseconds x number of fired transitions
     * @param milliseconds
     */
    public void setFiringDelay(int milliseconds);

}
