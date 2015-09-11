package uk.ac.imperial.pipe.animation;

import org.junit.Test;
import uk.ac.imperial.pipe.dsl.*;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.visitor.ClonePetriNet;

import java.awt.Color;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PetriNetAnimatorTest {



    @Test
    public void correctlyIncrementsTokenCountInSelfLoop() throws PetriNetComponentException {

        PetriNet petriNet = createSelfLoopPetriNet("1");
        Place place = petriNet.getComponent("P0", Place.class);
        place.setTokenCount("Default", 1);
        place.setCapacity(1);

        Animator animator = new PetriNetAnimator(petriNet);
        Transition transition = petriNet.getComponent("T1", Transition.class);
        animator.fireTransition(transition);
        assertEquals(1, place.getTokenCount("Default"));
    }


    @Test
    public void firingFunctionalTransitionMovesTokens() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Red").withColor(Color.RED)).and(
                AToken.called("Default").withColor(Color.BLACK)).and(
                APlace.withId("P0").containing(5, "Default").tokens()).and(APlace.withId("P1")).and(
                AnImmediateTransition.withId("T1")).and(
                ANormalArc.withSource("P0").andTarget("T1").with("#(P0)", "Default").tokens()).andFinally(
                ANormalArc.withSource("T1").andTarget("P1").with("#(P0)*2", "Red").tokens());

        Place p1 = petriNet.getComponent("P0", Place.class);
        Place p2 = petriNet.getComponent("P1", Place.class);
        Transition transition = petriNet.getComponent("T1", Transition.class);

        Animator animator = new PetriNetAnimator(petriNet);
        animator.fireTransition(transition);

        assertEquals(0, p1.getTokenCount("Default"));
        assertEquals(10, p2.getTokenCount("Red"));
    }

    @Test
    public void firingTransitionDoesNotDisableTransition() throws PetriNetComponentException {
        int tokenWeight = 1;
        PetriNet petriNet = createSimplePetriNet(tokenWeight);
        Place place = petriNet.getComponent("P1", Place.class);
        Token token = petriNet.getComponent("Default", Token.class);
        place.setTokenCount(token.getId(), 2);

        Transition transition = petriNet.getComponent("T1", Transition.class);
        Animator animator = new PetriNetAnimator(petriNet);
        animator.fireTransition(transition);


        Collection<Transition> enabled = animator.getEnabledTransitions();
        assertThat(enabled).contains(transition);
    }

    @Test
    public void firingTransitionEnablesNextTransition() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
                APlace.withId("P1").containing(1, "Default").token()).and(APlace.withId("P2")).and(
                AnImmediateTransition.withId("T1")).and(AnImmediateTransition.withId("T2")).and(
                ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token()).and(
                ANormalArc.withSource("T1").andTarget("P2").with("1", "Default").token()).andFinally(
                ANormalArc.withSource("P2").andTarget("T2").with("1", "Default").token());

        Animator animator = new PetriNetAnimator(petriNet);
        Transition transition = petriNet.getComponent("T1", Transition.class);
        animator.fireTransition(transition);

        Transition transition2 = petriNet.getComponent("T2", Transition.class);

        Collection<Transition> enabled = animator.getEnabledTransitions();
        assertThat(enabled).contains(transition2);
    }
    @Test
    public void randomTransitionsReturnsSingleEligibleTransition() throws PetriNetComponentException {
    	// PN with single enabled transition
    	PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
    			APlace.withId("P1").containing(1, "Default").token()).
    			and(APlace.withId("P2")).
    			and(AnImmediateTransition.withId("T1")).and(
    			ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token()).andFinally(
    			ANormalArc.withSource("T1").andTarget("P2").with("1", "Default").token());
    	
    	Animator animator = new PetriNetAnimator(petriNet);
