package uk.ac.imperial.pipe.visitor.connectable.arc;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.Connectable;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.Transition;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class to determine if an inhibitor arc can be built from the source
 */
public final class InhibitorSourceVisitor implements ArcSourceVisitor {
    private static final Logger LOGGER = Logger.getLogger(InhibitorSourceVisitor.class.getName());

    private boolean canCreate = false;

    @Override
    public void visit(Place place) {
        canCreate = true;
    }

    @Override
    public void visit(Transition transition) {
        canCreate = false;
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
