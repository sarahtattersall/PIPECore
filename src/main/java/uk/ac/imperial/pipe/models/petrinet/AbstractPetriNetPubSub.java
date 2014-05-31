package uk.ac.imperial.pipe.models.petrinet;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Abstract class that implements change support for PIPE
 */
public abstract class AbstractPetriNetPubSub {
    protected PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    /**
     *
     * @param listener listener which will process all events of the implementing class
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     *
     * @param listener listener to no longer listen to events in the implementing class
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }
}
