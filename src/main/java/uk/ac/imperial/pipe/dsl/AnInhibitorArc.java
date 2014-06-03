package uk.ac.imperial.pipe.dsl;

import uk.ac.imperial.pipe.models.petrinet.Connectable;
import uk.ac.imperial.pipe.models.petrinet.Arc;
import uk.ac.imperial.pipe.models.petrinet.InboundInhibitorArc;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.Transition;

import java.util.Map;

public final class AnInhibitorArc implements DSLCreator<Arc<? extends Connectable, ? extends Connectable>> {
    private String source;

    private String target;

    private AnInhibitorArc() {
    }

    public static AnInhibitorArc withSource(String source) {
        AnInhibitorArc anInhibitorArc = new AnInhibitorArc();
        anInhibitorArc.source = source;
        return anInhibitorArc;
    }

    public AnInhibitorArc andTarget(String target) {
        this.target = target;
        return this;
    }

    @Override
    public Arc<? extends Connectable, ? extends Connectable> create(Map<String, Token> tokens,
                                                                    Map<String, Place> places,
                                                                    Map<String, Transition> transitions, Map<String, FunctionalRateParameter> rateParameters) {
        return new InboundInhibitorArc(places.get(source), transitions.get(target));
    }
}
