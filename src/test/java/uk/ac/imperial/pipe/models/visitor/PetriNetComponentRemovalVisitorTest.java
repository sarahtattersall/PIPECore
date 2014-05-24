package uk.ac.imperial.pipe.models.visitor;

import org.junit.Before;
import org.junit.Test;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.component.annotation.Annotation;
import uk.ac.imperial.pipe.models.component.arc.*;
import uk.ac.imperial.pipe.models.component.place.DiscretePlace;
import uk.ac.imperial.pipe.models.component.place.Place;
import uk.ac.imperial.pipe.models.component.rate.RateParameter;
import uk.ac.imperial.pipe.models.component.token.Token;
import uk.ac.imperial.pipe.models.component.transition.Transition;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.PetriNetComponentRemovalVisitor;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PetriNetComponentRemovalVisitorTest {
    PetriNet mockNet;

    PetriNetComponentRemovalVisitor visitor;

    @Before
    public void setUp() {
        mockNet = mock(PetriNet.class);
        visitor = new PetriNetComponentRemovalVisitor(mockNet);
    }

    @Test
    public void testDeletesInboundNormalArc() {
        Place place = new DiscretePlace("", "");
        Transition transition = new Transition("", "");
        Map<String, String> weights = new HashMap<>();
        InboundArc arc = new InboundNormalArc(place, transition, weights);
        arc.accept(visitor);
        verify(mockNet).removeArc(arc);
    }


    @Test
    public void testDeletesOutboundNormalArc() {
        Place place = new DiscretePlace("", "");
        Transition transition = new Transition("", "");
        Map<String, String> weights = new HashMap<>();
        OutboundArc arc = new OutboundNormalArc(transition, place, weights);
        arc.accept(visitor);
        verify(mockNet).removeArc(arc);
    }


    @Test
    public void testDeletesInboundInhibitorArc() {
        Place place = new DiscretePlace("", "");
        Transition transition = new Transition("", "");
        InboundArc arc = new InboundInhibitorArc(place, transition);
        arc.accept(visitor);
        verify(mockNet).removeArc(arc);
    }

    @Test
    public void testDeletesPlace() throws PetriNetComponentException {
        Place place = new DiscretePlace("", "");
        place.accept(visitor);
        verify(mockNet).removePlace(place);
    }

    @Test
    public void testDeletesTransition() {
        Transition transition = new Transition("", "");
        transition.accept(visitor);
        verify(mockNet).removeTransition(transition);
    }

    @Test
    public void testDeletesAnnotation() {
        Annotation annotation = new Annotation(0, 0, "", 0, 0, false);
        annotation.accept(visitor);
        verify(mockNet).removeAnnotation(annotation);

    }


    @Test
    public void testDeletesRateParameter() throws Exception {
        RateParameter parameter = new RateParameter("2", "Foo", "Foo");
        parameter.accept(visitor);
        verify(mockNet).removeRateParameter(parameter);
    }

    @Test
    public void testDeletesToken() throws PetriNetComponentException {
        Token token = new Token("", new Color(0, 0, 0));
        token.accept(visitor);
        verify(mockNet).removeToken(token);
    }
}
