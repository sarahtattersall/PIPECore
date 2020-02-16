package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

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
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;

@RunWith(MockitoJUnitRunner.class)
public class DiscreteTransitionTest {

    private ExecutablePetriNet executablePetriNet;
    private Transition transition;

    @Mock
    private PropertyChangeListener mockListener;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        transition = new DiscreteTransition("id", "name");
        transition.addPropertyChangeListener(mockListener);
    }

    @Test
    public void calculatesCorrectArcConnectionForTransitionAbove() {
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
        Point2D.Double expected = new Point2D.Double(targetX + transition.getWidth() / 2,
                targetY + transition.getHeight());
        assertEquals(expected, point);
    }

    @Test
    public void calculatesCorrectArcConnectionForTransitionLeftRotated() {
        Transition transition = new DiscreteTransition("id", "name");
        transition.setAngle(90);

        int sourceX = 0;
        int sourceY = 0;
        int targetX = 50;
        int targetY = 0;
        double angle = getAngleBetweenObjects(sourceX, sourceY, targetX, targetY);

        transition.setX(targetX);
        transition.setY(targetY);

        Point2D.Double point = transition.getArcEdgePoint(angle);
        Point2D.Double expected = new Point2D.Double(targetX + transition.getWidth() / 2 - transition.getHeight() / 2,
                targetY + transition.getHeight() / 2);
        assertEquals(expected, point);
    }

    private double getAngleBetweenObjects(double x1, double y1, double x2, double y2) {
        double deltax = x2 - x1;
        double deltay = y2 - y1;
        return Math.atan2(deltay, deltax);
    }

    @Test
    public void calculatesCorrectArcConnectionPointForTransitionRight() {
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
        Point2D.Double expected = new Point2D.Double(targetX + transition.getWidth(),
                targetY + transition.getHeight() / 2);
        assertEquals(expected, point);
    }

    @Test
    public void calculatesCorrectArcConnectionPointForTopRotated180() {
        transition.setAngle(180);

        int x1 = 100;
        int y1 = 100;
        int x2 = 100;
        int y2 = 200;
        double angle = getAngleBetweenObjects(x2, y2, x1, y1);

        transition.setX(x1);
        transition.setY(y1);

        Point2D.Double point = transition.getArcEdgePoint(angle);
        Point2D.Double expected = new Point2D.Double(105, 130);
        assertEquals(expected, point);
    }

    @Test
    public void calculatesCorrectArcConnectionPointForTransitionBelowRotated90() {
        transition.setAngle(90);

        int sourceX = 0;
        int sourceY = 0;
        int targetX = 0;
        int targetY = 50;
        double angle = getAngleBetweenObjects(sourceX, sourceY, targetX, targetY);

        transition.setX(targetX);
        transition.setY(targetY);

        Point2D.Double point = transition.getArcEdgePoint(angle);
        Point2D.Double expected = new Point2D.Double(5, 60);
        assertEquals(expected, point);
    }

    @Test
    public void infiniteServerRateMultipliesByEnablingDegreeNonFunctionalArc()
            throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P0").and(5, "Default").tokens()).and(APlace.withId("P1").and(2, "Default").tokens())
                .and(ATimedTransition.withId("T0").andIsAnInfinite().server().andRate("4"))
                .and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token())
                .andFinally(ANormalArc.withSource("P1").andTarget("T0").and("1", "Default").token());
        executablePetriNet = petriNet.getExecutablePetriNet();

        Transition t0 = executablePetriNet.getComponent("T0", Transition.class);

        double actualRate = t0.getActualRate(executablePetriNet);
        int expectedEnablingDegree = 2;
        int expectedISRate = expectedEnablingDegree * 4;
        assertEquals(expectedISRate, actualRate, 0.0001);
    }

    @Test
    public void infiniteServerRateMultipliesByEnablingDegreeFunctionalArcs()
            throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P0").and(5, "Default").tokens()).and(APlace.withId("P1").and(2, "Default").tokens())
                .and(ATimedTransition.withId("T0").andIsAnInfinite().server().andRate("4"))
                .and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token())
                .andFinally(ANormalArc.withSource("P1").andTarget("T0").and("#(P1)", "Default").token());
        executablePetriNet = petriNet.getExecutablePetriNet();

        Transition t0 = executablePetriNet.getComponent("T0", Transition.class);
        double actualRate = t0.getActualRate(executablePetriNet);
        int expectedEnablingDegree = 1;
        int expectedISRate = expectedEnablingDegree * 4;
        assertEquals(expectedISRate, actualRate, 0.0001);
    }

    @Test
    public void actualRateSingleServer()
            throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P0").and(5, "Default").tokens()).and(APlace.withId("P1").and(2, "Default").tokens())
                .and(ATimedTransition.withId("T0").andIsASingle().server().andRate("4"))
                .and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token())
                .andFinally(ANormalArc.withSource("P1").andTarget("T0").and("1", "Default").token());
        executablePetriNet = petriNet.getExecutablePetriNet();
        Transition t0 = executablePetriNet.getComponent("T0", Transition.class);
        double actualRate = t0.getActualRate(executablePetriNet);
        assertEquals(4, actualRate, 0.0001);
    }

    @Test
    public void visitDiscreteTransitionVisitor() {
        DiscreteTransition transition = new DiscreteTransition("id", "name");
        DiscreteTransitionVisitor visitor = mock(DiscreteTransitionVisitor.class);
        transition.accept(visitor);
        verify(visitor).visit(transition);
    }

    @Test
    public void visitTransitionVisitor() {
        DiscreteTransition transition = new DiscreteTransition("id", "name");
        TransitionVisitor visitor = mock(TransitionVisitor.class);
        transition.accept(visitor);
        verify(visitor).visit(transition);
    }

    @Test
    public void transitionsEqualEvenIfEnabledDifferent() {
        DiscreteTransition t1 = new DiscreteTransition("id", "name");
        DiscreteTransition t2 = new DiscreteTransition("id", "name");
        t2.enable();
        t1.disable();
        assertEquals(t1, t2);
    }

    @Test
    public void transitionNotEqualsIfTimedDiffers() throws Exception {
        DiscreteTransition t1 = new DiscreteTransition("id", "name");
        DiscreteTransition t2 = new DiscreteTransition("id", "name");
        t1.setTimed(true);
        t2.setTimed(false);
        assertNotEquals(t1, t2);
    }

    @Test
    public void transitionNotEqualsIfDelaysDiffer() throws Exception {
        DiscreteTransition t1 = new DiscreteTransition("id", "name");
        DiscreteTransition t2 = new DiscreteTransition("id", "name");
        t1.setTimed(true);
        t2.setTimed(true);
        t1.setDelay(1000);
        t2.setDelay(0);
        assertNotEquals(t1, t2);
    }

    @Test
    public void evaluatesRateAgainstPetriNet() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P0").and(5, "Default").tokens())
                .and(ATimedTransition.withId("T0").andIsASingle().server().andRate("#(P0)"))
                .andFinally(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token());
        executablePetriNet = petriNet.getExecutablePetriNet();
        Transition transition = executablePetriNet.getComponent("T0", Transition.class);
        double rate = transition.getActualRate(executablePetriNet);
        assertEquals(5, rate, 0.0001);
    }

    @Test
    public void delayCanBeSet() throws Exception {
        transition.setTimed(true);
        assertEquals("default", 0, transition.getDelay());
        transition.setDelay(1000);
        assertEquals(1000, transition.getDelay());
    }

    @Test
    public void throwsIfDelaySetForNonTimedTransition() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException
                .expectMessage("AbstractTransition.setDelay:  delay cannot be set if Transition is not timed.");
        transition.setDelay(1000);
    }

    @Test
    public void notifiesObserverOnIdChange() {
        transition.setId("T99");
        verify(mockListener).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void notifiesObserverOnPriorityChange() {
        transition.setPriority(4);
        verify(mockListener).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void notifiesObserverOnRateChange() {
        transition.setRate(new NormalRate("2"));
        verify(mockListener).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void notifiesObserverOnTimedChange() {
        transition.setTimed(false); // no change from default, so doesn't fire a change event
        transition.setTimed(true);
        verify(mockListener).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void notifiesObserverOnInfiniteChange() {
        transition.setInfiniteServer(false); // no change from default, so doesn't fire a change event
        transition.setInfiniteServer(true);
        verify(mockListener).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void notifiesObserverOnDelayChange() {
        transition.setTimed(true);
        transition.setDelay(10);
        verify(mockListener, times(2)).propertyChange(any(PropertyChangeEvent.class));
    }

}
