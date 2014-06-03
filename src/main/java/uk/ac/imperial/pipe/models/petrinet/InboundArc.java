package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

import java.util.Map;

/**
 * An arc that goes from places to transitions
 */
public abstract class InboundArc extends AbstractArc<Place, Transition> {
    public InboundArc(Place source, Transition target, Map<String, String> tokenWeights, ArcType type) {
        super(source, target, tokenWeights, type);
    }

    @Override
    public final void accept(PetriNetComponentVisitor visitor) {
        if (visitor instanceof ArcVisitor) {
            ((ArcVisitor) visitor).visit(this);
        }
    }
}
