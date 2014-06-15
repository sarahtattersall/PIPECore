package uk.ac.imperial.pipe.io;

import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.parsers.UnparsableException;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;

/**
 * API for reading Petri nets
 */
public interface PetriNetReader {

    /**
     * Read a petri net from the given path
     * @param path this path must point to an xml file that contains a Petri net in PNML format
     * @return the read Petri net
     * @throws UnparsableException
     */
    PetriNet read(String path) throws JAXBException, FileNotFoundException;
}
