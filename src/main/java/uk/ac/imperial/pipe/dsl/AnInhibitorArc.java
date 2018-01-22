package uk.ac.imperial.pipe.dsl;

import uk.ac.imperial.pipe.models.petrinet.Connectable;
import uk.ac.imperial.pipe.models.petrinet.Arc;
import uk.ac.imperial.pipe.models.petrinet.InboundInhibitorArc;
import uk.ac.imperial.pipe.models.petrinet.PetriNetComponent;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.Transition;

import java.util.Map;

/**
 * DSL for creating arcs, to be used in conjunction with {@link uk.ac.imperial.pipe.dsl.APetriNet}
 *
 * The format can be AnInhibitorArc.withSource("P0").andTarget("T0")
 *
 * For implementation reasons arcs must be declared after places and transitions
 *
 */
public final class AnInhibitorArc implements DSLCreator<Arc<? extends Connectable, ? extends Connectable>> {
    /**
     * Inhibitor arc source
     */
    private String source;

    /**
     * Inhinbitor arc target
     */
    private String target;

    /**
     * Private constructor
     */
    private AnInhibitorArc() {
    }

    /**
     * Factory constructor
     * @param source arc source
     * @return builder
     */
    public static AnInhibitorArc withSource(String source) {
        AnInhibitorArc anInhibitorArc = new AnInhibitorArc();
        anInhibitorArc.source = source;
        return anInhibitorArc;
    }

    /**
     * Required target for an arc
     *
     * @param target place or transition for the arc
     * @return builder
     */
    public AnInhibitorArc andTarget(String target) {
        this.target = target;
        return this;
    }

    /**
     * @param tokens map of created tokens with id of Token
     * @param places map of created places with id of Connectable
     * @param transitions map of created transitions with id of Transition
     * @param rateParameters map of created rateParameters with id of rateParameter
     * @return inhibitor arc
     */
    @Override
    public Arc<? extends Connectable, ? extends Connectable> create(Map<String, Token> tokens,
            Map<String, Place> places,
            Map<String, Transition> transitions, Map<String, FunctionalRateParameter> rateParameters) {
        return new InboundInhibitorArc(places.get(source), transitions.get(target));
    }
}
