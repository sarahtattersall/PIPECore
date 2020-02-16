package uk.ac.imperial.pipe.models.petrinet;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.Collection;
import java.util.Random;

import org.apache.logging.log4j.Level;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.dsl.AnInhibitorArc;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.Animator;
import uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.PetriNetAnimationLogic;
import uk.ac.imperial.pipe.models.petrinet.PetriNetAnimator;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.pipe.visitor.PetriNetCloner;
//<<<<<<< 0e5245da7684e9b205c7f7b8cd2102aa8601f94b
import utils.AbstractTestLog4J2;
//=======
import uk.ac.imperial.state.State;
import uk.ac.imperial.pipe.models.petrinet.TimingQueue;
//>>>>>>> Added TimedPetriNetRunner and sorted out timed transitions in the PNAnimationLogic and PNAnimator.

@RunWith(MockitoJUnitRunner.class)
public class PetriNetAnimatorTest extends AbstractTestLog4J2 {

    private PetriNetAnimationLogic animationLogic;
    private ExecutablePetriNet epn;
    private Animator animator;

    @Mock
    private Animator mockAnimator;

    @Before
    public void setUp() throws Exception {
        //		setUpLog4J2(PetriNetAnimator.class, Level.DEBUG, true); 
        //		setUpLog4J2ForRoot(Level.DEBUG);  
    }

    @Test
    public void correctlyIncrementsTokenCountInSelfLoop() throws PetriNetComponentException {

        PetriNet petriNet = createSelfLoopPetriNet("1");
        ExecutablePetriNet epn = petriNet.getExecutablePetriNet();
        Animator animator = new PetriNetAnimator(epn);
        Transition transition = epn.getComponent("T1", Transition.class);
        animator.fireTransition(transition);

        //        TimingQueue timedState = epn.getTimingQueue();
        State state = epn.getState();
        assertEquals(1, (int) state.getTokens("P0").get("Default"));
        // I think this way is now wrong to ask for change in the STATE (not network)
        //Place epnPlace = epn.getComponent("P0", Place.class);
        //assertEquals(1, epnPlace.getTokenCount("Default"));
    }

    private PetriNet createSelfLoopPetriNet(String tokenWeight) throws PetriNetComponentException {
        return APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P0").andCapacity(1).and(1, "Default").token())
                .and(AnImmediateTransition.withId("T1"))
                .and(ANormalArc.withSource("T1").andTarget("P0").with(tokenWeight, "Default").tokens())
                .andFinally(ANormalArc.withSource("P0").andTarget("T1").with(tokenWeight, "Default").tokens());
    }

    @Test
    public void firingFunctionalTransitionMovesTokens() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Red").withColor(Color.RED))
                .and(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P0").containing(5, "Default").tokens()).and(APlace.withId("P1"))
                .and(AnImmediateTransition.withId("T1"))
                .and(ANormalArc.withSource("P0").andTarget("T1").with("#(P0)", "Default").tokens())
                .andFinally(ANormalArc.withSource("T1").andTarget("P1").with("#(P0)*2", "Red").tokens());
        ExecutablePetriNet epn = petriNet.getExecutablePetriNet();
        Animator animator = new PetriNetAnimator(epn);
        Transition transition = epn.getComponent("T1", Transition.class);
        //        TimingQueue timedState = epn.getTimingQueue();
        animator.fireTransition(transition);

        State state = epn.getState();
        assertEquals(0, (int) state.getTokens("P0").get("Default"));
        assertEquals(10, (int) state.getTokens("P1").get("Red"));

        //        timedState = epn.getTimingQueue();
        //        assertEquals(0, (int) timedState.getState().getTokens("P0").get("Default") );
        //        assertEquals(10, (int) timedState.getState().getTokens("P1").get("Red") );
        // I think this way is now wrong to ask for change in the STATE (not network)
        /*
         * Place ep1 = epn.getComponent("P0", Place.class); Place ep2 =
         * epn.getComponent("P1", Place.class); assertEquals(0,
         * ep1.getTokenCount("Default")); assertEquals(10,
         * ep2.getTokenCount("Red"));
         */
    }

    @Test
    public void firingTransitionDoesNotDisableTransitionIfCouldStillFire() throws PetriNetComponentException {
        int tokenWeight = 1;
        PetriNet petriNet = createSimplePetriNet(tokenWeight);
        Place place = petriNet.getComponent("P1", Place.class);
        Token token = petriNet.getComponent("Default", Token.class);
        place.setTokenCount(token.getId(), 2);

        epn = petriNet.getExecutablePetriNet();
        animator = new PetriNetAnimator(epn);
        Transition transition = epn.getComponent("T1", Transition.class);
        animator.fireTransition(transition);

        Collection<Transition> enabled = getEnabledTransitions();
        assertThat(enabled).contains(transition);
    }

