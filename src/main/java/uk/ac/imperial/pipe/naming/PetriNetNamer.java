package uk.ac.imperial.pipe.naming;

import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;

/**
 * This class provides names for petri nets by registering them with this class
 */
public final class PetriNetNamer extends AbstractUniqueNamer {

    /**
     * Constructor, sets the name of the Petri net to be "Petri net {@code <number>}"
     */
    public PetriNetNamer() {
        super("Petri Net ");
    }

    /**
     *
     * Registers the petri net name in the system
     *
     * @param petriNet new petri net
     */
    public void registerPetriNet(PetriNet petriNet) {
        names.add(petriNet.getNameValue());
    }

    /**
     *
     * Registers the minimally unique include name in the system
     *
     * @param include hierarchy from which minimally unique name will be taken
     */
    public void registerIncludeName(IncludeHierarchy include) {
        names.add(include.getUniqueName());
    }

    /**
     *
     * Removes the petri net name from the system
     *
     * @param petriNet existing petri net whose name can be reused
     */
    public void deRegisterPetriNet(PetriNet petriNet) {
        names.remove(petriNet.getNameValue());
    }

    /**
     *
     * Removes the minimally unique include name from the system
     *
     * @param include hierarchy from which minimally unique name can be reused
     */
    public void deRegisterIncludeName(IncludeHierarchy include) {
        names.remove(include.getUniqueName());
    }

}
