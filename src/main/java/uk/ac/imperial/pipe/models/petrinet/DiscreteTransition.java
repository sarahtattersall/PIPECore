package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.parsers.FunctionalResults;
import uk.ac.imperial.pipe.parsers.PetriNetWeightParser;
import uk.ac.imperial.pipe.parsers.StateEvalVisitor;
import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;
import uk.ac.imperial.state.State;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.floor;


/**
 * Discrete implementation of a transition
 */
public final class DiscreteTransition extends AbstractConnectable implements Transition {

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
     * The priority of this transition, the transition(s) with the highest priority will be enabled
     * when multiple transitions have the possiblity of being enabled
     */
    private int priority = 1;

    /**
     * The rate/weight of the transition. It is considered to be the rate if the transition
     * is timed and the weight otherwise
     */
    //TODO: I think this logic would be better split out into different classes
    private Rate rate = new NormalRate("1");

    /**
     * Defaults to an immediate transition
     */
    private boolean timed = false;

    /**
     * Defaults to single server semantics
     */
    private boolean infiniteServer = false;

    /**
     * Angle at which this transition should be displayed
     */
    private int angle = 0;

    /**
     * Enabled
     */
    private boolean enabled = false;

    /**
     * Constructor with default rate and priority
     * @param id of the transition
     * @param name of the transition
     */
    public DiscreteTransition(String id, String name) {
        super(id, name);
    }

    /**
     * Constructor that sets the default rate priority and the name of the transition to its id
     * @param id of the transition
     */
    public DiscreteTransition(String id) {
        super(id, id);
    }

    /**
     * Constructor with the specified rate and priority
     * @param id of the transition
     * @param name of the transition
     * @param rate of the transition
     * @param priority of the transition
     */
    public DiscreteTransition(String id, String name, Rate rate, int priority) {
        super(id, name);
        this.rate = rate;
        this.priority = priority;
    }

    /**
     * Copy constructor
     * @param transition to be copied
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
    private boolean connectToTop(double angle) {
        return angle >= Math.toRadians(DEGREES_45) && angle < Math.toRadians(DEGREES_135);
    }

    /**
     * @param angle in radians
     * @return true if an arc connecting to this should
     * connect to the top edge of the transition
     */
    private boolean connectToBottom(double angle) {
        return angle < Math.toRadians(DEGREES_315) && angle >= Math.toRadians(DEGREES_225);
    }

    /**
     * @param angle in radians
     * @return true if an arc connecting to this should
     * connect to the left edge of the transition
     */
    private boolean connectToRight(double angle) {
        return angle < Math.toRadians(DEGREES_225) && angle >= Math.toRadians(DEGREES_135);
    }

    /**
     * Rotates point on transition around transition center
     *
     * @param angle rotation angle in degrees
     * @param point point to rotate
     * @return rotated point
     */
    private Point2D.Double rotateAroundCenter(double angle, Point2D.Double point) {
        AffineTransform tx = AffineTransform.getRotateInstance(angle, getCentre().getX(), getCentre().getY());
        Point2D center = getCentre();
        Point2D.Double rotatedPoint = new Point2D.Double();
        tx.transform(point, rotatedPoint);
        return rotatedPoint;
    }

    /**
     *
     * @return true
     */
    @Override
    public boolean isEndPoint() {
        return true;
    }

    /**
     *
     * Returns the priority of the transition, priorities are used in animation
     * of a Petri net where the highest priority transitions are enabled
     *
     * @return the priority of the transition
     */
    @Override
    public int getPriority() {
        return priority;
    }

    /**
     *
     * @param priority the priority of this transition. Must be &gt; 0.
     */
    @Override
    public void setPriority(int priority) {
        int old = this.priority;
        this.priority = priority;
        changeSupport.firePropertyChange(PRIORITY_CHANGE_MESSAGE, old, priority);
    }

    /**
     *
     * @return the rate at which the transition fires
     */
    @Override
    public Rate getRate() {
        return rate;
    }

    /**
     *
     * @param rate the new rate for the transitions firing rate
     */
    @Override
    public void setRate(Rate rate) {
        this.rate = rate;
    }

    /**
     * Evaluate the transitions rate against the given state
     * <p>
     * If an infinite server the transition will return its rate * enabling degree
     * </p>
     * @param state given state of a petri net to evaluate the functional rate of
     * @return actual evaluated rate of the Petri net
     */
    @Override
    public Double getActualRate(PetriNet petriNet, State state) {
        StateEvalVisitor stateEvalVisitor = new StateEvalVisitor(petriNet, state);
        PetriNetWeightParser parser = new PetriNetWeightParser(stateEvalVisitor, petriNet);
        FunctionalResults<Double> results = parser.evaluateExpression(getRateExpr());
        if (results.hasErrors()) {
            //TODO:
            return -1.;
        }
        Double rate = results.getResult();

        if (!isInfiniteServer()) {
            return rate;
        }
        Map<String, Map<String, Double>> arcWeights = evaluateInboundArcWeights(parser, petriNet.inboundArcs(this));
        int enablingDegree = getEnablingDegree(state, arcWeights);
        return rate * enablingDegree;
    }

