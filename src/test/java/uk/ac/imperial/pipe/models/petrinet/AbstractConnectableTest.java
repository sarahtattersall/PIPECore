package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.*;

import java.awt.geom.Point2D.Double;

import org.junit.Test;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

public class AbstractConnectableTest {

	private TestingConnectable connectable;
	private TestingConnectable cloned;
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
	public void originalKnowsItsClonedConnectable() {
		connectable = new TestingConnectable("A", "A"); 
		cloned = new TestingConnectable(connectable, true); 
		assertEquals(connectable, cloned.getClone()); 
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
	
	
	private class TestingConnectable extends AbstractConnectable {

		private boolean original = true;
		private Connectable clone;

		protected TestingConnectable(String id, String name) {
			super(id, name);
		}

		public Connectable getClone() {
			return clone;
		}

		public TestingConnectable(TestingConnectable connectable) {
			this(connectable, false);
		}

		public TestingConnectable(TestingConnectable connectable, boolean linkClone) {
			super(connectable);
			if (linkClone) {
				original = false;
				clone = connectable; 
			} else {
				original = true; 
				clone = this; 
			}
			
		}

		@Override
		public boolean equals(Object o) {
			if (!super.equals(o)) {
				return false; 
			}
			TestingConnectable that = (TestingConnectable) o;
			if (that.isOriginal() != isOriginal()) {
				return false; 
			}
			return true;
		}
		
		public boolean isOriginal() {
			return original;
		}

		@Override
		public Double getCentre() {
			return null;
		}

		@Override
		public Double getArcEdgePoint(double angle) {
			return null;
		}

		@Override
		public boolean isEndPoint() {
			return false;
		}

		@Override
		public int getHeight() {
			return 0;
		}

		@Override
		public int getWidth() {
			return 0;
		}

		@Override
		public boolean isSelectable() {
			return false;
		}

		@Override
		public boolean isDraggable() {
			return false;
		}

		@Override
		public void accept(PetriNetComponentVisitor visitor) throws PetriNetComponentException {
			
		}
		
	}
}
