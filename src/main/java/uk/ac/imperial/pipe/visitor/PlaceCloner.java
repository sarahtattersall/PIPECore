package uk.ac.imperial.pipe.visitor;

import uk.ac.imperial.pipe.models.component.place.DiscretePlace;
import uk.ac.imperial.pipe.models.component.place.DiscretePlaceVisitor;
import uk.ac.imperial.pipe.models.component.place.Place;

/**
 * Clones a place by visiting the place and calling the correct
 * constructor for each concrete implementation of {@link uk.ac.imperial.pipe.models.component.place.Place}
 */
public class PlaceCloner implements DiscretePlaceVisitor {
    public Place cloned = null;

    @Override
    public void visit(DiscretePlace discretePlace) {
        cloned = new DiscretePlace(discretePlace);
    }

}
