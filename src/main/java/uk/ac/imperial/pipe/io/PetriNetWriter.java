package uk.ac.imperial.pipe.io;

import uk.ac.imperial.pipe.models.petrinet.PetriNet;

import java.io.Writer;

public interface PetriNetWriter {

    void writeTo(String path, PetriNet petriNet);

    void writeTo(Writer stream, PetriNet petriNet);

}
