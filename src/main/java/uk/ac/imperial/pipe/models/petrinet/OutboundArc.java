package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

import java.util.Map;

/**
 * An arc that goes from transitions to places
 */
public abstract class OutboundArc extends AbstractArc<Transition, Place> {
    /**
     * Constructor
     * @param source connectable of the arc
     * @param target connectable of the arc
     * @param tokenWeights  of the arc
     * @param type of the arc 
     */
    public OutboundArc(Transition source, Place target, Map<String, String> tokenWeights, ArcType type) {
        super(source, target, tokenWeights, type);
        if (!target.getStatus().getArcConstraint().acceptOutboundArc())
            throw new IllegalArgumentException(
                    "Place has an inputOnly ArcConstraint, and will only accept InboundArcs: " + target.getId());
    }

    /**
     * Visits the arc if the visitor is an {@link uk.ac.imperial.pipe.models.petrinet.ArcVisitor}
     * @param visitor to be accepted 
     */
    @Override
    public final void accept(PetriNetComponentVisitor visitor) {
        if (visitor instanceof ArcVisitor) {
            ((ArcVisitor) visitor).visit(this);
        }
    }
}
