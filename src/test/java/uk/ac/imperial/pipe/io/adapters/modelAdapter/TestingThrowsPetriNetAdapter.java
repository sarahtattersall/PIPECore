package uk.ac.imperial.pipe.io.adapters.modelAdapter;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.io.adapters.model.AdaptedPetriNet;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;

public class TestingThrowsPetriNetAdapter extends PetriNetAdapter {

    @Override
    public PetriNet unmarshal(AdaptedPetriNet v)
            throws PetriNetComponentException {
        throw new RuntimeException("TestingThrowsPetriNetAdapter exception.");
    }

}
