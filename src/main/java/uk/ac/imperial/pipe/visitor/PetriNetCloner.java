package uk.ac.imperial.pipe.visitor;

import uk.ac.imperial.pipe.models.petrinet.AbstractPetriNet;
import uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet;
import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.PetriNetComponent;
import uk.ac.imperial.pipe.models.petrinet.Place;

/**
 * Class for cloning exactly a Petri net, or for refreshing an existing {@link ExecutablePetriNet} from the Petri nets of its {@link IncludeHierarchy} 
 */
public final class PetriNetCloner extends AbstractPetriNetCloner {

    protected PetriNet newPetriNet;

    protected static PetriNetCloner cloneInstance;

    /**
     *
     * @param petriNet to be cloned
     * @return  cloned Petri net
     */
    public static PetriNet clone(PetriNet petriNet) {
        cloneInstance = new PetriNetCloner(petriNet);
        return cloneInstance.clonePetriNet();
    }

    /**
     * private constructor
     * @param petriNet petri net to clone
     */
    private PetriNetCloner(PetriNet petriNet) {
        this.petriNet = petriNet;
        newPetriNet = new PetriNet();
        simpleClone = true;
    }

    /**
     *
     * Clones the petri net by visiting all its components and adding them to the new Petri net
     *
     * @return cloned Petri net
     */
    private PetriNet clonePetriNet() {
        visitAllComponents();
        return (PetriNet) newPetriNet;
    }

    @Override
    protected PetriNetCloner getInstance() {
        return cloneInstance;
    }

    @Override
    protected void prefixIdWithQualifiedName(PetriNetComponent component) {
    }

    @Override
    protected void prepareExecutablePetriNetPlaceProcessing(Place place, Place newPlace) {
    }

    @Override
    protected AbstractPetriNet getNewPetriNet() {
        return newPetriNet;
    }

}
