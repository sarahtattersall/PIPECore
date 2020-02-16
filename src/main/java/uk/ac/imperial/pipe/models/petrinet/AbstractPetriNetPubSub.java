package uk.ac.imperial.pipe.models.petrinet;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Abstract class that implements change support for PIPE
 */
public abstract class AbstractPetriNetPubSub {
    public final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    /**
     *
     * @param listener listener which will process all events of the implementing class
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     *
     * @param propertyName name of the events to be listened for
     * @param listener listener which will process propertyName events of the implementing class
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     *
     * @param listener listener to no longer listen to events in the implementing class
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    public void removeAllListeners() {
        PropertyChangeListener[] listeners = changeSupport.getPropertyChangeListeners();
        for (int i = 0; i < listeners.length; i++) {
            removePropertyChangeListener(listeners[i]);
        }
    }
}
