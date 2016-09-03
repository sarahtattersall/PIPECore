package uk.ac.imperial.pipe.io;

import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import uk.ac.imperial.pipe.models.petrinet.PetriNet;

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
    
    /**
     * Searches XML retrieved from the file location for presence of tags identifying the type of 
     * the file:  <code>pnml</code> for a Petri net, or <code>include</code> for an include hierarchy 
     * @param path to an xml file to evaluate
     * @return xmlFileEnum for Petri net or include hierarchy
     * @throws PetriNetFileException if the file does not exist at path, or is not valid XML, 
     * or whose highest level tags are not <code>pnml</code> or <code>include</code>   
     */
	XmlFileEnum determineFileType(String path) throws PetriNetFileException;

}
