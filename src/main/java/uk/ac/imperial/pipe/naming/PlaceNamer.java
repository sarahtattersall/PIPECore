package uk.ac.imperial.pipe.naming;

import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;

/**
 * A class that attempts to produce names for {@link uk.ac.imperial.pipe.models.petrinet.DiscretePlace}
 * that are distinct from others.
 */
public class PlaceNamer extends ComponentNamer {

    public PlaceNamer(PetriNet petriNet) {
        super(petriNet, "P", PetriNet.NEW_PLACE_CHANGE_MESSAGE, PetriNet.DELETE_PLACE_CHANGE_MESSAGE);
        initialisePlaceNames();
    }

    private void initialisePlaceNames() {
        for (Place place : petriNet.getPlaces()) {
            place.addPropertyChangeListener(nameListener);
            names.add(place.getId());
        }
    }
}
