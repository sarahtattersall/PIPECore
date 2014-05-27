package uk.ac.imperial.pipe.models;

import org.junit.Test;
import uk.ac.imperial.pipe.animation.AnimationUtils;
import uk.ac.imperial.pipe.dsl.*;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.*;
import uk.ac.imperial.state.State;

import java.awt.Color;
import java.awt.geom.Point2D;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DiscreteTransitionTest {


    @Test
    public void calculatesCorrectArcConnectionForTransitionAbove() {
        Transition transition = new DiscreteTransition("id", "name");
        // No rotation
        transition.setAngle(0);

        int sourceX = 0;
        int sourceY = 50;
        int targetX = 0;
        int targetY = 0;
        double angle = getAngleBetweenObjects(sourceX, sourceY, targetX, targetY);

        transition.setX(targetX);
        transition.setY(targetY);

        assertEquals(-90, Math.toDegrees(angle), 0.001);
        Point2D.Double point = transition.getArcEdgePoint(angle);
        Point2D.Double expected =
                new Point2D.Double(targetX + transition.getWidth() / 2, targetY + transition.getHeight());
        assertEquals(expected, point);
    }

    private double getAngleBetweenObjects(double x1, double y1, double x2, double y2) {
        double deltax = x2 - x1;
        double deltay = y2 - y1;
        return Math.atan2(deltay, deltax);
    }

    @Test
    public void calculatesCorrectArcConnectionPointForTransitionRight() {
        Transition transition = new DiscreteTransition("id", "name");
        // No rotation
        transition.setAngle(0);

        int sourceX = 0;
        int sourceY = 0;
        int targetX = 50;
        int targetY = 0;
        double angle = getAngleBetweenObjects(sourceX, sourceY, targetX, targetY);

        transition.setX(targetX);
        transition.setY(targetY);

        Point2D.Double point = transition.getArcEdgePoint(angle);
        Point2D.Double expected = new Point2D.Double(targetX, targetY + transition.getHeight() / 2);
        assertEquals(expected, point);
    }

    @Test
    public void calculatesCorrectArcConnectionPointForTargetOnLeft() {
        Transition transition = new DiscreteTransition("id", "name");
        // No rotation
        transition.setAngle(0);

        int sourceX = 50;
        int sourceY = 0;
        int targetX = 0;
        int targetY = 0;
        double angle = getAngleBetweenObjects(sourceX, sourceY, targetX, targetY);

        transition.setX(targetX);
        transition.setY(targetY);

        Point2D.Double point = transition.getArcEdgePoint(angle);
        Point2D.Double expected =
                new Point2D.Double(targetX + transition.getWidth(), targetY + transition.getHeight() / 2);
        assertEquals(expected, point);
    }

    @Test
    public void calculatesCorrectArcConnectionPointForBottomRotated180() {
        Transition transition = new DiscreteTransition("id", "name");
        transition.setAngle(180);

        int x1 = 100;
        int y1 = 100;
        int x2 = 100;
        int y2 = 200;
        double angle = getAngleBetweenObjects(x1, y1, x2, y2);

        transition.setX(x1);
        transition.setY(y1);

        Point2D.Double point = transition.getArcEdgePoint(angle);
        Point2D.Double expected = new Point2D.Double(100, 115);
        assertEquals(expected, point);
    }

    @Test
    public void calculatesCorrectArcConnectionPointForTransitionBelowRotated90() {
        Transition transition = new DiscreteTransition("id", "name");
        transition.setAngle(90);

        int sourceX = 0;
        int sourceY = 0;
        int targetX = 0;
        int targetY = 50;
        double angle = getAngleBetweenObjects(sourceX, sourceY, targetX, targetY);

        transition.setX(targetX);
        transition.setY(targetY);

        Point2D.Double point = transition.getArcEdgePoint(angle);
        Point2D.Double expected = new Point2D.Double(5, 70);
        assertEquals(expected, point);
    }

    @Test
    public void infiniteServerRateMultipliesByEnablingDegreeNonFunctionalArc()
            throws PetriNetComponentNotFoundException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
                APlace.withId("P0").and(5, "Default").tokens()).and(APlace.withId("P1").and(2, "Default").tokens()).and(
                ATransition.withId("T0").whichIsTimed().andIsAnInfinite().server().andRate("4")).and(
                ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token()).andFinally(
                ANormalArc.withSource("P1").andTarget("T0").and("1", "Default").token());
        State state = AnimationUtils.getState(petriNet);

        Transition t0 = petriNet.getComponent("T0", Transition.class);
        double actualRate = t0.getActualRate(petriNet, state);
        int expectedEnablingDegree = 2;
        int expectedISRate = expectedEnablingDegree * 4;
        assertEquals(expectedISRate, actualRate, 0.0001);
    }

    @Test
    public void infiniteServerRateMultipliesByEnablingDegreeFunctionalArcs()
            throws PetriNetComponentNotFoundException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
                APlace.withId("P0").and(5, "Default").tokens()).and(APlace.withId("P1").and(2, "Default").tokens()).and(
                ATransition.withId("T0").whichIsTimed().andIsAnInfinite().server().andRate("4")).and(
                ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token()).andFinally(
                ANormalArc.withSource("P1").andTarget("T0").and("#(P1)", "Default").token());
        State state = AnimationUtils.getState(petriNet);

        Transition t0 = petriNet.getComponent("T0", Transition.class);
        double actualRate = t0.getActualRate(petriNet, state);
        int expectedEnablingDegree = 1;
        int expectedISRate = expectedEnablingDegree * 4;
        assertEquals(expectedISRate, actualRate, 0.0001);
    }


    @Test
    public void actualRateSingleServer()
            throws PetriNetComponentNotFoundException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
                APlace.withId("P0").and(5, "Default").tokens()).and(APlace.withId("P1").and(2, "Default").tokens()).and(
                ATransition.withId("T0").whichIsTimed().andIsASingle().server().andRate("4")).and(
                ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token()).andFinally(
                ANormalArc.withSource("P1").andTarget("T0").and("1)", "Default").token());
        State state = AnimationUtils.getState(petriNet);

        Transition t0 = petriNet.getComponent("T0", Transition.class);
        double actualRate = t0.getActualRate(petriNet, state);
        assertEquals(4, actualRate, 0.0001);
    }

    @Test
    public void visitDiscreteTransitionVisitor(){
        DiscreteTransition transition = new DiscreteTransition("id", "name");
        DiscreteTransitionVisitor visitor = mock(DiscreteTransitionVisitor.class);
        transition.accept(visitor);
        verify(visitor).visit(transition);
    }


    @Test
    public void visitTransitionVisitor(){
        DiscreteTransition transition = new DiscreteTransition("id", "name");
        TransitionVisitor visitor = mock(TransitionVisitor.class);
        transition.accept(visitor);
        verify(visitor).visit(transition);
    }

    @Test
    public void evaluatesRateAgainstPetriNet() throws PetriNetComponentNotFoundException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
                APlace.withId("P0").and(5, "Default").tokens()).and(
                ATransition.withId("T0").whichIsTimed().andIsASingle().server().andRate("#(P0)")).andFinally(
                ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token());
        Transition transition = petriNet.getComponent("T0", Transition.class);
        double rate = transition.getActualRate(petriNet, AnimationUtils.getState(petriNet));
        assertEquals(5, rate, 0.0001);
    }
}
