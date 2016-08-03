package uk.ac.imperial.pipe.visitor.connectable.arc;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.Connectable;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.Transition;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Determines if a normal arc can be created from the source component.
 *
 * Normal arcs can be created from Places and Transitions
 */
public final class NormalArcSourceVisitor implements ArcSourceVisitor {

    /**
     * Class logger
     */
    private static final Logger LOGGER = Logger.getLogger(NormalArcSourceVisitor.class.getName());

    /**
     * Defaults to false
     */
    private boolean canCreate = false;

    /**
     * Can create normal arc from any place
     * @param place to be evaluated for normal arc creation
     */
    @Override
    public void visit(Place place) {
        canCreate = true;
    }

    /**
     * Can create normal arc from any transition
     * @param transition to be evaluated for normal arc creation
     */
    @Override
    public void visit(Transition transition) {
        canCreate = true;
    }

    /**
     * @return the result of the last item visited
     */
    @Override
    public boolean canStart(Connectable connectable) {
        try {
            connectable.accept(this);
        } catch (PetriNetComponentException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            return false;
        }
        return canCreate;
    }

}