    protected Collection<Transition> getEnabledTransitions() {
        animationLogic = (PetriNetAnimationLogic) animator.getAnimationLogic();
        //		    Collection<Transition> enabled = animationLogic.getEnabledImmediateOrTimedTransitions(epn.getTimingQueue());
        Collection<Transition> enabled = animationLogic.getEnabledImmediateOrTimedTransitions(epn.getState());
        return enabled;
    }

    @Test
    public void firingTransitionEnablesAndDisablesIndividualTransitionsAppropriately() throws Exception {
        //TODO should "transition" mirror changes in EPN, rather than fetching each time? 
        PetriNet petriNet = buildSequentiallyEnabledPetriNet();
        epn = petriNet.getExecutablePetriNet();
        animator = new PetriNetAnimator(epn);
        Transition transition = getTransitionFromExecutablePetriNet("T1");
        assertFalse(getTransitionFromExecutablePetriNet("T1").isEnabled());
        assertFalse(getTransitionFromExecutablePetriNet("T2").isEnabled());
        animator.startAnimation();
        assertTrue(getTransitionFromExecutablePetriNet("T1").isEnabled());
        assertFalse(getTransitionFromExecutablePetriNet("T2").isEnabled());
        animator.fireTransition(transition);
        assertFalse(getTransitionFromExecutablePetriNet("T1").isEnabled());
        assertTrue(getTransitionFromExecutablePetriNet("T2").isEnabled());
        animator.reset();
        assertFalse(getTransitionFromExecutablePetriNet("T1").isEnabled());
        assertFalse(getTransitionFromExecutablePetriNet("T2").isEnabled());

    }

    protected Transition getTransitionFromExecutablePetriNet(String transition)
            throws PetriNetComponentNotFoundException {
        return epn.getComponent(transition, Transition.class);
    }

    @Test
    public void firingTransitionEnablesNextTransition() throws PetriNetComponentException {
        PetriNet petriNet = buildSequentiallyEnabledPetriNet();

        epn = petriNet.getExecutablePetriNet();
        animator = new PetriNetAnimator(epn);
        Transition transition = epn.getComponent("T1", Transition.class);
        animator.fireTransition(transition);

        Transition transition2 = epn.getComponent("T2", Transition.class);

        Collection<Transition> enabled = getEnabledTransitions();
        assertThat(enabled).contains(transition2);
    }

