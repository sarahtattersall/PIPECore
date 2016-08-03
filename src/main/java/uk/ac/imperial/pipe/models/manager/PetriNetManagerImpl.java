package uk.ac.imperial.pipe.models.manager;

import uk.ac.imperial.pipe.io.PetriNetIOImpl;
import uk.ac.imperial.pipe.io.PetriNetReader;
import uk.ac.imperial.pipe.models.PetriNetHolder;
import uk.ac.imperial.pipe.models.petrinet.ColoredToken;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;
import uk.ac.imperial.pipe.models.petrinet.name.PetriNetFileName;
import uk.ac.imperial.pipe.models.petrinet.name.PetriNetName;
import uk.ac.imperial.pipe.naming.PetriNetNamer;
import uk.ac.imperial.pipe.parsers.UnparsableException;

import javax.xml.bind.JAXBException;
import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Manages addition and deletion of Petri nets. It uses the publish-subscribe design
 * pattern to notify observers of additons and deletions of Petri nets.
 */
public final class PetriNetManagerImpl implements PetriNetManager {
    /**
     * Message fired to listeners when a new petri net is created
     */
    public static final String NEW_PETRI_NET_MESSAGE = "New Petri net!";

    /**
     * Message fired when when you remove the Petri net from the manager
     */
    public static final String REMOVE_PETRI_NET_MESSAGE = "Removed Petri net";

    /**
     * Responsible for creating unique names for Petri nets
     */
    private final PetriNetNamer petriNetNamer = new PetriNetNamer();

    /**
     * Container for holding created Petri nets
     */
    private final PetriNetHolder holder = new PetriNetHolder();

    /**
     * Fires Petri net changes
     */
    protected final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);


    /**
     *
     * @param listener notify this listener on any changes
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     *
     * @param listener registered listener that no longer wishes to be notified
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);

    }

    /**
     * Return the last Petri net created
     */
    @Override
    public PetriNet getLastNet() {
        if (!holder.isEmpty()) {
            return holder.getNet(holder.size() - 1);
        }
        throw new RuntimeException("No Petri nets stored in the manager");
    }

    /**
     *
     * Loads the Petri net from the file and adds it to the internal holder,
     * firing a change message to indicate a Petri net has been added
     *
     * @param file location of Petri net xml file
     * @throws JAXBException if Petri net cannot be unmarshalled
     * @throws UnparsableException  if rate parameter expression cannot be parsed 
     * @throws FileNotFoundException  if file not found
     */
    @Override
    public void createFromFile(File file) throws JAXBException, UnparsableException, FileNotFoundException {
        PetriNetReader petriNetIO = new PetriNetIOImpl();
        PetriNet petriNet = petriNetIO.read(file.getAbsolutePath());
        namePetriNetFromFile(petriNet, file);
        changeSupport.firePropertyChange(NEW_PETRI_NET_MESSAGE, null, petriNet);
    }

    /**
     * Save the petri net to the output file
     * @param petriNet petri net to save
     * @param outFile file to save petri net to
     * @throws JAXBException if Petri net cannot be marshalled 
     * @throws IOException if IO error while writing 
     */
    @Override
    public void savePetriNet(PetriNet petriNet, File outFile) throws JAXBException, IOException {

        uk.ac.imperial.pipe.io.PetriNetWriter writer = new PetriNetIOImpl();
        writer.writeTo(outFile.getAbsolutePath(), petriNet);
        petriNetNamer.deRegisterPetriNet(petriNet);
        namePetriNetFromFile(petriNet, outFile);
    }

    /**
     * Removes the petri net from the internal holder, firing a change message that it
     * has been deleted
     * @param petriNet to be removed 
     */
    @Override
    public void remove(PetriNet petriNet) {
        holder.remove(petriNet);
        changeSupport.firePropertyChange(REMOVE_PETRI_NET_MESSAGE, petriNet, null);
    }

    /**
     * Sets the petri nets name to the name of the file
     * @param petriNet
     * @param file
     */
    private void namePetriNetFromFile(PetriNet petriNet, File file) {
        PetriNetName petriNetName = new PetriNetFileName(file);
        petriNet.setName(petriNetName);
        petriNetNamer.registerPetriNet(petriNet);
    }

    /**
     * Creates a new Petri net and adds a Default black token to it.
     */
    @Override
    public void createNewPetriNet() {
        PetriNet petriNet = new PetriNet();
        namePetriNet(petriNet);
        petriNet.addToken(createDefaultToken());
        changeSupport.firePropertyChange(NEW_PETRI_NET_MESSAGE, null, petriNet);
        holder.addNet(petriNet);
    }

    /**
     *
     * @return token with id "Default" and color black
     */
    private Token createDefaultToken() {
        return new ColoredToken("Default", Color.BLACK);
    }

    /**
     * Names the petri net with a unique name
     * Adds petri net to the unique namer so not to produce the same name twice
     *
     * @param petriNet petri net to name
     */
    private void namePetriNet(PetriNet petriNet) {
        String name = petriNetNamer.getName();
        PetriNetName petriNetName = new NormalPetriNetName(name);
        petriNet.setName(petriNetName);
        petriNetNamer.registerPetriNet(petriNet);
    }
}
