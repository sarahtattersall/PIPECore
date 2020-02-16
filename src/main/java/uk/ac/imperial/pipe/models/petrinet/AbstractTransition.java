package uk.ac.imperial.pipe.models.petrinet;

import static java.lang.Math.floor;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.pipe.parsers.FunctionalResults;
import uk.ac.imperial.pipe.parsers.FunctionalWeightParser;
import uk.ac.imperial.state.HashedStateBuilder;
import uk.ac.imperial.state.State;

public abstract class AbstractTransition extends AbstractConnectable implements Transition {

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
    protected ExecutablePetriNet executablePetriNet;
    private State state;
    private HashedStateBuilder builder;
    /**
     * The priority of this transition, the transition(s) with the highest priority will be enabled
     * when multiple transitions have the possiblity of being enabled
     */
    protected int priority = 1;
    /**
     * The rate/weight of the transition. It is considered to be the rate if the transition
     * is timed and the weight otherwise
     */
    protected Rate rate = new NormalRate("1");
    /**
     * Defaults to an immediate transition
     */
    protected boolean timed = false;
    /**
     * Defaults to single server semantics
     */
    protected boolean infiniteServer = false;
    /**
     * Enabled
     */
    boolean enabled = false;
    /**
     * Angle at which this transition should be displayed
     */
    protected int angle = 0;

    int delay = 0;
    //long nextFiringTime = Long.MIN_VALUE;

    public AbstractTransition(String id, String name) {
        super(id, name);
    }

    public AbstractTransition(AbstractConnectable connectable) {
        super(connectable);
    }

    /**
     * Treats Integer.MAX_VALUE as infinity and so will not subtract the weight
     * from it if this is the case
     *
     * @param currentWeight current tokens in the connected place
     * @param arcWeight weight of the arc
     * @return subtracted weight
     */
    protected int subtractWeight(int currentWeight, int arcWeight) {
        if (currentWeight == Integer.MAX_VALUE) {
            return currentWeight;
        }
        return currentWeight - arcWeight;
    }

    /**
     * Treats Integer.MAX_VALUE as infinity and so will not add the weight
     * to it if this is the case
     *
     * @param currentWeight current tokens in the connected place
     * @param arcWeight weight of the arc
     * @return added weight
     */
    protected int addWeight(int currentWeight, int arcWeight) {
        if (currentWeight == Integer.MAX_VALUE) {
            return currentWeight;
        }
        return currentWeight + arcWeight;
    }

    /**
     * @param state  petri net state to evaluate weight against
     * @param weight a functional weight
     * @param executablePetriNet to evaluate
     * @return the evaluated weight for the given state
     */
    protected double getArcWeight(ExecutablePetriNet executablePetriNet, State state, String weight) {
        double result = executablePetriNet.evaluateExpression(state, weight);
        if (result == -1.0) {
            //TODO:
            throw new RuntimeException("Could not parse arc weight");
        }
        return result;
    }

    /**
     *
     * @return true as a transition is always an end point
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
        Rate old = this.rate;
        this.rate = rate;
        changeSupport.firePropertyChange(RATE_CHANGE_MESSAGE, old, rate);
    }

    /**
     * Evaluate the transitions rate against the given state
     * <p>
     * If an infinite server the transition will return its rate * enabling degree
     * </p>
     * @return actual evaluated rate of the Petri net
     */
    @Override
    public Double getActualRate(ExecutablePetriNet executablePetriNet) {
        Double rate = getRateGivenCurrentState(executablePetriNet);
        if (rate == -1) {
            //TODO:
            return rate;
        }

        if (!isInfiniteServer()) {
            return rate;
        }
        Map<String, Map<String, Double>> arcWeights = evaluateInboundArcWeights(executablePetriNet
                .getFunctionalWeightParserForCurrentState(), executablePetriNet.inboundArcs(this));
        int enablingDegree = getEnablingDegree(executablePetriNet.getState(), arcWeights);
        return rate * enablingDegree;
    }

    private Double getRateGivenCurrentState(ExecutablePetriNet executablePetriNet) {
        return executablePetriNet.evaluateExpression(getRateExpr());
    }

    /**
     *
     * @return the unevaluated text representation of a transition weight
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
    private Map<String, Map<String, Double>> evaluateInboundArcWeights(FunctionalWeightParser<Double> parser,
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
    protected int getEnablingDegree(State state, Map<String, Map<String, Double>> arcWeights) {
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
    protected Map<String, Double> evaluateArcWeight(FunctionalWeightParser<Double> parser,
            Map<String, String> arcWeights) {
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
    protected double getArcWeight(FunctionalWeightParser<Double> parser, String weight) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractTransition)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        AbstractTransition that = (AbstractTransition) o;

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

        if (delay != that.delay) {
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
        //TODO do we need override this in DiscreteExternalTransition to include className?
        return result;
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

    @Override
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
            rotatedAngle = 2 * Math.PI + rotatedAngle;
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
     * Mirror enable status in cloned Transition
     * (e.g., enabling a transition in ExecutablePetriNet will be mirrored to corresponding transition in source Petri net)
     *
     * @param event for the property change
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals(ENABLED_CHANGE_MESSAGE)) {
            enable();
        } else if (event.getPropertyName().equals(DISABLED_CHANGE_MESSAGE)) {
            disable();
        }
    }

    @Override
    public void setDelay(int delay) {
        if (!isTimed())
            throw new IllegalStateException(
                    "AbstractTransition.setDelay:  delay cannot be set if Transition is not timed.");
        int old = this.delay;
        this.delay = delay;
        changeSupport.firePropertyChange(DELAY_CHANGE_MESSAGE, old, delay);
    }

    @Override
    public int getDelay() {
        return delay;
    }

}