    protected PetriNet buildSequentiallyEnabledPetriNet()
            throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P1").containing(1, "Default").token()).and(APlace.withId("P2"))
                .and(AnImmediateTransition.withId("T1")).and(AnImmediateTransition.withId("T2"))
                .and(ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token())
                .and(ANormalArc.withSource("T1").andTarget("P2").with("1", "Default").token())
                .andFinally(ANormalArc.withSource("P2").andTarget("T2").with("1", "Default").token());
        return petriNet;
    }

    @Test
    public void randomTransitionsReturnsSingleEligibleTransition() throws PetriNetComponentException {
        // PN with single enabled transition
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P1").containing(1, "Default").token()).and(APlace.withId("P2"))
                .and(AnImmediateTransition.withId("T1")).and(AnImmediateTransition.withId("T2"))
                .and(ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token())
                .andFinally(ANormalArc.withSource("T1").andTarget("P2").with("1", "Default").token());

        ExecutablePetriNet epn = petriNet.getExecutablePetriNet();
        Animator animator = new PetriNetAnimator(epn);
        animator.setRandom(new Random(123456l));
        Transition t = animator.getRandomEnabledTransition();
        assertEquals("T1", t.getId());
    }

    @Test
    public void randomTransitionsIncludesAllEligibleTransitionsInRoughProportion() throws PetriNetComponentException {
        // PN with two enabled transitions
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P1").containing(1, "Default").token()).and(APlace.withId("P2"))
                .and(APlace.withId("P3")).and(AnImmediateTransition.withId("T1"))
                .and(AnImmediateTransition.withId("T2"))
                .and(ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token())
                .and(ANormalArc.withSource("T1").andTarget("P2").with("1", "Default").token())
                .and(ANormalArc.withSource("P1").andTarget("T2").with("1", "Default").token())
                .andFinally(ANormalArc.withSource("T2").andTarget("P3").with("1", "Default").token());

        ExecutablePetriNet epn = petriNet.getExecutablePetriNet();
        Animator animator = new PetriNetAnimator(epn);
        animator.setRandom(new Random(123456l));
        assertEquals("T1", animator.getRandomEnabledTransition().getId());
        assertEquals("T2", animator.getRandomEnabledTransition().getId());
        assertEquals("T2", animator.getRandomEnabledTransition().getId());
        assertEquals("roughly random", "T1", animator.getRandomEnabledTransition().getId());
    }

    @Test
    public void firingTransitionBackwardMovesTokensBack() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P1").containing(0, "Default").token()).and(AnImmediateTransition.withId("T1"))
                .andFinally(ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token());
        ExecutablePetriNet epn = petriNet.getExecutablePetriNet();
        Animator animator = new PetriNetAnimator(epn);
        Transition transition = epn.getComponent("T1", Transition.class);
        animator.fireTransitionBackwards(transition);
        Place place = epn.getComponent("P1", Place.class);
        Token token = epn.getComponent("Default", Token.class);

        assertThat(place.getTokenCount(token.getId())).isEqualTo(1);
    }

    @Test
    public void correctlyEnablesTransitionEvenAfterFiring() throws PetriNetComponentException {
        PetriNet petriNet = createSimpleInhibitorPetriNet(1);
        epn = petriNet.getExecutablePetriNet();
        animator = new PetriNetAnimator(epn);
        Transition transition = epn.getComponent("T1", Transition.class);
        animator.fireTransition(transition);
        Collection<Transition> enabled = getEnabledTransitions();
        assertThat(enabled).contains(transition);
    }

    @Test
    public void firingTransitionMovesToken() throws PetriNetComponentException {
        int tokenWeight = 1;
        PetriNet petriNet = createSimplePetriNet(tokenWeight);
        ExecutablePetriNet epn = petriNet.getExecutablePetriNet();
        Animator animator = new PetriNetAnimator(epn);
        Transition transition = epn.getComponent("T1", Transition.class);
        animator.fireTransition(transition);
        State state = epn.getState();
        assertEquals(0, (int) state.getTokens("P1").get("Default"));
        assertEquals(1, (int) state.getTokens("P2").get("Default"));
        //        TimingQueue timedState = epn.getTimingQueue();
        //        assertEquals(0, (int) timedState.getState().getTokens("P1").get("Default") );
        //        assertEquals(1, (int) timedState.getState().getTokens("P2").get("Default") );
        // I think this way is now wrong to ask for change in the STATE (not network)
        /*
         * Token token = epn.getComponent("Default", Token.class); Place p1 =
         * epn.getComponent("P1", Place.class); Place p2 =
         * epn.getComponent("P2", Place.class); assertEquals(0,
         * p1.getTokenCount(token.getId())); assertEquals(1,
         * p2.getTokenCount(token.getId()));
         */
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
        return APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P1"))
                .and(APlace.withId("P2")).and(AnImmediateTransition.withId("T1"))
                .and(AnInhibitorArc.withSource("P1").andTarget("T1")).andFinally(ANormalArc.withSource("T1")
                        .andTarget("P2").with(Integer.toString(tokenWeight), "Default").tokens());
    }

    @Test
    public void firingTransitionDisablesTransition() throws PetriNetComponentException {
        int tokenWeight = 1;
        PetriNet petriNet = createSimplePetriNet(tokenWeight);
        epn = petriNet.getExecutablePetriNet();
        animator = new PetriNetAnimator(epn);
        Transition transition = epn.getComponent("T1", Transition.class);
        animator.fireTransition(transition);
        Collection<Transition> enabled = getEnabledTransitions();
        assertThat(enabled).doesNotContain(transition);
    }

    @Test
    public void firingTransitionBackwardEnablesTransition() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P1"))
                .and(APlace.withId("P2").containing(1, "Default").token()).and(AnImmediateTransition.withId("T1"))
                .andFinally(ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token());
        epn = petriNet.getExecutablePetriNet();
        animator = new PetriNetAnimator(epn);
        Transition transition = epn.getComponent("T1", Transition.class);
        animator.fireTransitionBackwards(transition);

        Collection<Transition> enabled = getEnabledTransitions();
        assertThat(enabled).contains(transition);
    }

    // I think the reset function is a little bit tricky - am not sure it belongs in
    // the animator.
    //@Test
    public void restoresPetriNet() throws PetriNetComponentException {
        PetriNet petriNet = createSimplePetriNet(1);
        PetriNet copy = PetriNetCloner.clone(petriNet);
        ExecutablePetriNet epn = petriNet.getExecutablePetriNet();
        ExecutablePetriNet copyepn = copy.getExecutablePetriNet();
        assertEquals(copyepn, epn);
        Animator animator = new PetriNetAnimator(epn);
        animator.fireTransition(animator.getRandomEnabledTransition());
        assertFalse(copyepn.equals(epn));

        animator.reset();
        assertEquals(copyepn, epn);
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

}
