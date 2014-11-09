package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;

public class DiscretePlaceTest {

	
    @Rule
    public ExpectedException exception = ExpectedException.none();

    DiscretePlace place;

	private PetriNet net;

	private IncludeHierarchy includes;

    @Before
    public void setUp() {
        place = new DiscretePlace("test", "test");
    	net = new PetriNet(); 
    	net.addPlace(place);
    	includes = new IncludeHierarchy(net, "top"); 

    }

    @Test
    public void placeObjectIsSelectable() {
        assertTrue(place.isSelectable());
    }

    @Test
    public void calculatesCorrectArcAttachmentPointsDirectlyAbove() {
        int x1 = 0;
        int y1 = 0;

        place.setX(x1);
        place.setY(y1);

        Point2D.Double point = place.getArcEdgePoint(Math.toRadians(90));
        Point2D.Double expected = new Point2D.Double(15, 0);
        assertEquals(expected.x, point.x, 0.001);
        assertEquals(expected.y, point.y, 0.001);
    }

    @Test
    public void visitsDiscretePlaceVisitor() throws PetriNetComponentException {
        DiscretePlaceVisitor visitor = mock(DiscretePlaceVisitor.class);
        place.accept(visitor);
        verify(visitor).visit(place);
    }


    @Test
    public void visitsPlaceVisitor() throws PetriNetComponentException {
        PlaceVisitor visitor = mock(PlaceVisitor.class);
        place.accept(visitor);
        verify(visitor).visit(place);
    }


    @Test
    public void calculatesCorrectArcAttachmentPointsDirectlyBelow() {
        int x1 = 0;
        int y1 = 0;

        place.setX(x1);
        place.setY(y1);

        Point2D.Double point = place.getArcEdgePoint(Math.toRadians(-90));
        Point2D.Double expected = new Point2D.Double(15, 30);
        assertEquals(expected.x, point.x, 0.001);
        assertEquals(expected.y, point.y, 0.001);
    }

    private double getAngleBetweenObjects(double x1, double y1, double x2, double y2) {
        double deltax = x1 - x2;
        double deltay = y1 - y2;
        return Math.atan2(deltax, deltay);
    }

    @Test
    public void calculatesCorrectArcAttachmentPointsDirectlyRight() {
        int x1 = 0;
        int y1 = 0;

        place.setX(x1);
        place.setY(y1);

        Point2D.Double point = place.getArcEdgePoint(Math.toRadians(180));
        Point2D.Double expected = new Point2D.Double(30, 15);
        assertEquals(expected.x, point.x, 0.001);
        assertEquals(expected.y, point.y, 0.001);
    }

    @Test
    public void calculatesCorrectArcAttachmentPointsDirectlyLeft() {
        int x1 = 0;
        int y1 = 0;

        place.setX(x1);
        place.setY(y1);

        Point2D.Double point = place.getArcEdgePoint(0);
        Point2D.Double expected = new Point2D.Double(0, 15);
        assertEquals(expected.x, point.x, 0.001);
        assertEquals(expected.y, point.y, 0.001);
    }

    /**
     * I.e. A->B we're calculating B
     */
    @Test
    public void calculatesCorrectArcAttachmentPointsAsSource() {
        Point source = new Point(0, 0);
        Point target = new Point(30, 30);
        double angle = getAngleBetweenObjects(source.x, source.y, target.x, target.y);

        place.setX(source.x);
        place.setY(source.y);

        double FORTY_FIVE_RADIANS = Math.toRadians(45);
        Point2D.Double expected = new Point2D.Double(Math.sin(FORTY_FIVE_RADIANS) * place.getWidth() / 2 + 15,
                Math.cos(FORTY_FIVE_RADIANS) * place.getWidth() / 2 + 15);

        Point2D.Double point = place.getArcEdgePoint(angle);
        assertEquals(expected, point);
    }

    @Test
    public void calculatesCorrectArcAttachmentPointsAsTarget() {
        Point source = new Point(0, 0);
        Point target = new Point(30, 30);
        double angle = getAngleBetweenObjects(source.x, source.y, target.x, target.y);

        place.setX(target.x);
        place.setY(target.y);

        double FOURTY_FIVE_RADIANS = Math.toRadians(45);
        Point2D.Double expected = new Point2D.Double(45 - Math.sin(FOURTY_FIVE_RADIANS) * place.getWidth() / 2,
                45 - Math.cos(FOURTY_FIVE_RADIANS) * place.getWidth() / 2);

        Point2D.Double point = place.getArcEdgePoint(Math.PI + angle);
        assertEquals(expected, point);
    }

    @Test
    public void addNewTokenSetsCountToOne() {
        place.incrementTokenCount("red");
        assertEquals(1, place.getTokenCount("red"));
    }

    @Test
    public void addExistingTokenIncrementsCount() {
        place.incrementTokenCount("red");

        place.incrementTokenCount("red");
        assertEquals(2, place.getTokenCount("red"));
    }

    @Test
    public void decrementExistingTokenDecreasesCount() {
        place.incrementTokenCount("red");

        place.decrementTokenCount("red");
        assertEquals(0, place.getTokenCount("red"));
    }

    @Test
    public void tokenCountIsZeroIfPlaceDoesNotContainToken() {
        assertEquals(0, place.getTokenCount("red"));
    }

