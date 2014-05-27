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

public class PetriNetManagerImpl implements PetriNetManager {
    /**
     * Message fired to listeners when a new petri net is created
     */
    public static final String NEW_PETRI_NET_MESSAGE = "New Petri net!";

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



    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

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

    @Override
    public void createFromFile(File file) throws JAXBException, UnparsableException {
        PetriNetReader petriNetIO = new PetriNetIOImpl();
        PetriNet petriNet = petriNetIO.read(file.getAbsolutePath());
        namePetriNetFromFile(petriNet, file);
        changeSupport.firePropertyChange(NEW_PETRI_NET_MESSAGE, null, petriNet);
    }

    @Override
    public void savePetriNet(PetriNet petriNet, File outFile) throws JAXBException {

        uk.ac.imperial.pipe.io.PetriNetWriter writer = new PetriNetIOImpl();
        writer.writeTo(outFile.getAbsolutePath(), petriNet);
        petriNetNamer.deRegisterPetriNet(petriNet);
        namePetriNetFromFile(petriNet, outFile);
    }

    @Override
    public void remove(PetriNet petriNet) {
        holder.remove(petriNet);
        changeSupport.firePropertyChange(REMOVE_PETRI_NET_MESSAGE, petriNet, null);
    }

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
