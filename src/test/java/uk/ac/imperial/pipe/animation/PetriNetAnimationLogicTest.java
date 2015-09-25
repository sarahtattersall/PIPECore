package uk.ac.imperial.pipe.animation;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.dsl.AnInhibitorArc;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.ColoredToken;
import uk.ac.imperial.pipe.models.petrinet.DiscreteTransition;
import uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet;
import uk.ac.imperial.pipe.models.petrinet.InboundArc;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.state.State;
import uk.ac.imperial.pipe.models.petrinet.TimedState;

@RunWith(MockitoJUnitRunner.class)
public class PetriNetAnimationLogicTest {

	
    private ExecutablePetriNet executablePetriNet;
	private PetriNetAnimationLogic animationLogic;
	private TimedState timedState;
	private Map<TimedState, Collection<Transition>> successors;
	private TimedState successor;
	private PetriNetAnimator animator;

    @Before
	public void setUp() throws Exception {
	}
    
	@Test
//<<<<<<< 5bfbeec3b4365eac5d86077ff5c2ef1f2643b576
//    public void infiniteServerSemantics() throws PetriNetComponentException {
//        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
//                APlace.withId("P0").and(2, "Default").tokens()).and(APlace.withId("P1").and(0, "Default").tokens()).and(
//                AnImmediateTransition.withId("T0").andIsAnInfinite()).and(
//                ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token()).andFinally(
//                ANormalArc.withSource("T0").andTarget("P1").and("1", "Default").token());
//        executablePetriNet = petriNet.getExecutablePetriNet(); 
//        PetriNetAnimationLogic animator = new PetriNetAnimationLogic(executablePetriNet);
//=======
    public void infiniteServerSemantics() throws PetriNetComponentException {
		PetriNet petriNet = buildPetriNet();
		executablePetriNet = petriNet.getExecutablePetriNet(); 
		animationLogic = new PetriNetAnimationLogic(executablePetriNet);

        timedState = executablePetriNet.getTimedState();
        successors = animationLogic.getSuccessors(timedState);

        assertEquals(1, successors.size());
        successor = successors.keySet().iterator().next();

        checkCountForPlace(1, "P0");
        checkCountForPlace(1, "P1");

    }
	
	
	@Test
//<<<<<<< 2d290c87bc606112a7e9b5a10c5be8ffacaf0b75
//	public void timedTransitionExecutesFollowingDelay() {
//		PetriNet petriNet = buildTimedPetriNet();
//		executablePetriNet = petriNet.getExecutablePetriNet(); 
//		animator = new PetriNetAnimationLogic(executablePetriNet, 40000);
//		State state = executablePetriNet.getState();
//		((PetriNetAnimationLogic) animator).advanceToTime(40001); 
//		Map<State, Collection<Transition>> successors = animator.getSuccessors(state);
//SJDclean=======
	public void timedTransitionExecutesFollowingDelay() throws PetriNetComponentException {
//<<<<<<< 55df0c4d7513e7ac33409170339c49988ee1b32e
		advanceNetToTime(buildTimedPetriNet(1000), 40001);
//>>>>>>> added 3 commented tests
//=======
//		buildTimedPetriNet(1000, 40000);
//>>>>>>> Introduced a TimedState as an extension of the PN-State which includes the currentTime and for the timed Petri Networks the time when transitions ar allowed to fire.
		assertEquals(0, successors.size());
		advanceNetToTime(41000, executablePetriNet.getTimedState() );
		//successors = animationLogic.getSuccessors(executablePetriNet.getTimedState());
		assertEquals(1, successors.size());
		successor = successors.keySet().iterator().next();
		
		checkCountForPlace(2, "P0");
		checkCountForPlace(1, "P1");
		
		System.out.println(successors);
		advanceNetToTime(42000, successor );
		System.out.println(successors);
		//successors = animationLogic.getSuccessors(successor);
		assertEquals(1, successors.size());
		successor = successors.keySet().iterator().next();
		
		checkCountForPlace(1, "P0");
		checkCountForPlace(2, "P1");
		
	}
	
