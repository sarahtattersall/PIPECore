package uk.ac.imperial.pipe.models.petrinet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.ARateParameter;
import uk.ac.imperial.pipe.dsl.ATimedTransition;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.exceptions.InvalidRateException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;

@RunWith(MockitoJUnitRunner.class)
public class PetriNetTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private PetriNet net;

    @Mock
    private PropertyChangeListener mockListener;
    @Mock
    private MergeInterfaceStatus mockMergeStatus;

    private Place oldPlace;
    private Place newPlace;

    private ExecutablePetriNet executablePetriNet;

    private Transition transition;

    private Place place;

    private InboundNormalArc arc;

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
    public void hasNameOrDefaultsToBlank() throws Exception {
        assertEquals("", net.getName().getName());
        net = new PetriNet(new NormalPetriNetName("net1"));
        assertEquals("net1", net.getName().getName());
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
    public void throwsIfRemovingPlaceGetsResultFromMergeStatusRemove() throws Exception {
        expectedException.expect(PetriNetComponentException.class);
        expectedException.expectMessage("Cannot delete P0:\nresult message");
        Result<InterfacePlaceAction> result = new Result<>();
        result.addMessage("result message");
        when(mockMergeStatus.remove(any(IncludeHierarchy.class))).thenReturn(result);
        Place place = new DiscretePlace("P0", "P0");
        place.getStatus().setMergeInterfaceStatus(mockMergeStatus);
        net.addPlace(place);
        net.removePlace(place);
    }

    @Test
    public void addingArcNotifiesObservers() {
        Place place = new DiscretePlace("P0", "P0");
        Transition transition = new DiscreteTransition("T0", "T0");
        net.addPlace(place);
        net.addTransition(transition);
        net.addPropertyChangeListener(mockListener);
        InboundArc mockArc = new InboundNormalArc(place, transition, new HashMap<String, String>());
        net.addArc(mockArc);

        verify(mockListener).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void addingDuplicateArcDoesNotNotifyObservers() {
        Place place = new DiscretePlace("P0", "P0");
        Transition transition = new DiscreteTransition("T0", "T0");
        net.addPlace(place);
        net.addTransition(transition);
        InboundArc mockArc = new InboundNormalArc(place, transition, new HashMap<String, String>());
        net.addArc(mockArc);
        net.addPropertyChangeListener(mockListener);
        net.addArc(mockArc);

        verify(mockListener, never()).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void removingArcNotifiesObservers() {
        Place place = new DiscretePlace("P0", "P0");
        Transition transition = new DiscreteTransition("T0", "T0");
        net.addPlace(place);
        net.addTransition(transition);
        net.addPropertyChangeListener(mockListener);

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
    public void changingStructuralTransitionPropertiesForcesExecutablePetriNetRefresh() {
        buildTransition();
        transition.setId("T99");
        verifyRefreshRequiredAndRebuildTransition();
        transition.setPriority(5);
        verifyRefreshRequiredAndRebuildTransition();
        transition.setRate(new NormalRate("3"));
        verifyRefreshRequiredAndRebuildTransition();
        transition.setTimed(true);
        verifyRefreshRequiredAndRebuildTransition();
        transition.setInfiniteServer(true);
        verifyRefreshRequiredAndRebuildTransition();
        transition.setTimed(true); // cant set delay unless timed
        executablePetriNet.setRefreshNotRequiredForTesting();
        transition.setDelay(4);
        verifyRefreshRequiredAndRebuildTransition();
    }

    @Test
    public void changingNonStructuralTransitionPropertiesDoesNotForceExecutablePetriNetRefresh() {
        buildTransition();
        transition.enable();
        verifyNoRefreshRequiredAndRebuildTransition();
        transition.disable();
        verifyNoRefreshRequiredAndRebuildTransition();
        transition.setAngle(45);
        verifyNoRefreshRequiredAndRebuildTransition();
    }

    private void verifyRefreshRequiredAndRebuildTransition() {
        assertTrue(executablePetriNet.isRefreshRequired());
        buildTransition();
    }

    private void verifyNoRefreshRequiredAndRebuildTransition() {
        assertFalse(executablePetriNet.isRefreshRequired());
        buildTransition();
    }

    private void buildTransition() {
        net = new PetriNet();
        transition = new DiscreteTransition("T0");
        net.addTransition(transition);
        executablePetriNet = net.getExecutablePetriNet();
        assertFalse(executablePetriNet.isRefreshRequired());
    }

    @Test
    public void changingStructuralPlacePropertiesForcesExecutablePetriNetRefresh() {
        buildPlace();
        place.setId("P2");
        verifyRefreshRequiredAndRebuildPlace();
        place.setCapacity(3);
        verifyRefreshRequiredAndRebuildPlace();
    }

    //FIXME	@Test
    public void changingNonStructuralPlacePropertiesDoesNotForceExecutablePetriNetRefresh() {
        buildPlace();
        //TODO triggers both TOKEN_CHANGE_MESSAGE and TOKEN_CHANGE_MIRROR_MESSAGE; should it?
        place.setTokenCount("default", 2);
        verifyNoRefreshRequiredAndRebuildPlace();
    }

    private void verifyRefreshRequiredAndRebuildPlace() {
        assertTrue(executablePetriNet.isRefreshRequired());
        buildPlace();
    }

    private void verifyNoRefreshRequiredAndRebuildPlace() {
        assertFalse(executablePetriNet.isRefreshRequired());
        buildPlace();
    }

    protected void buildPlace() {
        net = new PetriNet();
        place = new DiscretePlace("P1", "P1");
        net.addPlace(place);
        executablePetriNet = net.getExecutablePetriNet();
        assertFalse(executablePetriNet.isRefreshRequired());
    }

    @Test
    public void changingStructuralArcPropertiesForcesExecutablePetriNetRefresh() throws Exception {
        buildArc();
        arc.setSource(new DiscretePlace("P1"));
        verifyRefreshRequiredAndRebuildArc();
        arc.setTarget(new DiscreteTransition("T1"));
        verifyRefreshRequiredAndRebuildArc();
        arc.setWeight("red", "2");
        verifyRefreshRequiredAndRebuildArc();
    }

    @Test
    //	   component.addPropertyChangeListener(getExecutablePetriNetBare()); //TODO drop this when each component is separately listening
    public void changingNonStructuralArcPropertiesDoesNotForceExecutablePetriNetRefresh() throws Exception {
        buildArcForIntermediatePointTests();
        arc.addIntermediatePoint(null);
        verifyNoRefreshRequiredAndRebuildArc();
        arc.removeIntermediatePoint(null);
        verifyNoRefreshRequiredAndRebuildArc();
    }

    private void verifyRefreshRequiredAndRebuildArc() throws Exception {
        assertTrue(executablePetriNet.isRefreshRequired());
        buildArc();
    }

    private void verifyNoRefreshRequiredAndRebuildArc() throws Exception {
        assertFalse(executablePetriNet.isRefreshRequired());
        buildArcForIntermediatePointTests();
    }

    private void buildArc() throws PetriNetComponentException {
        buildNetWithOldAndNewPlaces("P0", "T0");
        arc = (InboundNormalArc) net.getComponent("P0 TO T0", InboundArc.class);
        executablePetriNet = net.getExecutablePetriNet();
        assertFalse(executablePetriNet.isRefreshRequired());
    }

    private void buildArcForIntermediatePointTests() throws PetriNetComponentException {
        net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0"))
                .andFinally(AnImmediateTransition.withId("T0"));
        arc = new InboundNormalArc(net.getComponent("P0", Place.class),
                net.getComponent("T0", Transition.class), new HashMap<String, String>()) {
            @Override
            public void addIntermediatePoint(ArcPoint point) {
                changeSupport.firePropertyChange(NEW_INTERMEDIATE_POINT_CHANGE_MESSAGE, null, point);
            }

            @Override
            public void removeIntermediatePoint(ArcPoint point) {
                changeSupport.firePropertyChange(DELETE_INTERMEDIATE_POINT_CHANGE_MESSAGE, point, null);
            }
        };
        net.addArc(arc);
        executablePetriNet = net.getExecutablePetriNet();
        assertFalse(executablePetriNet.isRefreshRequired());
    }

    @Test
    public void changingAnythingNotifiesExecutablePetriNet() {
        ExecutablePetriNet epn = net.getExecutablePetriNet();
        assertFalse(epn.isRefreshRequired());
        Place place = new DiscretePlace("P1", "P1");
        net.addPlace(place);
        assertTrue(epn.isRefreshRequired());
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
        net.addPlace(place);
        net.addTransition(transition);
        Map<String, String> weights = new HashMap<>();
        InboundNormalArc arc = new InboundNormalArc(place, transition, weights);
        net.addArc(arc);

        assertEquals(1, net.getArcs().size());
        net.remove(arc);
        assertTrue(net.getArcs().isEmpty());
    }

    @Test
    public void returnsCorrectToken() throws PetriNetComponentNotFoundException {
        String id = "Token1";
        Color color = new Color(132, 16, 130);
        Token token = new ColoredToken(id, color);
        net.addToken(token);
        assertEquals(token, net.getComponent(id, Token.class));
    }

    @Test
    public void throwsErrorIfNoTokenExists() throws PetriNetComponentNotFoundException {
        expectedException.expect(PetriNetComponentNotFoundException.class);
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
        return APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P1"))
                .and(APlace.withId("P2")).and(AnImmediateTransition.withId("T1"))
                .and(ANormalArc.withSource("P1").andTarget("T1").with(weight, "Default").tokens())
                .andFinally(ANormalArc.withSource("P2").andTarget("T1").with(weight, "Default").tokens());
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
        assertTrue("Transition rate was not changed to normal rate on deletion", transition
                .getRate() instanceof NormalRate);
    }

    @Test
    public void testEqualityEqualPetriNets() throws PetriNetComponentException {
        PetriNet net1 = createSimplePetriNet(1, "");
        PetriNet net2 = createSimplePetriNet(1, "");
        assertTrue(net1.equals(net2));
    }

    /**
     * Create simple Petri net with P1 -> T1 -> P2
     * Initialises a token in P1 and gives arcs A1 and A2 a weight of tokenWeight to a default token
     *
     * @param tokenWeight
     * @param name TODO
     * @return
     * @throws PetriNetComponentException
     */
    public PetriNet createSimplePetriNet(int tokenWeight, String name) throws PetriNetComponentException {
        String arcWeight = Integer.toString(tokenWeight);
        return APetriNet.named(name).and(AToken.called("Default").withColor(Color.BLACK))
                .and(AToken.called("red").withColor(Color.RED))
                .and(APlace.withId("P1").containing(1, "Default").token()).and(APlace.withId("P2"))
                .and(AnImmediateTransition.withId("T1"))
                .and(ANormalArc.withSource("P1").andTarget("T1").with(arcWeight, "Default").tokens())
                .andFinally(ANormalArc.withSource("T1").andTarget("P2").with(arcWeight, "Default").tokens());
    }

    @Test
    public void testEqualityNotEqualPetriNets() throws PetriNetComponentException {
        PetriNet net1 = createSimplePetriNet(1, "net1");
        PetriNet net2 = createSimplePetriNet(4, "net1");
        assertFalse(net1.equals(net2));
        PetriNet net3 = createSimplePetriNet(1, "net3");
        assertFalse(net1.equals(net3));
    }

    @Test
    public void equalsAndHashCodeLawsWhenEqual() throws PetriNetComponentException {
        PetriNet net1 = createSimplePetriNet(1, "net1");
        PetriNet net2 = createSimplePetriNet(1, "net1");
        assertTrue(net1.equals(net2));
        assertEquals(net1.hashCode(), net2.hashCode());
    }

    @Test
    public void equalsAndHashCodeLawsWhenNotEqual() throws PetriNetComponentException {
        PetriNet net1 = createSimplePetriNet(1, "");
        PetriNet net2 = createSimplePetriNet(5, "");
        assertFalse(net1.equals(net2));
    }

    @Test
    public void canGetTokenById() throws PetriNetComponentNotFoundException {
        Token t = new ColoredToken("Default", Color.BLACK);
        net.addToken(t);
        assertEquals(t, net.getComponent(t.getId(), Token.class));
    }

    @Test
    public void canGetTokenByIdAfterNameChange() throws PetriNetComponentNotFoundException {
        Token t = new ColoredToken("Default", Color.BLACK);
        net.addToken(t);
        t.setId("Red");
        assertEquals(t, net.getComponent(t.getId(), Token.class));
    }

    @Test
    public void canGetPlaceById() throws PetriNetComponentNotFoundException {
        Place p = new DiscretePlace("P0", "P0");
        net.addPlace(p);
        assertEquals(p, net.getComponent(p.getId(), Place.class));
    }

    @Test
    public void canGetPlaceByIdAfterIdChange() throws PetriNetComponentNotFoundException {
        Place p = new DiscretePlace("P0", "P0");
        net.addPlace(p);
        p.setId("P1");
        assertEquals(p, net.getComponent(p.getId(), Place.class));
    }

    @Test
    public void canGetRateParameterById() throws PetriNetComponentNotFoundException, InvalidRateException {
        FunctionalRateParameter r = new FunctionalRateParameter("2", "R0", "R0");
        net.addRateParameter(r);
        assertEquals(r, net.getComponent(r.getId(), RateParameter.class));
    }

    @Test
    public void canGetRateParameterByIdAfterIdChange() throws PetriNetComponentNotFoundException, InvalidRateException {
        FunctionalRateParameter r = new FunctionalRateParameter("2", "R0", "R0");
        net.addRateParameter(r);
        r.setId("R1");
        assertEquals(r, net.getComponent(r.getId(), RateParameter.class));
    }

    @Test
    public void canGetTransitionById() throws PetriNetComponentNotFoundException {
        Transition t = new DiscreteTransition("T0", "T0");
        net.addTransition(t);
        assertEquals(t, net.getComponent(t.getId(), Transition.class));
    }

    @Test
    public void canGetTransitionByIdAfterNameChange() throws PetriNetComponentNotFoundException {
        Transition t = new DiscreteTransition("T0", "T0");
        net.addTransition(t);
        t.setId("T2");
        assertEquals(t, net.getComponent(t.getId(), Transition.class));
    }

    @Test
    public void canGetArcById() throws PetriNetComponentNotFoundException {
        Place place = new DiscretePlace("P0", "P0");
        Transition transition = new DiscreteTransition("T0", "T0");
        net.addPlace(place);
        net.addTransition(transition);

        InboundArc arc = new InboundNormalArc(place, transition, new HashMap<String, String>());
        net.addArc(arc);
        assertEquals(arc, net.getComponent(arc.getId(), InboundArc.class));
    }

    @Test
    public void canGetArcByIdAfterNameChange() throws PetriNetComponentNotFoundException {
        Place place = new DiscretePlace("P0", "P0");
        Transition transition = new DiscreteTransition("T0", "T0");
        net.addPlace(place);
        net.addTransition(transition);
        InboundArc arc = new InboundNormalArc(place, transition, new HashMap<String, String>());
        net.addArc(arc);
        arc.setId("A1");
        assertEquals(arc, net.getComponent(arc.getId(), InboundArc.class));
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
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0"))
                .andFinally(AnImmediateTransition.withId("T0"));
        Transition t0 = petriNet.getComponent("T0", Transition.class);
        assertTrue(petriNet.outboundArcs(t0).isEmpty());
    }

    @Test
    public void correctEmptyInboundArcs() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0"))
                .andFinally(AnImmediateTransition.withId("T0"));
        Transition t0 = petriNet.getComponent("T0", Transition.class);
        assertTrue(petriNet.inboundArcs(t0).isEmpty());
    }

    @Test
    public void removesTransitionInboundWhenDeleted() throws PetriNetComponentException {
        PetriNet petriNet = buildSimpleNet();
        Transition t0 = petriNet.getComponent("T0", Transition.class);
        petriNet.removeTransition(t0);

        Collection<InboundArc> inboundArcs = petriNet.inboundArcs(t0);
        assertTrue(inboundArcs.isEmpty());
    }

    @Test
    public void removeTransitionForcesExecutablePetriNetRefresh() throws PetriNetComponentException {
        PetriNet net = buildPetriNetAndVerifyNoRefreshRequired();
        net.removeTransition(net.getComponent("T0", Transition.class));
        assertTrue(executablePetriNet.isRefreshRequired());
    }

    @Test
    public void removePlaceForcesExecutablePetriNetRefresh() throws PetriNetComponentException {
        PetriNet net = buildPetriNetAndVerifyNoRefreshRequired();
        net.removePlace(net.getComponent("P0", Place.class));
        assertTrue(executablePetriNet.isRefreshRequired());
    }

    @Test
    public void removeInboundArcForcesExecutablePetriNetRefresh() throws PetriNetComponentException {
        PetriNet net = buildPetriNetAndVerifyNoRefreshRequired();
        net.removeArc(net.getComponent("P0 TO T0", InboundArc.class));
        assertTrue(executablePetriNet.isRefreshRequired());
    }

    @Test
    public void removeOutboundArcForcesExecutablePetriNetRefresh() throws PetriNetComponentException {
        PetriNet net = buildPetriNetAndVerifyNoRefreshRequired();
        net.removeArc(net.getComponent("T1 TO P1", OutboundArc.class));
        assertTrue(executablePetriNet.isRefreshRequired());
    }

    @Test
    public void removePetriNetComponentForcesExecutablePetriNetRefresh() throws PetriNetComponentException {
        PetriNet net = buildPetriNetAndVerifyNoRefreshRequired();
        net.remove(net.getComponent("P1", Place.class));
        assertTrue(executablePetriNet.isRefreshRequired());
    }

    @Test
    public void addTransitionForcesExecutablePetriNetRefresh() throws PetriNetComponentException {
        PetriNet net = buildPetriNetAndVerifyNoRefreshRequired();
        net.addTransition(new DiscreteTransition("T2"));
        assertTrue(executablePetriNet.isRefreshRequired());
    }

    @Test
    public void addPlaceForcesExecutablePetriNetRefresh() throws PetriNetComponentException {
        PetriNet net = buildPetriNetAndVerifyNoRefreshRequired();
        net.addPlace(new DiscretePlace("P2"));
        assertTrue(executablePetriNet.isRefreshRequired());
    }

    @Test
    public void addInboundArcForcesExecutablePetriNetRefresh() throws PetriNetComponentException {
        PetriNet net = buildPetriNetAndVerifyNoRefreshRequired();
        net.addArc(new InboundNormalArc(net.getComponent("P0", Place.class),
                net.getComponent("T1", Transition.class), new HashMap<String, String>()));
        assertTrue(executablePetriNet.isRefreshRequired());
    }

    @Test
    public void addOutboundArcForcesExecutablePetriNetRefresh() throws PetriNetComponentException {
        PetriNet net = buildPetriNetAndVerifyNoRefreshRequired();
        net.addArc(new OutboundNormalArc(net.getComponent("T1", Transition.class),
                net.getComponent("P0", Place.class), new HashMap<String, String>()));
        assertTrue(executablePetriNet.isRefreshRequired());
    }

    public PetriNet buildPetriNetAndVerifyNoRefreshRequired() throws PetriNetComponentException {
        PetriNet net = buildSimpleNet();
        executablePetriNet = net.getExecutablePetriNet();
        assertFalse(executablePetriNet.isRefreshRequired());
        return net;
    }

    public PetriNet buildSimpleNet() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0"))
                .and(APlace.withId("P1")).and(AnImmediateTransition.withId("T0"))
                .and(AnImmediateTransition.withId("T1")).and(ANormalArc.withSource("T1").andTarget("P1"))
                .andFinally(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token());
        return petriNet;
    }

    @Test
    public void correctInboundArcs() throws PetriNetComponentException {
        PetriNet petriNet = buildSimpleNet();
        Transition t0 = petriNet.getComponent("T0", Transition.class);
        InboundArc arc = petriNet.getComponent("P0 TO T0", InboundArc.class);
        Collection<InboundArc> inboundArcs = petriNet.inboundArcs(t0);
        assertTrue(inboundArcs.contains(arc));
    }

    @Test
    public void correctDeletesFromInboundArcs() throws PetriNetComponentException {
        PetriNet petriNet = buildSimpleNet();
        Transition t0 = petriNet.getComponent("T0", Transition.class);
        InboundArc arc = petriNet.getComponent("P0 TO T0", InboundArc.class);
        petriNet.removeArc(arc);
        Collection<InboundArc> inboundArcs = petriNet.inboundArcs(t0);
        assertTrue(inboundArcs.isEmpty());
    }

    @Test
    public void correctOutboundArcs() throws PetriNetComponentException {
        PetriNet petriNet = buildNetNoInboundArcs();
        Transition t0 = petriNet.getComponent("T0", Transition.class);
        OutboundArc arc = petriNet.getComponent("T0 TO P0", OutboundArc.class);
        Collection<OutboundArc> outboundArcs = petriNet.outboundArcs(t0);
        assertEquals(1, outboundArcs.size());
        assertTrue(outboundArcs.contains(arc));
    }

    public PetriNet buildNetNoInboundArcs() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0"))
                .and(APlace.withId("P1")).and(AnImmediateTransition.withId("T0"))
                .and(AnImmediateTransition.withId("T1")).and(ANormalArc.withSource("T1").andTarget("P1"))
                .andFinally(ANormalArc.withSource("T0").andTarget("P0").with("1", "Default").token());
        return petriNet;
    }

    @Test
    public void removesAllOutBoundWhenTransitionDeleted() throws PetriNetComponentException {
        PetriNet petriNet = buildNetNoInboundArcs();
        Transition t0 = petriNet.getComponent("T0", Transition.class);
        petriNet.removeTransition(t0);
        Collection<OutboundArc> outboundArcs = petriNet.outboundArcs(t0);
        assertTrue(outboundArcs.isEmpty());
    }

    @Test
    public void correctOutboundArcsIfTransitionChangesName() throws PetriNetComponentException {
        PetriNet petriNet = buildNetNoInboundArcs();
        Transition t0 = petriNet.getComponent("T0", Transition.class);
        t0.setId("T2");
        OutboundArc arc = petriNet.getComponent("T0 TO P0", OutboundArc.class);
        Collection<OutboundArc> outboundArcs = petriNet.outboundArcs(t0);
        assertEquals(1, outboundArcs.size());
        assertTrue(outboundArcs.contains(arc));
    }

    @Test
    public void correctRemovalDeletesFromOutboundArcs() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0"))
                .and(AnImmediateTransition.withId("T0"))
                .andFinally(ANormalArc.withSource("T0").andTarget("P0").with("1", "Default").token());
        Transition t0 = petriNet.getComponent("T0", Transition.class);
        OutboundArc arc = petriNet.getComponent("T0 TO P0", OutboundArc.class);
        petriNet.removeArc(arc);
        Collection<OutboundArc> outboundArcs = petriNet.outboundArcs(t0);
        assertTrue(outboundArcs.isEmpty());
    }

    @Test
    public void cannotDeletePlaceIfReferencedByTransition() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(APlace.withId("P0"))
                .andFinally(ATimedTransition.withId("T0").andRate("#(P0)"));
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
        PetriNet petriNet = APetriNet.with(APlace.withId("P0"))
                .andFinally(ARateParameter.withId("R1").andExpression("#(P0)"));
        Place place = petriNet.getComponent("P0", Place.class);
        try {
            petriNet.removePlace(place);
            fail("Did not throw Petri net exception!");
        } catch (PetriNetComponentException e) {
            String expected = "Cannot delete " + place.getId() + " it is referenced in a functional expression!";
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void cannotDeletePlaceIfReferencedByArc() throws PetriNetComponentException {
        PetriNet petriNet = buildNetWithRateOnArc();
        Place place = petriNet.getComponent("P0", Place.class);
        try {
            petriNet.removePlace(place);
            fail("Did not throw Petri net exception!");
        } catch (PetriNetComponentException e) {
            String expected = "Cannot delete " + place.getId() + " it is referenced in a functional expression!";
            assertEquals(expected, e.getMessage());
        }
    }

    public PetriNet buildNetWithRateOnArc() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0"))
                .and(APlace.withId("P1")).and(AnImmediateTransition.withId("T0"))
                .and(AnImmediateTransition.withId("T1")).and(ANormalArc.withSource("T1").andTarget("P1"))
                .andFinally(ANormalArc.withSource("T0").andTarget("P0").with("#(P0)", "Default").token());
        return petriNet;
    }

    @Test
    public void allComponents() throws PetriNetComponentException {
        PetriNet petriNet = buildNetWithRateOnArc();
        Set<String> components = petriNet.getComponentIds();
        assertEquals(7, components.size());
        assertThat(components).contains("Default", "P0", "P1", "T1", "T0", "T1 TO P1", "T0 TO P0");
    }

    @Test
    public void containsComponents() throws PetriNetComponentException {
        PetriNet petriNet = buildNetWithRateOnArc();
        assertTrue(petriNet.contains("T0 TO P0"));
        assertTrue(petriNet.contains("T0"));
        assertTrue(petriNet.contains("T1"));
        assertTrue(petriNet.contains("Default"));
    }

    @Test
    public void doesNotContainComponents() throws PetriNetComponentException {
        PetriNet petriNet = buildNetWithRateOnArc();
        assertFalse(petriNet.contains("P0 TO T0"));
        assertFalse(petriNet.contains("T2"));
        assertFalse(petriNet.contains("P3"));
        assertFalse(petriNet.contains("Red"));
    }

    @Test
    public void createsAnExecutablePetriNet() throws Exception {
        ExecutablePetriNet epn = net.getExecutablePetriNet();
        assertThat(epn.getPlaces()).hasSize(0);
    }

    @Test
    public void executablePetriNetRefreshesAutomaticallyUponChangeToPetriNet() throws Exception {
        ExecutablePetriNet epn = net.getExecutablePetriNet();
        assertThat(epn.getPlaces()).hasSize(0);
        net.addPlace(new DiscretePlace("P0"));
        assertThat(epn.getPlaces()).hasSize(1);
    }

    @Test
    public void verifyPlaceHasTokenCountZeroForEachToken() throws Exception {
        Place place = new DiscretePlace("P1", "P1");
        net.addPlace(place);
        Token token = new ColoredToken("Default", Color.black);
        net.addToken(token);
        int count = place.getTokenCounts().get("Default");
        assertEquals(0, count);
    }

    @Test
    public void removeTokenCleansUpPlaceReferencesIfCountIsStillZero() throws PetriNetComponentException {
        Token token = new ColoredToken("Default", Color.BLACK);
        Place place = new DiscretePlace("P0", "P0");
        net.addPlace(place);
        net.addToken(token);
        assertThat(place.getTokenCounts()).containsEntry("Default", 0);
        net.removeToken(token);
        assertThat(place.getTokenCounts()).doesNotContainKey("Default");
    }

    //TODO testIfNewPlaceHasSameIdAsPlaceItReplaces
    //TODO testFunctionalExpressionIsUpdatedWithNewPlaceId
    @Test
    public void outboundArcsRebuiltWhenPlaceReplacedWithNewPlace() throws Exception {
        buildNetWithOldAndNewPlaces("T0", "P0");
        net.convertOutboundArcsToUseNewPlace(oldPlace, newPlace);
        assertEquals(1, net.outboundArcs.size());
        OutboundArc arc = net.getOutboundArcs().iterator().next();
        assertEquals(newPlace, arc.getTarget());
        assertEquals("T0 TO P1", arc.getId());
    }

    @Test
    public void inboundArcsRebuiltWhenPlaceReplacedWithNewPlace() throws Exception {
        buildNetWithOldAndNewPlaces("P0", "T0");
        net.convertInboundArcsToUseNewPlace(oldPlace, newPlace);
        assertEquals(1, net.inboundArcs.size());
        InboundArc arc = net.getInboundArcs().iterator().next();
        assertEquals(newPlace, arc.getSource());
        assertEquals("P1 TO T0", arc.getId());
    }

    @Test
    public void onePlaceReplacesAnother() throws Exception {
        buildNetWithOldAndNewPlaces("P0", "T0");
        assertEquals(0, net.inboundArcs(newPlace).size());
        assertEquals(0, net.outboundArcs(newPlace).size());
        assertEquals(1, net.outboundArcs(oldPlace).size());
        assertEquals(2, net.getPlaces().size());
        net.replacePlace(oldPlace, newPlace);
        assertEquals(1, net.outboundArcs(newPlace).size());
        assertEquals(1, net.getPlaces().size());
        assertEquals(newPlace, net.getInboundArcs().iterator().next().getSource());
        assertEquals(newPlace, net.getPlaces().iterator().next());
        net.replacePlace(newPlace, new DiscretePlace("P2", "P2"));
        Place p2 = net.getComponent("P2", Place.class);
        assertEquals(1, net.outboundArcs(p2).size());
        assertEquals(1, net.getPlaces().size());
        assertEquals(p2, net.getInboundArcs().iterator().next().getSource());
        assertEquals("wasnt previously in the net, but now added", p2, net.getPlaces().iterator().next());
    }

    @Test
    public void printsReportOfAllPlaceMarkings() throws Exception {
        net = createSimplePetriNet(1, "aNet");
        assertEquals("P1: red=0  Default=1  \n" +
                "P2: red=0  Default=0  \n", net.getPlaceReport(false));
    }

    @Test
    public void printsReportOfOnlyMarkedPlaces() throws Exception {
        net = createSimplePetriNet(1, "aNet");
        assertEquals("P1: red=0  Default=1  \n", net.getPlaceReport(true));
    }

    protected void buildNetWithOldAndNewPlaces(String source, String target) throws PetriNetComponentException {
        net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0"))
                .and(APlace.withId("P1")).and(AnImmediateTransition.withId("T0"))
                .andFinally(ANormalArc.withSource(source).andTarget(target));
        oldPlace = net.getComponent("P0", Place.class);
        newPlace = net.getComponent("P1", Place.class);
    }
}