    @Test
    public void throwsErrorIfSetTokenCountGreaterThanCapacity() {
        exception.expect(RuntimeException.class);
        exception.expectMessage("Cannot set token count that exceeds the capacity");
        place.setCapacity(1);
        place.setTokenCount("red", 2);
    }

    @Test
    public void throwsErrorIfIncrementTokenCountGreaterThanCapacity() {
        exception.expect(RuntimeException.class);
        exception.expectMessage("Cannot set token count that exceeds the capacity");
        place.setCapacity(1);

        place.incrementTokenCount("red");
        place.incrementTokenCount("red");
    }

    @Test
    public void capacityZeroMeansNoRestriction() {
        int capacity = 0;
        place.setCapacity(capacity);

        place.incrementTokenCount("red");
    }

    @Test
    public void setTokenCountsCannotExceedCapacity() {
        exception.expect(RuntimeException.class);
        exception.expectMessage("Count of tokens exceeds capacity!");
        place.setCapacity(1);

        Map<String, Integer> tokenCounts = new HashMap<>();
        tokenCounts.put("red", 10);

        place.setTokenCounts(tokenCounts);
    }

    @Test
    public void changingNumberOfTokensDoesNotTriggerExceedCapacityError() {
        int capacity = 1;
        place.setCapacity(capacity);

        place.incrementTokenCount("red");

        place.setTokenCount("red", 1);
    }

    @Test
    public void correctlyCountsNumberOfTokensStored() {
        int capacity = 20;
        place.setCapacity(capacity);

        int redTokenCount = 3;
        place.setTokenCount("red", 3);

        int blueTokenCount = 10;
        place.setTokenCount("blue", blueTokenCount);

        assertEquals(redTokenCount + blueTokenCount, place.getNumberOfTokensStored());
    }

    @Test
    public void notifiesObserverOnTokenChange() {
        PropertyChangeListener mockListener = mock(PropertyChangeListener.class);

        place.addPropertyChangeListener(mockListener);

        place.setTokenCount("Default", 7);
        verify(mockListener).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void notifiesObserverOnTokenMapChange() {
        Map<String, Integer> tokenCounts = new HashMap<>();
        PropertyChangeListener mockListener = mock(PropertyChangeListener.class);
        tokenCounts.put("Default", 7);
        place.addPropertyChangeListener(mockListener);

        place.setTokenCounts(tokenCounts);
        verify(mockListener).propertyChange(any(PropertyChangeEvent.class));
    }
    @Test
	public void canMirrorTokenCountOfAnotherPlace() throws Exception {
    	Place mirror = new DiscretePlace("P99", "P99"); 
    	place.addPropertyChangeListener(mirror); 
    	place.setTokenCount("Default", 3); 
    	assertEquals(3, mirror.getTokenCount("Default")); 
	}
    
    // Hier.addToInterface(place)
    //   place.setIsInInterface
    @Test
	public void placeStatusChangesOnceInInterface() throws Exception {
    	assertTrue(place.getStatus() instanceof PlaceStatusNormal); 
    	place.setInInterface(true); 
    	assertTrue(place.getStatus() instanceof PlaceStatusInterface);
    	place.setInInterface(false); 
    	assertTrue(place.getStatus() instanceof PlaceStatusNormal); 
    	
    	
	}
    @Test
	public void copyConstructorIncludesStatus() throws Exception {
    	//FIXME set in interface implies an include hierarchy, but we're not giving one. 
    	place.addToInterface(includes); 
    	place.getStatus().setMergeStatus(true); 
    	place.getStatus().setExternalStatus(true); 
    	place.getStatus().setOutputOnlyStatus(true); 
    	place.getStatus().setInputOnlyStatus(false); 
    	DiscretePlace newPlace = new DiscretePlace(place); 
    	assertTrue(newPlace.getStatus().isMergeStatus());
    	assertTrue(newPlace.getStatus().isExternalStatus());
    	assertTrue(newPlace.getStatus().isOutputOnlyStatus());
    	assertFalse(newPlace.getStatus().isInputOnlyStatus());
	}
    @Test
	public void interfaceRequestsThrowUnsupportedOperationExceptionIfNotInInterface() throws Exception {
    	try {
    		place.getStatus().setMergeStatus(true); 
    		fail("should throw because PlaceStatusNormal means not in the interface"); 
    	} catch (UnsupportedOperationException e) {
    		assertEquals("PlaceStatusNormal:  setMergeStatus not a valid request for place test until Place.addToInterface(IncludeHierarchy) has been requested", e.getMessage());
    	}
    	try {
    		place.getStatus().setExternalStatus(true); 
    		fail("should throw because PlaceStatusNormal means not in the interface"); 
    	} catch (UnsupportedOperationException e) {}
    	try {
    		place.getStatus().setInputOnlyStatus(true); 
    		fail("should throw because PlaceStatusNormal means not in the interface"); 
    	} catch (UnsupportedOperationException e) {}
    	try {
    		place.getStatus().setOutputOnlyStatus(true); 
    		fail("should throw because PlaceStatusNormal means not in the interface"); 
    	} catch (UnsupportedOperationException e) {}
	}
    @Test
	public void addToInterfaceCreatesPlaceStatusInterface() throws Exception {
    	assertTrue(place.getStatus() instanceof PlaceStatusNormal); 
    	place.addToInterface(includes); 
    	assertTrue(place.getStatus() instanceof PlaceStatusInterface);
//    	assertTrue(place.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatus); 
	}
}

