package uk.ac.imperial.pipe.dsl;

import uk.ac.imperial.pipe.models.component.Connectable;
import uk.ac.imperial.pipe.models.component.arc.Arc;
import uk.ac.imperial.pipe.models.component.arc.InboundInhibitorArc;
import uk.ac.imperial.pipe.models.component.place.Place;
import uk.ac.imperial.pipe.models.component.rate.RateParameter;
import uk.ac.imperial.pipe.models.component.token.Token;
import uk.ac.imperial.pipe.models.component.transition.Transition;

import java.util.Map;

public class AnInhibitorArc implements DSLCreator<Arc<? extends Connectable, ? extends Connectable>> {
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
                                                                    Map<String, Transition> transitions, Map<String, RateParameter> rateParameters) {
        return new InboundInhibitorArc(places.get(source), transitions.get(target));
    }
}
