package uk.ac.imperial.pipe.naming;

import uk.ac.imperial.pipe.models.petrinet.Connectable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;

/**
 * Listens for connectable name changes and modifies the given names data structure
 */
public final class NameChangeListener implements PropertyChangeListener {

    /**
     * Already existing names
     */
    private final Collection<String> names;

    /**
     * Constructorr
     * @param names data structure to update on an ID_CHANGE_MESSAGE
     */
    NameChangeListener(Collection<String> names) {

        this.names = names;
    }

    /**
     * When a connectable changes is id this change is reflected in the names data structure
     *
     * It removes the old name and adds the new one
     *
     * @param evt event to process 
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(Connectable.ID_CHANGE_MESSAGE)) {
            String newName = (String) evt.getNewValue();
            String oldName = (String) evt.getOldValue();
            names.remove(oldName);
            names.add(newName);
        }
    }
}
