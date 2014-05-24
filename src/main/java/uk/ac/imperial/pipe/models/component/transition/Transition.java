package uk.ac.imperial.pipe.models.component.transition;

import uk.ac.imperial.pipe.models.component.Connectable;
import uk.ac.imperial.pipe.models.component.arc.InboundArc;
import uk.ac.imperial.pipe.models.component.rate.NormalRate;
import uk.ac.imperial.pipe.models.component.rate.Rate;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
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


public class Transition extends Connectable {
    /**
     * Message fired when the Transitions priority changes
     */
    public static final String PRIORITY_CHANGE_MESSAGE = "priority";

    /**
     * Message fired when the rate changes
     */
    public static final String RATE_CHANGE_MESSAGE = "rate";

    /**
     * Message fired when the angle changes
     */
    public static final String ANGLE_CHANGE_MESSAGE = "angle";

    /**
     * Message fired when the transition becomes timed/becomes immediate
     */
    public static final String TIMED_CHANGE_MESSAGE = "timed";

    /**
     * Message fired when the transition becomes an infinite/single server
     */
    public static final String INFINITE_SEVER_CHANGE_MESSAGE = "infiniteServer";

    /**
     * Message fired when the transition is enabled
     */
    public static final String ENABLED_CHANGE_MESSAGE = "enabled";

    /**
     * Message fired when the transition is enabled
     */
    public static final String DISABLED_CHANGE_MESSAGE = "disabled";

    public static final int TRANSITION_HEIGHT = 30;

    public static final int TRANSITION_WIDTH = TRANSITION_HEIGHT / 3;

    private int priority = 1;

    private Rate rate = new NormalRate("1");

    private boolean timed = false;

    private boolean infiniteServer = false;

    private int angle = 0;

    private boolean enabled = false;

    public Transition(String id, String name) {
        super(id, name);
    }

    public Transition(String id, String name, Rate rate, int priority) {
        super(id, name);
        this.rate = rate;
        this.priority = priority;
    }

    public Transition(Transition transition) {
        super(transition);
        this.infiniteServer = transition.infiniteServer;
        this.angle = transition.angle;
        this.timed = transition.timed;
        this.rate = transition.rate;
        this.priority = transition.priority;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + priority;
        result = 31 * result + (rate != null ? rate.hashCode() : 0);
        result = 31 * result + (timed ? 1 : 0);
        result = 31 * result + (infiniteServer ? 1 : 0);
        result = 31 * result + angle;
        result = 31 * result + (enabled ? 1 : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        Transition that = (Transition) o;

        if (angle != that.angle) {
            return false;
        }
        if (enabled != that.enabled) {
            return false;
        }
        if (infiniteServer != that.infiniteServer) {
            return false;
        }
        if (priority != that.priority) {
            return false;
        }
        if (timed != that.timed) {
            return false;
        }
        if (rate != null ? !rate.equals(that.rate) : that.rate != null) {
            return false;
        }

        return true;
    }

    @Override
    public Point2D.Double getCentre() {
        return new Point2D.Double(getX() + getWidth() / 2, getY() + getHeight() / 2);
    }

    @Override
    public Point2D.Double getArcEdgePoint(double angle) {
        double halfHeight = getHeight() / 2;
        double halfWidth = getWidth() / 2;
        double centreX = x + halfWidth;
        double centreY = y + halfHeight;

        Point2D.Double connectionPoint = new Point2D.Double(centreX, centreY);

        double rotatedAngle = angle + Math.toRadians(this.angle);
        if (connectToTop(rotatedAngle)) {
            connectionPoint.y -= halfHeight;
        } else if (connectToBottom(rotatedAngle)) {
            connectionPoint.y += halfHeight;
        } else if (connectToLeft(rotatedAngle)) {
            connectionPoint.x -= halfWidth;
        } else { //connectToRight
            connectionPoint.x += halfWidth;
        }

        return rotateAroundCenter(Math.toRadians(this.angle), connectionPoint);
    }

    @Override
    public int getHeight() {
        return TRANSITION_HEIGHT;
    }

    @Override
    public int getWidth() {
        return TRANSITION_WIDTH;
    }

    /**
     * @param angle in radians
     * @return true if an arc connecting to this should connect to the bottom edge
     * of the transition
     */
    private boolean connectToTop(double angle) {
        return angle > Math.toRadians(45) && angle < Math.toRadians(135);
    }

    //    public void setRateExpr(String string) {
    //        rateExpr = string;
    //    }

    /**
     * @param angle in radians
     * @return true if an arc connecting to this should
     * connect to the top edge of the transition
     */
    private boolean connectToBottom(double angle) {
        return angle < Math.toRadians(-45) && angle > Math.toRadians(-135);
    }

    /**
     * @param angle in radians
     * @return true if an arc connecting to this should
     * connect to the left edge of the transition
     */
    private boolean connectToLeft(double angle) {
        return angle > Math.toRadians(-45) && angle < Math.toRadians(45);
    }

    /**
     * Rotates point on transition around transition center
     *
     * @param angle rotation angle in degrees
     * @param point point to rotate
     * @return rotated point
     */
    private Point2D.Double rotateAroundCenter(double angle, Point2D.Double point) {
        AffineTransform tx = new AffineTransform();
        Point2D center = getCentre();
        tx.rotate(angle, center.getX(), center.getY());
        Point2D.Double rotatedPoint = new Point2D.Double();
        tx.transform(point, rotatedPoint);
        return rotatedPoint;
    }

    @Override
    public boolean isEndPoint() {
        return true;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        int old = this.priority;
        this.priority = priority;
        changeSupport.firePropertyChange(PRIORITY_CHANGE_MESSAGE, old, priority);
    }

    public Rate getRate() {
        return rate;
    }

    public void setRate(Rate rate) {
        this.rate = rate;
    }

    /**
     * Evaluate the transitions rate against the given state
     * <p/>
     * If an infinite server the transition will return its rate * enabling degree
     *
     * @param state given state of a petri net to evaluate the functional rate of
     * @return actual evaluated rate of the Petri net
     */
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

    public String getRateExpr() {
        return rate.getExpression();
    }

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
     * <p/>
     * The enabling degree is the number of times that a transition is enabled
     *
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
                    int currentDegree = (int) floor(placeTokenCount / requiredTokenCount);
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

    public void setInfiniteServer(boolean infiniteServer) {
        boolean old = this.infiniteServer;
        this.infiniteServer = infiniteServer;
        changeSupport.firePropertyChange(INFINITE_SEVER_CHANGE_MESSAGE, old, infiniteServer);
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        int old = this.angle;
        this.angle = angle;
        changeSupport.firePropertyChange(ANGLE_CHANGE_MESSAGE, old, angle);
    }

    public boolean isTimed() {
        return timed;
    }

    public void setTimed(boolean timed) {
        boolean old = this.timed;
        this.timed = timed;
        changeSupport.firePropertyChange(TIMED_CHANGE_MESSAGE, old, timed);
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    public boolean isDraggable() {
        return true;
    }

    @Override
    public void accept(PetriNetComponentVisitor visitor) {
        if (visitor instanceof TransitionVisitor) {
            ((TransitionVisitor) visitor).visit(this);
        }
    }

    public void enable() {
        enabled = true;
        changeSupport.firePropertyChange(ENABLED_CHANGE_MESSAGE, false, true);
    }

    public void disable() {
        enabled = false;
        changeSupport.firePropertyChange(DISABLED_CHANGE_MESSAGE, true, false);
    }

    public boolean isEnabled() {
        return enabled;
    }
}
