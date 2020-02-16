package uk.ac.imperial.pipe.io.adapters.modelAdapter;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.io.adapters.model.AdaptedPetriNet;
import uk.ac.imperial.pipe.models.petrinet.PetriNetComponent;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Marshals a Petri net into the verbose format needed for PNML
 */
public class PetriNetAdapter extends XmlAdapter<AdaptedPetriNet, PetriNet> {
    /**
     *
     * @param v Petri net to be unmarshalled
     * @return unmarshaled Petri net
     * @throws PetriNetComponentException if the Petri net structure is not valid
     */
    @Override
    public PetriNet unmarshal(AdaptedPetriNet v) throws PetriNetComponentException {
        PetriNet petriNet = new PetriNet();
        if (v.id != null) {
            petriNet.setName(new NormalPetriNetName(v.id));
        }
        addToPetriNet(v.tokens, petriNet);
        addToPetriNet(v.annotations, petriNet);
        addToPetriNet(v.places, petriNet);
        addToPetriNet(v.rateParameters, petriNet);
        addToPetriNet(v.transitions, petriNet);
        addToPetriNet(v.arcs, petriNet);
        return petriNet;
    }

    /**
     *
     * @param v to be marshalled
     * @return marshaled Petri net
     */
    @Override
    public AdaptedPetriNet marshal(PetriNet v) {
        AdaptedPetriNet petriNet = new AdaptedPetriNet();
        if (!(v.getNameValue().trim().equals(""))) {
            petriNet.id = v.getNameValue();
        }
        petriNet.tokens = v.getTokens();
        petriNet.annotations = v.getAnnotations();
        petriNet.rateParameters = v.getRateParameters();
        for (Place place : v.getPlaces()) {
            petriNet.places.add(place);
        }
        //        petriNet.places = v.getPlaces();
        petriNet.transitions = v.getTransitions();
        petriNet.arcs = v.getArcs();
        return petriNet;
    }

    /**
     * Adds components to the Petri net
     * @param components to add 
     * @param petriNet to build 
     * @throws PetriNetComponentException if the Petri net structure is not valid
     */
    private void addToPetriNet(Iterable<? extends PetriNetComponent> components, PetriNet petriNet)
            throws PetriNetComponentException {
        if (components != null) {
            for (PetriNetComponent component : components) {
                petriNet.add(component);
            }
        }
    }
}
