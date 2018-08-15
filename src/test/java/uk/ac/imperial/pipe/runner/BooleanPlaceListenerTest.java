package uk.ac.imperial.pipe.runner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.pipe.models.petrinet.DiscretePlace;
import uk.ac.imperial.pipe.models.petrinet.Place;

public class BooleanPlaceListenerTest implements PropertyChangeListener {

    private BooleanPlaceListener booleanListener;
    private boolean eventFired;
    private Place place;

    @Before
    public void setUp() {
        eventFired = false;
        booleanListener = new BooleanPlaceListener("P1");
        place = new DiscretePlace("P1");
        place.addPropertyChangeListener(booleanListener);
        booleanListener.changeSupport.addPropertyChangeListener(this);
    }

    @Test
    public void noEventgeneratedWhenCountsAreZero() {
        place.setTokenCount("Default", 0);
        assertFalse(eventFired);
    }

    @Test
    public void generatesNewEventWhenReceivesEventAndCountsGoFromEmptyToNonZero() {
        assertEquals(0, place.getTokenCounts().size());
        place.setTokenCount("Default", 1);
        assertTrue(eventFired);
    }

    @Test
    public void generatesNewEventWhenReceivesEventAndCountsGoFromZeroToNonZero() {
        place.setTokenCount("Default", 0);
        assertFalse(eventFired);
        place.setTokenCount("Default", 1);
        assertTrue(eventFired);
    }

    @Test
    public void eventGeneratedForOtherColors() {
        place.setTokenCount("Default", 0);
        assertFalse(eventFired);
        place.setTokenCount("red", 0);
        assertFalse(eventFired);
        place.setTokenCount("red", 1);
        assertTrue(eventFired);
        PropertyChangeEvent event = booleanListener.getEventForTesting();
        Map<String, Integer> newCounts = (Map<String, Integer>) event.getNewValue();
        assertEquals(new Integer(1), newCounts.get("red"));
    }

    @Test
    public void noEventGeneratedWhenGoingFromNonZeroToNonZero() {
        place.setTokenCount("Default", 1);
        assertTrue(eventFired);
        eventFired = false;
        place.setTokenCount("Default", 2);
        assertFalse("going from 1 to 2 tokens doesn't fire event", eventFired);
    }

    @Test
    public void noEventGeneratedWhenGoingFromNonZeroToZero() {
        place.setTokenCount("Default", 1);
        assertTrue(eventFired);
        eventFired = false;
        place.setTokenCount("Default", 0);
        assertFalse("going from 1 to 0 tokens doesn't fire event", eventFired);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        eventFired = true;
        assertEquals(BooleanPlaceListener.PLACE_TRUE, evt.getPropertyName());
        assertEquals(true, evt.getNewValue());
        assertEquals(false, evt.getOldValue());
        assertEquals("P1", ((PlaceListener) evt.getSource()).getPlaceId());
    }

}
