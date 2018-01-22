package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)

public class AbstractConnectableTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private AbstractConnectable connectable;
    private Connectable cloned;
    private static final double DOUBLE_DELTA = 0.001;

    @Before
    public void setUp() {
        connectable = new TestingConnectable("A", "Aname");
    }

    @Test
    public void notifiesObserversOnXChange() {
        PropertyChangeListener mockListener = mock(PropertyChangeListener.class);
        connectable.addPropertyChangeListener(mockListener);
        connectable.setX(10);
        verify(mockListener).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void defaultNameOffsetValues() {
        assertEquals(-5, connectable.getNameXOffset(), DOUBLE_DELTA);
        assertEquals(35, connectable.getNameYOffset(), DOUBLE_DELTA);
    }

    @Test
    public void notifiesObserversOnNameChange() {
        PropertyChangeListener mockListener = mock(PropertyChangeListener.class);
        connectable.addPropertyChangeListener(mockListener);
        connectable.setName("");
        verify(mockListener).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void notifiesObserversOnIdChange() {
        PropertyChangeListener mockListener = mock(PropertyChangeListener.class);
        connectable.addPropertyChangeListener(mockListener);
        connectable.setId("");
        verify(mockListener).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void copyConstructorCreatesCopy() {
        cloned = new TestingConnectable(connectable);
        assertTrue(cloned.equals(connectable));
    }

    @Test
    public void equalsChecksNameIdAndPosition() {
        Connectable connectable2 = new TestingConnectable("A", "Aname");
        assertEquals(connectable, connectable2);
        assertTrue(connectable.hashCode() == connectable2.hashCode());
        connectable2 = new TestingConnectable("B", "Aname");
        assertNotEquals(connectable, connectable2);
        connectable2 = new TestingConnectable("A", "Bname");
        assertNotEquals(connectable, connectable2);

        connectable2 = new TestingConnectable("A", "Aname");
        connectable2.setX(3);
        assertNotEquals(connectable, connectable2);
        assertFalse(connectable.hashCode() == connectable2.hashCode());
    }

    @Test
    public void equalsStructureChecksNameAndIdButIgnoresPosition() {
        Connectable connectable2 = new TestingConnectable("A", "Aname");
        assertTrue(connectable.equalsStructure(connectable2));
        connectable2 = new TestingConnectable("B", "Aname");
        assertFalse(connectable.equalsStructure(connectable2));
        connectable2 = new TestingConnectable("A", "Bname");
        assertFalse(connectable.equalsStructure(connectable2));

        connectable2 = new TestingConnectable("A", "Aname");
        connectable2.setX(3);
        assertTrue(connectable.equalsStructure(connectable2));
    }

    @Test
    public void equalsPositionChecksXandYandNameOffsets() {
        Connectable connectable2 = new TestingConnectable("B", "Bname");
        assertTrue(connectable.equalsPosition(connectable2));
        connectable2 = new TestingConnectable("B", "Bname");
        connectable2.setX(2);
        assertFalse(connectable.equalsPosition(connectable2));
        connectable2 = new TestingConnectable("B", "Bname");
        connectable2.setY(3);
        assertFalse(connectable.equalsPosition(connectable2));
        connectable2 = new TestingConnectable("B", "Bname");
        connectable2.setNameXOffset(1);
        assertFalse(connectable.equalsPosition(connectable2));
        connectable2 = new TestingConnectable("B", "Bname");
        connectable2.setNameYOffset(1);
        assertFalse(connectable.equalsPosition(connectable2));
    }

    @Test
    public void equalsStructureAndPositionVerifyMinimalConditions() {
        assertFalse(connectable.equalsStructure(null));
        assertFalse(connectable.equalsStructure(new DiscretePlace("P0")));
        assertFalse(connectable.equalsPosition(null));
        assertFalse(connectable.equalsPosition(new DiscretePlace("P0")));
    }
}
