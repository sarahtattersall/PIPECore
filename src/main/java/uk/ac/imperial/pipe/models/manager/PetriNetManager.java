package uk.ac.imperial.pipe.models.manager;

import uk.ac.imperial.pipe.exceptions.IncludeException;
import uk.ac.imperial.pipe.io.PetriNetFileException;
import uk.ac.imperial.pipe.io.XmlFileEnum;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.parsers.UnparsableException;

import javax.xml.bind.JAXBException;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Responsible for creating and managing Petri nets
 * It stores the nets it creates for easy retrial and can notify
 * listeners on changes to its structure
 */
public interface PetriNetManager {

    /**
     * Creates a new Petri net and stores it for retrieval later
     */
    void createNewPetriNet();

    /**
     * Registers a listener for petri net change events
     * @param listener notify this listener on any changes
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes a listener, after this call it will no longer be called
     * on change events
     * @param listener registered listener that no longer wishes to be notified
     *                 on change
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Returns the last Petri net that it holds, this will be the most recently created
     * Petri net (that has not been deleted)
     * @return {@link PetriNet} last Petri net that was added 
     */
    PetriNet getLastNet();

    /**
     * Creates one or more Petri nets by reading in and parsing the contents of the file
     * If the file is in PNML format, a single Petri net is created with a name corresponding to the 
     * file name, without its suffix.  If the file is in include 
     * format, one or more Petri nets are created, corresponding to each level of the include hierarchy.
     * The names of the Petri nets are the minimally unique include names. 
     * @param file location of xml file
     * @throws JAXBException if error during unmarshalling
     * @throws UnparsableException if rate parameter expression cannot be parsed 
     * @throws PetriNetFileException if the file does not exist, or is not valid XML, 
     * or whose highest level tags are not <code>pnml</code> or <code>include</code>   
     * @throws IncludeException if errors are encountered building an include hierarchy 
     * @throws FileNotFoundException if one of the referenced files does not exist 
     */
    void createFromFile(File file)
            throws JAXBException, UnparsableException, PetriNetFileException, IncludeException, FileNotFoundException;

    /**
     *
     * Saves the specified petri net to the location
     *
     * @param petriNet petri net to save
     * @param outFile file to save petri net to
     * @throws IOException if IO error while writing 
     * @throws JAXBException if error during marshalling
     */
    //TODO: SHOULD REALLY TELL IT TO SAVE ONE OF ITS OWN PETRI NETS RATHER THAN PASSING IT IN
    void savePetriNet(PetriNet petriNet, File outFile) throws JAXBException, IOException;

    /**
     * Remove this Petri net from storage
     * @param petriNet to be removed 
     */
    void remove(PetriNet petriNet);

}
