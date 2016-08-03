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
     * @param visitor to be accepted
     * @throws PetriNetComponentException if component not found or other logic error
     */
    void accept(PetriNetComponentVisitor visitor) throws PetriNetComponentException;

    /**
     * @return objectId of this component
     * @return component Id
     */
    String getId();

    /**
     * 
     * @param id of the component
     */
    void setId(String id);
    /**
     * 
     * @param listener to be added 
     */
    void addPropertyChangeListener(PropertyChangeListener listener);
    /**
     * 
     * @param listener to be removed 
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

}
