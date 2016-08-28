package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

/**
 * Place visitor
 */
public interface PlaceVisitor extends PetriNetComponentVisitor {
    /**
     * Visit the place
     * @param place to be added 
     * @throws PetriNetComponentException if component already exists or other logic error 
     */
    void visit(Place place) throws PetriNetComponentException;
}
