package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

import java.beans.PropertyChangeListener;


public interface PetriNetComponent {


    /**
     * Message fired with the id field is set
     */
    public String ID_CHANGE_MESSAGE = "id";

    /**
     * Message fired when the name field is set
     */
    public String NAME_CHANGE_MESSAGE = "name";

    public boolean isSelectable();

    public boolean isDraggable();

    /**
     * Visitor pattern, this is particularly useful when we do not know
     * the exact type of Component, we can visit them to perform actions
     *
     * @param visitor
     */
    public void accept(PetriNetComponentVisitor visitor) throws PetriNetComponentException;

    /**
     * @return objectId
     */
    public String getId();

    public void setId(String id);

    /**
     *
     * @param listener listener which will process all events of the implementing class
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     *
     * @param listener listener to no longer listen to events in the implementing class
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);

}
