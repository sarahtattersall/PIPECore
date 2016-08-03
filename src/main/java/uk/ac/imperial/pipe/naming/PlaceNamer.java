package uk.ac.imperial.pipe.naming;

import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;

/**
 * A class that attempts to produce names for {@link uk.ac.imperial.pipe.models.petrinet.DiscretePlace}
 * that are distinct from others.
 */
public class PlaceNamer extends ComponentNamer {

    /**
     * Constructor sets the name of the places in the Petri net to be in the form "P{@code <number>}"
     * @param petriNet of the places to be named 
     */
    public PlaceNamer(PetriNet petriNet) {
        super(petriNet, "P", PetriNet.NEW_PLACE_CHANGE_MESSAGE, PetriNet.DELETE_PLACE_CHANGE_MESSAGE);
        initialisePlaceNames();
    }

    /**
     * Initialises the names list with places already stored in the Petri net
     */
    private void initialisePlaceNames() {
        for (Place place : petriNet.getPlaces()) {
            place.addPropertyChangeListener(nameListener);
            names.add(place.getId());
        }
    }
}
