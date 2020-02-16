package uk.ac.imperial.pipe.models.petrinet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.ATestArc;
import uk.ac.imperial.pipe.dsl.ATimedTransition;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.dsl.AnInhibitorArc;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.runner.TimedPetriNetRunner;
import uk.ac.imperial.state.HashedStateBuilder;
import uk.ac.imperial.state.State;
import utils.AbstractTestLog4J2;

@RunWith(MockitoJUnitRunner.class)
public class PetriNetAnimationLogicTest extends AbstractTestLog4J2 {

    private ExecutablePetriNet executablePetriNet;
    private PetriNetAnimationLogic animationLogic;
    private Map<State, Collection<Transition>> successors;
    private State successor;
    private PetriNetAnimator animator;
    private State state;

    @Before
    public void setUp() throws Exception {
        setUpLog4J2(PetriNetAnimationLogic.class, Level.ERROR, true);
        //    	setUpLog4J2(PetriNetAnimationLogic.class, Level.DEBUG, true);
        //    	setUpLog4J2ForRoot(Level.DEBUG);
    }

    @Test
    public void infiniteServerSemantics() throws PetriNetComponentException {
        PetriNet petriNet = buildPetriNet();
        executablePetriNet = petriNet.getExecutablePetriNet();
        animationLogic = new PetriNetAnimationLogic(executablePetriNet);
        state = executablePetriNet.getState();
        successor = buildSuccessorsAndCheckSize(1, state);
        checkCountForPlace(1, "P0");
        checkCountForPlace(1, "P1");

    }

