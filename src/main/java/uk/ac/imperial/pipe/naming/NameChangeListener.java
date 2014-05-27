package uk.ac.imperial.pipe.naming;

import uk.ac.imperial.pipe.models.petrinet.Connectable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;

/**
 * Listens for connectable name changes
 */
public class NameChangeListener implements PropertyChangeListener {

    private final Collection<String> names;

    NameChangeListener(Collection<String> names) {

        this.names = names;
    }

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

