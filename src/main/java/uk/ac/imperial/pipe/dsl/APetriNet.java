package uk.ac.imperial.pipe.dsl;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.PetriNetComponent;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

// Create:
// APetriNet.with(aToken("Red")).andAPlace("P0").containing(5, "Red")

/**
 * Usage:
 * APetriNet.with(AToken.withName("Red").andColor(Color.RED))
 *          .and(APlace.withId("P0").containing(5, "Red").tokens())
 *          .and(AToken.withId("T0"))
 *          .andFinally(ANormalArc.withSource("P0").andTarget("T0"))
 *
 * Alternatively to only add one item to the Petri net:
 * APetriNet.withOnly(AToken.withName("Default").andColor(Color.BLACK));
 */
public final class APetriNet {
    private static final Logger LOGGER = Logger.getLogger(APetriNet.class.getName());
    private Collection<DSLCreator<? extends PetriNetComponent>> creators = new ArrayList<>();
    private String name;

    private APetriNet(String name) {
        this.name = name;
    }

    private APetriNet() {
        this("");
    }

    /**
     * Entry method for creating a Petri Net
     * @param creator item creator to add to Petri net
     * @param <T> type of PetriNetComponent
     * @return instance of APetriNet class for chaining
     */
    public static <T extends PetriNetComponent> APetriNet with(DSLCreator<T> creator) {
        APetriNet aPetriNet = new APetriNet();
        aPetriNet.and(creator);
        return aPetriNet;
    }

    /**
     * Alternate entry method for creating a Petri Net
     * @param name of the Petri Net to be created
     * @return instance of APetriNet class for chaining
     */
    public static APetriNet named(String name) {
        APetriNet aPetriNet = new APetriNet(name);
        return aPetriNet;
    }

    /**
     *
     * Adds more 'items' to the PetriNet by collecting their creators
     *
     * @param creator item creator to add to Petri net
     * @param <T> type of PetriNetComponent
     * @return instance of APetriNet class for chaining
     */
    public <T extends PetriNetComponent> APetriNet and(DSLCreator<T> creator) {
        creators.add(creator);
        return this;
    }

    /**
     *
     * Adds more 'items' to the PetriNet by collecting their creators
     *
     * @param finalCreator last item creator to add to Petri net
     * @param <T> type of PetriNetComponent
     * @return the created Petri net containing all the items made from the added creators
     * @throws PetriNetComponentException if the PetriNet has errors due to mis-specified components
     */
    public <T extends PetriNetComponent> PetriNet andFinally(DSLCreator<T> finalCreator)
            throws PetriNetComponentException {
        return and(finalCreator).makePetriNet();
    }

    /**
     * Creates a PetriNet with a single item
     * @param creator item creator to add to Petri net
     * @param <T> type of PetriNetComponent
     * @return created petri net containing the item
     * @throws PetriNetComponentException if the PetriNet has errors due to mis-specified components
     */
    public static <T extends PetriNetComponent> PetriNet withOnly(DSLCreator<T> creator)
            throws PetriNetComponentException {
        APetriNet aPetriNet = new APetriNet();
        return aPetriNet.andFinally(creator);
    }

    /**
     * Creates a petri net by looping through the creators and calling
     * their create methods
     * @return petri net with components added
     * @throws PetriNetComponentException 
     */
    private PetriNet makePetriNet() throws PetriNetComponentException {
        Map<String, Token> tokens = new HashMap<>();
        Map<String, Place> places = new HashMap<>();
        Map<String, Transition> transitions = new HashMap<>();
        Map<String, FunctionalRateParameter> rateParameters = new HashMap<>();

        PetriNet petriNet = new PetriNet(new NormalPetriNetName(name));
        for (DSLCreator<? extends PetriNetComponent> creator : creators) {
            try {
                petriNet.add(creator.create(tokens, places, transitions, rateParameters));
            } catch (PetriNetComponentException e) {
                throw e;
            }
        }
        return petriNet;
    }

}
