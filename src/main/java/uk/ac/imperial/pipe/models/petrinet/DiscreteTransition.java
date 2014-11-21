package uk.ac.imperial.pipe.models.petrinet;


import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;


/**
 * Discrete implementation of a transition
 */
public final class DiscreteTransition extends AbstractTransition implements Transition {

    /**
     * 135 degrees
     */
    public static final int DEGREES_135 = 135;

    /**
     * 45 degrees
     */
    public static final int DEGREES_45 = 45;

    public static final int DEGREES_225 = 225;
    public static final int DEGREES_315 = 315;


    /**
     * Angle at which this transition should be displayed
     */
    private int angle = 0;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DiscreteTransition)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        DiscreteTransition that = (DiscreteTransition) o;

        if (infiniteServer != that.infiniteServer) {
            return false;
        }
        if (priority != that.priority) {
            return false;
        }
        if (timed != that.timed) {
            return false;
        }
        if (!rate.equals(that.rate)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + priority;
        result = 31 * result + rate.hashCode();
        result = 31 * result + (timed ? 1 : 0);
        result = 31 * result + (infiniteServer ? 1 : 0);
        return result;
    }

    @Override

    /**
     * Center of the transition
     */
    public Point2D.Double getCentre() {
        return new Point2D.Double(getX() + getWidth() / 2, getY() + getHeight() / 2);
    }

    /**
     * @param angle angle at which the arc meets this component
     * @return the location where the arc should meet this component
     */
    @Override
    public Point2D.Double getArcEdgePoint(double angle) {
        int halfHeight = getHeight() / 2;
        int halfWidth = getWidth() / 2;
        double centreX = x + halfWidth;
        double centreY = y + halfHeight;

        Point2D.Double connectionPoint = new Point2D.Double(centreX, centreY);

        double rotatedAngle = angle - Math.toRadians(this.angle);
        if (rotatedAngle < 0) {
            rotatedAngle = 2* Math.PI + rotatedAngle;
        }
        if (connectToTop(rotatedAngle)) {
            connectionPoint.y -= halfHeight;
        } else if (connectToBottom(rotatedAngle)) {
            connectionPoint.y += halfHeight;
        } else if (connectToRight(rotatedAngle)) {
            connectionPoint.x += halfWidth;
        } else {
            //connect to left
            connectionPoint.x -= halfWidth;
        }

        return rotateAroundCenter(Math.toRadians(this.angle), connectionPoint);
    }

    /**
     *
     * @return the height of the component
     */
    @Override
    public int getHeight() {
        return TRANSITION_HEIGHT;
    }

    /**
     *
     * @return the width of the component
     */
    @Override
    public int getWidth() {
        return TRANSITION_WIDTH;
    }

    /**
     *
     * @param angle in radians between 0 and 2pi
     * @return true if an arc connecting to this should connect to the bottom edge
     * of the transition
     */
    protected boolean connectToTop(double angle) {
        return angle >= Math.toRadians(DEGREES_45) && angle < Math.toRadians(DEGREES_135);
    }

    /**
     * @param angle in radians
     * @return true if an arc connecting to this should
     * connect to the top edge of the transition
     */
    protected boolean connectToBottom(double angle) {
        return angle < Math.toRadians(DEGREES_315) && angle >= Math.toRadians(DEGREES_225);
    }

    /**
     * @param angle in radians
     * @return true if an arc connecting to this should
     * connect to the left edge of the transition
     */
    protected boolean connectToRight(double angle) {
        return angle < Math.toRadians(DEGREES_225) && angle >= Math.toRadians(DEGREES_135);
    }

    /**
     * Rotates point on transition around transition center
     *
     * @param angle rotation angle in degrees
     * @param point point to rotate
     * @return rotated point
     */
    protected Point2D.Double rotateAroundCenter(double angle, Point2D.Double point) {
        AffineTransform tx = AffineTransform.getRotateInstance(angle, getCentre().getX(), getCentre().getY());
        Point2D center = getCentre();
        Point2D.Double rotatedPoint = new Point2D.Double();
        tx.transform(point, rotatedPoint);
        return rotatedPoint;
    }

    /**
     *
     * @return angle at which the transition should be displayed
     */
    @Override
    public int getAngle() {
        return angle;
    }

    /**
     *
     * @param angle new angle starting from pointing NORTH at which the transition should be displayed
     */
    @Override
    public void setAngle(int angle) {
        int old = this.angle;
        this.angle = angle;
        changeSupport.firePropertyChange(ANGLE_CHANGE_MESSAGE, old, angle);
    }

    /**
     *
     * @return true since a transition appears on the canvas so is always selectable
     */
    @Override
    public boolean isSelectable() {
        return true;
    }

    /**
     *
     * @return true since a transition appears on the canvas so is always draggable
     */
    @Override
    public boolean isDraggable() {
        return true;
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

}
