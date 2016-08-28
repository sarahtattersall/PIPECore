package uk.ac.imperial.pipe.io;

import uk.ac.imperial.pipe.models.petrinet.PetriNet; 

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
     * @throws javax.xml.bind.JAXBException  if there is an error in unmarshalling the Petri net
     * @throws FileNotFoundException if no file found at path
     */
    PetriNet read(String path) throws JAXBException, FileNotFoundException;
}
