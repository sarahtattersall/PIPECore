package uk.ac.imperial.pipe.runner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.imperial.pipe.models.petrinet.DiscretePlace;
import uk.ac.imperial.pipe.models.petrinet.Place;

@RunWith(MockitoJUnitRunner.class)
public class BooleanPlaceListenerTest implements PropertyChangeListener {

    private BooleanPlaceListener booleanListener;
    private boolean eventFired;
    private Place place;

    @Mock
    private PetriNetRunner mockRunner;
    private boolean acknowledgement;
    private boolean checkFalseNow;

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

    @Test
    public void acknowledgementForZeroTokenEventsBecauseNotPresentedToClient() {
        setUpAcknowledgingListener();
        place.setTokenCount("Default", 0);
        verify(mockRunner).acknowledge("P1");
    }

    @Test
    public void eventGeneratedWhenGoingFromNonZeroToZeroIfRequested() {
        setUpBothEventsPossiblyAcknowledgingListener(false);
        checkFalseNow = false;
        place.setTokenCount("Default", 1);
        assertTrue(eventFired);
        eventFired = false;
        checkFalseNow = true;
        place.setTokenCount("Default", 0);
        assertTrue("going from 1 to 0 tokens fires event when bothEvents requested", eventFired);
    }

    @Test
    public void acknowledgementForInitialZeroTokenEventsEvenWhenBothEventsBecauseNotPresentedToClient() {
        setUpBothEventsPossiblyAcknowledgingListener(true);
        checkFalseNow = true;
        place.setTokenCount("Default", 0); // didn't go from nonzero to zero (instead, null to zero)
        verify(mockRunner).acknowledge("P1");
    }

    @Test
    public void noAcknowledgementForNonZeroTokenEventsBecausePresentedToClient() {
        setUpAcknowledgingListener();
        place.setTokenCount("Default", 1);
        verify(mockRunner, never()).acknowledge(any(String.class));
    }

    @Test
    public void noAcknowledgementForNonZeroOrZeroTokenEventsBecauseBothEventsPresentedToClient() {
        setUpBothEventsPossiblyAcknowledgingListener(true);
        checkFalseNow = false;
        place.setTokenCount("Default", 1);
        checkFalseNow = true;
        place.setTokenCount("Default", 0);
        verify(mockRunner, never()).acknowledge(any(String.class));
    }

    @Test
    public void acknowledgementForNonZeroToNonZeroTokenEventsEvenWhenBothEvents() {
        setUpBothEventsPossiblyAcknowledgingListener(true);
        checkFalseNow = false;
        place.setTokenCount("Default", 1);
        checkFalseNow = false;
        place.setTokenCount("Default", 2);
        verify(mockRunner).acknowledge("P1");
    }

    public void setUpAcknowledgingListener() {
        acknowledgement = true;
        booleanListener = new BooleanPlaceListener("P1", mockRunner, acknowledgement);
        place = new DiscretePlace("P1");
        place.addPropertyChangeListener(booleanListener);
        booleanListener.changeSupport.addPropertyChangeListener(this);
    }

    public void setUpBothEventsPossiblyAcknowledgingListener(boolean acknowledgement) {
        boolean bothEvents = true;
        booleanListener = new BooleanPlaceListener("P1", mockRunner, acknowledgement, bothEvents);
        place = new DiscretePlace("P1");
        place.addPropertyChangeListener(booleanListener);
        booleanListener.changeSupport.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        eventFired = true;
        BooleanPlaceListener listener = (BooleanPlaceListener) evt.getSource();
        if (listener.isBothEvents() && checkFalseNow) {
            checkFalseEvent(evt);
        } else {
            checkTrueEvent(evt);
        }
    }

    private void checkFalseEvent(PropertyChangeEvent evt) {
        assertEquals(BooleanPlaceListener.PLACE_FALSE, evt.getPropertyName());
        assertEquals(false, evt.getNewValue());
        assertEquals(true, evt.getOldValue());
        assertEquals("P1", ((PlaceTokensListener) evt.getSource()).getPlaceId());
    }

    private void checkTrueEvent(PropertyChangeEvent evt) {
        assertEquals(BooleanPlaceListener.PLACE_TRUE, evt.getPropertyName());
        assertEquals(true, evt.getNewValue());
        assertEquals(false, evt.getOldValue());
        assertEquals("P1", ((PlaceTokensListener) evt.getSource()).getPlaceId());
    }

}