//    	animator.setRandom(new Random(123456l)); 
    	Transition t = animator.getRandomEnabledTransition(); 
    	assertEquals("T1", t.getId()); 
    }
    @Test
    public void randomTransitionsIncludesAllEligibleTransitionsInRoughProportion() throws PetriNetComponentException {
    	// PN with two enabled transitions
    	PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
    			APlace.withId("P1").containing(1, "Default").token()).
    			and(APlace.withId("P2")).and(APlace.withId("P3")).
    			and(AnImmediateTransition.withId("T1")).and(AnImmediateTransition.withId("T2")).and(
    			ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token()).and(
    			ANormalArc.withSource("T1").andTarget("P2").with("1", "Default").token()).and(
    			ANormalArc.withSource("P1").andTarget("T2").with("1", "Default").token()).andFinally(
    			ANormalArc.withSource("T2").andTarget("P3").with("1", "Default").token());
    	
    	Animator animator = new PetriNetAnimator(petriNet);
    	Transition t = null; 
    	int t1 = 0; 
    	int t2 = 0; 
    	for (int i = 0; i < 10; i++) {
    		t = animator.getRandomEnabledTransition();
    		if (t.getId().equalsIgnoreCase("T1")) t1++;
    		else t2++;
		}
    	assertTrue(t1 > 0); 
    	assertTrue(t2 > 0); 
    }

    @Test
    public void firingTransitionBackwardMovesTokensBack() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
                APlace.withId("P1").containing(0, "Default").token()).and(AnImmediateTransition.withId("T1")).andFinally(
                ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token());


        Transition transition = petriNet.getComponent("T1", Transition.class);
        Animator animator = new PetriNetAnimator(petriNet);
        animator.fireTransitionBackwards(transition);

        Place place = petriNet.getComponent("P1", Place.class);
        Token token = petriNet.getComponent("Default", Token.class);

        assertThat(place.getTokenCount(token.getId())).isEqualTo(1);
    }



    private PetriNet createSelfLoopPetriNet(String tokenWeight) throws PetriNetComponentException {
        return APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
                AnImmediateTransition.withId("T1")).and(
                ANormalArc.withSource("T1").andTarget("P0").with(tokenWeight, "Default").tokens()).andFinally(
                ANormalArc.withSource("P0").andTarget("T1").with(tokenWeight, "Default").tokens());
    }



    @Test
    public void correctlyEnablesTransitionEvenAfterFiring() throws PetriNetComponentException {
        PetriNet petriNet = createSimpleInhibitorPetriNet(1);
        Transition transition = petriNet.getComponent("T1", Transition.class);

        Animator animator = new PetriNetAnimator(petriNet);
        animator.fireTransition(transition);
        Collection<Transition> enabled = animator.getEnabledTransitions();
        assertThat(enabled).contains(transition);
    }



    @Test
    public void firingTransitionMovesToken() throws PetriNetComponentException {
        int tokenWeight = 1;
        PetriNet petriNet = createSimplePetriNet(tokenWeight);

        Transition transition = petriNet.getComponent("T1", Transition.class);

        Animator animator = new PetriNetAnimator(petriNet);
        animator.fireTransition(transition);

        Token token = petriNet.getComponent("Default", Token.class);
        Place p1 = petriNet.getComponent("P1", Place.class);
        Place p2 = petriNet.getComponent("P2", Place.class);
        assertEquals(0, p1.getTokenCount(token.getId()));
        assertEquals(1, p2.getTokenCount(token.getId()));
    }

    /**
     * Create simple Petri net with P1 -o T1 -> P2
     * Initialises a token in P1 and gives arcs A1 and A2 a weight of tokenWeight to a default token
     *
     * @param tokenWeight
     * @return
     * @throws PetriNetComponentException 
     */
    public PetriNet createSimpleInhibitorPetriNet(int tokenWeight) throws PetriNetComponentException {
        return APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P1")).and(
                APlace.withId("P2")).and(AnImmediateTransition.withId("T1")).and(
                AnInhibitorArc.withSource("P1").andTarget("T1")).andFinally(
                ANormalArc.withSource("T1").andTarget("P2").with(Integer.toString(tokenWeight), "Default").tokens());
    }


    @Test
    public void firingTransitionDisablesTransition() throws PetriNetComponentException {
        int tokenWeight = 1;
        PetriNet petriNet = createSimplePetriNet(tokenWeight);

        Transition transition = petriNet.getComponent("T1", Transition.class);
        Animator animator = new PetriNetAnimator(petriNet);
        animator.fireTransition(transition);

        Collection<Transition> enabled = animator.getEnabledTransitions();
        assertThat(enabled).doesNotContain(transition);
    }


    @Test
    public void firingTransitionBackwardEnablesTransition() throws PetriNetComponentException {
        PetriNet petriNet =
                APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P1")).and(
                        APlace.withId("P2").containing(1, "Default").token()).and(AnImmediateTransition.withId("T1")).andFinally(
                        ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token());

        Transition transition = petriNet.getComponent("T1", Transition.class);
        Animator animator = new PetriNetAnimator(petriNet);
        animator.fireTransitionBackwards(transition);

        Collection<Transition> enabled = animator.getEnabledTransitions();
        assertThat(enabled).contains(transition);
    }

    @Test
    public void restoresPetriNet() throws PetriNetComponentException {
        PetriNet petriNet = createSimplePetriNet(1);
        PetriNet copy = ClonePetriNet.clone(petriNet);

        Animator animator = new PetriNetAnimator(petriNet);
        animator.fireTransition(animator.getRandomEnabledTransition());

        animator.reset();
        assertEquals(copy, petriNet);
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





}
