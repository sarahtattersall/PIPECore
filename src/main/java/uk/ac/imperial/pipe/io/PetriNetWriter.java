package uk.ac.imperial.pipe.io;

import uk.ac.imperial.pipe.models.petrinet.PetriNet;

import javax.xml.bind.JAXBException;

import java.io.IOException;
import java.io.Writer;

/**
 * API for writing Petri nets
 */
public interface PetriNetWriter {

    /**
     * Write the petri net to the given path
     * @param path  where the file is to be written
     * @param petriNet  to be written
     * @throws IOException if error occurs while writing the Petri net
     * @throws JAXBException if error occurs while marshalling the Petri net
     */
    void writeTo(String path, PetriNet petriNet) throws JAXBException, IOException;

    /**
     * Write the Petri net to the given stream
     * @param stream to which the Petri net is to be written
     * @param petriNet to be written
     * @throws JAXBException if error occurs while marshalling the Petri net
     */
    void writeTo(Writer stream, PetriNet petriNet) throws JAXBException;

}
