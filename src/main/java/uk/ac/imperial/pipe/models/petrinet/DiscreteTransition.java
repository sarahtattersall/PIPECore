package uk.ac.imperial.pipe.models.petrinet;



import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;


/**
 * Discrete implementation of a transition
 */
public class DiscreteTransition extends AbstractTransition implements Transition {

    /**
     * Constructor with default rate and priority
     * @param id
     * @param name
     */
    public DiscreteTransition(String id, String name) {
        super(id, name);
    }

    /**
     * Constructor that sets the default rate priority and the name of the transition to its id
     * @param id
     */
    public DiscreteTransition(String id) {
        super(id, id);
    }

    /**
     * Constructor with the specified rate and priority
     * @param id
     * @param name
     * @param rate
     * @param priority
     */
    public DiscreteTransition(String id, String name, Rate rate, int priority) {
        super(id, name);
        this.rate = rate;
        this.priority = priority;
    }

    /**
     * Copy constructor
     * @param transition
     */
    public DiscreteTransition(DiscreteTransition transition) {
        super(transition);
        this.infiniteServer = transition.infiniteServer;
        this.angle = transition.angle;
        this.timed = transition.timed;
        this.rate = transition.rate;
        this.priority = transition.priority;
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
        if (visitor instanceof DiscreteTransitionVisitor) {
            ((DiscreteTransitionVisitor) visitor).visit(this);
        }
    }

    @Override
	public  void fire() {
		// timing delays should be implemented here. 
	}
	@Override
	public boolean equals(Object o) {
		if (!super.equals(o)) {
			return false;
		}
	    if (!(o instanceof DiscreteTransition)) {
	        return false;
	    }
	    return true;
	}

}
