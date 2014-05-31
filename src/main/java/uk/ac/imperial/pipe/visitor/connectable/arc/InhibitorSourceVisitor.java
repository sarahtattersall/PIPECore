package uk.ac.imperial.pipe.visitor.connectable.arc;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.Connectable;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.Transition;

/**
 * A class to determine if an inhibitor arc can be built from the source
 */
public final class InhibitorSourceVisitor implements ArcSourceVisitor {

    boolean canCreate = false;

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
            e.printStackTrace();
            return false;
        }
        return canCreate;
    }
}