	@Test
	public void timedTransitionWithZeroDelayExecutesImmediately() throws PetriNetComponentException {
		buildTimedPetriNet(0, 40000);
		successors = animationLogic.getSuccessors( executablePetriNet.getTimedState() );
		assertEquals(1, successors.size());
		successor = successors.keySet().iterator().next();
		
		checkCountForPlace(2, "P0");
		checkCountForPlace(1, "P1");
	}
	protected void checkCountForPlace(Integer count, String place) {
		assertEquals(count, successor.getState().getTokens(place).get("Default"));
	}
	
	protected void advanceNetToTime(int advanceToTime, TimedState timedState) {
		//timedState = executablePetriNet.getTimedState();
		animator.advanceToTime(timedState, advanceToTime); 
		successors = animationLogic.getSuccessors(timedState);
	}

	protected PetriNet buildPetriNet() throws PetriNetComponentException {
		PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
                APlace.withId("P0").and(2, "Default").tokens()).and(APlace.withId("P1").and(0, "Default").tokens()).and(
                AnImmediateTransition.withId("T0").andIsAnInfinite()).and(
                ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token()).andFinally(
                ANormalArc.withSource("T0").andTarget("P1").and("1", "Default").token());
		return petriNet;
	}
//<<<<<<< 55df0c4d7513e7ac33409170339c49988ee1b32e
////<<<<<<< 2d290c87bc606112a7e9b5a10c5be8ffacaf0b75
////	protected PetriNet buildTimedPetriNet() {
////SJDclean=======
//	protected PetriNet buildTimedPetriNet(int delay) throws PetriNetComponentException {
//=======
	protected PetriNet buildTimedPetriNet(int delay, long initTime) throws PetriNetComponentException {
//>>>>>>> Introduced a TimedState as an extension of the PN-State which includes the currentTime and for the timed Petri Networks the time when transitions ar allowed to fire.
		PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
		APlace.withId("P0").and(3, "Default").tokens()).and(APlace.withId("P1").and(0, "Default").tokens()).and(
		ATimedTransition.withId("T0").andDelay(delay)).and(
		ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token()).andFinally(
		ANormalArc.withSource("T0").andTarget("P1").and("1", "Default").token());
		executablePetriNet = petriNet.getExecutablePetriNet(); 
		executablePetriNet.getTimedState().setCurrentTime(initTime);
		animationLogic = new PetriNetAnimationLogic(executablePetriNet);
		timedState = executablePetriNet.getTimedState();
		successors = animationLogic.getSuccessors(timedState);
		animator = new PetriNetAnimator(executablePetriNet);
		animationLogic.registerEnabledTimedTransitions(timedState);
		return petriNet;
	}
	
	@Test
	public void timerForSecondTimedTransitionOnlyStartsWhenTransitionIsEnabled() throws PetriNetComponentException {
		PetriNet petriNet = buildTimedPetriNetTwoTimedTransitions(500, 40000);
		advanceNetToTime(41000, executablePetriNet.getTimedState());
		assertEquals(1, successors.size());
		successor = successors.keySet().iterator().next();
		
		checkCountForPlace(0, "P0");
		checkCountForPlace(1, "P1");
		checkCountForPlace(0, "P2");
		
		successors = animationLogic.getSuccessors(successor);
		assertEquals(0, successors.size());
		
		advanceNetToTime(41500, successor); 

		//successors = animationLogic.getSuccessors(successor);
		assertEquals(1, successors.size());
		successor = successors.keySet().iterator().next();
		checkCountForPlace(0, "P0");
		checkCountForPlace(0, "P1");
		checkCountForPlace(1, "P2");
	}
	//FIXME commented test
	@Test  
	public void secondTimedTransitionWithZeroDelayFiresWhenTransitionIsEnabled() throws PetriNetComponentException {
		PetriNet petriNet = buildTimedPetriNetTwoTimedTransitions(0, 40000);
		advanceNetToTime(41000, executablePetriNet.getTimedState() );
		assertEquals(1, successors.size());
		successor = successors.keySet().iterator().next();
		
		checkCountForPlace(0, "P0");
		checkCountForPlace(1, "P1");
		checkCountForPlace(0, "P2");
		
		successors = animationLogic.getSuccessors(successor);
		assertEquals(1, successors.size());
		successor = successors.keySet().iterator().next();
		checkCountForPlace(0, "P0");
		checkCountForPlace(0, "P1");
		checkCountForPlace(1, "P2");
	}

	
	protected PetriNet buildTimedPetriNetTwoTimedTransitions(int t1delay, long initTime) throws PetriNetComponentException {
		PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
				APlace.withId("P0").and(1, "Default").tokens()).and(
				APlace.withId("P1").and(0, "Default").tokens()).and(
				APlace.withId("P2").and(0, "Default").tokens()).and(
				ATimedTransition.withId("T0").andDelay(1000)).and(
				ATimedTransition.withId("T1").andDelay(t1delay)).and(
				ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token()).and(
				ANormalArc.withSource("T0").andTarget("P1").with("1", "Default").token()).and(
				ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token()).
				andFinally(ANormalArc.withSource("T1").andTarget("P2").and("1", "Default").token());
		
		executablePetriNet = petriNet.getExecutablePetriNet(); 
		executablePetriNet.getTimedState().setCurrentTime(initTime);
		animationLogic = new PetriNetAnimationLogic(executablePetriNet);
		timedState = executablePetriNet.getTimedState();
		successors = animationLogic.getSuccessors(timedState);
		animator = new PetriNetAnimator(executablePetriNet);
		//TODO: registerTransitions must be invoked - should be bound to constructor;
		animationLogic.registerEnabledTimedTransitions(timedState);
		
		return petriNet;
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

        executablePetriNet = petriNet.getExecutablePetriNet(); 
        Transition t0 = executablePetriNet.getComponent("T0", Transition.class);
        Transition t1 = executablePetriNet.getComponent("T1", Transition.class);

        AnimationLogic animator = new PetriNetAnimationLogic(executablePetriNet);
        
        Collection<Transition> transitions = animator.getEnabledTransitions(executablePetriNet.getTimedState());
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

        executablePetriNet = petriNet.getExecutablePetriNet(); 
        Transition t0 = executablePetriNet.getComponent("T0", Transition.class);
        Transition t1 = executablePetriNet.getComponent("T1", Transition.class);
        
        AnimationLogic animator = new PetriNetAnimationLogic(executablePetriNet);
        Collection<Transition> transitions = animator.getEnabledTransitions(executablePetriNet.getTimedState());
        assertEquals("Both transitions were not enabled", 2, transitions.size());
        assertThat(transitions).contains(t0, t1);
    }
    @Test
    public void arcweightThatEvaluatesToZeroDoesNotEnableTransition() throws PetriNetComponentException {
    	PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
    			APlace.withId("P0").and(0, "Default").tokens()).and(
    			AnImmediateTransition.withId("T0")).andFinally(
    			ANormalArc.withSource("P0").andTarget("T0").with("#(P0)", "Default").token());
    	executablePetriNet = petriNet.getExecutablePetriNet(); 
        TimedState timedState = executablePetriNet.getTimedState();
        InboundArc arc = petriNet.getComponent("P0 TO T0", InboundArc.class);
        assertFalse(arc.canFire(executablePetriNet, timedState.getState() )); 
        AnimationLogic animator = new PetriNetAnimationLogic(executablePetriNet);
        Collection<Transition> transitions = animator.getEnabledTransitions(timedState);
        assertEquals(0, transitions.size());
    }

    @Test
    public void correctlyIdentifiesEnabledTransition() throws PetriNetComponentException {
        int tokenWeight = 1;
        PetriNet petriNet = createSimplePetriNet(tokenWeight);
        executablePetriNet = petriNet.getExecutablePetriNet(); 
        Transition transition = executablePetriNet.getComponent("T1", Transition.class);
        AnimationLogic animator = new PetriNetAnimationLogic(executablePetriNet);
        Collection<Transition> enabled = animator.getEnabledTransitions(executablePetriNet.getTimedState());
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
        executablePetriNet = petriNet.getExecutablePetriNet(); 
        Transition transition = executablePetriNet.getComponent("T1", Transition.class);
        AnimationLogic animator = new PetriNetAnimationLogic(executablePetriNet);
        Collection<Transition> enabled = animator.getEnabledTransitions(executablePetriNet.getTimedState());
        assertTrue("Petri net did not put transition in enabled collection", enabled.contains(transition));
    }

    @Test
    public void correctlyIdentifiesNotEnabledTransitionDueToEmptyPlace() throws PetriNetComponentException {
        int tokenWeight = 4;
        PetriNet petriNet = createSimplePetriNet(tokenWeight);
        executablePetriNet = petriNet.getExecutablePetriNet(); 
        Transition transition = executablePetriNet.getComponent("T1", Transition.class);
        AnimationLogic animator = new PetriNetAnimationLogic(executablePetriNet);
        Place place = executablePetriNet.getComponent("P1", Place.class);
        place.decrementTokenCount("Default");

        Collection<Transition> enabled = animator.getEnabledTransitions(executablePetriNet.getTimedState());
        assertThat(enabled).doesNotContain(transition);
    }

    @Test
    public void correctlyIdentifiesNotEnabledTransitionDueToNotEnoughTokens()
            throws PetriNetComponentException {
        int tokenWeight = 4;
        PetriNet petriNet = createSimplePetriNet(tokenWeight);
        executablePetriNet = petriNet.getExecutablePetriNet(); 
        Transition transition = executablePetriNet.getComponent("T1", Transition.class);
        AnimationLogic animator = new PetriNetAnimationLogic(executablePetriNet);
        Collection<Transition> enabled = animator.getEnabledTransitions(executablePetriNet.getTimedState());
        assertThat(enabled).doesNotContain(transition);
    }

    @Test
    public void correctlyIdentifiesNotEnabledTransitionDueToOnePlaceNotEnoughTokens()
            throws PetriNetComponentException {
        int tokenWeight = 1;
        PetriNet petriNet = createSimplePetriNetTwoPlacesToTransition(tokenWeight);
        executablePetriNet = petriNet.getExecutablePetriNet(); 
        Transition transition = executablePetriNet.getComponent("T1", Transition.class);
        AnimationLogic animator = new PetriNetAnimationLogic(executablePetriNet);
        Collection<Transition> enabled = animator.getEnabledTransitions(executablePetriNet.getTimedState());
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
        executablePetriNet = petriNet.getExecutablePetriNet(); 
        Transition transition = executablePetriNet.getComponent("T1", Transition.class);
        AnimationLogic animator = new PetriNetAnimationLogic(executablePetriNet);
        Collection<Transition> enabled = animator.getEnabledTransitions(executablePetriNet.getTimedState());
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
        AnimationLogic animator = new PetriNetAnimationLogic(executablePetriNet);
        Collection<Transition> enabled = animator.getEnabledTransitions(executablePetriNet.getTimedState());
        assertThat(enabled).contains(transition);
    }

    @Test
    public void onlyEnablesHigherPriorityTransition() throws PetriNetComponentNotFoundException {
        PetriNet petriNet = new PetriNet();
        Transition t1 = new DiscreteTransition("1", "1");
        t1.setPriority(10);
        Transition t2 = new DiscreteTransition("2", "2");
        t2.setPriority(1);
        petriNet.addTransition(t1);
        petriNet.addTransition(t2);
        executablePetriNet = petriNet.getExecutablePetriNet(); 
        Transition transition = executablePetriNet.getComponent("1", Transition.class);
        AnimationLogic animator = new PetriNetAnimationLogic(executablePetriNet);
        Collection<Transition> enabled = animator.getEnabledTransitions(executablePetriNet.getTimedState());

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
        AnimationLogic animator = new PetriNetAnimationLogic(executablePetriNet);
        Collection<Transition> enabled = animator.getEnabledTransitions(executablePetriNet.getTimedState());
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
        AnimationLogic animator = new PetriNetAnimationLogic(executablePetriNet);
        Collection<Transition> enabled = animator.getEnabledTransitions(executablePetriNet.getTimedState());
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
        executablePetriNet = petriNet.getExecutablePetriNet(); 
        Transition transition = executablePetriNet.getComponent("T1", Transition.class);
        AnimationLogic animator = new PetriNetAnimationLogic(executablePetriNet);
        Collection<Transition> enabled = animator.getEnabledTransitions(executablePetriNet.getTimedState());
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
        executablePetriNet = petriNet.getExecutablePetriNet(); 
        TimedState timedState = executablePetriNet.getTimedState();
        AnimationLogic animator = new PetriNetAnimationLogic(executablePetriNet);
        Map<TimedState, Collection<Transition>> successors = animator.getSuccessors(timedState);

        assertEquals(1, successors.size());
        TimedState successor = successors.keySet().iterator().next();

        int actualP1 = successor.getState().getTokens("P1").get("Default");
        assertEquals(0, actualP1);

        int actualP2 = successor.getState().getTokens("P2").get("Default");
        assertEquals(1, actualP2);
    }

    @Test
    public void calculatesSelfLoop() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
                APlace.withId("P0").and(1, "Default").token()).and(AnImmediateTransition.withId("T0")).and(
                ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token()).andFinally(
                ANormalArc.withSource("T0").andTarget("P0").with("1", "Default").token());

        executablePetriNet = petriNet.getExecutablePetriNet(); 
        timedState = executablePetriNet.getTimedState();
        animationLogic = new PetriNetAnimationLogic(executablePetriNet);
        successors = animationLogic.getSuccessors(timedState);

        assertEquals(1, successors.size());
        successor = successors.keySet().iterator().next();

        checkCountForPlace(1, "P0");
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
        executablePetriNet = petriNet.getExecutablePetriNet(); 
        TimedState timedState = executablePetriNet.getTimedState();
        AnimationLogic animator = new PetriNetAnimationLogic(executablePetriNet);

        Map<TimedState, Collection<Transition>> successors = animator.getSuccessors(timedState);

        assertEquals(1, successors.size());
        TimedState successor = successors.keySet().iterator().next();

        int actualP1 = successor.getState().getTokens("P0").get("Default");
        assertEquals(Integer.MAX_VALUE, actualP1);
    }
    @Test
    public void clearsWhenExecutablePetriNetRefreshes() throws Exception {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
                AnImmediateTransition.withId("T0")).andFinally(APlace.withId("P0").and(Integer.MAX_VALUE, "Default").token());
        executablePetriNet = petriNet.getExecutablePetriNet(); 
        State state = executablePetriNet.getState();
        PetriNetAnimationLogic animator = new PetriNetAnimationLogic(executablePetriNet);
        Set<Transition> transitions = new HashSet<>(); 
        transitions.add(petriNet.getComponent("T0", Transition.class));
        animator.cachedEnabledTransitions.put(state, transitions); 
        assertEquals(1, animator.cachedEnabledTransitions.size());
        executablePetriNet.refreshRequired();
        executablePetriNet.refresh();
        assertEquals("cache should be cleared",0, animator.cachedEnabledTransitions.size());

    }
    
}