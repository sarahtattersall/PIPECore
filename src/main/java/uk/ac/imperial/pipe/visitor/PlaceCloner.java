package uk.ac.imperial.pipe.visitor;

import uk.ac.imperial.pipe.models.petrinet.DiscretePlace;
import uk.ac.imperial.pipe.models.petrinet.DiscretePlaceVisitor;
import uk.ac.imperial.pipe.models.petrinet.Place;

/**
 * Clones a place by visiting the place and calling the correct
 * constructor for each concrete implementation of {@link uk.ac.imperial.pipe.models.petrinet.Place}
 */
public final class PlaceCloner implements DiscretePlaceVisitor {
    /**
     * Cloned place, null before visit is called
     */
    public Place cloned = null;

    /**
     * Clones a discrete place
     * @param discretePlace to be visited 
     */
    @Override
    public void visit(DiscretePlace discretePlace) {
        cloned = new DiscretePlace(discretePlace);
    }

}
