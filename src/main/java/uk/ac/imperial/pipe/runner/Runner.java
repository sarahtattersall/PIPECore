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

}
