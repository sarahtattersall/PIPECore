package uk.ac.imperial.pipe.models.petrinet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;
import uk.ac.imperial.state.State;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class AbstractArcTest {

    private Place place = new DiscretePlace("P0");

    private Transition transition = new DiscreteTransition("P0");

    @Test
    public void startPoint() {
        place.setX(0);
        place.setY(0);
        transition.setX(50);
        transition.setY(0);
        DummyArc dummyArc = new DummyArc(place, transition);
        List<ArcPoint> arcpoints = dummyArc.getArcPoints();
        ArcPoint endPoint = arcpoints.get(0);
        Point2D expected = new Point2D.Double(Place.DIAMETER, Place.DIAMETER / 2);
        assertEquals(expected, endPoint.getPoint());
    }

    @Test
    public void endPoint() {
        place.setX(0);
        place.setY(0);
        transition.setX(50);
        transition.setY(0);
        DummyArc dummyArc = new DummyArc(place, transition);
        List<ArcPoint> arcpoints = dummyArc.getArcPoints();
        ArcPoint endPoint = arcpoints.get(arcpoints.size() - 1);
        Point2D expected = new Point2D.Double(50, Transition.TRANSITION_HEIGHT / 2);
        assertEquals(expected, endPoint.getPoint());
    }

    @Test
    public void endPoint2WithIntermediateBelowTransition() {
        place.setX(331);
        place.setY(82);
        transition.setX(582);
        transition.setY(82);
        DummyArc dummyArc = new DummyArc(place, transition);
        dummyArc.addIntermediatePoint(new ArcPoint(new Point2D.Double(transition.getCentre().getX(), 400), false));
        List<ArcPoint> arcpoints = dummyArc.getArcPoints();
        ArcPoint endPoint = arcpoints.get(arcpoints.size() - 1);
        Point2D expected = new Point2D.Double(582 + Transition.TRANSITION_WIDTH / 2, 82 + Transition.TRANSITION_HEIGHT);
        assertEquals(expected, endPoint.getPoint());
    }

    @Test
    public void movingIntermediatePointChangesSourceLocation() {
        place.setX(0);
        place.setY(100);
        transition.setX(50);
        transition.setY(100);
        DummyArc dummyArc = new DummyArc(place, transition);
        ArcPoint point = new ArcPoint(new Point2D.Double(place.getCentre().getX(), 0), false);
        dummyArc.addIntermediatePoint(point);
        List<ArcPoint> arcpoints = dummyArc.getArcPoints();
        ArcPoint endPoint = arcpoints.get(0);
        Point2D expected = new Point2D.Double(place.getCentre().getX(), place.getCentre().getY() - Place.DIAMETER / 2);
        assertEquals(expected, endPoint.getPoint());

        point.setY(200);
        arcpoints = dummyArc.getArcPoints();
        endPoint = arcpoints.get(0);
        expected = new Point2D.Double(place.getCentre().getX(), place.getCentre().getY() + Place.DIAMETER / 2);
        assertEquals(expected, endPoint.getPoint());
    }

    @Test
    public void movingIntermediatePointChangesTargetLocation() {
        place.setX(0);
        place.setY(100);
        transition.setX(50);
        transition.setY(100);
        DummyArc dummyArc = new DummyArc(place, transition);
        ArcPoint point = new ArcPoint(new Point2D.Double(transition.getCentre().getX(), 0), false);
        dummyArc.addIntermediatePoint(point);
        List<ArcPoint> arcpoints = dummyArc.getArcPoints();
        ArcPoint endPoint = arcpoints.get(arcpoints.size() - 1);
        Point2D expected = new Point2D.Double(transition.getCentre().getX(),
                transition.getCentre().getY() - Transition.TRANSITION_HEIGHT / 2);
        assertEquals(expected, endPoint.getPoint());

        point.setY(200);
        arcpoints = dummyArc.getArcPoints();
        endPoint = arcpoints.get(arcpoints.size() - 1);
        expected = new Point2D.Double(transition.getCentre().getX(),
                transition.getCentre().getY() + Transition.TRANSITION_HEIGHT / 2);
        assertEquals(expected, endPoint.getPoint());
    }

    @Test
    public void removeIntermediatePointEndPoint() {
        place.setX(331);
        place.setY(82);
        transition.setX(582);
        transition.setY(82);
        DummyArc dummyArc = new DummyArc(place, transition);
        ArcPoint point = new ArcPoint(new Point2D.Double(transition.getCentre().getX(), 400), false);
        dummyArc.addIntermediatePoint(point);
        dummyArc.removeIntermediatePoint(point);
        List<ArcPoint> arcpoints = dummyArc.getArcPoints();
        ArcPoint endPoint = arcpoints.get(arcpoints.size() - 1);
        Point2D expected = new Point2D.Double(582, 82 + Transition.TRANSITION_HEIGHT / 2);
        assertEquals(expected, endPoint.getPoint());
    }

    public class DummyArc extends AbstractArc<Place, Transition> {

        /**
         * Abstract arc constructor sets arc to <source id> TO <target id>
         *
         * @param source
         * @param target
         */
        public DummyArc(Place source, Transition target) {
            super(source, target, new HashMap<String, String>(), ArcType.NORMAL);
        }

        @Override
        public void accept(PetriNetComponentVisitor visitor) throws PetriNetComponentException {

        }

        @Override
        public boolean canFire(ExecutablePetriNet executablePetriNet,
                State state) {
            return false;
        }
    }

}