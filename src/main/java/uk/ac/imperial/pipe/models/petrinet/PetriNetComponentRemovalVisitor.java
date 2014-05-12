package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.exceptions.InvalidRateException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.component.annotation.Annotation;
import uk.ac.imperial.pipe.models.component.annotation.AnnotationVisitor;
import uk.ac.imperial.pipe.models.component.arc.ArcVisitor;
import uk.ac.imperial.pipe.models.component.arc.InboundArc;
import uk.ac.imperial.pipe.models.component.arc.OutboundArc;
import uk.ac.imperial.pipe.models.component.place.Place;
import uk.ac.imperial.pipe.models.component.place.PlaceVisitor;
import uk.ac.imperial.pipe.models.component.rate.RateParameter;
import uk.ac.imperial.pipe.models.component.rate.RateParameterVisitor;
import uk.ac.imperial.pipe.models.component.token.Token;
import uk.ac.imperial.pipe.models.component.token.TokenVisitor;
import uk.ac.imperial.pipe.models.component.transition.Transition;
import uk.ac.imperial.pipe.models.component.transition.TransitionVisitor;

public class PetriNetComponentRemovalVisitor
        implements PlaceVisitor, TransitionVisitor, ArcVisitor, TokenVisitor, AnnotationVisitor, RateParameterVisitor {
    private final PetriNet net;

    public PetriNetComponentRemovalVisitor(PetriNet net) {
        this.net = net;
    }

    @Override
    public void visit(Place place) {
        net.removePlace(place);

    }

    @Override
    public void visit(Transition transition) {
        net.removeTransition(transition);

    }

    @Override
    public void visit(Token token) throws PetriNetComponentException {
        net.removeToken(token);
    }

    @Override
    public void visit(Annotation annotation) {
        net.removeAnnotation(annotation);
    }

    @Override
    public void visit(RateParameter rate) throws InvalidRateException {
        net.removeRateParameter(rate);
    }

    @Override
    public void visit(InboundArc inboundArc) {
        net.removeArc(inboundArc);
    }

    @Override
    public void visit(OutboundArc outboundArc) {
        net.removeArc(outboundArc);
    }
}