    /**
     *
     * @return the unevaluated text representation of a transition reight
     */
    @Override
    public String getRateExpr() {
        return rate.getExpression();
    }

    /**
     *
     * @return true if the transition is an infinite sever, false if it is a single server
     */
    @Override
    public boolean isInfiniteServer() {
        return infiniteServer;
    }

    /**
     * @param parser parser for a given state of Petri net
     * @param arcs   set of inbound arcs to evaluate weight against the current state
     * @return map of arc place id -> arc weights associated with it
     */
    private Map<String, Map<String, Double>> evaluateInboundArcWeights(PetriNetWeightParser parser,
                                                                       Collection<InboundArc> arcs) {
        Map<String, Map<String, Double>> result = new HashMap<>();
        for (InboundArc arc : arcs) {
            String placeId = arc.getSource().getId();
            Map<String, String> arcWeights = arc.getTokenWeights();
            Map<String, Double> weights = evaluateArcWeight(parser, arcWeights);
            result.put(placeId, weights);
        }

        return result;
    }

    /**
     * A Transition is enabled if all its input places are marked with at least one token
     * This method calculates the minimum number of tokens needed in order for a transition to be enabled
     * <p>
     * The enabling degree is the number of times that a transition is enabled
     * </p>
     * @param state state of the petri net
     * @param arcWeights evaluated arc weights for the given state
     * @return number of times this transition is enabled for the given state
     */
    private int getEnablingDegree(State state, Map<String, Map<String, Double>> arcWeights) {
        int enablingDegree = Integer.MAX_VALUE;

        for (Map.Entry<String, Map<String, Double>> entry : arcWeights.entrySet()) {
            String placeId = entry.getKey();
            Map<String, Double> weights = entry.getValue();
            for (Map.Entry<String, Double> weightEntry : weights.entrySet()) {
                String tokenId = weightEntry.getKey();
                Double weight = weightEntry.getValue();

                int requiredTokenCount = (int) floor(weight);
                if (requiredTokenCount == 0) {
                    enablingDegree = 0;
                } else {
                    Map<String, Integer> tokenCount = state.getTokens(placeId);
                    int placeTokenCount = tokenCount.get(tokenId);
                    int currentDegree = placeTokenCount / requiredTokenCount;
                    if (currentDegree < enablingDegree) {
                        enablingDegree = currentDegree;
                    }
                }
            }

        }
        return enablingDegree;
    }

    /**
     * Parses a string representation of a weight with respect to the Petri net
     *
     * @param parser     parser for a given state of the Petri net
     * @param arcWeights arc weights
     * @return arc weights evaluated to the current state
     */

    private Map<String, Double> evaluateArcWeight(PetriNetWeightParser parser, Map<String, String> arcWeights) {
        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<String, String> entry : arcWeights.entrySet()) {
            String tokenId = entry.getKey();
            double arcWeight = getArcWeight(parser, arcWeights.get(tokenId));
            result.put(tokenId, arcWeight);
        }
        return result;
    }

    /**
     * @param parser parser for a given state of the Petri net
     * @param weight arc functional rate
     * @return arc weight for a given state
     */
    private double getArcWeight(PetriNetWeightParser parser, String weight) {
        FunctionalResults<Double> result = parser.evaluateExpression(weight);
        if (result.hasErrors()) {
            //TODO:
            throw new RuntimeException("Could not parse arc weight");
        }

        return result.getResult();
    }

    /**
     *
     * @param infiniteServer true =&gt; infinite server, false =&gt; single server
     */
    @Override
    public void setInfiniteServer(boolean infiniteServer) {
        boolean old = this.infiniteServer;
        this.infiniteServer = infiniteServer;
        changeSupport.firePropertyChange(INFINITE_SEVER_CHANGE_MESSAGE, old, infiniteServer);
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
     * @return true if the transition is timed, false for immediate
     */
    @Override
    public boolean isTimed() {
        return timed;
    }

    /**
     *
     * @param timed true =&gt; timed, false =&gt; immediate
     */
    @Override
    public void setTimed(boolean timed) {
        boolean old = this.timed;
        this.timed = timed;
        changeSupport.firePropertyChange(TIMED_CHANGE_MESSAGE, old, timed);
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
     * @param visitor to be accepted 
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

    /**
     * Enable the transition
     */
    @Override
    public void enable() {
        enabled = true;
        changeSupport.firePropertyChange(ENABLED_CHANGE_MESSAGE, false, true);
    }

    /**
     * Disable the transition
     */
    @Override
    public void disable() {
        enabled = false;
        changeSupport.firePropertyChange(DISABLED_CHANGE_MESSAGE, true, false);
    }

    /**
     *
     * @return true if the transition has been enabled
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
