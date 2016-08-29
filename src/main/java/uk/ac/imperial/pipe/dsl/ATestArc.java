package uk.ac.imperial.pipe.dsl;

import uk.ac.imperial.pipe.models.petrinet.Connectable;
import uk.ac.imperial.pipe.models.petrinet.Arc;
import uk.ac.imperial.pipe.models.petrinet.InboundTestArc;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.Transition;

import java.util.Map;

/**
 * DSL for creating arcs, to be used in conjunction with {@link uk.ac.imperial.pipe.dsl.APetriNet}
 *
 * The format can be ATestArc.withSource("P0").andTarget("T0")
 *
 * For implementation reasons arcs must be declared after places and transitions
 *
 */
public final class ATestArc implements DSLCreator<Arc<? extends Connectable, ? extends Connectable>> {
    /**
     * Test arc source
     */
    private String source;

    /**
     * Test arc target
     */
    private String target;

    /**
     * Private constructor
     */
    private ATestArc() {
    }

    /**
     * Factory constructor
     * @param source of the arc
     * @return builder for chaining
     */
    public static ATestArc withSource(String source) {
        ATestArc aTestArc = new ATestArc();
        aTestArc.source = source;
        return aTestArc;
    }

    /**
     * Required target for an arc
     *
     * @param target of the arc
     * @return builder for chaining
     */
    public ATestArc andTarget(String target) {
        this.target = target;
        return this;
    }


    /**
     * @param tokens map of created tokens with id -&gt; Token
     * @param places map of created places with id -&gt; Connectable
     * @param transitions map of created transitions with id -&gt; Transition
     * @param rateParameters map of created rate parameters with id -&gt; FunctionalRateParameter 
     * @return inbound test arc
     */
    @Override
    public Arc<? extends Connectable, ? extends Connectable> create(Map<String, Token> tokens,
                                                                    Map<String, Place> places,
                                                                    Map<String, Transition> transitions, Map<String, FunctionalRateParameter> rateParameters) {
        return new InboundTestArc(places.get(source), transitions.get(target));
    }
}
