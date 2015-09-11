package uk.ac.imperial.pipe.models;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.ac.imperial.pipe.dsl.*;
import uk.ac.imperial.pipe.exceptions.InvalidRateException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.*;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PetriNetTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private PetriNet net;

    @Mock
    private PropertyChangeListener mockListener;

    @Before
    public void setUp() {
        net = new PetriNet();
    }

    @Test
    public void emptyPetriNetsEqual() {
        PetriNet petriNet1 = new PetriNet();
        PetriNet petriNet2 = new PetriNet();
        assertEquals(petriNet1, petriNet2);
    }

    @Test
    public void addingPlaceNotifiesObservers() {
        net.addPropertyChangeListener(mockListener);
        Place place = new DiscretePlace("P1", "P1");
        net.addPlace(place);

        verify(mockListener).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void addingDuplicatePlaceDoesNotNotifyObservers() {
        Place place = new DiscretePlace("P0", "P0");
        net.addPlace(place);

        net.addPropertyChangeListener(mockListener);
        net.addPlace(place);
        verify(mockListener, never()).propertyChange(any(PropertyChangeEvent.class));

    }


    @Test
    public void deletingNotContainedComponentDoesNotNotifyObservers() throws PetriNetComponentException {
        Place place = new DiscretePlace("P0", "P0");
        net.addPropertyChangeListener(mockListener);
        net.remove(place);
        verify(mockListener, never()).propertyChange(any(PropertyChangeEvent.class));

    }

    @Test
    public void removingPlaceNotifiesObservers() throws PetriNetComponentException {
        net.addPropertyChangeListener(mockListener);
        Place place = new DiscretePlace("", "");
        net.addPlace(place);
        net.removePlace(place);

        verify(mockListener, times(2)).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void addingArcNotifiesObservers() {
        Place place = new DiscretePlace("P0", "P0");
        Transition transition = new DiscreteTransition("T0", "T0");
        net.addPropertyChangeListener(mockListener);
        InboundArc mockArc = new InboundNormalArc(place, transition, new HashMap<String, String>());
        net.addArc(mockArc);

        verify(mockListener).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void addingDuplicateArcDoesNotNotifyObservers() {
        Place place = new DiscretePlace("P0", "P0");
        Transition transition = new DiscreteTransition("T0", "T0");
        InboundArc mockArc = new InboundNormalArc(place, transition, new HashMap<String, String>());
        net.addArc(mockArc);
        net.addPropertyChangeListener(mockListener);
        net.addArc(mockArc);

        verify(mockListener, never()).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void removingArcNotifiesObservers() {
        net.addPropertyChangeListener(mockListener);
        Place place = new DiscretePlace("P0", "P0");
        Transition transition = new DiscreteTransition("T0", "T0");
        InboundArc mockArc = new InboundNormalArc(place, transition, new HashMap<String, String>());
        net.addArc(mockArc);
        net.removeArc(mockArc);

        verify(mockListener, times(2)).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void addingTransitionNotifiesObservers() {
        net.addPropertyChangeListener(mockListener);
        Transition transition = new DiscreteTransition("", "");
        net.addTransition(transition);
        verify(mockListener).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void addingDuplicateTransitionDoesNotNotifyObservers() {
        Transition transition = new DiscreteTransition("", "");
        net.addTransition(transition);
        net.addPropertyChangeListener(mockListener);
        net.addTransition(transition);
        verify(mockListener, never()).propertyChange(any(PropertyChangeEvent.class));

    }

    @Test
    public void removingTransitionNotifiesObservers() {
        net.addPropertyChangeListener(mockListener);
        Transition transition = new DiscreteTransition("", "");
        net.addTransition(transition);
        net.removeTransition(transition);
        verify(mockListener, times(2)).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void cannotRemoveTokenIfPlaceDependsOnIt() throws PetriNetComponentException {
        expectedException.expect(PetriNetComponentException.class);
        expectedException.expectMessage("Cannot remove Default token places: P0 contains it");
        Token token = new ColoredToken("Default", Color.BLACK);
        Place place = new DiscretePlace("P0", "P0");
        place.setTokenCount(token.getId(), 2);
        net.addPlace(place);

        net.removeToken(token);
    }

    @Test
    public void cannotRemoveBlueTokenIfTransitionReferencesIt() throws PetriNetComponentException {
        expectedException.expect(PetriNetComponentException.class);
        expectedException.expectMessage("Cannot remove Blue token transitions: T0 reference it\n");
        Token token = new ColoredToken("Blue", Color.BLUE);
        Transition transition = new DiscreteTransition("T0", "T0");
        transition.setRate(new NormalRate("#(P0, Blue)"));
        net.addTransition(transition);
        net.removeToken(token);
    }


    @Test
    public void cannotRemoveDefaultTokenIfTransitionReferencesIt() throws PetriNetComponentException {
        expectedException.expect(PetriNetComponentException.class);
        expectedException.expectMessage("Cannot remove Default token transitions: T0 reference it\n");
        Token token = new ColoredToken("Default", Color.BLACK);
        Transition transition = new DiscreteTransition("T0", "T0");
        transition.setRate(new NormalRate("#(P0, Default)"));
        net.addTransition(transition);
        net.removeToken(token);
    }


    @Test
    public void addingAnnotationNotifiesObservers() {
        net.addPropertyChangeListener(mockListener);
        AnnotationImpl annotation = new AnnotationImpl(10, 10, "", 10, 10, false);
        net.addAnnotation(annotation);
        verify(mockListener).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void containsAnnotation() {
        net.addPropertyChangeListener(mockListener);
        AnnotationImpl annotation = new AnnotationImpl(10, 10, "hello", 10, 10, false);
        net.addAnnotation(annotation);
        assertTrue(net.containsComponent(annotation.getId()));
    }


    @Test
    public void containsAnnotationAfterTextChange() {
        net.addPropertyChangeListener(mockListener);
        AnnotationImpl annotation = new AnnotationImpl(10, 10, "hello", 10, 10, false);
        net.addAnnotation(annotation);
        annotation.setText("foo");
        assertTrue(net.containsComponent(annotation.getId()));
    }


    @Test
    public void removingAnnotationNotifiesObservers() {
        AnnotationImpl annotation = new AnnotationImpl(10, 10, "", 10, 10, false);
        net.addAnnotation(annotation);
        net.addPropertyChangeListener(mockListener);
        net.removeAnnotation(annotation);
        verify(mockListener).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void removingAnnotation() {
        AnnotationImpl annotation = new AnnotationImpl(10, 10, "hello", 10, 10, false);
        net.addAnnotation(annotation);
        net.removeAnnotation(annotation);
        assertFalse(net.containsComponent(annotation.getId()));
    }

    @Test
    public void addingDuplicateAnnotationDoesNotNotifyObservers() {
        AnnotationImpl annotation = new AnnotationImpl(10, 10, "", 10, 10, false);
        net.addAnnotation(annotation);
        net.addPropertyChangeListener(mockListener);
        net.addAnnotation(annotation);
        verify(mockListener, never()).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void addingRateParameterNotifiesObservers() throws InvalidRateException {
        net.addPropertyChangeListener(mockListener);
        FunctionalRateParameter rateParameter = new FunctionalRateParameter("5", "id", "name");
        net.addRateParameter(rateParameter);
        verify(mockListener).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void addingTokenNotifiesObservers() {
        net.addPropertyChangeListener(mockListener);
        Token token = new ColoredToken("Default", Color.BLACK);
        net.addToken(token);
        verify(mockListener).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void addingDuplicateTokenDoesNotNotifyObservers() {
        Token token = new ColoredToken("Default", Color.BLACK);
        net.addToken(token);

        net.addPropertyChangeListener(mockListener);
        net.addToken(token);
        verify(mockListener, never()).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void genericRemoveMethodRemovesPlace() throws PetriNetComponentException {
        Place place = new DiscretePlace("", "");
        net.addPlace(place);

        assertEquals(1, net.getPlaces().size());
        net.remove(place);
        assertTrue(net.getPlaces().isEmpty());
    }

    @Test
    public void genericRemoveMethodRemovesArc() throws PetriNetComponentException {
        Place place = new DiscretePlace("source", "source");
        Transition transition = new DiscreteTransition("target", "target");
        Map<String, String> weights = new HashMap<>();
        InboundNormalArc arc = new InboundNormalArc(place, transition, weights);
        net.addArc(arc);

        assertEquals(1, net.getArcs().size());
        net.remove(arc);
        assertTrue(net.getArcs().isEmpty());
    }

    @Test
    public void returnsCorrectToken() throws PetriNetComponentException {
        String id = "Token1";
        Color color = new Color(132, 16, 130);
        Token token = new ColoredToken(id, color);
        net.addToken(token);
        assertEquals(token, net.getComponent(id, Token.class));
    }

    @Test
    public void throwsErrorIfNoTokenExists() throws PetriNetComponentException {
        expectedException.expect(PetriNetComponentException.class);
        expectedException.expectMessage("No component foo exists in Petri net");
        net.getComponent("foo", Token.class);
    }

    @Test
    public void throwsExceptionIfRateParameterIsNotValid() throws InvalidRateException {
        expectedException.expect(InvalidRateException.class);
        expectedException.expectMessage("Rate of hsfg is invalid");
        FunctionalRateParameter rateParameter = new FunctionalRateParameter("hsfg", "id", "name");
        net.addRateParameter(rateParameter);
    }

    /**
     * Create simple Petri net with P1 -> T1 and P2 -> T1
     * Initialises a token in P1 and gives arcs A1 and A2 a weight of tokenWeight to a default token
     *
     * @param tokenWeight
     * @return
     * @throws PetriNetComponentException 
     */
    public PetriNet createSimplePetriNetTwoPlacesToTransition(int tokenWeight) throws PetriNetComponentException {
        String weight = Integer.toString(tokenWeight);
        return APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P1")).and(
                APlace.withId("P2")).and(AnImmediateTransition.withId("T1")).and(
                ANormalArc.withSource("P1").andTarget("T1").with(weight, "Default").tokens()).andFinally(
                ANormalArc.withSource("P2").andTarget("T1").with(weight, "Default").tokens());
    }

    @Test
    public void deletingRateParameterRemovesItFromTransition() throws InvalidRateException {
        String rate = "5.0";
        FunctionalRateParameter rateParameter = new FunctionalRateParameter(rate, "R0", "R0");
        Transition transition = new DiscreteTransition("T0", "T0", rateParameter, 0);

        net.addRateParameter(rateParameter);
        net.addTransition(transition);

        net.removeRateParameter(rateParameter);

        assertEquals(rate, transition.getRateExpr());
        assertTrue("Transition rate was not changed to normal rate on deletion",
                transition.getRate() instanceof NormalRate);
    }

    @Test
    public void testEqualityEqualPetriNets() throws PetriNetComponentException {
        PetriNet net1 = createSimplePetriNet(1);
        PetriNet net2 = createSimplePetriNet(1);
        assertTrue(net1.equals(net2));
    }

    /**
     * Create simple Petri net with P1 -> T1 -> P2
     * Initialises a token in P1 and gives arcs A1 and A2 a weight of tokenWeight to a default token
     *
     * @param tokenWeight
     * @return
     * @throws PetriNetComponentException 
     */
    public PetriNet createSimplePetriNet(int tokenWeight) throws PetriNetComponentException {
        String arcWeight = Integer.toString(tokenWeight);
        return APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
                APlace.withId("P1").containing(1, "Default").token()).and(APlace.withId("P2")).and(
                AnImmediateTransition.withId("T1")).and(
                ANormalArc.withSource("P1").andTarget("T1").with(arcWeight, "Default").tokens()).andFinally(
                ANormalArc.withSource("T1").andTarget("P2").with(arcWeight, "Default").tokens());
    }

    @Test
    public void testEqualityNotEqualPetriNets() throws PetriNetComponentException {
        PetriNet net1 = createSimplePetriNet(1);
        PetriNet net2 = createSimplePetriNet(4);
        assertFalse(net1.equals(net2));
    }

    @Test
    public void equalsAndHashCodeLawsWhenEqual() throws PetriNetComponentException {
        PetriNet net1 = createSimplePetriNet(1);
        PetriNet net2 = createSimplePetriNet(1);
        assertTrue(net1.equals(net2));
        assertEquals(net1.hashCode(), net2.hashCode());
    }

    @Test
    public void equalsAndHashCodeLawsWhenNotEqual() throws PetriNetComponentException {
        PetriNet net1 = createSimplePetriNet(1);
        PetriNet net2 = createSimplePetriNet(5);
        assertFalse(net1.equals(net2));
    }

    @Test
    public void canGetTokenById() throws PetriNetComponentException {
        Token t = new ColoredToken("Default", Color.BLACK);
        net.addToken(t);
        assertEquals(t, net.getComponent(t.getId(), Token.class));
    }

    @Test
    public void canGetTokenByIdAfterNameChange() throws PetriNetComponentException {
        Token t = new ColoredToken("Default", Color.BLACK);
        net.addToken(t);
        t.setId("Red");
        assertEquals(t, net.getComponent(t.getId(), Token.class));
    }

    @Test
    public void canGetPlaceById() throws PetriNetComponentException {
        Place p = new DiscretePlace("P0", "P0");
        net.addPlace(p);
        assertEquals(p, net.getComponent(p.getId(), Place.class));
    }

    @Test
    public void canGetPlaceByIdAfterIdChange() throws PetriNetComponentException {
        Place p = new DiscretePlace("P0", "P0");
        net.addPlace(p);
        p.setId("P1");
        assertEquals(p, net.getComponent(p.getId(), Place.class));
    }

    @Test
    public void canGetRateParameterById() throws PetriNetComponentException, InvalidRateException {
        FunctionalRateParameter r = new FunctionalRateParameter("2", "R0", "R0");
        net.addRateParameter(r);
        assertEquals(r, net.getComponent(r.getId(), RateParameter.class));
    }

    @Test
    public void canGetRateParameterByIdAfterIdChange() throws PetriNetComponentException, InvalidRateException {
        FunctionalRateParameter r = new FunctionalRateParameter("2", "R0", "R0");
        net.addRateParameter(r);
        r.setId("R1");
        assertEquals(r, net.getComponent(r.getId(), RateParameter.class));
    }

    @Test
    public void canGetTransitionById() throws PetriNetComponentException {
        Transition t = new DiscreteTransition("T0", "T0");
        net.addTransition(t);
        assertEquals(t, net.getComponent(t.getId(), Transition.class));
    }

    @Test
    public void canGetTransitionByIdAfterNameChange() throws PetriNetComponentException {
        Transition t = new DiscreteTransition("T0", "T0");
        net.addTransition(t);
        t.setId("T2");
        assertEquals(t, net.getComponent(t.getId(), Transition.class));
    }

    @Test
    public void canGetArcById() throws PetriNetComponentException {
        Place p = new DiscretePlace("P0", "P0");
        Transition t = new DiscreteTransition("T0", "T0");
        InboundArc a = new InboundNormalArc(p, t, new HashMap<String, String>());
        net.addArc(a);
        assertEquals(a, net.getComponent(a.getId(), InboundArc.class));
    }

    @Test
    public void canGetArcByIdAfterNameChange() throws PetriNetComponentException {
        Place p = new DiscretePlace("P0", "P0");
        Transition t = new DiscreteTransition("T0", "T0");
        InboundArc a = new InboundNormalArc(p, t, new HashMap<String, String>());
        net.addArc(a);
        a.setId("A1");
        assertEquals(a, net.getComponent(a.getId(), InboundArc.class));
    }

    @Test
    public void changingTokenIdChangesPlaceReferences() {
        Token token = new ColoredToken("Default", Color.BLACK);
        Place place = new DiscretePlace("P1", "P1");
        place.setTokenCount(token.getId(), 5);

        net.addToken(token);
        net.addPlace(place);
        token.setId("Red");

        assertEquals(0, place.getTokenCount("Default"));
        assertEquals(5, place.getTokenCount("Red"));
    }

    @Test
    public void changingTokenIdChangesArcReferences() {
        Place p = new DiscretePlace("P0", "P0");
        Transition t = new DiscreteTransition("T0", "T0");
        Token token = new ColoredToken("Default", Color.BLACK);

        HashMap<String, String> weights = new HashMap<>();
        weights.put(token.getId(), "5");
        InboundArc a = new InboundNormalArc(p, t, weights);

        net.addToken(token);
        net.addPlace(p);
        net.addTransition(t);
        net.addArc(a);

        token.setId("Red");
        assertEquals("0", a.getWeightForToken("Default"));
        assertEquals("5", a.getWeightForToken("Red"));
    }

    @Test
    public void correctEmptyOutboundArcs() throws PetriNetComponentException {
        PetriNet petriNet =
                APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).andFinally(
                        AnImmediateTransition.withId("T0"));
        Transition t0 = petriNet.getComponent("T0", Transition.class);
        assertTrue(petriNet.outboundArcs(t0).isEmpty());
    }


    @Test
    public void correctEmptyInboundArcs() throws PetriNetComponentException {
        PetriNet petriNet =
                APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).andFinally(
                        AnImmediateTransition.withId("T0"));
        Transition t0 = petriNet.getComponent("T0", Transition.class);
        assertTrue(petriNet.inboundArcs(t0).isEmpty());
    }


    @Test
    public void removesTransitionInboundWhenDeleted() throws PetriNetComponentException {
        PetriNet petriNet =

                APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
                        APlace.withId("P1")).and(AnImmediateTransition.withId("T0")).and(
                        AnImmediateTransition.withId("T1")).and(
                        ANormalArc.withSource("P1").andTarget("T1")).andFinally(
                        ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token());
        Transition t0 = petriNet.getComponent("T0", Transition.class);
        petriNet.removeTransition(t0);

        Collection<InboundArc> inboundArcs = petriNet.inboundArcs(t0);
        assertTrue(inboundArcs.isEmpty());
    }


    @Test
    public void correctInboundArcs() throws PetriNetComponentException {
        PetriNet petriNet =

                APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
                        APlace.withId("P1")).and(AnImmediateTransition.withId("T0")).and(
                        AnImmediateTransition.withId("T1")).and(
                        ANormalArc.withSource("P1").andTarget("T1")).andFinally(
                        ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token());
        Transition t0 = petriNet.getComponent("T0", Transition.class);
        InboundArc arc = petriNet.getComponent("P0 TO T0", InboundArc.class);
        Collection<InboundArc> inboundArcs = petriNet.inboundArcs(t0);
        assertTrue(inboundArcs.contains(arc));
    }

    @Test
    public void correctDeletesFromInboundArcs() throws PetriNetComponentException {
        PetriNet petriNet =
                APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
                        AnImmediateTransition.withId("T0")).andFinally(
                        ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token());
        Transition t0 = petriNet.getComponent("T0", Transition.class);
        InboundArc arc = petriNet.getComponent("P0 TO T0", InboundArc.class);
        petriNet.removeArc(arc);
        Collection<InboundArc> inboundArcs = petriNet.inboundArcs(t0);
        assertTrue(inboundArcs.isEmpty());
    }

    @Test
    public void correctOutboundArcs() throws PetriNetComponentException {
        PetriNet petriNet =
                APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
                        APlace.withId("P1")).and(AnImmediateTransition.withId("T0")).and(
                        AnImmediateTransition.withId("T1")).and(
                        ANormalArc.withSource("T1").andTarget("P1")).andFinally(
                        ANormalArc.withSource("T0").andTarget("P0").with("1", "Default").token());
        Transition t0 = petriNet.getComponent("T0", Transition.class);
        OutboundArc arc = petriNet.getComponent("T0 TO P0", OutboundArc.class);
        Collection<OutboundArc> outboundArcs = petriNet.outboundArcs(t0);
        assertEquals(1, outboundArcs.size());
        assertTrue(outboundArcs.contains(arc));
    }

    @Test
    public void removesAllOutBoundWhenTransitionDeleted() throws PetriNetComponentException {
        PetriNet petriNet =
                APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
                        APlace.withId("P1")).and(AnImmediateTransition.withId("T0")).and(
                        AnImmediateTransition.withId("T1")).and(
                        ANormalArc.withSource("T1").andTarget("P1")).andFinally(
                        ANormalArc.withSource("T0").andTarget("P0").with("1", "Default").token());
        Transition t0 = petriNet.getComponent("T0", Transition.class);
        petriNet.removeTransition(t0);
        Collection<OutboundArc> outboundArcs = petriNet.outboundArcs(t0);
        assertTrue(outboundArcs.isEmpty());
    }

    @Test
    public void correctOutboundArcsIfTransitionChangesName() throws PetriNetComponentException {
        PetriNet petriNet =
                APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
                        APlace.withId("P1")).and(AnImmediateTransition.withId("T0")).and(
                        AnImmediateTransition.withId("T1")).and(
                        ANormalArc.withSource("T1").andTarget("P1")).andFinally(
                        ANormalArc.withSource("T0").andTarget("P0").with("1", "Default").token());
        Transition t0 = petriNet.getComponent("T0", Transition.class);
        t0.setId("T2");
        OutboundArc arc = petriNet.getComponent("T0 TO P0", OutboundArc.class);
        Collection<OutboundArc> outboundArcs = petriNet.outboundArcs(t0);
        assertEquals(1, outboundArcs.size());
        assertTrue(outboundArcs.contains(arc));
    }


    @Test
    public void correctRemovalDeletesFromOutboundArcs() throws PetriNetComponentException {
        PetriNet petriNet =
                APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
                        AnImmediateTransition.withId("T0")).andFinally(
                        ANormalArc.withSource("T0").andTarget("P0").with("1", "Default").token());
        Transition t0 = petriNet.getComponent("T0", Transition.class);
        OutboundArc arc = petriNet.getComponent("T0 TO P0", OutboundArc.class);
        petriNet.removeArc(arc);
        Collection<OutboundArc> outboundArcs = petriNet.outboundArcs(t0);
        assertTrue(outboundArcs.isEmpty());
    }

    @Test
    public void cannotDeletePlaceIfReferencedByTransition() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(APlace.withId("P0")).andFinally(
                ATimedTransition.withId("T0").andRate("#(P0)"));
        Place place = petriNet.getComponent("P0", Place.class);
        try {
            petriNet.removePlace(place);
        } catch (PetriNetComponentException e) {
            String expected = "Cannot delete " + place.getId() + " it is referenced in a functional expression!";
            assertEquals(expected, e.getMessage());
            return;
        }
        fail("Did not throw Petri net exception!");
    }


    @Test
    public void cannotDeletePlaceIfReferencedByRateParam() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(APlace.withId("P0")).andFinally(ARateParameter.withId("R1").andExpression("#(P0)"));
        Place place = petriNet.getComponent("P0", Place.class);
        try {
            petriNet.removePlace(place);
        } catch (PetriNetComponentException e) {
            String expected = "Cannot delete " + place.getId() + " it is referenced in a functional expression!";
            assertEquals(expected, e.getMessage());
            return;
        }
        fail("Did not throw Petri net exception!");
    }

    @Test
    public void cannotDeletePlaceIfReferencedByArc() throws PetriNetComponentException {
        PetriNet petriNet =
                APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
                        APlace.withId("P1")).and(AnImmediateTransition.withId("T0")).and(
                        AnImmediateTransition.withId("T1")).and(
                        ANormalArc.withSource("T1").andTarget("P1")).andFinally(
                        ANormalArc.withSource("T0").andTarget("P0").with("#(P0)", "Default").token());
        Place place = petriNet.getComponent("P0", Place.class);
        try {
            petriNet.removePlace(place);
        } catch (PetriNetComponentException e) {
            String expected = "Cannot delete " + place.getId() + " it is referenced in a functional expression!";
            assertEquals(expected, e.getMessage());
            return;
        }
        fail("Did not throw Petri net exception!");
    }

    @Test
    public void allComponents() throws PetriNetComponentException {
        PetriNet petriNet =
                APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
                        APlace.withId("P1")).and(AnImmediateTransition.withId("T0")).and(
                        AnImmediateTransition.withId("T1")).and(
                        ANormalArc.withSource("T1").andTarget("P1")).andFinally(
                        ANormalArc.withSource("T0").andTarget("P0").with("#(P0)", "Default").token());
        Set<String> components = petriNet.getComponentIds();
        assertEquals(7, components.size());
        assertThat(components).contains("Default", "P0", "P1", "T1", "T0", "T1 TO P1", "T0 TO P0");
    }


    @Test
    public void containsComponents() throws PetriNetComponentException {
        PetriNet petriNet =
                APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
                        APlace.withId("P1")).and(AnImmediateTransition.withId("T0")).and(
                        AnImmediateTransition.withId("T1")).and(
                        ANormalArc.withSource("T1").andTarget("P1")).andFinally(
                        ANormalArc.withSource("T0").andTarget("P0").with("#(P0)", "Default").token());
        assertTrue(petriNet.contains("T0 TO P0"));
        assertTrue(petriNet.contains("T0"));
        assertTrue(petriNet.contains("T1"));
        assertTrue(petriNet.contains("Default"));
    }

    @Test
    public void doesNotContainComponents() throws PetriNetComponentException {
        PetriNet petriNet =
                APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
                        APlace.withId("P1")).and(AnImmediateTransition.withId("T0")).and(
                        AnImmediateTransition.withId("T1")).and(
                        ANormalArc.withSource("T1").andTarget("P1")).andFinally(
                        ANormalArc.withSource("T0").andTarget("P0").with("#(P0)", "Default").token());
        assertFalse(petriNet.contains("P0 TO T0"));
        assertFalse(petriNet.contains("T2"));
        assertFalse(petriNet.contains("P3"));
        assertFalse(petriNet.contains("Red"));
    }




}
