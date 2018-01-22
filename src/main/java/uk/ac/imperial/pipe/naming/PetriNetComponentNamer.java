package uk.ac.imperial.pipe.naming;

import uk.ac.imperial.pipe.models.petrinet.PetriNet;

/**
 * Gives unique names to places, transitions and arcs
 */
public final class PetriNetComponentNamer implements MultipleNamer {

    /**
     * Place namer
     */
    private final UniqueNamer placeNamer;

    /**
     * Transition namer
     */
    private final UniqueNamer transitionNamer;

    /**
     * Constructor
     * @param net Petri net to name items in
     */
    public PetriNetComponentNamer(PetriNet net) {
        placeNamer = new PlaceNamer(net);
        transitionNamer = new TransitionNamer(net);
    }

    /**
     *
     * @return unique place name
     */
    @Override
    public String getPlaceName() {
        return placeNamer.getName();
    }

    /**
     *
     * @return unique transition name
     */
    @Override
    public String getTransitionName() {
        return transitionNamer.getName();
    }

    /**
     *
     * @return unique arc name
     */
    //TODO:
    @Override
    public String getArcName() {
        return "";
    }
}
