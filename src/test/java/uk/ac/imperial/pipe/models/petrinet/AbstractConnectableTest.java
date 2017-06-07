package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.*;

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
	@Test
	public void connectableKnowsItIsOriginal() {
		connectable = new TestingConnectable("A", "A"); 
		assertTrue(connectable.isOriginal()); 
	}
	@Test
	public void copyConstructorWithLinkCloneFlagCreatesCloned() {
		connectable = new TestingConnectable("A", "A"); 
		cloned = new TestingConnectable(connectable, true); 
		assertFalse(cloned.isOriginal()); 
	}
	@Test
	public void ordinaryCopyConstructorCreatesTrueCopy() {
		connectable = new TestingConnectable("A", "A"); 
		cloned = new TestingConnectable(connectable); 
		assertTrue(cloned.isOriginal()); 
		assertEquals("A", cloned.getId()); 
	}
	@Test
	public void ifNotLinkedJustReturnsSelfAndCloneIsExactCopy() {
		connectable = new TestingConnectable("A", "A"); 
		cloned = new TestingConnectable(connectable, false); 
		assertEquals(connectable, connectable.getLinkedConnectable()); 
		assertEquals(cloned, cloned.getLinkedConnectable()); 
		assertEquals(connectable, cloned); 
	}
	@Test
	public void cloneKnowsItsOriginal() {
		connectable = new TestingConnectable("A", "A"); 
		cloned = new TestingConnectable(connectable, true); 
		assertEquals(connectable, cloned.getLinkedConnectable()); 
	}
	@Test
	public void equalsIncludesCheckingOriginalFlag() {
		connectable = new TestingConnectable("A", "A"); 
		cloned = new TestingConnectable(connectable, true); 
		assertNotEquals(connectable, cloned); 

		cloned = new TestingConnectable(connectable); 
		assertEquals("but ordinary copy constructor returns simple copy",
				connectable, cloned); 
	}
	@Test
	public void beforeCloningOriginalLinksToItself() {
		connectable = new TestingConnectable("A", "A");
		assertEquals(connectable, connectable.getLinkedConnectable()); 
	}
	@Test
	public void originalKnowsItsClonedConnectable() {
		connectable = new TestingConnectable("A", "A"); 
		cloned = new TestingConnectable(connectable, true); 
		assertEquals(cloned, connectable.getLinkedConnectable()); 
	}
	@Test
	public void originalUpdatesItsClonedConnectableWithEachCopy() {
		connectable = new TestingConnectable("A", "A"); 
		cloned = new TestingConnectable(connectable, true); 
		assertEquals(cloned, connectable.getLinkedConnectable()); 
		Connectable cloned2 = new TestingConnectable(connectable, true); 
		assertEquals(cloned2, connectable.getLinkedConnectable()); 
	}
	@Test
	public void isOrClonedFromTestsObjectEqualityOnOriginalOrItsClone() {
		connectable = new TestingConnectable("A", "A"); 
		cloned = new TestingConnectable(connectable, true);
		assertTrue(cloned.isOrClonedFrom(connectable)); 
		assertTrue(connectable.isOrClonedFrom(connectable)); 
		assertTrue(cloned.isOrClonedFrom(cloned)); 
		assertTrue(connectable.isOrClonedFrom(cloned)); 
	}
	@Test
	public void anotherConnectablePassesLogicalEqualsButNotisOrClonedFrom() {
		connectable = new TestingConnectable("A", "A"); 
		cloned = new TestingConnectable(connectable, true);
		TestingConnectable connectable2 = new TestingConnectable("A", "A"); 
		assertTrue("passes logical equals",connectable2.equals(connectable)); 
		assertFalse("...but not isOrClonedFrom",connectable2.isOrClonedFrom(connectable)); 
	}
	@Test
	public void bothConnectablesReturnOriginalIdOrQualifiedId() {
		connectable = new TestingConnectable("A", "A"); 
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
		connectable = new TestingConnectable("A", "A"); 
		TestingConnectable cloned = new TestingConnectable(connectable, true);
		TestingConnectable cloned2 = new TestingConnectable(cloned, true);
	}
	
	
}
