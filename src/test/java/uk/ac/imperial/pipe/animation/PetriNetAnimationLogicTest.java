package uk.ac.imperial.pipe.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.Collection;
import java.util.Map;

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
import uk.ac.imperial.pipe.models.petrinet.ColoredToken;
import uk.ac.imperial.pipe.models.petrinet.DiscreteTransition;
import uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet;
import uk.ac.imperial.pipe.models.petrinet.InboundArc;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.TimedState;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.pipe.runner.TimedPetriNetRunner;
import utils.AbstractTestLog4J2;

@RunWith(MockitoJUnitRunner.class)
public class PetriNetAnimationLogicTest extends AbstractTestLog4J2 {


    private ExecutablePetriNet executablePetriNet;
	private PetriNetAnimationLogic animationLogic;
	private TimedState timedState;
	private Map<TimedState, Collection<Transition>> successors;
	private TimedState successor;
	private PetriNetAnimator animator;

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

        timedState = executablePetriNet.getTimedState();
        successors = animationLogic.getSuccessors(timedState);
        assertEquals(1, successors.size());
        successor = successors.keySet().iterator().next();

        checkCountForPlace(1, "P0");
        checkCountForPlace(1, "P1");

    }
	
    //@Test
    public void testTimedPNRunner() throws PetriNetComponentException, InterruptedException {
    	PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
    		APlace.withId("P0").and(3, "Default").tokens()).and(APlace.withId("P1").and(0, "Default").tokens()).and(
    		ATimedTransition.withId("T0").andDelay(1000)).and(
    		ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token()).andFinally(
    		ANormalArc.withSource("T0").andTarget("P1").and("1", "Default").token());
    	executablePetriNet = petriNet.getExecutablePetriNet(); 
    	executablePetriNet.getTimedState().resetTimeAndTimedTransitions(0);
    	animator = new PetriNetAnimator(executablePetriNet);
    	animationLogic = new PetriNetAnimationLogic(executablePetriNet);
    	timedState = executablePetriNet.getTimedState();
    	TimedPetriNetRunner runner = new TimedPetriNetRunner(petriNet);
    	runner.run();
    	runner.getTimedRunnerThread().join();
    	//System.out.println("DONE " + executablePetriNet.getTimedState() );
    }
	
    /*
     * Tests the basic firing of transitions depending on inbound arcs.
     */
	@Test
	public void arcEnablesTransitionInboundNormalArc() throws PetriNetComponentException {
		PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
				APlace.withId("P0").and(2, "Default").tokens()).and(
				APlace.withId("P1").and(1, "Default").tokens()).and(
				APlace.withId("P2").and(0, "Default").tokens()).and(
				AnImmediateTransition.withId("T0").andIsAnInfinite()).and(
                ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token()).and(
                ANormalArc.withSource("P1").andTarget("T0").with("1", "Default").token()).andFinally(
                ANormalArc.withSource("T0").andTarget("P2").and("1", "Default").token());
		
		executablePetriNet = petriNet.getExecutablePetriNet(); 
		animator = new PetriNetAnimator(executablePetriNet);
		animationLogic = new PetriNetAnimationLogic(executablePetriNet);
		timedState = executablePetriNet.getTimedState();
		successors = animationLogic.getSuccessors(timedState);
        assertEquals(1, successors.size());
        successor = successors.keySet().iterator().next();
        checkCountForPlace(1, "P0");
        checkCountForPlace(0, "P1");
        checkCountForPlace(1, "P2");
        successors = animationLogic.getSuccessors(successor);
        assertEquals(0, successors.size());		
	}
	
    /*
     * Tests the basic firing of transitions depending on inbound arcs.
     * For the inhibitory arc different cases are checked: is only fired when place on inhibitory arc
     * is not marked.
     */
	@Test
	public void arcEnablesTransitionInboundInhibitoryArc() throws PetriNetComponentException {
		PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
				APlace.withId("P1").and(1, "Default").tokens()).and(
				APlace.withId("P2").and(0, "Default").tokens()).and(
				AnImmediateTransition.withId("T0").andIsAnInfinite()).and(
                AnInhibitorArc.withSource("P1").andTarget("T0")).andFinally(
                ANormalArc.withSource("T0").andTarget("P2").and("1", "Default").token());

		// Check that transition is not firing when inhibited.
		executablePetriNet = petriNet.getExecutablePetriNet(); 
		animator = new PetriNetAnimator(executablePetriNet);
		animationLogic = new PetriNetAnimationLogic(executablePetriNet);
		timedState = executablePetriNet.getTimedState();
		successors = animationLogic.getSuccessors(timedState);
        assertEquals(0, successors.size());
        
        // Check that transition fires when not inhibited
        Place inhPlace = petriNet.getComponent("P1", Place.class);
        inhPlace.setTokenCount("Default", 0); 
		timedState = executablePetriNet.getTimedState();
        successors = animationLogic.getSuccessors(timedState);
        assertEquals(1, successors.size());
        successor = successors.keySet().iterator().next();
        checkCountForPlace(0, "P1");
        checkCountForPlace(1, "P2");
        successors = animationLogic.getSuccessors(successor);
        assertEquals(1, successors.size());
		
        // Tests one inhibiting arc in a more complex network
		petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
				APlace.withId("P0").and(2, "Default").tokens()).and(
				APlace.withId("P1").and(1, "Default").tokens()).and(
				APlace.withId("P2").and(0, "Default").tokens()).and(
				AnImmediateTransition.withId("T0").andIsAnInfinite()).and(
                ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token()).and(
                AnInhibitorArc.withSource("P1").andTarget("T0")).andFinally(
                ANormalArc.withSource("T0").andTarget("P2").and("1", "Default").token());
		
		executablePetriNet = petriNet.getExecutablePetriNet(); 
		animator = new PetriNetAnimator(executablePetriNet);
		animationLogic = new PetriNetAnimationLogic(executablePetriNet);
		timedState = executablePetriNet.getTimedState();
		successors = animationLogic.getSuccessors(timedState);
        assertEquals(0, successors.size());
        
        inhPlace = petriNet.getComponent("P1", Place.class);
        inhPlace.setTokenCount("Default", 0); 
		timedState = executablePetriNet.getTimedState();
        successors = animationLogic.getSuccessors(timedState);
        assertEquals(1, successors.size());
        successor = successors.keySet().iterator().next();
        checkCountForPlace(1, "P0");
        checkCountForPlace(0, "P1");
        checkCountForPlace(1, "P2");
        successors = animationLogic.getSuccessors(successor);
        assertEquals(1, successors.size());
        successor = successors.keySet().iterator().next();
        checkCountForPlace(0, "P0");
        checkCountForPlace(0, "P1");
        checkCountForPlace(2, "P2");
        successors = animationLogic.getSuccessors(successor);
        assertEquals(0, successors.size());
	}
	
	/*
     * Tests the basic firing of transitions depending on inbound arcs.
     * For the test arc different cases are checked: is fired only when test arc
     * is marked.
     */
	@Test
	public void arcEnablesTransitionInboundTestArc() throws PetriNetComponentException {
		PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
				APlace.withId("P1").and(0, "Default").tokens()).and(
				APlace.withId("P2").and(0, "Default").tokens()).and(
				AnImmediateTransition.withId("T0").andIsAnInfinite()).and(
                ATestArc.withSource("P1").andTarget("T0")).andFinally(
                ANormalArc.withSource("T0").andTarget("P2").and("1", "Default").token());

		// Check that transition is not firing when place for enabling arc is not marked.
		executablePetriNet = petriNet.getExecutablePetriNet(); 
		animator = new PetriNetAnimator(executablePetriNet);
		animationLogic = new PetriNetAnimationLogic(executablePetriNet);
		timedState = executablePetriNet.getTimedState();
		successors = animationLogic.getSuccessors(timedState);
        assertEquals(0, successors.size());
        
        // Check that transition fires when enabling arc is active
        Place enablePlace = petriNet.getComponent("P1", Place.class);
        enablePlace.setTokenCount("Default", 1); 
		timedState = executablePetriNet.getTimedState();
        successors = animationLogic.getSuccessors(timedState);
        assertEquals(1, successors.size());
        successor = successors.keySet().iterator().next();
        checkCountForPlace(1, "P1");
        checkCountForPlace(1, "P2");
        successors = animationLogic.getSuccessors(successor);
        assertEquals(1, successors.size());
		
		petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
				APlace.withId("P0").and(2, "Default").tokens()).and(
				APlace.withId("P1").and(0, "Default").tokens()).and(
				APlace.withId("P2").and(0, "Default").tokens()).and(
				AnImmediateTransition.withId("T0").andIsAnInfinite()).and(
                ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token()).and(
                ATestArc.withSource("P1").andTarget("T0")).andFinally(
                ANormalArc.withSource("T0").andTarget("P2").and("1", "Default").token());
		
		executablePetriNet = petriNet.getExecutablePetriNet(); 
		animator = new PetriNetAnimator(executablePetriNet);
		animationLogic = new PetriNetAnimationLogic(executablePetriNet);
		timedState = executablePetriNet.getTimedState();
		successors = animationLogic.getSuccessors(timedState);
        assertEquals(0, successors.size());
        
        enablePlace = petriNet.getComponent("P1", Place.class);
        enablePlace.setTokenCount("Default", 1); 
		timedState = executablePetriNet.getTimedState();
        successors = animationLogic.getSuccessors(timedState);
        assertEquals(1, successors.size());
        successor = successors.keySet().iterator().next();
        checkCountForPlace(1, "P0");
        checkCountForPlace(1, "P1");
        checkCountForPlace(1, "P2");
        successors = animationLogic.getSuccessors(successor);
        assertEquals(1, successors.size());
        successor = successors.keySet().iterator().next();
        checkCountForPlace(0, "P0");
        checkCountForPlace(1, "P1");
        checkCountForPlace(2, "P2");
        successors = animationLogic.getSuccessors(successor);
        assertEquals(0, successors.size());
	}
    
	@Test
	public void timedTransitionExecutesFollowingDelay() throws PetriNetComponentException {
		buildTimedPetriNet(1000, 40000);
		assertEquals(0, successors.size());
		animator.advanceNetToTime( timedState, 41000 );
		successors = animationLogic.getSuccessors( timedState );
		assertEquals(1, successors.size());
		successor = successors.keySet().iterator().next();
		
		checkCountForPlace(2, "P0");
		checkCountForPlace(1, "P1");
		
		animator.advanceNetToTime( successor, 42000);
		successors = animationLogic.getSuccessors(successor);
		//advanceNetToTime(42000, successor );
		assertEquals(1, successors.size());
		successor = successors.keySet().iterator().next();
		
		checkCountForPlace(1, "P0");
		checkCountForPlace(2, "P1");
		
	}
	
	@Test
	public void timedTransitionWithZeroDelayExecutesImmediately() throws PetriNetComponentException {
		buildTimedPetriNet(0, 40000);
		successors = animationLogic.getSuccessors( timedState );
		assertEquals(1, successors.size());
		successor = successors.keySet().iterator().next();
		checkCountForPlace(2, "P0");
		checkCountForPlace(1, "P1");
	}
	
	protected void checkCountForPlace(Integer count, String place) {
		assertEquals(count, successor.getState().getTokens(place).get("Default"));
	}
	
	@Test
	public void timedTransitionsWithConflict() throws PetriNetComponentException {
		PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
				APlace.withId("P0").and(1, "Default").tokens()).and(APlace.withId("P1").and(0, "Default").tokens()).and(
				ATimedTransition.withId("T0").andDelay(500)).and(
				ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token()).and(
				ANormalArc.withSource("T0").andTarget("P1").and("1", "Default").token()).and(
				APlace.withId("P2").and(0, "Default").tokens()).and(
				ATimedTransition.withId("T1").andDelay(1000)).and(
				ANormalArc.withSource("P0").andTarget("T1").with("1", "Default").token()).andFinally(
				ANormalArc.withSource("T1").andTarget("P2").and("1", "Default").token());
		executablePetriNet = petriNet.getExecutablePetriNet();
		executablePetriNet.getTimedState().resetTimeAndTimedTransitions(40000);
		// TODO: Save State in Animator is tricky - as it checks for the state in the PN
		// not the TimedState object
		animator = new PetriNetAnimator(executablePetriNet);
		animationLogic = new PetriNetAnimationLogic(executablePetriNet);
		timedState = executablePetriNet.getTimedState();
		successors = animationLogic.getSuccessors(timedState);
		assertEquals(0, successors.size());
		
		animator.advanceNetToTime( timedState, 40500 );
		successors = animationLogic.getSuccessors( timedState );
		assertEquals(1, successors.size());
		successor = successors.keySet().iterator().next();
		checkCountForPlace(0, "P0");
		checkCountForPlace(1, "P1");
		checkCountForPlace(0, "P2");
		
		animator.advanceNetToTime( successor, 41000 );
		successors = animationLogic.getSuccessors( successor );
		assertEquals(0, successors.size());
		//successor = successors.keySet().iterator().next();
		checkCountForPlace(0, "P0");
		checkCountForPlace(1, "P1");
		checkCountForPlace(0, "P2");
		
	}
	
	//protected void advanceNetToTime(int advanceToTime, TimedState timedState) {
		//timedState = executablePetriNet.getTimedState();
	//	animator.advanceToTime(timedState, advanceToTime); 
	//	successors = animationLogic.getSuccessors(timedState);
	//}

	protected PetriNet buildPetriNet() throws PetriNetComponentException {
		PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
                APlace.withId("P0").and(2, "Default").tokens()).and(APlace.withId("P1").and(0, "Default").tokens()).and(
                AnImmediateTransition.withId("T0").andIsAnInfinite()).and(
                ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token()).andFinally(
                ANormalArc.withSource("T0").andTarget("P1").and("1", "Default").token());
		return petriNet;
	}
	protected PetriNet buildTimedPetriNet(int delay, long initTime) throws PetriNetComponentException {
		PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
		APlace.withId("P0").and(3, "Default").tokens()).and(APlace.withId("P1").and(0, "Default").tokens()).and(
		ATimedTransition.withId("T0").andDelay(delay)).and(
		ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token()).andFinally(
		ANormalArc.withSource("T0").andTarget("P1").and("1", "Default").token());
		executablePetriNet = petriNet.getExecutablePetriNet(); 
		executablePetriNet.getTimedState().resetTimeAndTimedTransitions(initTime);
		animator = new PetriNetAnimator(executablePetriNet);
		animationLogic = new PetriNetAnimationLogic(executablePetriNet);
		timedState = executablePetriNet.getTimedState();
		successors = animationLogic.getSuccessors(timedState);
		//TOMS animationLogic.registerEnabledTimedTransitions(timedState);
		return petriNet;
	}
	
	@Test
	public void timerForSecondTimedTransitionOnlyStartsWhenTransitionIsEnabled() throws PetriNetComponentException {
		buildTimedPetriNetTwoTimedTransitions(500, 40000);
		animator.advanceNetToTime( timedState, 41000 );
		successors = animationLogic.getSuccessors( timedState );
		assertEquals(1, successors.size());
		successor = successors.keySet().iterator().next();
		
		checkCountForPlace(0, "P0");
		checkCountForPlace(1, "P1");
		checkCountForPlace(0, "P2");
		
		successors = animationLogic.getSuccessors(successor);
		assertEquals(0, successors.size());
		
		animator.advanceNetToTime( successor, 41500 );

		successors = animationLogic.getSuccessors(successor);
		assertEquals(1, successors.size());
		successor = successors.keySet().iterator().next();
		checkCountForPlace(0, "P0");
		checkCountForPlace(0, "P1");
		checkCountForPlace(1, "P2");
	}
	
	@Test
	public void secondTimedTransitionWithZeroDelayFiresWhenTransitionIsEnabled() throws PetriNetComponentException {
		buildTimedPetriNetTwoTimedTransitions(0, 40000);
		animator.advanceNetToTime( timedState, 41000 );
		successors = animationLogic.getSuccessors( timedState );
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
		executablePetriNet.getTimedState().resetTimeAndTimedTransitions(initTime);
		animator = new PetriNetAnimator(executablePetriNet);
		animationLogic = new PetriNetAnimationLogic(executablePetriNet);
		timedState = executablePetriNet.getTimedState();
		successors = animationLogic.getSuccessors(timedState);
		//TODO: registerTransitions must be invoked - should be bound to constructor;
		//animationLogic.registerEnabledTimedTransitions(timedState);
		
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

        Collection<Transition> transitions = getEnabledImmediateOrTimedTransitionsFromAnimationLogic();
        assertEquals("Both transitions were not enabled", 2, transitions.size());
        assertThat(transitions).contains(t0, t1);
    }
	protected Collection<Transition> getEnabledImmediateOrTimedTransitionsFromAnimationLogic() {
		//        AnimationLogic animator = new PetriNetAnimationLogic(executablePetriNet);
		        animationLogic = new PetriNetAnimationLogic(executablePetriNet);
		        Collection<Transition> transitions = animationLogic.getEnabledImmediateOrTimedTransitions(executablePetriNet.getTimedState());
		return transitions;
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
        
        Collection<Transition> transitions = getEnabledImmediateOrTimedTransitionsFromAnimationLogic();
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
        Collection<Transition> transitions = getEnabledImmediateOrTimedTransitionsFromAnimationLogic();
        assertEquals(0, transitions.size());
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
}
