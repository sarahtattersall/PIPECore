package uk.ac.imperial.pipe.models.component.arc;

import uk.ac.imperial.pipe.models.component.place.Place;
import uk.ac.imperial.pipe.models.component.transition.Transition;
import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

import java.util.Map;

/**
 * An arc that goes from places to transitions
 */
public abstract class InboundArc extends Arc<Place, Transition>{
    public InboundArc(Place source, Transition target, Map<String, String> tokenWeights, ArcType type) {
        super(source, target, tokenWeights, type);
    }

    @Override
    public void accept(PetriNetComponentVisitor visitor) {
        if (visitor instanceof ArcVisitor) {
            ((ArcVisitor) visitor).visit(this);
        }
    }
}
