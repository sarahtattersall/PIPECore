package uk.ac.imperial.pipe.runner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.beans.PropertyChangeEvent;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.imperial.pipe.models.petrinet.DiscretePlace;
import uk.ac.imperial.pipe.models.petrinet.Place;

@RunWith(MockitoJUnitRunner.class)
public class PlaceTokensListenerTest {

    private PlaceTokensListener placeListener;
    private PropertyChangeEvent event;
    private boolean acknowledgement;
    private Place place;

    @Mock
    private PetriNetRunner mockRunner;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        place = new DiscretePlace("P1");
    }

    @Test
    public void listenerGetsEvent() {
        placeListener = new PlaceTokensListener("P1");
        place.setTokenCount("Default", 0);
        place.addPropertyChangeListener(placeListener);
        place.setTokenCount("Default", 1);
        event = placeListener.getEventForTesting();
        assertEquals(Place.TOKEN_CHANGE_MESSAGE, event.getPropertyName());
        assertEquals(place, event.getSource());
        Map<String, Integer> oldCounts = (Map<String, Integer>) event.getOldValue();
        Map<String, Integer> newCounts = (Map<String, Integer>) event.getNewValue();
        assertEquals(new Integer(0), oldCounts.get("Default"));
        assertEquals(new Integer(1), newCounts.get("Default"));
    }

    @Test
    public void listenerIgnoresNonTokenChanges() {
        placeListener = new PlaceTokensListener("P1");
        event = new PropertyChangeEvent(place, "aName", 0, 1);
        placeListener.propertyChange(event);
        assertNull(placeListener.getEventForTesting());
    }

    @Test
    public void getCounts() throws Exception {
        placeListener = new PlaceTokensListener("P1");
        Place place = new DiscretePlace("P1");
        place.setTokenCount("Default", 0);
        place.addPropertyChangeListener(placeListener);
        assertNull(placeListener.getCounts());
        place.setTokenCount("Default", 1);
        Map<String, Integer> counts = placeListener.getCounts();
        assertEquals(new Integer(1), counts.get("Default"));
    }

    @Test
    public void acknowledgesEventForTokensMessageForGivenPlaceIfRequested() throws Exception {
        acknowledgement = true;
        placeListener = new PlaceTokensListener("P1", mockRunner, acknowledgement);
        event = new PropertyChangeEvent(place, Place.TOKEN_CHANGE_MESSAGE, null, null);
        placeListener.propertyChange(event);
        verify(mockRunner).acknowledge("P1");
    }

    @Test
    public void doesntAcknowledgeTokensEventForGivenPlaceIfNotRequested() throws Exception {
        acknowledgement = false;
        placeListener = new PlaceTokensListener("P1", mockRunner, acknowledgement);
        event = new PropertyChangeEvent(place, Place.TOKEN_CHANGE_MESSAGE, null, null);
        placeListener.propertyChange(event);
        verify(mockRunner, never()).acknowledge("P1");
    }

    @Test
    public void doesntAcknowledgeNontokenEvents() throws Exception {
        acknowledgement = true;
        placeListener = new PlaceTokensListener("P1", mockRunner, acknowledgement);
        event = new PropertyChangeEvent(place, "anotherEvent", null, null);
        placeListener.propertyChange(event);
        verify(mockRunner, never()).acknowledge(any(String.class));
    }

    @Test
    public void throwsIfEventReceivedForDifferentPlace() throws Exception {
        expectedException
                .expectMessage("Logic error:  expected event for place P1, but received event for anotherPlaceId");
        placeListener = new PlaceTokensListener("P1");
        event = new PropertyChangeEvent(new DiscretePlace("anotherPlaceId"), Place.TOKEN_CHANGE_MESSAGE, null, null);
        placeListener.propertyChange(event);
    }

    @Test
    public void throwsIfEventSourceIsNotAPlace() throws Exception {
        expectedException
                .expectMessage("Logic error:  expected event for a place, but received event for another type");
        placeListener = new PlaceTokensListener("P1");
        event = new PropertyChangeEvent(new Integer(1), Place.TOKEN_CHANGE_MESSAGE, null, null);
        placeListener.propertyChange(event);
    }

}
