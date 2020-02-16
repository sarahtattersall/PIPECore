package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.*;

import java.awt.Color;
import java.util.Set;

import org.junit.Test;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.ATimedTransition;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;

public class SimpleAnimationLogicTest {

    @Test
    public void returnsBothImmediateOfAllPrioritiesAndTimedTransitionsAtAllFiringTimes()
            throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P0").and(1, "Default").tokens()).and(ATimedTransition.withId("T0").andDelay(100))
                .and(AnImmediateTransition.withId("T1").andPriority(1))
                .and(AnImmediateTransition.withId("T2").andPriority(2))
                .and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token())
                .and(ANormalArc.withSource("P0").andTarget("T1").with("1", "Default").token())
                .andFinally(ANormalArc.withSource("P0").andTarget("T2").and("1", "Default").token());
        ExecutablePetriNet executablePetriNet = petriNet.getExecutablePetriNet();
        executablePetriNet.setCurrentTime(50);
        AnimationLogic animationLogic = new SimpleAnimationLogic(executablePetriNet);
        Set<Transition> transitions = animationLogic.getEnabledTransitions(executablePetriNet.getState());
        assertEquals("all transitions enabled, all returned, even lower priority immediate transitions," +
                " and even timed transitions, including those that could not fire at current time.", 3, transitions
                        .size());
    }

    @Test
    public void otherMethodsThrowUnsupportedOperationException() throws Exception {
        AnimationLogic animationLogic = new SimpleAnimationLogic(null);
        try {
            animationLogic.getFiredState(null);
            fail("should throw");
        } catch (UnsupportedOperationException e) {
            assertEquals("SimpleAnimationLogic does not support this method.  Use PetriNetAnimationLogic instead.", e
                    .getMessage());
        }

    }

}
