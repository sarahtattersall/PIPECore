package uk.ac.imperial.pipe.models.petrinet;

import java.awt.geom.Point2D.Double;
import java.beans.PropertyChangeListener;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

public class DiscreteExternalTransition extends AbstractTransition implements Transition {

	private Class<ExternalTransition> clientClass;
	private ExternalTransition client;
	private String className;

	public DiscreteExternalTransition(String id, String name, String className) {
		super(id, name);
		this.className = className; 
		buildClient(); 
	}

	public DiscreteExternalTransition(DiscreteExternalTransition transition) {
		super(transition);
		this.className = transition.className; 
		buildClient(); 
	}
	

	@SuppressWarnings("unchecked")
	private void buildClient() {
		if (this.className == null) {
			throw new IllegalArgumentException("DiscreteExternalTransition.init:  client class name not specified.");
		}
		try {
			this.clientClass = (Class<ExternalTransition>) Class.forName(this.className);
			client = this.clientClass.newInstance(); 
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("DiscreteExternalTransition.init:  client class does not exist: "+this.className+"\n"+e.getMessage());
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("DiscreteExternalTransition.init:  client class does not implement uk.ac.imperial.pipe.models.petrinet.ExternalTransition: "+this.className+"\n"+e.getMessage());
		} catch (InstantiationException e) {
			throw new IllegalArgumentException("DiscreteExternalTransition.init:  client class does not have a null constructor: "+this.className+"\n"+e.getMessage());
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("DiscreteExternalTransition.init:  client class does not have a public constructor: "+this.className+"\n"+e.getMessage());
		}
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

    /**
     * visits the visitor of it is a {@link uk.ac.imperial.pipe.models.petrinet.DiscreteTransitionVisitor} or a
     * {@link uk.ac.imperial.pipe.models.petrinet.TransitionVisitor}.
     * @param visitor
     */
    @Override
    public void accept(PetriNetComponentVisitor visitor) {
        if (visitor instanceof TransitionVisitor) {
            ((TransitionVisitor) visitor).visit(this);
        }
        if (visitor instanceof DiscreteExternalTransitionVisitor) {
            ((DiscreteExternalTransitionVisitor) visitor).visit(this);
        }
    }


	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public void setPriority(int priority) {
	}

	@Override
	public Rate getRate() {
		return null;
	}

	@Override
	public void setRate(Rate rate) {
	}

	@Override
	public java.lang.Double getActualRate(ExecutablePetriNet executablePetriNet) {
		return null;
	}

	@Override
	public String getRateExpr() {
		return null;
	}

	@Override
	public boolean isInfiniteServer() {
		return false;
	}

	@Override
	public void setInfiniteServer(boolean infiniteServer) {
	}

	@Override
	public int getAngle() {
		return 0;
	}

	@Override
	public void setAngle(int angle) {
	}

	@Override
	public boolean isTimed() {
		return false;
	}

	@Override
	public void setTimed(boolean timed) {
	}

	@Override
	public void enable() {
	}

	@Override
	public void disable() {
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public void fire() {
		client.setExecutablePetriNet(executablePetriNet); 
		client.fire(); 
	}

	protected ExternalTransition getClient() {
		return client;
	}


	public void setContextForClient(Object context) {
		client.setContext(context); 
	}

}
