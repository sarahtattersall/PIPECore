package uk.ac.imperial.pipe.visitor.connectable.arc;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.Connectable;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.Transition;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class NormalArcSourceVisitor implements ArcSourceVisitor {

    private static final Logger LOGGER = Logger.getLogger(NormalArcSourceVisitor.class.getName());

    private boolean canCreate = false;

    @Override
    public void visit(Place place) {
        canCreate = true;
    }

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