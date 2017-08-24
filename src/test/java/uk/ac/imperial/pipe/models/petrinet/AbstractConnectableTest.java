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
	public void connectableKnowsItIsOriginal() {
		assertTrue(connectable.isOriginal()); 
	}
	@Test
	public void copyConstructorWithLinkCloneFlagCreatesCloned() {
		cloned = new TestingConnectable(connectable, true); 
		assertFalse(cloned.isOriginal()); 
	}
	@Test
	public void ordinaryCopyConstructorCreatesTrueCopy() {
		cloned = new TestingConnectable(connectable); 
		assertTrue(cloned.isOriginal()); 
		assertEquals("A", cloned.getId()); 
	}
	@Test
	public void ifNotLinkedJustReturnsSelfAndCloneIsExactCopy() {
		cloned = new TestingConnectable(connectable, false); 
		assertEquals(connectable, connectable.getLinkedConnectable()); 
		assertEquals(cloned, cloned.getLinkedConnectable()); 
		assertEquals(connectable, cloned); 
	}
	@Test
	public void cloneKnowsItsOriginal() {
		cloned = new TestingConnectable(connectable, true); 
		assertEquals(connectable, cloned.getLinkedConnectable()); 
	}
	
	@Test
	public void equalsChecksNameIdAndPosition() {
		Connectable connectable2 = new TestingConnectable("A", "Aname"); 
		assertEquals(connectable, connectable2); 
		connectable2 = new TestingConnectable("B", "Aname"); 
		assertNotEquals(connectable, connectable2); 
		connectable2 = new TestingConnectable("A", "Bname"); 
		assertNotEquals(connectable, connectable2); 

		connectable2 = new TestingConnectable("A", "Aname"); 
		connectable2.setX(3);
		assertNotEquals(connectable, connectable2); 
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
	public void equalsIncludesCheckingOriginalFlag() {
		cloned = new TestingConnectable(connectable, true); 
		assertNotEquals(connectable, cloned); 

		cloned = new TestingConnectable(connectable); 
		assertEquals("but ordinary copy constructor returns simple copy",
				connectable, cloned); 
	}
	@Test
	public void beforeCloningOriginalLinksToItself() {
		assertEquals(connectable, connectable.getLinkedConnectable()); 
	}
	@Test
	public void originalKnowsItsClonedConnectable() {
		cloned = new TestingConnectable(connectable, true); 
		assertEquals(cloned, connectable.getLinkedConnectable()); 
	}
	@Test
	public void originalUpdatesItsClonedConnectableWithEachCopy() {
		cloned = new TestingConnectable(connectable, true); 
		assertEquals(cloned, connectable.getLinkedConnectable()); 
		Connectable cloned2 = new TestingConnectable(connectable, true); 
		assertEquals(cloned2, connectable.getLinkedConnectable()); 
	}
	@Test
	public void isOrClonedFromTestsObjectEqualityOnOriginalOrItsClone() {
		cloned = new TestingConnectable(connectable, true);
		assertTrue(cloned.isOrClonedFrom(connectable)); 
		assertTrue(connectable.isOrClonedFrom(connectable)); 
		assertTrue(cloned.isOrClonedFrom(cloned)); 
		assertTrue(connectable.isOrClonedFrom(cloned)); 
	}
	@Test
	public void anotherConnectablePassesLogicalEqualsButNotisOrClonedFrom() {
		cloned = new TestingConnectable(connectable, true);
		TestingConnectable connectable2 = new TestingConnectable("A", "Aname"); 
		assertTrue("passes logical equals",connectable2.equals(connectable)); 
		assertFalse("...but not isOrClonedFrom",connectable2.isOrClonedFrom(connectable)); 
	}
	@Test
	public void bothConnectablesReturnOriginalIdOrQualifiedId() {
		cloned = new TestingConnectable(connectable, true);
		cloned.setId("root.A");
		assertEquals("original knows its id","A",connectable.getOriginalId()); 
		assertEquals("as does the clone ","A",cloned.getOriginalId()); 
		assertEquals("clone knows its unique id","root.A",cloned.getUniqueId()); 
		assertEquals("as does the original","root.A",connectable.getUniqueId()); 
	}
	@Test
	public void throwsAttemptingToCloneAClone() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Cannot create a cloned Connectable from another clone: A");
		TestingConnectable cloned = new TestingConnectable(connectable, true);
		TestingConnectable cloned2 = new TestingConnectable(cloned, true);
	}
}
