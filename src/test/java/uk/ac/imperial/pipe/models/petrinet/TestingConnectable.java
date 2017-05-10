package uk.ac.imperial.pipe.models.petrinet;

import java.awt.geom.Point2D.Double;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

public class TestingConnectable extends AbstractConnectable {

	protected TestingConnectable(String id, String name) {
		super(id, name);
	}

	public TestingConnectable(AbstractConnectable connectable) {
		super(connectable, false);
	}

	public TestingConnectable(AbstractConnectable connectable, boolean linkClone) {
		super(connectable, linkClone);
	}

	@Override
	public boolean equals(Object o) {
		if (!super.equals(o)) {
			return false; 
		}
		AbstractConnectable that = (AbstractConnectable) o;
		if (that.isOriginal() != isOriginal()) {
			return false; 
		}
		return true;
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