package uk.ac.imperial.pipe.naming;

import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;

public class TransitionNamer extends ComponentNamer {

    public TransitionNamer(PetriNet petriNet) {
        super(petriNet, "T", PetriNet.NEW_TRANSITION_CHANGE_MESSAGE, PetriNet.DELETE_TRANSITION_CHANGE_MESSAGE);
        initialiseTransitionNames();
    }

    private void initialiseTransitionNames() {
        for (Transition transition : petriNet.getTransitions()) {
            transition.addPropertyChangeListener(nameListener);
            names.add(transition.getId());
        }
    }
}
