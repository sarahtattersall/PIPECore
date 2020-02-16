package uk.ac.imperial.pipe.visitor;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.*;
import uk.ac.imperial.pipe.naming.MultipleNamer;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Paste visitor pastes components into a petri net
 */
public final class PasteVisitor implements TransitionVisitor, ArcVisitor, DiscretePlaceVisitor {

    /**
     * Class logger
     */
    private static final Logger LOGGER = Logger.getLogger(PasteVisitor.class.getName());

    /**
     * Used to name pasted components
     */
    private final MultipleNamer multipleNamer;

    /**
     * Petri net to paste components into
     */
    private final PetriNet petriNet;

    /**
     * Components to paste
     */
    private final Collection<PetriNetComponent> components = new HashSet<>();

    /**
     * Maps original id to copied connectable
     */
    private final Map<String, Place> createdPlaces = new HashMap<>();

    /**
     * Maps original id to copied transition
     */
    private final Map<String, Transition> createdTransitions = new HashMap<>();

    /**
     * New components created
     */
    private final Collection<PetriNetComponent> createdComponents = new LinkedList<>();

    /**
     * x offset to paste components at
     */
    private final int xOffset;

    /**
     * y offset to paste components at
     */
    private final int yOffset;

    /**
     * Constructor
     * @param petriNet to be visited 
     * @param components of the Petri net
     * @param multipleNamer namer 
     */
    public PasteVisitor(PetriNet petriNet, Collection<PetriNetComponent> components, MultipleNamer multipleNamer) {
        this(petriNet, components, multipleNamer, 0, 0);
    }

    /**
     * Constructor
     *
     * @param petriNet to be visited
     * @param components    components to paste
     * @param multipleNamer namer
     * @param xOffset coordinate
     * @param yOffset coordinate 
     */
    public PasteVisitor(PetriNet petriNet, Collection<PetriNetComponent> components, MultipleNamer multipleNamer,
            int xOffset, int yOffset) {
        this.petriNet = petriNet;
        this.multipleNamer = multipleNamer;
        this.components.addAll(components);
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    /**
     *
     * @return newly created components
     */
    public Collection<PetriNetComponent> getCreatedComponents() {
        return createdComponents;
    }

    /**
     * Visits a place cloning it
     * @param place to be visted
     */
    @Override
    public void visit(DiscretePlace place) {
        Place newPlace = new DiscretePlace(place);
        setId(newPlace);
        setName(newPlace);
        setOffset(newPlace);
        petriNet.addPlace(newPlace);
        createdPlaces.put(place.getId(), newPlace);
        createdComponents.add(newPlace);
    }

    /**
     * Give the place a unique id
     * @param place id 
     */
    private void setId(Place place) {
        place.setId(multipleNamer.getPlaceName());
    }

    /**
     * Gives the place a name
     * @param place name
     */
    private void setName(Place place) {
        place.setName(multipleNamer.getPlaceName());
    }

    /**
     * Sets the offset of the connectable to its x, y + the offset
     * @param connectable offset 
     */
    private void setOffset(Connectable connectable) {
        connectable.setX(connectable.getX() + xOffset);
        connectable.setY(connectable.getY() + yOffset);
    }

    /**
     * Visits a transition cloneing it and adding it to the Petri net
     * @param transition to be visited
     */
    @Override
    public void visit(Transition transition) {
        TransitionCloner cloner = new TransitionCloner();
        try {
            transition.accept(cloner);
        } catch (PetriNetComponentException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
        Transition newTransition = cloner.cloned;
        setId(newTransition);
        setName(newTransition);
        setOffset(newTransition);
        petriNet.addTransition(newTransition);
        createdTransitions.put(transition.getId(), newTransition);
        createdComponents.add(newTransition);
    }

    /**
     * Gives the transition a unique id
     * @param transition id 
     */
    private void setId(Transition transition) {
        transition.setId(multipleNamer.getTransitionName());
    }

    /**
     * Gives the transition a new name
     * @param transition name
     */
    private void setName(Transition transition) {
        transition.setName(multipleNamer.getTransitionName());
    }

    /**
     * Visits the arc cloning it and sets its source/target either to a new cloned component if it
     * was in the original pasting components, or the old original component if not
     * @param inboundArc to be visited 
     */
    @Override
    public void visit(InboundArc inboundArc) {
        Place source = inboundArc.getSource();
        Transition target = inboundArc.getTarget();

        if (components.contains(source)) {
            source = createdPlaces.get(source.getId());
        }

        if (components.contains(target)) {
            target = createdTransitions.get(target.getId());
        }

        InboundArc newArc;
        switch (inboundArc.getType()) {
        case INHIBITOR:
            newArc = new InboundInhibitorArc(source, target);
            break;
        case TEST:
            newArc = new InboundTestArc(source, target);
            break;
        default:
            newArc = new InboundNormalArc(source, target, inboundArc.getTokenWeights());
        }
        copyIntermediatePoints(inboundArc, newArc);
        petriNet.addArc(newArc);
        createdComponents.add(newArc);

    }

    /**
     * Copies the original arc intermediate points into the new arc.
     *
     * @param arc    original arc
     * @param newArc newly created arc
     */
    private void copyIntermediatePoints(Arc<? extends Connectable, ? extends Connectable> arc,
            Arc<? extends Connectable, ? extends Connectable> newArc) {
        List<ArcPoint> arcPoints = arc.getArcPoints();
        for (int i = 1; i < arcPoints.size() - 1; i++) {
            ArcPoint newArcPoint = new ArcPoint(arcPoints.get(i));
            newArc.addIntermediatePoint(newArcPoint);
        }
    }

    /**
     * Visits the arc cloning it and sets its source/target either to a new cloned component if it
     * was in the original pasting components, or the old original component if not
     * @param outboundArc to be visited 
     */
    @Override
    public void visit(OutboundArc outboundArc) {
        Transition source = outboundArc.getSource();
        Place target = outboundArc.getTarget();

        if (components.contains(source)) {
            source = createdTransitions.get(source.getId());
        }

        if (components.contains(target)) {
            target = createdPlaces.get(target.getId());
        }

        OutboundArc newArc = new OutboundNormalArc(source, target, outboundArc.getTokenWeights());
        copyIntermediatePoints(outboundArc, newArc);
        petriNet.addArc(newArc);
        createdComponents.add(newArc);
    }
}
