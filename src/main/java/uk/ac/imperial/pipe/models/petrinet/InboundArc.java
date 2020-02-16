package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

import java.util.Map;

/**
 * An arc that goes from places to transitions
 */
public abstract class InboundArc extends AbstractArc<Place, Transition> {
    /**
     * Constructor
     * @param source of the arc
     * @param target of the arc
     * @param tokenWeights of the arc
     * @param type of the arc
     */
    public InboundArc(Place source, Transition target, Map<String, String> tokenWeights, ArcType type) {
        super(source, target, tokenWeights, type);
        if (!source.getStatus().getArcConstraint().acceptInboundArc())
            throw new IllegalArgumentException(
                    "Place has an outputOnly ArcConstraint, and will only accept OutboundArcs: " + source.getId());
    }

    /**
     * Visits the visitor if it is an {@link uk.ac.imperial.pipe.models.petrinet.ArcVisitor}
     * @param visitor to be accepted 
     */
    @Override
    public final void accept(PetriNetComponentVisitor visitor) {
        if (visitor instanceof ArcVisitor) {
            ((ArcVisitor) visitor).visit(this);
        }
    }
}
