package uk.ac.imperial.pipe.visitor;

import uk.ac.imperial.pipe.models.petrinet.DiscretePlace;
import uk.ac.imperial.pipe.models.petrinet.DiscretePlaceVisitor;
import uk.ac.imperial.pipe.models.petrinet.Place;

/**
 * Clones a place by visiting the place and calling the correct
 * constructor for each concrete implementation of {@link uk.ac.imperial.pipe.models.petrinet.Place}
 */
public class PlaceCloner implements DiscretePlaceVisitor {
    public Place cloned = null;

    @Override
    public void visit(DiscretePlace discretePlace) {
        cloned = new DiscretePlace(discretePlace);
    }

}
