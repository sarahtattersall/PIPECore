package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.assertEquals;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.junit.Test;

public class AbstractPetriNetPubSubTest {

    @Test
    public void verifiesListenersAreCleanedUp() {
        TestingAbstractPetriNetPubSub pubSub = new TestingAbstractPetriNetPubSub();
        assertEquals(0, pubSub.changeSupport.getPropertyChangeListeners().length);
        pubSub.addPropertyChangeListener(new TestingListener());
        pubSub.addPropertyChangeListener("fred", new TestingListener());
        assertEquals(2, pubSub.changeSupport.getPropertyChangeListeners().length);
        pubSub.removeAllListeners();
        assertEquals("all listeners removed, both for specific properties and for any property", 0, pubSub.changeSupport
                .getPropertyChangeListeners().length);
    }

    private class TestingAbstractPetriNetPubSub extends AbstractPetriNetPubSub {

    }

    private class TestingListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
        }

    }
}
