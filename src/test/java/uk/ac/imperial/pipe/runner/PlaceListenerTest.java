package uk.ac.imperial.pipe.runner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.beans.PropertyChangeEvent;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.imperial.pipe.models.petrinet.DiscretePlace;
import uk.ac.imperial.pipe.models.petrinet.Place;

@RunWith(MockitoJUnitRunner.class)
public class PlaceListenerTest {

    private PlaceListener placeListener;
    private PropertyChangeEvent event;

    @Mock
    private PetriNetRunner mockRunner;
    private boolean acknowledgement;

    @Test
    public void listenerGetsEvent() {
        placeListener = new PlaceListener("P1");
        Place place = new DiscretePlace("P1");
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
        placeListener = new PlaceListener("P1");
        event = new PropertyChangeEvent("source", "aName", 0, 1);
        placeListener.propertyChange(event);
        assertNull(placeListener.getEventForTesting());
    }

    @Test
    public void getCounts() throws Exception {
        placeListener = new PlaceListener("P1");
        Place place = new DiscretePlace("P1");
        place.setTokenCount("Default", 0);
        place.addPropertyChangeListener(placeListener);
        assertNull(placeListener.getCounts());
        place.setTokenCount("Default", 1);
        Map<String, Integer> counts = placeListener.getCounts();
        assertEquals(new Integer(1), counts.get("Default"));
    }

    @Test
    public void willAcknowledgeAllNontokenEventsIfRequested() throws Exception {
        acknowledgement = true;
        placeListener = new PlaceListener("P1", mockRunner, acknowledgement);
        event = new PropertyChangeEvent("source", "aName", 0, 1);
        placeListener.propertyChange(event);
        verify(mockRunner).acknowledge();
    }

    @Test
    public void doesntAcknowledgeNontokenEventsIfNotRequested() throws Exception {
        acknowledgement = false;
        placeListener = new PlaceListener("P1", mockRunner, acknowledgement);
        event = new PropertyChangeEvent("source", "aName", 0, 1);
        placeListener.propertyChange(event);
        verify(mockRunner, never()).acknowledge();
    }

}
