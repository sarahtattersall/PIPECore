package uk.ac.imperial.pipe.animation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import uk.ac.imperial.pipe.dsl.*;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.InboundArc;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.ColoredToken;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.DiscreteTransition;
import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.state.State;

import java.awt.Color;
import java.util.Collection;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class PetriNetAnimationLogicTest {


    @Test
    public void infiniteServerSemantics() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
                APlace.withId("P0").and(2, "Default").tokens()).and(APlace.withId("P1").and(0, "Default").tokens()).and(
                AnImmediateTransition.withId("T0").andIsAnInfinite()).and(
                ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token()).andFinally(
                ANormalArc.withSource("T0").andTarget("P1").and("1", "Default").token());

        PetriNetAnimationLogic animator = new PetriNetAnimationLogic(petriNet);

        State state = AnimationUtils.getState(petriNet);
        Map<State, Collection<Transition>> successors = animator.getSuccessors(state);

        assertEquals(1, successors.size());
        State successor = successors.keySet().iterator().next();

        int actualP1 = successor.getTokens("P0").get("Default");
        assertEquals(1, actualP1);

        int actualP2 = successor.getTokens("P1").get("Default");
        assertEquals(1, actualP2);
    }

    @Test
    public void multiColorArcsCanFire() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
                AToken.called("Red").withColor(Color.RED)).and(
                APlace.withId("P0").containing(1, "Default").token().and(1, "Red").token()).and(
                APlace.withId("P1")).and(AnImmediateTransition.withId("T0")).and(AnImmediateTransition.withId("T1")).and(
                ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token()).and(
                ANormalArc.withSource("P0").andTarget("T1").with("1", "Red").token()).and(
                ANormalArc.withSource("T0").andTarget("P1").with("1", "Default").token()).andFinally(
                ANormalArc.withSource("T1").andTarget("P1").with("1", "Red").token());

        Transition t0 = petriNet.getComponent("T0", Transition.class);
        Transition t1 = petriNet.getComponent("T1", Transition.class);

        AnimationLogic animator = new PetriNetAnimationLogic(petriNet);
        Collection<Transition> transitions = animator.getEnabledTransitions(AnimationUtils.getState(petriNet));
        assertEquals("Both transitions were not enabled", 2, transitions.size());
        assertThat(transitions).contains(t0, t1);
    }


    @Test
    public void multiColorArcsCanFireWithZeroWeighting() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
                AToken.called("Red").withColor(Color.RED)).and(
                APlace.withId("P0").containing(1, "Default").token().and(1, "Red").token()).and(
                APlace.withId("P1")).and(AnImmediateTransition.withId("T0")).and(AnImmediateTransition.withId("T1")).and(
                ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token().and("0", "Red").tokens()).and(
                ANormalArc.withSource("P0").andTarget("T1").with("0", "Default").tokens().and("1", "Red").token()).and(
                ANormalArc.withSource("T0").andTarget("P1").with("1", "Default").token().and("0", "Red").tokens()
        ).andFinally(ANormalArc.withSource("T1").andTarget("P1").with("0", "Default").tokens().and("1", "Red").token());

        Transition t0 = petriNet.getComponent("T0", Transition.class);
        Transition t1 = petriNet.getComponent("T1", Transition.class);

        AnimationLogic animator = new PetriNetAnimationLogic(petriNet);
        Collection<Transition> transitions = animator.getEnabledTransitions(AnimationUtils.getState(petriNet));
        assertEquals("Both transitions were not enabled", 2, transitions.size());
        assertThat(transitions).contains(t0, t1);
    }
    @Test
    public void arcweightThatEvaluatesToZeroDoesNotEnableTransition() throws PetriNetComponentException {
    	PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
    			APlace.withId("P0").and(0, "Default").tokens()).and(
    			AnImmediateTransition.withId("T0")).andFinally(
    			ANormalArc.withSource("P0").andTarget("T0").with("#(P0)", "Default").token());
        State state = AnimationUtils.getState(petriNet);
        InboundArc arc = petriNet.getComponent("P0 TO T0", InboundArc.class);
        assertFalse(arc.canFire(petriNet, state)); 
        AnimationLogic animator = new PetriNetAnimationLogic(petriNet);
        Collection<Transition> transitions = animator.getEnabledTransitions(state);
        assertEquals(0, transitions.size());
    }

    @Test
    public void correctlyIdentifiesEnabledTransition() throws PetriNetComponentException {
        int tokenWeight = 1;
        PetriNet petriNet = createSimplePetriNet(tokenWeight);
        Transition transition = petriNet.getComponent("T1", Transition.class);
        AnimationLogic animator = new PetriNetAnimationLogic(petriNet);
        Collection<Transition> enabled = animator.getEnabledTransitions(AnimationUtils.getState(petriNet));
        assertTrue("Petri net did not put transition in enabled collection", enabled.contains(transition));
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
    public void correctlyIdentifiesEnabledWithNoSecondColourToken() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
                AToken.called("Red").withColor(Color.RED)).and(
                APlace.withId("P1").containing(1, "Red").token().and(1, "Default").token()).and(
                APlace.withId("P2")).and(AnImmediateTransition.withId("T1")).andFinally(
                ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token().and("0", "Red").tokens());

        Transition transition = petriNet.getComponent("T1", Transition.class);

        AnimationLogic animator = new PetriNetAnimationLogic(petriNet);
        Collection<Transition> enabled = animator.getEnabledTransitions(AnimationUtils.getState(petriNet));
        assertTrue("Petri net did not put transition in enabled collection", enabled.contains(transition));
    }

    @Test
    public void correctlyIdentifiesNotEnabledTransitionDueToEmptyPlace() throws PetriNetComponentException {
        int tokenWeight = 4;
        PetriNet petriNet = createSimplePetriNet(tokenWeight);
        Place place = petriNet.getComponent("P1", Place.class);
        Transition transition = petriNet.getComponent("T1", Transition.class);
        place.decrementTokenCount("Default");

        AnimationLogic animator = new PetriNetAnimationLogic(petriNet);
        Collection<Transition> enabled = animator.getEnabledTransitions(AnimationUtils.getState(petriNet));
        assertThat(enabled).doesNotContain(transition);
    }

    @Test
    public void correctlyIdentifiesNotEnabledTransitionDueToNotEnoughTokens()
            throws PetriNetComponentException {
        int tokenWeight = 4;
        PetriNet petriNet = createSimplePetriNet(tokenWeight);
        Transition transition = petriNet.getComponent("T1", Transition.class);

        AnimationLogic animator = new PetriNetAnimationLogic(petriNet);
        Collection<Transition> enabled = animator.getEnabledTransitions(AnimationUtils.getState(petriNet));
        assertThat(enabled).doesNotContain(transition);
    }

    @Test
    public void correctlyIdentifiesNotEnabledTransitionDueToOnePlaceNotEnoughTokens()
            throws PetriNetComponentException {
        int tokenWeight = 1;
        PetriNet petriNet = createSimplePetriNetTwoPlacesToTransition(tokenWeight);
        Transition transition = petriNet.getComponent("T1", Transition.class);

        AnimationLogic animator = new PetriNetAnimationLogic(petriNet);
        Collection<Transition> enabled = animator.getEnabledTransitions(AnimationUtils.getState(petriNet));
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
        return APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P1")).and(
                APlace.withId("P2")).and(AnImmediateTransition.withId("T1")).and(
                ANormalArc.withSource("P1").andTarget("T1").with(weight, "Default").tokens()).andFinally(
                ANormalArc.withSource("P2").andTarget("T1").with(weight, "Default").tokens());
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
        Transition transition = petriNet.getComponent("T1", Transition.class);

        AnimationLogic animator = new PetriNetAnimationLogic(petriNet);
        Collection<Transition> enabled = animator.getEnabledTransitions(AnimationUtils.getState(petriNet));
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
        Transition transition = petriNet.getComponent("T1", Transition.class);
        place.incrementTokenCount(redToken.getId());

        AnimationLogic animator = new PetriNetAnimationLogic(petriNet);
        Collection<Transition> enabled = animator.getEnabledTransitions(AnimationUtils.getState(petriNet));
        assertThat(enabled).contains(transition);
    }

    @Test
    public void onlyEnablesHigherPriorityTransition() {
        PetriNet petriNet = new PetriNet();
        Transition t1 = new DiscreteTransition("1", "1");
        t1.setPriority(10);
        Transition t2 = new DiscreteTransition("2", "2");
        t2.setPriority(1);
        petriNet.addTransition(t1);
        petriNet.addTransition(t2);

        AnimationLogic animator = new PetriNetAnimationLogic(petriNet);
        Collection<Transition> enabled = animator.getEnabledTransitions(AnimationUtils.getState(petriNet));
        assertEquals(1, enabled.size());
        assertThat(enabled).containsExactly(t1);
    }

    @Test
    public void correctlyDoesNotEnableTransitionsIfPlaceCapacityIsFull() throws PetriNetComponentException {
        PetriNet petriNet = createSimplePetriNet(2);
        Token token = petriNet.getComponent("Default", Token.class);

        Place p1 = petriNet.getComponent("P1", Place.class);
        p1.setTokenCount(token.getId(), 2);
        Place p2 = petriNet.getComponent("P2", Place.class);
        p2.setCapacity(1);

        Transition transition = petriNet.getComponent("T1", Transition.class);

        AnimationLogic animator = new PetriNetAnimationLogic(petriNet);
        Collection<Transition> enabled = animator.getEnabledTransitions(AnimationUtils.getState(petriNet));
        assertThat(enabled).doesNotContain(transition);
    }

    @Test
    public void correctlyEnablesTransitionIfSelfLoop() throws PetriNetComponentException {
        PetriNet petriNet = createSelfLoopPetriNet("1");
        Place place = petriNet.getComponent("P0", Place.class);
        Token token = petriNet.getComponent("Default", Token.class);
        place.setTokenCount(token.getId(), 1);
        place.setCapacity(1);

        Transition transition = petriNet.getComponent("T1", Transition.class);

        AnimationLogic animator = new PetriNetAnimationLogic(petriNet);
        Collection<Transition> enabled = animator.getEnabledTransitions(AnimationUtils.getState(petriNet));
        assertThat(enabled).containsExactly(transition);
    }

    private PetriNet createSelfLoopPetriNet(String tokenWeight) throws PetriNetComponentException {
        return APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
                AnImmediateTransition.withId("T1")).and(
                ANormalArc.withSource("T1").andTarget("P0").with(tokenWeight, "Default").tokens()).andFinally(
                ANormalArc.withSource("P0").andTarget("T1").with(tokenWeight, "Default").tokens());
    }

    @Test
    public void correctlyMarksInhibitorArcEnabledTransition() throws PetriNetComponentException {
        PetriNet petriNet = createSimpleInhibitorPetriNet(1);
        Transition transition = petriNet.getComponent("T1", Transition.class);
        AnimationLogic animator = new PetriNetAnimationLogic(petriNet);
        Collection<Transition> enabled = animator.getEnabledTransitions(AnimationUtils.getState(petriNet));
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
        return APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P1")).and(
                APlace.withId("P2")).and(AnImmediateTransition.withId("T1")).and(
                AnInhibitorArc.withSource("P1").andTarget("T1")).andFinally(
                ANormalArc.withSource("T1").andTarget("P2").with(Integer.toString(tokenWeight), "Default").tokens());
    }

    @Test
    public void calculatesSimpleSuccessorStates() throws PetriNetComponentException {
        PetriNet petriNet = createSimplePetriNet(1);
        State state = AnimationUtils.getState(petriNet);
        AnimationLogic animator = new PetriNetAnimationLogic(petriNet);
        Map<State, Collection<Transition>> successors = animator.getSuccessors(state);

        assertEquals(1, successors.size());
        State successor = successors.keySet().iterator().next();

        int actualP1 = successor.getTokens("P1").get("Default");
        assertEquals(0, actualP1);

        int actualP2 = successor.getTokens("P2").get("Default");
        assertEquals(1, actualP2);
    }


    @Test
    public void calculatesSelfLoop() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
                APlace.withId("P0").and(1, "Default").token()).and(AnImmediateTransition.withId("T0")).and(
                ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token()).andFinally(
                ANormalArc.withSource("T0").andTarget("P0").with("1", "Default").token());

        State state = AnimationUtils.getState(petriNet);
        AnimationLogic animator = new PetriNetAnimationLogic(petriNet);
        Map<State, Collection<Transition>> successors = animator.getSuccessors(state);

        assertEquals(1, successors.size());
        State successor = successors.keySet().iterator().next();

        int actualP1 = successor.getTokens("P0").get("Default");
        assertEquals(1, actualP1);
    }

    /**
     * If a state contains Integer.MAX_VALUE then this is considered to be infinite
     * so infinity addition and subtraction rules should apply
     * @throws PetriNetComponentException 
     */
    @Test
    public void infinityLogic() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
                AnImmediateTransition.withId("T0")).andFinally(APlace.withId("P0").and(Integer.MAX_VALUE, "Default").token());


        State state = AnimationUtils.getState(petriNet);
        AnimationLogic animator = new PetriNetAnimationLogic(petriNet);
        Map<State, Collection<Transition>> successors = animator.getSuccessors(state);

        assertEquals(1, successors.size());
        State successor = successors.keySet().iterator().next();

        int actualP1 = successor.getTokens("P0").get("Default");
        assertEquals(Integer.MAX_VALUE, actualP1);
    }
}