package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

import java.util.Map;

/**
 * An arc that goes from transitions to places
 */
public abstract class OutboundArc extends AbstractArc<Transition, Place> {
    public OutboundArc(Transition source, Place target, Map<String, String> tokenWeights, ArcType type) {
        super(source, target, tokenWeights, type);
    }

    @Override
    public final void accept(PetriNetComponentVisitor visitor) {
        if (visitor instanceof ArcVisitor) {
            ((ArcVisitor) visitor).visit(this);
        }
    }
}
