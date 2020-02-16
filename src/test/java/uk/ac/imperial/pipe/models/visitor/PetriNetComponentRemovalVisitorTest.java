package uk.ac.imperial.pipe.models.visitor;

import org.junit.Before;
import org.junit.Test;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.*;

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
        Transition transition = new DiscreteTransition("", "");
        Map<String, String> weights = new HashMap<>();
        InboundArc arc = new InboundNormalArc(place, transition, weights);
        arc.accept(visitor);
        verify(mockNet).removeArc(arc);
    }

    @Test
    public void testDeletesOutboundNormalArc() {
        Place place = new DiscretePlace("", "");
        Transition transition = new DiscreteTransition("", "");
        Map<String, String> weights = new HashMap<>();
        OutboundArc arc = new OutboundNormalArc(transition, place, weights);
        arc.accept(visitor);
        verify(mockNet).removeArc(arc);
    }

    @Test
    public void testDeletesInboundInhibitorArc() {
        Place place = new DiscretePlace("", "");
        Transition transition = new DiscreteTransition("", "");
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
    public void testDeletesTransition() throws PetriNetComponentException {
        Transition transition = new DiscreteTransition("", "");
        transition.accept(visitor);
        verify(mockNet).removeTransition(transition);
    }

    @Test
    public void testDeletesAnnotation() {
        AnnotationImpl annotation = new AnnotationImpl(0, 0, "", 0, 0, false);
        annotation.accept(visitor);
        verify(mockNet).removeAnnotation(annotation);

    }

    @Test
    public void testDeletesRateParameter() throws Exception {
        FunctionalRateParameter parameter = new FunctionalRateParameter("2", "Foo", "Foo");
        parameter.accept(visitor);
        verify(mockNet).removeRateParameter(parameter);
    }

    @Test
    public void testDeletesToken() throws PetriNetComponentException {
        Token token = new ColoredToken("", new Color(0, 0, 0));
        token.accept(visitor);
        verify(mockNet).removeToken(token);
    }
}
