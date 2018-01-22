package uk.ac.imperial.pipe.io.adapters.modelAdapter;

import java.util.Map;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.Arc;
import uk.ac.imperial.pipe.models.petrinet.Connectable;
import uk.ac.imperial.pipe.models.petrinet.InboundInhibitorArc;
import uk.ac.imperial.pipe.models.petrinet.InboundNormalArc;
import uk.ac.imperial.pipe.models.petrinet.OutboundNormalArc;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.Transition;

public class ArcEndpointsValidator {
    private static final String DOES_NOT_EXIST = " does not exist in file.";
    private static final String DO_NOT_EXIST = " do not exist in file.";
    private static final String BUT = " but ";
    private static final String AND = " and ";
    private static final String PLACE = "place ";
    private static final String TRANSITION = "transition ";
    private static final String REFERENCES = "' references ";
    private static final String IN_ARC_ADAPTER = " in uk.ac.imperial.pipe.io.adapters.modelAdapter.ArcAdapter: Arc '";
    private static final String SPECIFIES = "' specifies that both ";
    private static final String ARE = " are ";
    private static final String BUT_NOT_POSSIBLE_CONFIGURATION = " but this is not a possible configuration.";
    private static final String PLACES = "places,";
    private static final String TRANSITIONS = "transitions,";
    private static final String INHIBITORY = "' specifies an inhibitory arc with ";
    private static final String AS_SOURCE = " as source, ";
    private Place sourcePlace;
    private Transition sourceTransition;
    private Place targetPlace;
    private Transition targetTransition;
    private String target;
    private String source;
    private String arcId;
    private Map<String, Place> places;
    private Map<String, Transition> transitions;
    private boolean normal;
    private Map<String, String> weights;

    public ArcEndpointsValidator(String source, String target, String arcId, Map<String, Place> places,
            Map<String, Transition> transitions, boolean normal, Map<String, String> weights) {
        this.source = source;
        this.target = target;
        this.arcId = arcId;
        this.places = places;
        this.transitions = transitions;
        this.normal = normal;
        this.weights = weights;
        build();
    }

    private void build() {
        sourcePlace = places.get(source);
        sourceTransition = transitions.get(source);
        targetPlace = places.get(target);
        targetTransition = transitions.get(target);

    }

    public Arc<? extends Connectable, ? extends Connectable> createArc() throws PetriNetComponentException {
        Arc<? extends Connectable, ? extends Connectable> arc = null;
        if ((targetPlace != null) && (sourcePlace != null)) {
            throw new PetriNetComponentException(IN_ARC_ADAPTER + arcId + SPECIFIES +
                    source + AND + target + ARE + PLACES + BUT_NOT_POSSIBLE_CONFIGURATION);
        } else if ((targetTransition != null) && (sourceTransition != null)) {
            throw new PetriNetComponentException(IN_ARC_ADAPTER + arcId + SPECIFIES +
                    source + AND + target + ARE + TRANSITIONS + BUT_NOT_POSSIBLE_CONFIGURATION);
        } else if ((sourcePlace == null) && (targetTransition != null)) {
            throw new PetriNetComponentNotFoundException(IN_ARC_ADAPTER +
                    arcId + REFERENCES + PLACE + source + BUT + source + DOES_NOT_EXIST);
        } else if ((targetPlace == null) && (sourceTransition != null)) {
            throw new PetriNetComponentNotFoundException(IN_ARC_ADAPTER +
                    arcId + REFERENCES + PLACE + target + BUT + target + DOES_NOT_EXIST);
        } else if ((sourceTransition == null) && (targetPlace != null)) {
            throw new PetriNetComponentNotFoundException(IN_ARC_ADAPTER +
                    arcId + REFERENCES + TRANSITION + source + BUT + source + DOES_NOT_EXIST);
        } else if ((targetTransition == null) && (sourcePlace != null)) {
            throw new PetriNetComponentNotFoundException(IN_ARC_ADAPTER +
                    arcId + REFERENCES + TRANSITION + target + BUT + target + DOES_NOT_EXIST);
        } else if ((targetTransition == null) && (sourceTransition == null) &&
                (targetPlace == null) && (sourcePlace == null)) {
            throw new PetriNetComponentNotFoundException(IN_ARC_ADAPTER +
                    arcId + REFERENCES + source + AND + target + BUT + source + AND + target + DO_NOT_EXIST);
        }
        if (normal) {
            if ((sourcePlace != null) && (targetTransition != null)) {
                arc = new InboundNormalArc(sourcePlace, targetTransition, weights);
            }
            if ((sourceTransition != null) && (targetPlace != null)) {
                arc = new OutboundNormalArc(sourceTransition, targetPlace, weights);
            }
        } else {
            if ((sourcePlace != null) && (targetTransition != null)) {
                arc = new InboundInhibitorArc(sourcePlace, targetTransition);
            } else if (sourceTransition != null) {
                throw new PetriNetComponentException(IN_ARC_ADAPTER + arcId +
                        INHIBITORY + source + TRANSITION + AS_SOURCE + BUT_NOT_POSSIBLE_CONFIGURATION);
            }
        }
        if (arc != null) {
            arc.setId(arcId);
        }
        return arc;
    }

}
