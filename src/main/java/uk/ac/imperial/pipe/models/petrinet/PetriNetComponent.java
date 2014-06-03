package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

import java.beans.PropertyChangeListener;


public interface PetriNetComponent {


    /**
     * Message fired with the id field is set
     */
    String ID_CHANGE_MESSAGE = "id";

    /**
     * Message fired when the name field is set
     */
    String NAME_CHANGE_MESSAGE = "name";

    boolean isSelectable();

    boolean isDraggable();

    /**
     * Visitor pattern, this is particularly useful when we do not know
     * the exact type of Component, we can visit them to perform actions
     *
     * @param visitor
     */
    void accept(PetriNetComponentVisitor visitor) throws PetriNetComponentException;

    /**
     * @return objectId
     */
    String getId();

    void setId(String id);

    void addPropertyChangeListener(PropertyChangeListener listener);

    void removePropertyChangeListener(PropertyChangeListener listener);

}
