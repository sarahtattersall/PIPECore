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

public class PetriNetComponentAddVisitorTest {

    PetriNet mockNet;

    PetriNetComponentAddVisitor visitor;

    @Before
    public void setUp() {
        mockNet = mock(PetriNet.class);
        visitor = new PetriNetComponentAddVisitor(mockNet);
    }

    @Test
    public void testAddsInboundNormalArc() {
        Place place = new DiscretePlace("", "");
        Transition transition = new DiscreteTransition("", "");
        Map<String, String> weights = new HashMap<>();
        InboundArc arc = new InboundNormalArc(place, transition, weights);
        arc.accept(visitor);
        verify(mockNet).addArc(arc);

    }

    @Test
    public void testAddsOutboundNormalArc() {
        Place place = new DiscretePlace("", "");
        Transition transition = new DiscreteTransition("", "");
        Map<String, String> weights = new HashMap<>();
        OutboundArc arc = new OutboundNormalArc(transition, place, weights);
        arc.accept(visitor);
        verify(mockNet).addArc(arc);
    }

    @Test
    public void testAddsInhibitorArc() {
        Place place = new DiscretePlace("", "");
        Transition transition = new DiscreteTransition("", "");
        InboundArc arc = new InboundInhibitorArc(place, transition);
        arc.accept(visitor);
        verify(mockNet).addArc(arc);
    }

    @Test
    public void testAddsPlace() throws PetriNetComponentException {
        Place place = new DiscretePlace("", "");
        place.accept(visitor);
        verify(mockNet).addPlace(place);
    }

    @Test
    public void testAddsTransition() throws PetriNetComponentException {
        Transition transition = new DiscreteTransition("", "");
        transition.accept(visitor);
        verify(mockNet).addTransition(transition);
    }

    @Test
    public void testAddAnnotation() {
        AnnotationImpl annotation = new AnnotationImpl(0, 0, "", 0, 0, false);
        annotation.accept(visitor);
        verify(mockNet).addAnnotation(annotation);

    }

    @Test
    public void testAddsToken() throws PetriNetComponentException {
        Token token = new ColoredToken("", new Color(0, 0, 0));
        token.accept(visitor);
        verify(mockNet).addToken(token);
    }

}
