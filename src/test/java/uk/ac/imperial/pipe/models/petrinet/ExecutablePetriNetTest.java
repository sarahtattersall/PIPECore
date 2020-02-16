package uk.ac.imperial.pipe.models.petrinet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
import uk.ac.imperial.pipe.dsl.ATimedTransition;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;
import uk.ac.imperial.pipe.tuple.Tuple;
import uk.ac.imperial.state.HashedStateBuilder;
import uk.ac.imperial.state.State;

@RunWith(MockitoJUnitRunner.class)
public class ExecutablePetriNetTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private PetriNet net;
    private ExecutablePetriNet executablePetriNet;

    @Mock
    private PropertyChangeListener mockListener;

    private PetriNet net2;

    @Before
    public void setUp() {
        net = new PetriNet(new NormalPetriNetName("net"));
        executablePetriNet = net.getExecutablePetriNet();
    }

    @Test
    public void equalsAndHashCodeLawsWhenEqual() throws PetriNetComponentException {
        net = buildTestNet();
        executablePetriNet = net.getExecutablePetriNet();
        PetriNet net2 = buildTestNet();
        ExecutablePetriNet epn2 = net2.getExecutablePetriNet();
        assertTrue(executablePetriNet.equals(epn2));
        assertEquals(executablePetriNet.hashCode(), epn2.hashCode());
    }

    @Test
    public void buildsListOfTokensForValidation() throws PetriNetComponentException {
        net = buildTestNet();
        executablePetriNet = net.getExecutablePetriNet();
        List<String> tokenNames = executablePetriNet.getTokenNamesForValidation();
        assertEquals(1, tokenNames.size());
        assertEquals("Default", tokenNames.get(0));
    }

    @Test
    public void buildsListOfPlacesAndExternalAccessibility() throws PetriNetComponentException {
        net = buildTestNet();
        Place p1 = net.getComponent("P1", Place.class);
        p1.setStatus(new PlaceStatusInterface());
        p1.getStatus().setExternal(true);
        executablePetriNet = net.getExecutablePetriNet();
        Map<String, Boolean> placeMap = executablePetriNet.getPlaceMapForValidation();
        assertEquals(2, placeMap.size());
        assertEquals("P0", placeMap.keySet().iterator().next());
        assertFalse("not externally accessible", placeMap.get("P0"));
        assertTrue("externally accessible", placeMap.get("P1"));
    }

    @Test
    public void equalsAndHashCodeLawsWhenNotEqual() throws PetriNetComponentException {
        net = buildTestNet();
        executablePetriNet = net.getExecutablePetriNet();
        PetriNet net2 = buildTestNet();
        net2.add(new DiscreteTransition("T99"));
        ExecutablePetriNet epn2 = net2.getExecutablePetriNet();
        assertFalse(executablePetriNet.equals(epn2));
        assertNotEquals(executablePetriNet.hashCode(), epn2.hashCode());
    }

    @Test
    public void collectionsMatchOriginalPetriNet() throws PetriNetComponentException {
        net = buildTestNet();
        executablePetriNet = net.getExecutablePetriNet();
        assertThat(executablePetriNet.getAnnotations()).hasSize(0);
        assertThat(executablePetriNet.getTokens()).hasSize(1);
        assertThat(executablePetriNet.getTransitions()).hasSize(2);
        assertThat(executablePetriNet.getInboundArcs()).hasSize(1);
        assertThat(executablePetriNet.getOutboundArcs()).hasSize(1);
        assertThat(executablePetriNet.getArcs()).hasSize(2);
        assertThat(executablePetriNet.getPlaces()).hasSize(2);
        assertThat(executablePetriNet.getRateParameters()).hasSize(0);
    }

    @Test
    public void componentsFound() throws Exception {
        net = buildTestNet();
        executablePetriNet = net.getExecutablePetriNet();
        assertTrue(executablePetriNet.containsComponent("T0"));
        assertFalse(executablePetriNet.containsComponent("FRED"));

        Transition t0 = executablePetriNet.getComponent("T0", Transition.class);
        Transition t1 = executablePetriNet.getComponent("T1", Transition.class);
        assertThat(executablePetriNet.inboundArcs(t1)).hasSize(1);
        assertThat(executablePetriNet.inboundArcs(t0)).hasSize(0);
        assertThat(executablePetriNet.outboundArcs(t0)).hasSize(1);
        InboundArc arc = executablePetriNet.getComponent("P1 TO T1", InboundArc.class);
        assertTrue(executablePetriNet.inboundArcs(t1).contains(arc));
        //TODO outboundArcs(Place place) s
    }

    @Test
    public void verifyPlaceCountUpdateIsMirroredToPlaceInOriginalPetriNet() throws Exception {
        net = buildTestNet();
        executablePetriNet = net.getExecutablePetriNet();
        Place epnp1 = executablePetriNet.getComponent("P1", Place.class);
        Place netp1 = net.getComponent("P1", Place.class);
        assertEquals(0, epnp1.getTokenCount("Default"));
        epnp1.setTokenCount("Default", 2);
        assertEquals(2, epnp1.getTokenCount("Default"));
        assertEquals(2, netp1.getTokenCount("Default"));
    }

    @Test
    public void evaluatesFunctionalExpressionAgainstCurrentState() throws Exception {
        net = buildTestNet();
        executablePetriNet = net.getExecutablePetriNet();
        Place epnp1 = executablePetriNet.getComponent("P1", Place.class);
        epnp1.setTokenCount("Default", 2);
        assertEquals(new Double(2.0), executablePetriNet.evaluateExpression("#(P1)"));
    }

    @Test
    public void evaluatesFunctionalExpressionGivenState() throws Exception {
        net = buildTestNet();
        executablePetriNet = net.getExecutablePetriNet();
        HashedStateBuilder builder = new HashedStateBuilder();
        builder.placeWithToken("P1", "Default", 4);
        State state = builder.build();
        assertEquals(new Double(4.0), executablePetriNet.evaluateExpression(state, "#(P1)"));
    }

    @Test
    public void returnsNegativeOneForInvalidFunctionalExpression() throws Exception {
        net = buildTestNet();
        executablePetriNet = net.getExecutablePetriNet();
        Place epnp1 = executablePetriNet.getComponent("P1", Place.class);
        epnp1.setTokenCount("Default", 2);
        assertEquals(new Double(-1.0), executablePetriNet.evaluateExpression("Fred(P1)"));
    }

    @Test
    public void stateCanBeExtractedAndThenReappliedResettingBothExecutableAndSourcePetriNets() throws Exception {
        net = buildTestNet();
        executablePetriNet = net.getExecutablePetriNet();
        State beforeState = executablePetriNet.getState();
        Place epnp1 = executablePetriNet.getComponent("P1", Place.class);
        Place netp1 = net.getComponent("P1", Place.class);
        assertEquals(0, epnp1.getTokenCount("Default"));
        assertEquals(0, netp1.getTokenCount("Default"));
        epnp1.setTokenCount("Default", 2);
        assertEquals(2, epnp1.getTokenCount("Default"));
        assertEquals(2, netp1.getTokenCount("Default"));
        assertNotEquals(beforeState, executablePetriNet.getState());
        executablePetriNet.setState(beforeState);
        epnp1 = executablePetriNet.getComponent("P1", Place.class);
        netp1 = net.getComponent("P1", Place.class);
        assertEquals(0, epnp1.getTokenCount("Default"));
        assertEquals(0, netp1.getTokenCount("Default"));
        assertEquals(beforeState, executablePetriNet.getState());
    }

    @Test
    public void verifyExecutablePetriNetSeesAllIncludedComponentsWithAppropriatePrefixes() throws Exception {
        net.addPlace(new DiscretePlace("P0", "P0"));
        net2 = new PetriNet();
        net2.addPlace(new DiscretePlace("P1", "P1"));
        net2.addPlace(new DiscretePlace("P2", "P2"));
        net.getIncludeHierarchy().include(net2, "some-function");
        assertEquals("source PN only sees root components", 1, net.getPlaces().size());
        assertEquals("...but EPN sees all components", 3, executablePetriNet.getPlaces().size());
        assertEquals("components from root net default to no prefix", "P0", executablePetriNet
                .getComponent("P0", Place.class).getId());
        assertEquals("components from included nets are prefixed in the executable PN", ".some-function.P1", executablePetriNet
                .getComponent(".some-function.P1", Place.class).getId());
        assertEquals(".some-function.P2", executablePetriNet.getComponent(".some-function.P2", Place.class).getId());

        assertEquals("source PN component ids unaffected", "P0", net.getComponent("P0", Place.class).getId());
        assertEquals("P1", net2.getComponent("P1", Place.class).getId());
    }

    @Test
    public void notifiesListenersWhenRefreshed() throws Exception {
        executablePetriNet.addPropertyChangeListener(ExecutablePetriNet.PETRI_NET_REFRESHED_MESSAGE, mockListener);
        executablePetriNet.getState();
        verify(mockListener, never()).propertyChange(any(PropertyChangeEvent.class));
        executablePetriNet.refreshRequired();
        executablePetriNet.refresh();
        verify(mockListener).propertyChange(any(PropertyChangeEvent.class));
    }

    //This behavior is implicitly tested in ClonePetriNetTest so is not strictly necessary but left as a contrast to
    // refreshOfExecutablePetriNetRemovesOldExecutablePlacesAsListenersForGuiPlaceChanges,
    // where listening is bi-directional
    @Test
    public void refreshedExecutablePetriNetTransitionsWillNotifyGuiTransitions() throws Exception {
        PetriNet petriNet = buildSimpleNet();
        petriNet.setIncludeHierarchy(new IncludeHierarchy(petriNet, "root"));
        ExecutablePetriNet executablePetriNet = petriNet.getExecutablePetriNet();
        DiscreteTransition rootT0 = (DiscreteTransition) executablePetriNet.getComponent("root.T0", Transition.class);
        DiscreteTransition transition = (DiscreteTransition) petriNet.getComponent("T0", Transition.class);
        checkConnectableHasListener("enabling of executable transition will notify GUI", true, rootT0, transition);
        executablePetriNet.refreshRequired();
        executablePetriNet.refresh();
        DiscreteTransition rootT0new = (DiscreteTransition) executablePetriNet
                .getComponent("root.T0", Transition.class);
        checkConnectableHasListener("enabling of refreshed executable transition will notify GUI", true, rootT0new, transition);
        checkConnectableHasListener("old executable transition not being listened to", false, rootT0, transition);
        PropertyChangeListener[] listeners = rootT0.changeSupport.getPropertyChangeListeners();
        assertEquals("...no one now listening to old executable", 0, listeners.length);
    }

    @Test
    public void refreshOfExecutablePetriNetRemovesOldExecutablePlacesAsListenersForGuiPlaceChanges() throws Exception {
        PetriNet petriNet = buildSimpleNet();
        petriNet.setIncludeHierarchy(new IncludeHierarchy(petriNet, "root"));
        ExecutablePetriNet executablePetriNet = petriNet.getExecutablePetriNet();
        DiscretePlace rootP0 = (DiscretePlace) executablePetriNet.getComponent("root.P0", Place.class);
        DiscretePlace place = (DiscretePlace) petriNet.getComponent("P0", Place.class);
        checkConnectableHasListener("GUI token changes will notify executable", true, place, rootP0);
        executablePetriNet.refreshRequired();
        executablePetriNet.refresh();
        DiscretePlace rootP0new = (DiscretePlace) executablePetriNet.getComponent("root.P0", Place.class);
        checkConnectableHasListener("GUI token changes will notify refreshed executable", true, place, rootP0new);
        checkConnectableHasListener("...but will no longer notify old executable", false, place, rootP0);
        checkConnectableHasListener("token changes to executable will notify GUI", true, rootP0new, place);
        // rootP0 still has a reference to place, but not vice versa, so rootP0 should be garbage-collectable
        checkConnectableHasListener("old executable not being listened to", false, rootP0, place);
        PropertyChangeListener[] listeners = rootP0.changeSupport.getPropertyChangeListeners();
        assertEquals("...no one now listening to old executable", 0, listeners.length);
    }

    @Test
    public void refreshOfExecutablePetriNetRemovesOldListenersForAllComponents() throws Exception {
        PetriNet petriNet = buildSimpleNet();
        petriNet.setIncludeHierarchy(new IncludeHierarchy(petriNet, "root"));
        ExecutablePetriNet executablePetriNet = petriNet.getExecutablePetriNet();
        DiscretePlace rootP0 = (DiscretePlace) executablePetriNet.getComponent("root.P0", Place.class);
        DiscreteTransition rootT0 = (DiscreteTransition) executablePetriNet.getComponent("root.T0", Transition.class);
        OutboundNormalArc rootOutArc = (OutboundNormalArc) executablePetriNet
                .getComponent("root.T0 TO root.P1", OutboundArc.class);
        InboundNormalArc rootInArc = (InboundNormalArc) executablePetriNet
                .getComponent("root.P0 TO root.T0", InboundArc.class);
        ColoredToken token = (ColoredToken) executablePetriNet
                .getComponent("Default", Token.class);
        checkPubSubHasListeners(rootP0, true);
        checkPubSubHasListeners(rootT0, true);
        checkPubSubHasListeners(rootOutArc, true);
        checkPubSubHasListeners(rootInArc, true);

        token.addPropertyChangeListener(mockListener);
        checkPubSubHasListeners(token, true);
        FunctionalRateParameter rateParameter = new FunctionalRateParameter("5*2", "R1", "R1");
        executablePetriNet.rateParameters.put("rate", rateParameter);
        rateParameter.addPropertyChangeListener(mockListener);
        checkPubSubHasListeners(rateParameter, true);

        AnnotationImpl annotation = new AnnotationImpl(0, 0, "foo", 10, 10, false);
        executablePetriNet.annotations.put("foo", annotation);
        annotation.addPropertyChangeListener(mockListener);
        checkPubSubHasListeners(annotation, true);
        executablePetriNet.refreshRequired();
        executablePetriNet.refresh();

        checkPubSubHasListeners(rootP0, false);
        checkPubSubHasListeners(rootT0, false);
        checkPubSubHasListeners(rootOutArc, false);
        checkPubSubHasListeners(rootInArc, false);
        checkPubSubHasListeners(token, false);
        checkPubSubHasListeners(rateParameter, false);
        checkPubSubHasListeners(annotation, false);
    }

    private void checkPubSubHasListeners(AbstractPetriNetPubSub component, boolean hasListeners) {
        PropertyChangeListener[] listeners = component.changeSupport.getPropertyChangeListeners();
        assertEquals(hasListeners, (listeners.length != 0));
    }

    @Test
    public void returnsTupleOfOnlyEnabledImmediateAndTimedTransitions() throws Exception {
        PetriNet petriNet = buildNetWithImmediateAndTimedTransitionsSomeEnabled();
        ExecutablePetriNet executablePetriNet = petriNet.getExecutablePetriNet();
        Tuple<Set<Transition>, Set<Transition>> tuple = executablePetriNet.getEnabledImmediateAndTimedTransitions();
        checkT0ImmediateAndT2TimedEnabled(tuple);
    }

    @Test
    public void returnsEnabledTransitionsForProvidedState() throws Exception {
        executablePetriNet = buildNetWithImmediateAndTimedTransitionsSomeEnabled().getExecutablePetriNet();
        State state = executablePetriNet.getState();
        Transition t0 = executablePetriNet.getComponent("T0", Transition.class);
        assertTrue(executablePetriNet.isEnabled(t0));
        executablePetriNet.fireTransition(t0);
        assertFalse("T0 no longer enabled in EPN", executablePetriNet.isEnabled(t0));
        Tuple<Set<Transition>, Set<Transition>> tuple = executablePetriNet
                .getEnabledImmediateAndTimedTransitions(state);
        // although T0 in EPN is disabled, sets of enabled transitions for State are not affected
        checkT0ImmediateAndT2TimedEnabled(tuple);
        assertTrue("...but T0 still enabled in State", executablePetriNet.isEnabled(t0, state));
    }

    @Test
    public void transitionNotEnabledIfNoIncomingArcs() throws Exception {
        net = new PetriNet();
        net.addTransition(new DiscreteTransition("T0"));
        executablePetriNet = net.getExecutablePetriNet();
        State state = executablePetriNet.getState();
        Tuple<Set<Transition>, Set<Transition>> tuple = executablePetriNet
                .getEnabledImmediateAndTimedTransitions(state);
        assertEquals(0, tuple.tuple1.size());
        assertEquals(0, tuple.tuple2.size());
    }

    protected void checkT0ImmediateAndT2TimedEnabled(
            Tuple<Set<Transition>, Set<Transition>> tuple) {
        Set<Transition> immediateTransitions = tuple.tuple1;
        Set<Transition> timedTransitions = tuple.tuple2;
        assertEquals("only 1 of the 2 immediate transitions is enabled", 1, immediateTransitions.size());
        assertEquals("T0", immediateTransitions.iterator().next().getId());
        assertEquals("T2", timedTransitions.iterator().next().getId());
    }

    private PetriNet buildNetWithImmediateAndTimedTransitionsSomeEnabled() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P0").and(1, "Default").token()).and(APlace.withId("P1"))
                .and(AnImmediateTransition.withId("T0"))
                .and(AnImmediateTransition.withId("T1"))
                .and(ATimedTransition.withId("T2"))
                .and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token())
                .and(ANormalArc.withSource("T0").andTarget("P1").with("1", "Default").token())
                .and(ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token())
                .andFinally(ANormalArc.withSource("P0").andTarget("T2").with("1", "Default").token());
        return petriNet;
    }

    @Test
    public void returnsOnlyTransitionsThatHaveMaximumPriority() throws Exception {
        PetriNet petriNet = buildNetWithTransitionsOfMultiplePriorities();
        ExecutablePetriNet executablePetriNet = petriNet.getExecutablePetriNet();
        Collection<Transition> allTransitions = executablePetriNet.getTransitions();
        Set<Transition> maxPriorityTransitions = executablePetriNet.maximumPriorityTransitions(allTransitions);
        Transition t3 = executablePetriNet.getComponent("T3", Transition.class);
        Transition t4 = executablePetriNet.getComponent("T4", Transition.class);
        assertEquals(2, maxPriorityTransitions.size());
        assertTrue(maxPriorityTransitions.contains(t3));
        assertTrue(maxPriorityTransitions.contains(t4));
    }

    private PetriNet buildNetWithTransitionsOfMultiplePriorities() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(AnImmediateTransition.withId("T0").andPriority(1))
                .and(AnImmediateTransition.withId("T1").andPriority(1))
                .and(AnImmediateTransition.withId("T2").andPriority(2))
                .and(AnImmediateTransition.withId("T3").andPriority(3))
                .andFinally(AnImmediateTransition.withId("T4").andPriority(3));
        return petriNet;
    }

    @Test
    public void timedTransitionsUnderControlOfTimingQueue() throws Exception {
        executablePetriNet = buildNetMultipleTimedTransitionsDifferentTimesSomeEnabled()
                .getExecutablePetriNet();
        assertEquals(0, executablePetriNet.getCurrentTime());
        Set<Transition> currentTimedTransitions = executablePetriNet.getCurrentlyEnabledTimedTransitions();
        assertEquals(0, currentTimedTransitions.size());
        executablePetriNet.setCurrentTime(5);
        currentTimedTransitions = executablePetriNet.getCurrentlyEnabledTimedTransitions();
        assertEquals("T2", currentTimedTransitions.iterator().next().getId());
        executablePetriNet.setCurrentTime(10);
        currentTimedTransitions = executablePetriNet.getCurrentlyEnabledTimedTransitions();
        assertEquals("T3", currentTimedTransitions.iterator().next().getId());
    }

    @Test
    public void fireTimedTransitionsWithUpdatesToTimingQueue() throws Exception {
        executablePetriNet = buildNetMultipleTimedTransitionsDifferentTimesSomeEnabled()
                .getExecutablePetriNet();
        checkState("two marked input places", executablePetriNet.getState(), "P0", 1, "P2", 1);
        executablePetriNet.setCurrentTime(5);
        Set<Transition> currentTimedTransitions = executablePetriNet.getCurrentlyEnabledTimedTransitions();
        assertEquals(1, currentTimedTransitions.size());
        Transition t2 = currentTimedTransitions.iterator().next();
        executablePetriNet.fireTransition(t2);
        assertEquals(0, executablePetriNet.getCurrentlyEnabledTimedTransitions().size());
        executablePetriNet.setCurrentTime(10);
        currentTimedTransitions = executablePetriNet.getCurrentlyEnabledTimedTransitions();
        assertEquals(1, currentTimedTransitions.size());
        Transition t3 = currentTimedTransitions.iterator().next();
        State finalState = executablePetriNet.fireTransition(t3);
        assertEquals(0, executablePetriNet.getCurrentlyEnabledTimedTransitions().size());
        assertEquals("timing queue reflects current time", 10, executablePetriNet.getCurrentTime());
        checkStateAndPlaces("both input places empty", finalState, "P0", 0, "P2", 0);
    }

    @Test
    public void fireTimedTransitionsUsingSeparateTimingQueueWithoutUpdatesToEPNTimingQueue() throws Exception {
        executablePetriNet = buildNetMultipleTimedTransitionsDifferentTimesSomeEnabled()
                .getExecutablePetriNet();
        State state = executablePetriNet.getState();
        checkState("two marked input places", executablePetriNet.getState(), "P0", 1, "P2", 1);
        TimingQueue timingQueue = new TimingQueue(executablePetriNet, 0);
        assertEquals("2 times registered in timingQueue", 2, timingQueue.getAllFiringTimes().size());
        assertEquals("...and in the EPN", 2, executablePetriNet.getTimingQueue().getAllFiringTimes().size());
        timingQueue.setCurrentTime(5);
        executablePetriNet.setCurrentTime(5);
        Set<Transition> currentTimedTransitions = timingQueue.getCurrentlyEnabledTimedTransitions();
        Transition t2 = currentTimedTransitions.iterator().next();
        State stateT2 = executablePetriNet.fireTransition(t2, state);
        assertTrue(timingQueue.dequeueAndRebuild(t2, stateT2));
        assertEquals("local timing queue knows we fired transition", 0, timingQueue
                .getCurrentlyEnabledTimedTransitions().size());
        assertEquals("...but EPN does not", 1, executablePetriNet.getCurrentlyEnabledTimedTransitions().size());
        long nextTime = timingQueue.getNextFiringTime();
        // or, timingQueue.setCurrentTime(10) and tq.getCurrentlyEnabledTimedTransitions()
        assertEquals(10, nextTime);
        Transition t3 = timingQueue.getEnabledTransitionsAtTime(nextTime).iterator().next();
        State stateT3 = executablePetriNet.fireTransition(t3, stateT2);
        assertTrue(timingQueue.dequeueAndRebuild(t3, stateT3));
        assertEquals("no times left to fire", 0, timingQueue.getAllFiringTimes().size());
        assertEquals("EPN timing queue untouched", 2, executablePetriNet.getTimingQueue().getAllFiringTimes().size());
    }

    private PetriNet buildNetMultipleTimedTransitionsDifferentTimesSomeEnabled() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P0").and(1, "Default").token())
                .and(APlace.withId("P1")).and(APlace.withId("P2").and(1, "Default").token())
                .and(AnImmediateTransition.withId("T1")) // immediate, but not enabled (P1 empty)
                .and(ATimedTransition.withId("T0")) // timed, but not enabled (P1 empty)
                .and(ATimedTransition.withId("T2").andDelay(5)) // enabled (P0 marked)
                .and(ATimedTransition.withId("T3").andDelay(10)) // enabled (P2 marked)
                .and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token())
                .and(ANormalArc.withSource("P0").andTarget("T2").with("1", "Default").token())
                .and(ANormalArc.withSource("P1").andTarget("T0").with("1", "Default").token())
                .and(ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token())
                .andFinally(ANormalArc.withSource("P2").andTarget("T3").with("1", "Default").token());
        return petriNet;
    }

    @Test
    public void timedTransitionsFiredUnderTimingQueueControl() throws Exception {
        executablePetriNet = buildNetMultipleTimedTransitionsDifferentTimesSomeEnabled()
                .getExecutablePetriNet();
        State state = executablePetriNet.getState();
        TimingQueue timingQueue = new TimingQueue(executablePetriNet, 0);
        assertTrue(timingQueue.hasUpcomingTimedTransition());
        int round = 0;
        while (timingQueue.hasUpcomingTimedTransition()) {
            timingQueue.setCurrentTime(timingQueue.getNextFiringTime());
            Set<Transition> transitions = timingQueue.getCurrentlyEnabledTimedTransitions();
            for (Transition transition : transitions) {
                state = executablePetriNet.fireTransition(transition, state);
                timingQueue.dequeueAndRebuild(transition, state);
                checkTimingQueue(++round, transition, timingQueue);
            }
        }
    }

    @Test
    public void timedTransitionsFiredUnderExecutablePetriNetControl() throws Exception {
        executablePetriNet = buildNetMultipleTimedTransitionsDifferentTimesSomeEnabled()
                .getExecutablePetriNet();
        int round = 0;
        while (executablePetriNet.getTimingQueue().hasUpcomingTimedTransition()) {
            executablePetriNet.setCurrentTime(executablePetriNet.getTimingQueue().getNextFiringTime());
            Set<Transition> transitions = executablePetriNet.getCurrentlyEnabledTimedTransitions();
            for (Transition transition : transitions) {
                executablePetriNet.fireTransition(transition);
                checkTimingQueue(++round, transition, executablePetriNet.getTimingQueue());
            }
        }
    }

    private void checkTimingQueue(int round, Transition transition,
            TimingQueue timingQueue) {
        if (round == 1) {
            assertEquals(5, timingQueue.getCurrentTime());
            assertEquals("T2", transition.getId());
            assertEquals(0, timingQueue.getCurrentlyEnabledTimedTransitions().size());
        } else if (round == 2) {
            assertEquals(10, timingQueue.getCurrentTime());
            assertEquals("T3", transition.getId());
            assertEquals(0, timingQueue.getCurrentlyEnabledTimedTransitions().size());
        } else {
            fail("should not have more than 2 rounds");
        }
    }

    @Test
    public void twoEnabledTimedTransitionsBothDisabledWhenOneFires() throws Exception {
        executablePetriNet = buildNetTwoTimedTransitions()
                .getExecutablePetriNet();
        assertEquals(1, executablePetriNet.getTimingQueue().getAllFiringTimes().size());
        executablePetriNet.setCurrentTime(5);
        Set<Transition> currentTimedTransitions = executablePetriNet.getCurrentlyEnabledTimedTransitions();
        assertEquals(2, currentTimedTransitions.size());
        Transition t0 = currentTimedTransitions.iterator().next();
        executablePetriNet.fireTransition(t0);
        assertEquals("both T0 and T1 no longer enabled", 0, executablePetriNet.getCurrentlyEnabledTimedTransitions()
                .size());
        assertEquals(0, executablePetriNet.getTimingQueue().getAllFiringTimes().size());
    }

    private PetriNet buildNetTwoTimedTransitions() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P0").and(1, "Default").token())
                .and(ATimedTransition.withId("T0").andDelay(5))
                .and(ATimedTransition.withId("T1").andDelay(5))
                .and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token())
                .andFinally(ANormalArc.withSource("P0").andTarget("T1").with("1", "Default").token());
        return petriNet;
    }

    @Test
    public void consumesAndProducesTokensUpdatingStateOnly() throws Exception {
        executablePetriNet = buildSimpleNet()
                .getExecutablePetriNet();
        State state = executablePetriNet.getState();
        HashedStateBuilder builder = new HashedStateBuilder(state);
        checkState(state, "P0", 1, "P1", 0);
        Transition t0 = executablePetriNet.getComponent("T0", Transition.class);
        Transition t1 = executablePetriNet.getComponent("T1", Transition.class);
        State consumeState0 = executablePetriNet.consumeInboundTokens(builder, t0, state, false);
        checkState(consumeState0, "P0", 0, "P1", 0);
        checkState(executablePetriNet.getState(), "P0", 1, "P1", 0);
        State produceState0 = executablePetriNet.produceOutboundTokens(builder, t0, consumeState0, false);
        checkState(produceState0, "P0", 0, "P1", 1);
        checkState(executablePetriNet.getState(), "P0", 1, "P1", 0);
        State consumeState1 = executablePetriNet.consumeInboundTokens(builder, t1, produceState0, false);
        checkState(consumeState1, "P0", 0, "P1", 0);
        checkState(executablePetriNet.getState(), "P0", 1, "P1", 0);
        State produceState1 = executablePetriNet.produceOutboundTokens(builder, t1, consumeState1, false);
        checkState(produceState1, "P0", 1, "P1", 0);
        checkState(executablePetriNet.getState(), "P0", 1, "P1", 0);
    }

    @Test
    public void consumesAndProducesTokensUpdatingStateAndPlaces() throws Exception {
        executablePetriNet = buildSimpleNet()
                .getExecutablePetriNet();
        State state = executablePetriNet.getState();
        HashedStateBuilder builder = new HashedStateBuilder(state);
        checkState(state, "P0", 1, "P1", 0);
        Transition t0 = executablePetriNet.getComponent("T0", Transition.class);
        Transition t1 = executablePetriNet.getComponent("T1", Transition.class);
        State consumeState0 = executablePetriNet.consumeInboundTokens(builder, t0, state, true);
        checkStateAndPlaces(consumeState0, "P0", 0, "P1", 0);
        State produceState0 = executablePetriNet.produceOutboundTokens(builder, t0, consumeState0, true);
        checkStateAndPlaces(produceState0, "P0", 0, "P1", 1);
        State consumeState1 = executablePetriNet.consumeInboundTokens(builder, t1, produceState0, true);
        checkStateAndPlaces(consumeState1, "P0", 0, "P1", 0);
        State produceState1 = executablePetriNet.produceOutboundTokens(builder, t1, consumeState1, true);
        checkStateAndPlaces(produceState1, "P0", 1, "P1", 0);
    }

    @Test
    public void fireTransitionConsumesAndProducesTokensUpdatingStateOnly() throws Exception {
        executablePetriNet = buildSimpleNet()
                .getExecutablePetriNet();
        State state = executablePetriNet.getState();
        checkStateAndPlaces("initial state and EPN are the same", state, "P0", 1, "P1", 0);
        Transition t0 = executablePetriNet.getComponent("T0", Transition.class);
        State returnedState = executablePetriNet.fireTransition(t0, state);
        checkState(returnedState, "P0", 0, "P1", 1);
        checkState("underlying EPN unchanged", executablePetriNet.getState(), "P0", 1, "P1", 0);
        Transition t1 = executablePetriNet.getComponent("T1", Transition.class);
        State finalState = executablePetriNet.fireTransition(t1, returnedState);
        checkStateAndPlaces("final state matches original state", finalState, "P0", 1, "P1", 0);
    }

    @Test
    public void fireTransitionConsumesAndProducesTokensUpdatingStateAndPlaces() throws Exception {
        executablePetriNet = buildSimpleNet()
                .getExecutablePetriNet();
        State state = executablePetriNet.getState();
        checkStateAndPlaces(state, "P0", 1, "P1", 0);
        Transition t0 = executablePetriNet.getComponent("T0", Transition.class);
        State returnedState = executablePetriNet.fireTransition(t0);
        checkStateAndPlaces("as state not passed but taken from EPN, both state & places always match", returnedState, "P0", 0, "P1", 1);
        Transition t1 = executablePetriNet.getComponent("T1", Transition.class);
        State finalState = executablePetriNet.fireTransition(t1);
        checkStateAndPlaces("final state matches original state", finalState, "P0", 1, "P1", 0);
    }

    private void checkStateAndPlaces(String comment, State state, String place0, int count0,
            String place1, int count1) {
        checkState(comment, state, place0, count0, place1, count1);
        checkState(comment, executablePetriNet.getState(), place0, count0, place1, count1);
    }

    private void checkStateAndPlaces(State state, String place0, int count0,
            String place1, int count1) {
        checkStateAndPlaces("", state, place0, count0, place1, count1);
    }

    private void checkState(String comment, State state, String place0, int count0, String place1, int count1) {
        assertEquals(comment, count0, (int) state.getTokens(place0).get("Default"));
        assertEquals(comment, count1, (int) state.getTokens(place1).get("Default"));
    }

    private void checkState(State state, String place0, int count0, String place1, int count1) {
        checkState("", state, place0, count0, place1, count1);
    }

    protected void checkConnectableHasListener(boolean expected, Connectable connectable,
            Connectable listeningConnectable) {
        checkConnectableHasListener("", expected, connectable, listeningConnectable);
    }

    protected void checkConnectableHasListener(String comment, boolean expected, Connectable connectable,
            Connectable listeningConnectable) {
        boolean found = false;
        PropertyChangeListener[] listeners = ((AbstractPetriNetPubSub) connectable).changeSupport
                .getPropertyChangeListeners();
        for (PropertyChangeListener propertyChangeListener : listeners) {
            if (propertyChangeListener == listeningConnectable) {
                // equals() won't work because overridden in DiscretePlace
                found = true;
            }
        }
        assertEquals(expected, found);
    }

    private PetriNet buildSimpleNet() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P0").and(1, "Default").token()).and(APlace.withId("P1"))
                .and(AnImmediateTransition.withId("T0")).and(AnImmediateTransition.withId("T1"))
                .and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token())
                .and(ANormalArc.withSource("T0").andTarget("P1").with("1", "Default").token())
                .and(ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token())
                .andFinally(ANormalArc.withSource("T1").andTarget("P0").with("1", "Default").token());
        return petriNet;
    }

    protected PetriNet buildNet1() throws PetriNetComponentException {
        PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0"))
                .and(AnImmediateTransition.withId("T0")).and(AnImmediateTransition.withId("T1"))
                .andFinally(ANormalArc.withSource("T0").andTarget("P0").with("#(P0)", "Default").token());
        return net;
    }

    protected PetriNet buildNet2() throws PetriNetComponentException {
        PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0"))
                .and(APlace.withId("P1")).and(APlace.withId("P2")).and(APlace.withId("P3"))
                .and(AnImmediateTransition.withId("T0")).and(AnImmediateTransition.withId("T1"))
                .and(AnImmediateTransition.withId("T2")).and(AnImmediateTransition.withId("T3"))
                .and(ANormalArc.withSource("P1").andTarget("T2")).and(ANormalArc.withSource("T2").andTarget("P2"))
                .andFinally(ANormalArc.withSource("T3").andTarget("P3").with("#(P3)", "Default").token());
        return net;
    }

    @Test
    public void convertArcsForPlacesWithMergeStatusToArcsFromSourcePlace() throws Exception {
        net = buildNet1();
        net.setName(new NormalPetriNetName("net"));
        net2 = buildNet2();
        IncludeHierarchy includes = new IncludeHierarchy(net, "top");
        includes.include(net2, "a");
        net.setIncludeHierarchy(includes);
        executablePetriNet = net.getExecutablePetriNet();
        assertEquals(5, executablePetriNet.getPlaces().size());
        assertEquals(6, executablePetriNet.getTransitions().size());
        assertEquals(4, executablePetriNet.getArcs().size());
        assertEquals(1, includes.getPetriNet().getPlaces().size());
        assertEquals(1, includes.getPetriNet().getArcs().size());
        assertEquals(4, includes.getInclude("a").getPetriNet().getPlaces().size());
        assertEquals(3, includes.getInclude("a").getPetriNet().getArcs().size());
        Place originP1 = net2.getComponent("P1", Place.class);
        Place originP2 = net2.getComponent("P2", Place.class);
        Place originP3 = net2.getComponent("P3", Place.class);
        includes.getInclude("a").addToInterface(originP1, true, false, false, false);
        includes.getInclude("a").addToInterface(originP2, true, false, false, false);
        includes.getInclude("a").addToInterface(originP3, true, false, false, false);
        assertEquals("haven't added them to the net yet", 1, includes.getPetriNet().getPlaces().size());
        assertTrue(includes.getInterfacePlace("a.P1").getStatus()
                .getMergeInterfaceStatus() instanceof MergeInterfaceStatusAvailable);
        assertTrue(includes.getInterfacePlace("a.P2").getStatus()
                .getMergeInterfaceStatus() instanceof MergeInterfaceStatusAvailable);
        assertTrue(includes.getInterfacePlace("a.P3").getStatus()
                .getMergeInterfaceStatus() instanceof MergeInterfaceStatusAvailable);
        assertTrue(includes.getInclude("a").getInterfacePlace("P1").getStatus()
                .getMergeInterfaceStatus() instanceof MergeInterfaceStatusHome);
        includes.addAvailablePlaceToPetriNet(includes.getInterfacePlace("a.P1"));
        includes.addAvailablePlaceToPetriNet(includes.getInterfacePlace("a.P2"));
        assertTrue(includes.getInterfacePlace("a.P1").getStatus()
                .getMergeInterfaceStatus() instanceof MergeInterfaceStatusAway);
        assertTrue(includes.getInterfacePlace("a.P2").getStatus()
                .getMergeInterfaceStatus() instanceof MergeInterfaceStatusAway);
        assertTrue("didn't use it, so still available", includes.getInterfacePlace("a.P3").getStatus()
                .getMergeInterfaceStatus() instanceof MergeInterfaceStatusAvailable);
        assertEquals("2 have been added", 3, includes.getPetriNet().getPlaces().size());
        assertEquals(5, executablePetriNet.getPlaces().size());

        checkNewHomePlace("a.P1");
        checkNewHomePlace("a.P2");

        Place topIP1 = includes.getInterfacePlace("a.P1");
        Place topIP2 = includes.getInterfacePlace("a.P2");
        Transition topT1 = net.getComponent("T1", Transition.class);
        assertEquals(4, executablePetriNet.getArcs().size());
        InboundArc arcIn = new InboundNormalArc(topIP1, topT1, new HashMap<String, String>());
        OutboundArc arcOut = new OutboundNormalArc(topT1, topIP2, new HashMap<String, String>());
        assertEquals(1, includes.getPetriNet().getArcs().size());
        net.add(arcIn);
        net.add(arcOut);
        assertEquals(3, includes.getPetriNet().getArcs().size());
        assertEquals(6, executablePetriNet.getArcs().size());
        assertEquals(5, executablePetriNet.getPlaces().size());
        checkPlaces("top.P0", "top.a.P0", "top.a.P1", "top.a.P2", "top.a.P3");
        OutboundArc exArcIn = executablePetriNet.getComponent("top.T1 TO top.a.P2", OutboundArc.class);
        InboundArc exArcOut = executablePetriNet.getComponent("top.a.P1 TO top.T1", InboundArc.class);
        originP2.setId("top.a.P2");
        originP1.setId("top.a.P1");
        assertTrue(originP2.equalsPosition(exArcIn.getTarget()) && originP2.equalsStructure(exArcIn.getTarget()));
        assertTrue(originP1.equalsPosition(exArcOut.getSource()) && originP1.equalsStructure(exArcOut.getSource()));
        expectInterfacePlaceArcNotFound("top.T1 TO a.P2", OutboundArc.class);
        expectInterfacePlaceArcNotFound("a.P1 TO top.T1", InboundArc.class);
    }

    protected void checkNewHomePlace(String awayId)
            throws PetriNetComponentNotFoundException {
        String newId = "top." + awayId;
        Place exPlace = executablePetriNet.getComponent(newId, Place.class);
        assertTrue(exPlace.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusHome);
        assertEquals(exPlace, exPlace.getStatus().getMergeInterfaceStatus().getHomePlace());
        assertEquals(awayId, exPlace.getStatus().getMergeInterfaceStatus().getAwayId());
        int count = 0;
        for (Place place : executablePetriNet.getPlaces()) {
            if ((place.getStatus() instanceof PlaceStatusInterface) &&
                    (place.getStatus().getMergeInterfaceStatus().getAwayId().equals(awayId))) {
                count++;
            }
        }
        assertEquals(1, count);
    }

    //TODO functionalExpressionsOnAwayInterfacePlacesAreConvertedToReferenceHomePlace
    //TODO break this into multiple tests
    private void checkPlaces(String... places) {
        for (int i = 0; i < places.length; i++) {
            try {
                assertEquals(places[i], executablePetriNet.getComponent(places[i], Place.class).getId());
            } catch (PetriNetComponentNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void expectInterfacePlaceArcNotFound(String id, Class clazz) {
        try {
            executablePetriNet.getComponent(id, clazz);
            fail("should throw");
        } catch (PetriNetComponentNotFoundException e) {
        }
    }

    protected PetriNet buildTestNet() throws PetriNetComponentException {
        PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0"))
                .and(APlace.withId("P1")).and(AnImmediateTransition.withId("T0"))
                .and(AnImmediateTransition.withId("T1")).and(ANormalArc.withSource("P1").andTarget("T1"))
                .andFinally(ANormalArc.withSource("T0").andTarget("P0").with("#(P0)", "Default").token());
        return net;
    }
}
