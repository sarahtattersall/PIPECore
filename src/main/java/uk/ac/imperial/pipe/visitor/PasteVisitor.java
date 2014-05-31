package uk.ac.imperial.pipe.visitor;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.*;
import uk.ac.imperial.pipe.naming.MultipleNamer;

import java.util.*;

/**
 * Paste visitor pastes components into a petri net
 */
public final class PasteVisitor implements TransitionVisitor, ArcVisitor, DiscretePlaceVisitor {

    private final MultipleNamer multipleNamer;

    private final PetriNet petriNet;

    private final Collection<PetriNetComponent> components = new HashSet<>();

    /**
     * Maps original id to copied connectable
     */
    private final Map<String, Place> createdPlaces = new HashMap<>();

    private final Map<String, Transition> createdTransitions = new HashMap<>();

    private final Collection<PetriNetComponent> createdComponents = new LinkedList<>();

    private final int xOffset;

    private final int yOffset;

    public PasteVisitor(PetriNet petriNet, Collection<PetriNetComponent> components, MultipleNamer multipleNamer) {
        this(petriNet, components, multipleNamer, 0, 0);
    }

    /**
     * @param petriNet
     * @param components    components to paste
     * @param multipleNamer
     * @param xOffset
     * @param yOffset
     */
    public PasteVisitor(PetriNet petriNet, Collection<PetriNetComponent> components, MultipleNamer multipleNamer,
                        int xOffset, int yOffset) {
        this.petriNet = petriNet;
        this.multipleNamer = multipleNamer;
        this.components.addAll(components);
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    public Collection<PetriNetComponent> getCreatedComponents() {
        return createdComponents;
    }

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

    private void setId(Place place) {
        place.setId(multipleNamer.getPlaceName());
    }

    private void setName(Place place) {
        place.setName(multipleNamer.getPlaceName());
    }

    private void setOffset(Connectable connectable) {
        connectable.setX(connectable.getX() + xOffset);
        connectable.setY(connectable.getY() + yOffset);
    }

    @Override
    public void visit(Transition transition) {
        TransitionCloner cloner = new TransitionCloner();
        try {
            transition.accept(cloner);
        } catch (PetriNetComponentException e) {
            e.printStackTrace();
        }
        Transition newTransition = cloner.cloned;
        setId(newTransition);
        setName(newTransition);
        setOffset(newTransition);
        petriNet.addTransition(newTransition);
        createdTransitions.put(transition.getId(), newTransition);
        createdComponents.add(newTransition);
    }

    private void setId(Transition transition) {
        transition.setId(multipleNamer.getTransitionName());
    }

    private void setName(Transition transition) {
        transition.setName(multipleNamer.getTransitionName());
    }

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
