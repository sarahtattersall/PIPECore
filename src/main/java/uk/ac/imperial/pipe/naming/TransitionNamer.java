package uk.ac.imperial.pipe.naming;

import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;

/**
 * Used to give transitions a unique name in the Petri net
 */
public class TransitionNamer extends ComponentNamer {

    /**
     *
     * Constructor initialises the unique name for the transitions to be in the format "T {@code <number>}"
     *
     * @param petriNet of the transitions to be named 
     */
    public TransitionNamer(PetriNet petriNet) {
        super(petriNet, "T", PetriNet.NEW_TRANSITION_CHANGE_MESSAGE, PetriNet.DELETE_TRANSITION_CHANGE_MESSAGE);
        initialiseTransitionNames();
    }

    /**
     * Populates the names data structure with transitions that already exist in the Petri net
     */
    private void initialiseTransitionNames() {
        for (Transition transition : petriNet.getTransitions()) {
            transition.addPropertyChangeListener(nameListener);
            names.add(transition.getId());
        }
    }
}
