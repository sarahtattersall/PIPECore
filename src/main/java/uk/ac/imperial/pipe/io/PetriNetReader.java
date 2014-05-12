package uk.ac.imperial.pipe.io;

import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.parsers.UnparsableException;

public interface PetriNetReader {

    PetriNet read(String path) throws UnparsableException;
}