    //@Test
    //TODO uncomment test; fix or delete
    public void testTimedPNRunner() throws PetriNetComponentException, InterruptedException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P0").and(3, "Default").tokens()).and(APlace.withId("P1").and(0, "Default").tokens())
                .and(ATimedTransition.withId("T0").andDelay(1000))
                .and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token())
                .andFinally(ANormalArc.withSource("T0").andTarget("P1").and("1", "Default").token());
        executablePetriNet = petriNet.getExecutablePetriNet();
        executablePetriNet.getTimingQueue().resetTimeAndRebuildTimedTransitions(0);
        animator = new PetriNetAnimator(executablePetriNet);
        animationLogic = new PetriNetAnimationLogic(executablePetriNet);
        TimingQueue timingQueue = executablePetriNet.getTimingQueue();
        TimedPetriNetRunner runner = new TimedPetriNetRunner(petriNet);
        runner.run();
        runner.getTimedRunnerThread().join();
        //System.out.println("DONE " + executablePetriNet.getTimedState() );
    }

    @Test
    public void calculatesSimpleSuccessorStates() throws PetriNetComponentException {
        PetriNet petriNet = createSimplePetriNet(1);
        state = buildExecutablePetriNetAndAnimationAndState(petriNet);
        successor = buildSuccessorsAndCheckSize(1, state);
        checkCountForPlace(0, "P1");
        checkCountForPlace(1, "P2");

    }

    protected State buildExecutablePetriNetAndAnimationAndState(PetriNet petriNet) {
        executablePetriNet = petriNet.getExecutablePetriNet();
        animator = new PetriNetAnimator(executablePetriNet);
        animationLogic = new PetriNetAnimationLogic(executablePetriNet);
        state = executablePetriNet.getState();
        return state;
    }

    @Test
    public void calculatesSelfLoop() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P0").and(1, "Default").token()).and(AnImmediateTransition.withId("T0"))
                .and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token())
                .andFinally(ANormalArc.withSource("T0").andTarget("P0").with("1", "Default").token());

        state = buildExecutablePetriNetAndAnimationAndState(petriNet);
        successor = buildSuccessorsAndCheckSize(1, state);
        checkCountForPlace(1, "P0");
    }

    @Test
    public void arcEnablesTransitionInboundNormalArc() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P0").and(2, "Default").tokens()).and(APlace.withId("P1").and(1, "Default").tokens())
                .and(APlace.withId("P2").and(0, "Default").tokens())
                .and(AnImmediateTransition.withId("T0").andIsAnInfinite())
                .and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token())
                .and(ANormalArc.withSource("P1").andTarget("T0").with("1", "Default").token())
                .andFinally(ANormalArc.withSource("T0").andTarget("P2").and("1", "Default").token());

        state = buildExecutablePetriNetAndAnimationAndState(petriNet);
        successor = buildSuccessorsAndCheckSize(1, state);
        checkCountForPlace(1, "P0");
        checkCountForPlace(0, "P1");
        checkCountForPlace(1, "P2");
        buildSuccessorsAndCheckSize(0, successor);
    }

    @Test
    public void inhibitoryArcDisablesTransitionUntilInboundInhibitoryPlaceEmptiedThenTransitionFiresIndefinitely()
            throws PetriNetComponentException {
        // T0 has no normal inbound arcs so fires indefinitely once not inhibited
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P1").and(1, "Default").tokens()).and(APlace.withId("P2").and(0, "Default").tokens())
                .and(AnImmediateTransition.withId("T0").andIsAnInfinite())
                .and(AnInhibitorArc.withSource("P1").andTarget("T0"))
                .andFinally(ANormalArc.withSource("T0").andTarget("P2").and("1", "Default").token());

        state = buildExecutablePetriNetAndAnimationAndState(petriNet);
        buildSuccessorsAndCheckSize(0, state);

        // Check that transition fires when not inhibited
        Place inhPlace = petriNet.getComponent("P1", Place.class);
        inhPlace.setTokenCount("Default", 0);
        state = executablePetriNet.getState();
        buildSuccessorsAndCheckSize(1, state);
        checkCountForPlace(0, "P1");
        checkCountForPlace(1, "P2");
        successors = animationLogic.getSuccessors(successor);
        assertEquals(1, successors.size());
    }

    @Test
    public void inhibitoryArcDisablesTransitionUntilInboundInhibitoryPlaceEmptiedThenTransitionFiresTwice()
            throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P0").and(2, "Default").tokens()).and(APlace.withId("P1").and(1, "Default").tokens())
                .and(APlace.withId("P2").and(0, "Default").tokens())
                .and(AnImmediateTransition.withId("T0").andIsAnInfinite())
                .and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token())
                .and(AnInhibitorArc.withSource("P1").andTarget("T0"))
                .andFinally(ANormalArc.withSource("T0").andTarget("P2").and("1", "Default").token());

        state = buildExecutablePetriNetAndAnimationAndState(petriNet);
        buildSuccessorsAndCheckSize(0, state);
        Place inhPlace = petriNet.getComponent("P1", Place.class);
        inhPlace.setTokenCount("Default", 0);
        state = executablePetriNet.getState();
        successor = buildSuccessorsAndCheckSize(1, state);
        checkCountForPlace(1, "P0");
        checkCountForPlace(0, "P1");
        checkCountForPlace(1, "P2");
        successor = buildSuccessorsAndCheckSize(1, successor);
        checkCountForPlace(0, "P0");
        checkCountForPlace(0, "P1");
        checkCountForPlace(2, "P2");
        buildSuccessorsAndCheckSize(0, successor);
    }

    protected State buildSuccessorsAndCheckSize(int size, State state) {
        successors = animationLogic.getSuccessors(state, false);
        assertEquals(size, successors.size());
        if (size > 0) {
            successor = successors.keySet().iterator().next();
        }
        return successor;
    }

    @Test
    public void arcEnablesTransitionInboundTestArc() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P1").and(0, "Default").tokens()).and(APlace.withId("P2").and(0, "Default").tokens())
                .and(AnImmediateTransition.withId("T0").andIsAnInfinite())
                .and(ATestArc.withSource("P1").andTarget("T0"))
                .andFinally(ANormalArc.withSource("T0").andTarget("P2").and("1", "Default").token());

        state = buildExecutablePetriNetAndAnimationAndState(petriNet);
        buildSuccessorsAndCheckSize(0, state);
        // Check that transition fires when enabling arc is active
        Place enablePlace = petriNet.getComponent("P1", Place.class);
        enablePlace.setTokenCount("Default", 1);
        state = executablePetriNet.getState();
        successor = buildSuccessorsAndCheckSize(1, state);
        checkCountForPlace(1, "P1");
        checkCountForPlace(1, "P2");
        buildSuccessorsAndCheckSize(1, successor);
    }

    @Test
    public void testArcEnablesTransitionToFireWhileOtherInboundPlacesArePopulated() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P0").and(2, "Default").tokens()).and(APlace.withId("P1").and(0, "Default").tokens())
                .and(APlace.withId("P2").and(0, "Default").tokens())
                .and(AnImmediateTransition.withId("T0").andIsAnInfinite())
                .and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token())
                .and(ATestArc.withSource("P1").andTarget("T0"))
                .andFinally(ANormalArc.withSource("T0").andTarget("P2").and("1", "Default").token());

        state = buildExecutablePetriNetAndAnimationAndState(petriNet);
        buildSuccessorsAndCheckSize(0, state);

        Place enablePlace = petriNet.getComponent("P1", Place.class);
        enablePlace.setTokenCount("Default", 1);
        state = executablePetriNet.getState();
        successor = buildSuccessorsAndCheckSize(1, state);
        checkCountForPlace(1, "P0");
        checkCountForPlace(1, "P1");
        checkCountForPlace(1, "P2");
        successor = buildSuccessorsAndCheckSize(1, successor);
        checkCountForPlace(0, "P0");
        checkCountForPlace(1, "P1");
        checkCountForPlace(2, "P2");
        buildSuccessorsAndCheckSize(0, successor);
    }

    @Test
    public void timedTransitionExecutesFollowingDelay() throws PetriNetComponentException {
        buildTimedPetriNet(1000, 40000);
        successor = buildSuccessorsAndCheckSize(0, state);
        animator.advanceNetToTime(41000);
        successor = buildSuccessorsAndCheckSize(1, state);
        checkCountForPlace(2, "P0");
        checkCountForPlace(1, "P1");
        animator.advanceNetToTime(42000);
        successor = buildSuccessorsAndCheckSize(1, successor);
        checkCountForPlace(1, "P0");
        checkCountForPlace(2, "P1");
    }

    @Test
    public void timedTransitionWithZeroDelayExecutesImmediately() throws PetriNetComponentException {
        buildTimedPetriNet(0, 40000);
        successor = buildSuccessorsAndCheckSize(1, state);
        checkCountForPlace(2, "P0");
        checkCountForPlace(1, "P1");
    }

    protected void checkCountForPlace(Integer count, String place) {
        assertEquals(count, successor.getTokens(place).get("Default"));
    }

    @Test
    public void timedTransitionsWithConflict() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P0").and(1, "Default").tokens()).and(APlace.withId("P1").and(0, "Default").tokens())
                .and(ATimedTransition.withId("T0").andDelay(500))
                .and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token())
                .and(ANormalArc.withSource("T0").andTarget("P1").and("1", "Default").token())
                .and(APlace.withId("P2").and(0, "Default").tokens()).and(ATimedTransition.withId("T1").andDelay(1000))
                .and(ANormalArc.withSource("P0").andTarget("T1").with("1", "Default").token())
                .andFinally(ANormalArc.withSource("T1").andTarget("P2").and("1", "Default").token());
        state = buildExecutablePetriNetAndAnimationAndState(petriNet);
        executablePetriNet = petriNet.getExecutablePetriNet();
        executablePetriNet.getTimingQueue().resetTimeAndRebuildTimedTransitions(40000);
        executablePetriNet.getTimingQueue().rebuild(state);
        buildSuccessorsAndCheckSize(0, state);
        animator.advanceNetToTime(40500);
        successor = buildSuccessorsAndCheckSize(1, state);
        checkCountForPlace(0, "P0");
        checkCountForPlace(1, "P1");
        checkCountForPlace(0, "P2");
        animator.advanceNetToTime(41000);
        successor = buildSuccessorsAndCheckSize(0, successor);
        checkCountForPlace(0, "P0");
        checkCountForPlace(1, "P1");
        checkCountForPlace(0, "P2");
    }

    protected PetriNet buildPetriNet() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P0").and(2, "Default").tokens()).and(APlace.withId("P1").and(0, "Default").tokens())
                .and(AnImmediateTransition.withId("T0").andIsAnInfinite())
                .and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token())
                .andFinally(ANormalArc.withSource("T0").andTarget("P1").and("1", "Default").token());
        return petriNet;
    }

    protected PetriNet buildTimedPetriNet(int delay, long initTime) throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P0").and(3, "Default").tokens()).and(APlace.withId("P1").and(0, "Default").tokens())
                .and(ATimedTransition.withId("T0").andDelay(delay))
                .and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token())
                .andFinally(ANormalArc.withSource("T0").andTarget("P1").and("1", "Default").token());
        state = buildExecutablePetriNetAndAnimationAndState(petriNet);
        executablePetriNet.getTimingQueue().resetTimeAndRebuildTimedTransitions(initTime);
        executablePetriNet.getTimingQueue().rebuild(state);
        return petriNet;
    }

    @Test
    public void timerForSecondTimedTransitionOnlyStartsWhenTransitionIsEnabled() throws PetriNetComponentException {
        buildTimedPetriNetTwoTimedTransitions(500, 40000);
        successor = buildSuccessorsAndCheckSize(0, state);
        animator.advanceNetToTime(41000);
        successor = buildSuccessorsAndCheckSize(1, state);
        checkCountForPlace(0, "P0");
        checkCountForPlace(1, "P1");
        checkCountForPlace(0, "P2");
        successor = buildSuccessorsAndCheckSize(0, successor);
        animator.advanceNetToTime(41500);
        successor = buildSuccessorsAndCheckSize(1, successor);
        checkCountForPlace(0, "P0");
        checkCountForPlace(0, "P1");
        checkCountForPlace(1, "P2");
    }

    @Test
    public void secondTimedTransitionWithZeroDelayFiresWhenTransitionIsEnabled() throws PetriNetComponentException {
        buildTimedPetriNetTwoTimedTransitions(0, 40000);
        buildSuccessorsAndCheckSize(0, state);
        animator.advanceNetToTime(41000);
        successor = buildSuccessorsAndCheckSize(1, state);
        checkCountForPlace(0, "P0");
        checkCountForPlace(1, "P1");
        checkCountForPlace(0, "P2");
        successor = buildSuccessorsAndCheckSize(1, successor);
        checkCountForPlace(0, "P0");
        checkCountForPlace(0, "P1");
        checkCountForPlace(1, "P2");
        buildSuccessorsAndCheckSize(0, successor);
    }

    protected PetriNet buildTimedPetriNetTwoTimedTransitions(int t1delay, long initTime)
            throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P0").and(1, "Default").tokens()).and(APlace.withId("P1").and(0, "Default").tokens())
                .and(APlace.withId("P2").and(0, "Default").tokens()).and(ATimedTransition.withId("T0").andDelay(1000))
                .and(ATimedTransition.withId("T1").andDelay(t1delay))
                .and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token())
                .and(ANormalArc.withSource("T0").andTarget("P1").with("1", "Default").token())
                .and(ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token())
                .andFinally(ANormalArc.withSource("T1").andTarget("P2").and("1", "Default").token());

        state = buildExecutablePetriNetAndAnimationAndState(petriNet);
        executablePetriNet = petriNet.getExecutablePetriNet();
        executablePetriNet.getTimingQueue().resetTimeAndRebuildTimedTransitions(40000);
        executablePetriNet.getTimingQueue().rebuild(state);
        return petriNet;
    }

    @Test
    public void complexColorNetFiresBothColorsReachingExpectedStates() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(AToken.called("Red").withColor(Color.RED))
                .and(APlace.withId("P0").containing(1, "Default").token().and(1, "Red").token())
                .and(APlace.withId("P1")).and(AnImmediateTransition.withId("T0"))
                .and(AnImmediateTransition.withId("T1")).and(AnImmediateTransition.withId("T2"))
                .and(AnImmediateTransition.withId("T3"))
                .and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token().and("0", "Red").token())
                .and(ANormalArc.withSource("P0").andTarget("T1").with("1", "Red").token().and("0", "Default").token())
                .and(ANormalArc.withSource("P1").andTarget("T2").with("1", "Default").token().and("0", "Red").token())
                .and(ANormalArc.withSource("P1").andTarget("T3").with("1", "Red").token().and("0", "Default").token())
                .and(ANormalArc.withSource("T0").andTarget("P1").with("1", "Default").token().and("0", "Red").token())
                .and(ANormalArc.withSource("T1").andTarget("P1").with("1", "Red").token().and("0", "Default").token())
                .and(ANormalArc.withSource("T2").andTarget("P0").with("1", "Default").token().and("0", "Red").token())
                .andFinally(ANormalArc.withSource("T3").andTarget("P0").with("1", "Red").token().and("0", "Default")
                        .token());
        //                and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token()).
        //                and(ANormalArc.withSource("P0").andTarget("T1").with("1", "Red").token()).
        //                and(ANormalArc.withSource("P1").andTarget("T2").with("1", "Default").token()).
        //                and(ANormalArc.withSource("P1").andTarget("T3").with("1", "Red").token()).
        //                and(ANormalArc.withSource("T0").andTarget("P1").with("1", "Default").token()).
        //                and(ANormalArc.withSource("T1").andTarget("P1").with("1", "Red").token()).
        //                and(ANormalArc.withSource("T2").andTarget("P0").with("1", "Default").token()).
        //                andFinally(ANormalArc.withSource("T3").andTarget("P0").with("1", "Red").token());
        State originalState = buildExecutablePetriNetAndAnimationAndState(petriNet);
        successors = animationLogic.getSuccessors(originalState);
        State successor0 = new SB().add("P1", "Default", 1, "Red", 0).add("P0", "Default", 0, "Red", 1).getState();
        State successor1 = new SB().add("P1", "Default", 0, "Red", 1).add("P0", "Default", 1, "Red", 0).getState();
        assertTrue(successors.get(successor0).contains(getT("T0")));
        assertTrue(successors.get(successor1).contains(getT("T1")));

        // test the successors of each of the two states
        successors = animationLogic.getSuccessors(successor0);
        State successor01 = new SB().add("P1", "Default", 1, "Red", 1).add("P0", "Default", 0, "Red", 0).getState();
        assertTrue(successors.get(successor01).contains(getT("T1")));
        assertTrue(successors.get(originalState).contains(getT("T2")));
        successors = animationLogic.getSuccessors(successor1);
        assertTrue(successors.get(successor01).contains(getT("T0")));
        assertTrue(successors.get(originalState).contains(getT("T3")));
        // test the successors of P1 with both tokens
        successors = animationLogic.getSuccessors(successor01);
        assertTrue(successors.get(successor0).contains(getT("T3")));
        assertTrue(successors.get(successor1).contains(getT("T2")));
        //  Transition / State combinations
        //      t0 0
        //      T0 01
        //      t1 1
        //      T1 01
        //      T2 OS
        //      T2 1
        //      T3 0
        //      T3 OS
    }

    private Transition getT(String transition) throws PetriNetComponentNotFoundException {
        return executablePetriNet.getComponent(transition, Transition.class);
    }

    @Test
    public void multiColorArcsCanFire() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(AToken.called("Red").withColor(Color.RED))
                .and(APlace.withId("P0").containing(1, "Default").token().and(1, "Red").token())
                .and(APlace.withId("P1")).and(AnImmediateTransition.withId("T0"))
                .and(AnImmediateTransition.withId("T1"))
                .and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token())
                .and(ANormalArc.withSource("P0").andTarget("T1").with("1", "Red").token())
                .and(ANormalArc.withSource("T0").andTarget("P1").with("1", "Default").token())
                .andFinally(ANormalArc.withSource("T1").andTarget("P1").with("1", "Red").token());

        executablePetriNet = petriNet.getExecutablePetriNet();
        Transition t0 = executablePetriNet.getComponent("T0", Transition.class);
        Transition t1 = executablePetriNet.getComponent("T1", Transition.class);

        Collection<Transition> transitions = getEnabledImmediateOrTimedTransitionsFromAnimationLogic();
        assertEquals("Both transitions were not enabled", 2, transitions.size());
        assertThat(transitions).contains(t0, t1);
    }

    protected Collection<Transition> getEnabledImmediateOrTimedTransitionsFromAnimationLogic() {
        animationLogic = new PetriNetAnimationLogic(executablePetriNet);
        Collection<Transition> transitions = animationLogic
                .getEnabledImmediateOrTimedTransitions(executablePetriNet.getState());
        return transitions;
    }

    @Test
    public void multiColorArcsCanFireWithZeroWeighting() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(AToken.called("Red").withColor(Color.RED))
                .and(APlace.withId("P0").containing(1, "Default").token().and(1, "Red").token())
                .and(APlace.withId("P1")).and(AnImmediateTransition.withId("T0"))
                .and(AnImmediateTransition.withId("T1"))
                .and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token().and("0", "Red").tokens())
                .and(ANormalArc.withSource("P0").andTarget("T1").with("0", "Default").tokens().and("1", "Red").token())
                .and(ANormalArc.withSource("T0").andTarget("P1").with("1", "Default").token().and("0", "Red").tokens())
                .andFinally(ANormalArc.withSource("T1").andTarget("P1").with("0", "Default").tokens().and("1", "Red")
                        .token());

        executablePetriNet = petriNet.getExecutablePetriNet();
        Transition t0 = executablePetriNet.getComponent("T0", Transition.class);
        Transition t1 = executablePetriNet.getComponent("T1", Transition.class);

        Collection<Transition> transitions = getEnabledImmediateOrTimedTransitionsFromAnimationLogic();
        assertEquals("Both transitions were not enabled", 2, transitions.size());
        assertThat(transitions).contains(t0, t1);
    }

    @Test
    public void throwsIfArcExpressionCantBeEvaluatedEvenIfPlaceIsPopulated() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P0").and(1, "Default").tokens()).and(AnImmediateTransition.withId("T0"))
                .andFinally(ANormalArc.withSource("P0").andTarget("T0").with("#(nonexistentPlace)", "Default").token());
        try {
            executablePetriNet = petriNet.getExecutablePetriNet();
            //            State state = executablePetriNet.getState();
            //            InboundArc arc = petriNet.getComponent("P0 TO T0", InboundArc.class);
            //            arc.canFire(executablePetriNet, state);
            fail("should throw; all logic evaluated when building executable PN");
        } catch (RuntimeException e) {
            assertEquals("Error evaluating arc weight expression #(nonexistentPlace) for arc: P0 TO T0", e
                    .getMessage());
        }
    }

    @Test
    public void arcweightThatEvaluatesToZeroDoesNotEnableTransition() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P0").and(0, "Default").tokens()).and(AnImmediateTransition.withId("T0"))
                .andFinally(ANormalArc.withSource("P0").andTarget("T0").with("#(P0)", "Default").token());
        executablePetriNet = petriNet.getExecutablePetriNet();
        State state = executablePetriNet.getState();
        InboundArc arc = petriNet.getComponent("P0 TO T0", InboundArc.class);
        assertFalse(arc.canFire(executablePetriNet, state));
        Collection<Transition> transitions = getEnabledImmediateOrTimedTransitionsFromAnimationLogic();
        assertEquals(0, transitions.size());
    }

    @Test
    public void arcweightThatHasOneZeroAndOneNonZeroWithNonZeroTokensEnablesTransition()
            throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(AToken.called("Red").withColor(Color.RED))
                .and(APlace.withId("P0").containing(0, "Default").token().and(1, "Red").token())
                .and(APlace.withId("P1")).and(AnImmediateTransition.withId("T0"))
                .and(ANormalArc.withSource("P0").andTarget("T0").with("0", "Default").token().and("1", "Red").tokens())
                .andFinally(ANormalArc.withSource("T0").andTarget("P1").with("0", "Default").tokens().and("1", "Red")
                        .token());
        executablePetriNet = petriNet.getExecutablePetriNet();
        Transition t0 = executablePetriNet.getComponent("T0", Transition.class);
        InboundArc arc = executablePetriNet.inboundArcs(t0).iterator().next();
        assertTrue(arc.canFire(executablePetriNet, executablePetriNet.getState()));
    }

    @Test
    public void correctlyIdentifiesEnabledTransition() throws PetriNetComponentException {
        int tokenWeight = 1;
        PetriNet petriNet = createSimplePetriNet(tokenWeight);
        executablePetriNet = petriNet.getExecutablePetriNet();
        Transition transition = executablePetriNet.getComponent("T1", Transition.class);
        Collection<Transition> transitions = getEnabledImmediateOrTimedTransitionsFromAnimationLogic();
        assertTrue("Petri net did not put transition in enabled collection", transitions.contains(transition));
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
        return APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P1").containing(1, "Default").token()).and(APlace.withId("P2"))
                .and(AnImmediateTransition.withId("T1"))
                .and(ANormalArc.withSource("P1").andTarget("T1").with(arcWeight, "Default").tokens())
                .andFinally(ANormalArc.withSource("T1").andTarget("P2").with(arcWeight, "Default").tokens());
    }

    @Test
    public void correctlyIdentifiesEnabledWithNoSecondColourToken() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(AToken.called("Red").withColor(Color.RED))
                .and(APlace.withId("P1").containing(1, "Red").token().and(1, "Default").token())
                .and(APlace.withId("P2")).and(AnImmediateTransition.withId("T1")).andFinally(ANormalArc.withSource("P1")
                        .andTarget("T1").with("1", "Default").token().and("0", "Red").tokens());
        executablePetriNet = petriNet.getExecutablePetriNet();
        Transition transition = executablePetriNet.getComponent("T1", Transition.class);
        Collection<Transition> enabled = getEnabledImmediateOrTimedTransitionsFromAnimationLogic();
        assertTrue("Petri net did not put transition in enabled collection", enabled.contains(transition));
    }

    @Test
    public void correctlyIdentifiesNotEnabledTransitionDueToEmptyPlace() throws PetriNetComponentException {
        int tokenWeight = 4;
        PetriNet petriNet = createSimplePetriNet(tokenWeight);
        executablePetriNet = petriNet.getExecutablePetriNet();
        Transition transition = executablePetriNet.getComponent("T1", Transition.class);
        Place place = executablePetriNet.getComponent("P1", Place.class);
        place.decrementTokenCount("Default");

        Collection<Transition> enabled = getEnabledImmediateOrTimedTransitionsFromAnimationLogic();
        assertThat(enabled).doesNotContain(transition);
    }

    @Test
    public void correctlyIdentifiesNotEnabledTransitionDueToNotEnoughTokens()
            throws PetriNetComponentException {
        int tokenWeight = 4;
        PetriNet petriNet = createSimplePetriNet(tokenWeight);
        executablePetriNet = petriNet.getExecutablePetriNet();
        Transition transition = executablePetriNet.getComponent("T1", Transition.class);
        Collection<Transition> enabled = getEnabledImmediateOrTimedTransitionsFromAnimationLogic();
        assertThat(enabled).doesNotContain(transition);
    }

    @Test
    public void correctlyIdentifiesNotEnabledTransitionDueToOnePlaceNotEnoughTokens()
            throws PetriNetComponentException {
        int tokenWeight = 1;
        PetriNet petriNet = createSimplePetriNetTwoPlacesToTransition(tokenWeight);
        executablePetriNet = petriNet.getExecutablePetriNet();
        Transition transition = executablePetriNet.getComponent("T1", Transition.class);
        Collection<Transition> enabled = getEnabledImmediateOrTimedTransitionsFromAnimationLogic();
        assertFalse("Petri net put transition in enabled collection", enabled.contains(transition));
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
    public void correctlyIdentifiesNotEnabledTransitionDueToArcNeedingTwoDifferentTokens()
            throws PetriNetComponentException {
        int tokenWeight = 1;
        PetriNet petriNet = createSimplePetriNet(tokenWeight);

        Token redToken = new ColoredToken("red", new Color(255, 0, 0));
        petriNet.addToken(redToken);

        InboundArc arc = petriNet.getComponent("P1 TO T1", InboundArc.class);
        arc.getTokenWeights().put(redToken.getId(), "1");
        executablePetriNet = petriNet.getExecutablePetriNet();
        Transition transition = executablePetriNet.getComponent("T1", Transition.class);
        Collection<Transition> enabled = getEnabledImmediateOrTimedTransitionsFromAnimationLogic();
        assertThat(enabled).doesNotContain(transition);
    }

    @Test
    public void correctlyIdentifiesEnabledTransitionRequiringTwoTokens() throws PetriNetComponentException {
        int tokenWeight = 1;
        PetriNet petriNet = createSimplePetriNet(tokenWeight);

        Token redToken = new ColoredToken("red", new Color(255, 0, 0));
        petriNet.addToken(redToken);
        InboundArc arc = petriNet.getComponent("P1 TO T1", InboundArc.class);
        arc.getTokenWeights().put(redToken.getId(), "1");

        Place place = petriNet.getComponent("P1", Place.class);
        place.incrementTokenCount(redToken.getId());
        executablePetriNet = petriNet.getExecutablePetriNet();
        Transition transition = executablePetriNet.getComponent("T1", Transition.class);
        Collection<Transition> enabled = getEnabledImmediateOrTimedTransitionsFromAnimationLogic();
        assertThat(enabled).contains(transition);
    }

    @Test
    public void onlyEnablesHigherPriorityTransition() throws Exception {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P0").containing(1, "Default").token())
                .and(AnImmediateTransition.withId("T0").andPriority(1))
                .and(AnImmediateTransition.withId("T1").andPriority(10))
                .and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").tokens())
                .andFinally(ANormalArc.withSource("P0").andTarget("T1").with("1", "Default").tokens());
        executablePetriNet = petriNet.getExecutablePetriNet();
        Transition transition = executablePetriNet.getComponent("T1", Transition.class);
        Collection<Transition> enabled = getEnabledImmediateOrTimedTransitionsFromAnimationLogic();
        assertEquals(1, enabled.size());
        assertThat(enabled).containsExactly(transition);
    }

    @Test
    public void correctlyDoesNotEnableTransitionsIfPlaceCapacityIsFull() throws PetriNetComponentException {
        PetriNet petriNet = createSimplePetriNet(2);
        Token token = petriNet.getComponent("Default", Token.class);

        Place p1 = petriNet.getComponent("P1", Place.class);
        p1.setTokenCount(token.getId(), 2);
        Place p2 = petriNet.getComponent("P2", Place.class);
        p2.setCapacity(1);
        executablePetriNet = petriNet.getExecutablePetriNet();
        Transition transition = executablePetriNet.getComponent("T1", Transition.class);
        Collection<Transition> enabled = getEnabledImmediateOrTimedTransitionsFromAnimationLogic();
        assertThat(enabled).doesNotContain(transition);
    }

    @Test
    public void correctlyEnablesTransitionIfSelfLoop() throws PetriNetComponentException {
        PetriNet petriNet = createSelfLoopPetriNet("1");
        Place place = petriNet.getComponent("P0", Place.class);
        Token token = petriNet.getComponent("Default", Token.class);
        place.setTokenCount(token.getId(), 1);
        place.setCapacity(1);
        executablePetriNet = petriNet.getExecutablePetriNet();
        Transition transition = executablePetriNet.getComponent("T1", Transition.class);
        Collection<Transition> enabled = getEnabledImmediateOrTimedTransitionsFromAnimationLogic();
        assertThat(enabled).containsExactly(transition);
    }

    @Test
    public void transitionWithNoInputsIsDisabled() throws PetriNetComponentException {
        PetriNet net = new PetriNet();
        net.addTransition(new DiscreteTransition("T0"));
        executablePetriNet = net.getExecutablePetriNet();
        Collection<Transition> enabled = getEnabledImmediateOrTimedTransitionsFromAnimationLogic();
        assertEquals(0, enabled.size());
    }

    private PetriNet createSelfLoopPetriNet(String tokenWeight) throws PetriNetComponentException {
        return APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0"))
                .and(AnImmediateTransition.withId("T1"))
                .and(ANormalArc.withSource("T1").andTarget("P0").with(tokenWeight, "Default").tokens())
                .andFinally(ANormalArc.withSource("P0").andTarget("T1").with(tokenWeight, "Default").tokens());
    }

    @Test
    public void correctlyMarksInhibitorArcEnabledTransition() throws PetriNetComponentException {
        PetriNet petriNet = createSimpleInhibitorPetriNet(1);
        executablePetriNet = petriNet.getExecutablePetriNet();
        Transition transition = executablePetriNet.getComponent("T1", Transition.class);
        Collection<Transition> enabled = getEnabledImmediateOrTimedTransitionsFromAnimationLogic();
        assertThat(enabled).contains(transition);
    }

    /**
     * Create simple Petri net with P1 -o T1 -> P2
     * Initialises a token in P1 and gives arcs A1 and A2 a weight of tokenWeight to a default token
     *
     * @param tokenWeight
     * @return simple Petri net with P1 -o T1 -> P2
     * @throws PetriNetComponentException
     */
    public PetriNet createSimpleInhibitorPetriNet(int tokenWeight) throws PetriNetComponentException {
        return APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P1"))
                .and(APlace.withId("P2")).and(AnImmediateTransition.withId("T1"))
                .and(AnInhibitorArc.withSource("P1").andTarget("T1")).andFinally(ANormalArc.withSource("T1")
                        .andTarget("P2").with(Integer.toString(tokenWeight), "Default").tokens());
    }

    /**
     * If a state contains Integer.MAX_VALUE then this is considered to be infinite
     * so infinity addition and subtraction rules should apply
     * @throws PetriNetComponentException
     */
    @Test
    public void infinityLogic() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(AnImmediateTransition.withId("T0"))
                .and(APlace.withId("P0").and(Integer.MAX_VALUE, "Default").token())
                .andFinally(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").tokens());

        state = buildExecutablePetriNetAndAnimationAndState(petriNet);
        buildSuccessorsAndCheckSize(1, state);
        checkCountForPlace(Integer.MAX_VALUE, "P0");
    }

    @Test
    public void clearsWhenExecutablePetriNetRefreshes() throws Exception {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(AnImmediateTransition.withId("T0"))
                .andFinally(APlace.withId("P0").and(Integer.MAX_VALUE, "Default").token());
        executablePetriNet = petriNet.getExecutablePetriNet();
        State state = executablePetriNet.getState();
        animationLogic = new PetriNetAnimationLogic(executablePetriNet);
        Set<Transition> transitions = new HashSet<>();
        transitions.add(petriNet.getComponent("T0", Transition.class));
        animationLogic.cachedEnabledImmediateTransitions.put(state, transitions);
        assertEquals(1, animationLogic.cachedEnabledImmediateTransitions.size());
        executablePetriNet.refreshRequired();
        executablePetriNet.refresh();
        assertEquals("cache should be cleared", 0, animationLogic.cachedEnabledImmediateTransitions.size());

    }

    // concise state builder
    private class SB {
        private HashedStateBuilder builder;

        public SB() {
            builder = new HashedStateBuilder();
        }

        public SB add(String place, String token1, int count1) {
            builder.placeWithToken(place, token1, count1);
            return this;
        }

        public SB add(String place, String token1, int count1, String token2, int count2) {
            HashMap<String, Integer> tokens = new HashMap<>();
            tokens.put(token1, count1);
            tokens.put(token2, count2);
            builder.placeWithTokens(place, tokens);
            return this;
        }

        public State getState() {
            return builder.build();
        }
    }
}
