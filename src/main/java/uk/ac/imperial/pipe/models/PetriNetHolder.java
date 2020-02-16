package uk.ac.imperial.pipe.models;

import uk.ac.imperial.pipe.io.adapters.modelAdapter.PetriNetAdapter;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * This class just holds all petri nets and forms the base level of PNML
 */
@XmlRootElement(name = "pnml")
public final class PetriNetHolder {
    /**
     * A list of petri nets in this holder
     */
    @XmlJavaTypeAdapter(PetriNetAdapter.class)
    @XmlElement(name = "net")
    private final List<PetriNet> nets = new ArrayList<>();

    /**
     * Add the net to the end of the holder i.e. inserted in order
     * @param net to be added 
     */
    public void addNet(PetriNet net) {
        nets.add(net);
    }

    /**
     *
     *
     *
     * @param index index of the net added in order
     * @return Petri net at the index
     */
    public PetriNet getNet(int index) {
        return nets.get(index);
    }

    /**
     *
     * @return the number of Petri nets stored in this holder
     */
    public int size() {
        return nets.size();
    }

    /**
     *
     * @return true if holder contains no Petri nets
     */
    public boolean isEmpty() {
        return nets.isEmpty();
    }

    /**
     * Removes the petriNet from this holder
     * @param petriNet to be removed 
     */
    public void remove(PetriNet petriNet) {
        nets.remove(petriNet);
    }
}
