package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.exceptions.InvalidRateException;
import uk.ac.imperial.pipe.models.component.annotation.Annotation;
import uk.ac.imperial.pipe.models.component.annotation.AnnotationVisitor;
import uk.ac.imperial.pipe.models.component.arc.ArcVisitor;
import uk.ac.imperial.pipe.models.component.arc.InboundArc;
import uk.ac.imperial.pipe.models.component.arc.OutboundArc;
import uk.ac.imperial.pipe.models.component.place.Place;
import uk.ac.imperial.pipe.models.component.place.PlaceVisitor;
import uk.ac.imperial.pipe.models.component.rate.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.component.rate.RateParameterVisitor;
import uk.ac.imperial.pipe.models.component.token.Token;
import uk.ac.imperial.pipe.models.component.token.TokenVisitor;
import uk.ac.imperial.pipe.models.component.transition.Transition;
import uk.ac.imperial.pipe.models.component.transition.TransitionVisitor;

public class PetriNetComponentAddVisitor
        implements PlaceVisitor, ArcVisitor, TransitionVisitor, TokenVisitor, AnnotationVisitor, RateParameterVisitor {
    private final PetriNet petriNet;

    public PetriNetComponentAddVisitor(PetriNet petriNet) {
        this.petriNet = petriNet;
    }

    @Override
    public void visit(Place place) {
        petriNet.addPlace(place);
    }

    @Override
    public void visit(Transition transition) {
        petriNet.addTransition(transition);
    }

    @Override
    public void visit(Token token) {
        petriNet.addToken(token);
    }

    @Override
    public void visit(Annotation annotation) {
        petriNet.addAnnotation(annotation);
    }

    @Override
    public void visit(FunctionalRateParameter rate) throws InvalidRateException {
        petriNet.addRateParameter(rate);
    }

    @Override
    public void visit(InboundArc inboundArc) {
        petriNet.addArc(inboundArc);
    }

    @Override
    public void visit(OutboundArc outboundArc) {
        petriNet.addArc(outboundArc);
    }
}
