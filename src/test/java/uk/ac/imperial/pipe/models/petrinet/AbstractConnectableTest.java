package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.*;

import org.junit.Test;

public class AbstractConnectableTest {

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
	
	
}
