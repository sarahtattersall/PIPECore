package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

import java.util.Map;

/**
 * An arc that goes from places to transitions
 */
public abstract class InboundArc extends AbstractArc<Place, Transition> {
    /**
     * Constructor
     * @param source
     * @param target
     * @param tokenWeights
     * @param type
     */
    public InboundArc(Place source, Transition target, Map<String, String> tokenWeights, ArcType type) {
        super(source, target, tokenWeights, type);
    }


    /**
     * Visits the visitor if it is an {@link uk.ac.imperial.pipe.models.petrinet.ArcVisitor}
     * @param visitor
     */
    @Override
    public final void accept(PetriNetComponentVisitor visitor) {
        if (visitor instanceof ArcVisitor) {
            ((ArcVisitor) visitor).visit(this);
        }
    }
}
