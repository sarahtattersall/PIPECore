package uk.ac.imperial.pipe.models.petrinet;

import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * AbstractArc class
 * @param <S> Source type
 * @param <T> Target type
 */
public abstract class AbstractArc<S extends Connectable, T extends Connectable> extends AbstractPetriNetPubSub
        implements Arc<S, T> {

    /**
     * Arc source
     */
    private S source;

    /**
     * Arc target
     */
    private T target;

    /**
     * Arc id
     */
    private String id;

    /**
     * Arc taggged
     */
    private boolean tagged;

    /**
     * Map of Token to corresponding weights
     * Weights can be functional e.g {@code '> 5'}
     */
    protected Map<String, String> tokenWeights = new HashMap<>();

    /**
     * Arc type e.g. Normal, Inhibitor, etc.
     */
    private final ArcType type;

    /**
     * Intermediate path arcPoints
     */
    private final List<ArcPoint> arcPoints = new LinkedList<>();

    /**
     * The point at which the arc coonnects to the source
     */
    private final ArcPoint sourcePoint;

    /**
     * The point at which the arc connects to the target
     */
    private final ArcPoint targetPoint;

    private final PropertyChangeListener intermediateListener = new ArcPointChangeListener();

    /**
     * Abstract arc constructor sets arc to {@code <source id> TO <target id>}
     * @param source connectable
     * @param target connectable
     * @param tokenWeights for this arc
     * @param type of arc
     */
    public AbstractArc(S source, T target, Map<String, String> tokenWeights, ArcType type) {
        this.source = source;
        this.target = target;
        this.tokenWeights = tokenWeights;
        this.type = type;

        buildId(false);
        tagged = false;

        sourcePoint = new ArcPoint(getStartPoint(), false, false);
        targetPoint = new ArcPoint(getEndPoint(), false, false);
        arcPoints.add(sourcePoint);
        arcPoints.add(targetPoint);

        source.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();
                if (name.equals(Connectable.X_CHANGE_MESSAGE) || name.equals(Connectable.Y_CHANGE_MESSAGE) ||
                        name.equals(Transition.ANGLE_CHANGE_MESSAGE)) {

                    sourcePoint.setPoint(getStartPoint());
                    targetPoint.setPoint(getEndPoint());
                }
            }
        });
        target.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();
                if (name.equals(Connectable.X_CHANGE_MESSAGE) || name.equals(Connectable.Y_CHANGE_MESSAGE) ||
                        name.equals(Transition.ANGLE_CHANGE_MESSAGE)) {
                    sourcePoint.setPoint(getStartPoint());
                    targetPoint.setPoint(getEndPoint());
                }
            }
        });

    }

    protected void buildId(boolean rebuild) {
        String old = id;
        id = source.getId() + " TO " + target.getId();
        if (rebuild) {
            changeSupport.firePropertyChange(ID_CHANGE_MESSAGE, old, id);
        }
    }

    /**
     *
     * @return weight of the arc
     */
    @Override
    public Map<String, String> getTokenWeights() {
        return tokenWeights;
    }

    /**
     *
     * @return source
     */
    @Override
    public S getSource() {
        return source;
    }

    /**
     * Sets the new source of the arc
     *
     * @param source new source of arc
     */
    @Override
    public void setSource(S source) {
        S old = this.source;
        this.source = source;
        buildId(true);
        changeSupport.firePropertyChange(SOURCE_CHANGE_MESSAGE, old, source);
    }

    /**
     *
     * @return target
     */
    @Override
    public T getTarget() {
        return target;
    }

    /**
     *
     * @param target new target of the arc
     */
    @Override
    public void setTarget(T target) {
        T old = this.target;
        this.target = target;
        buildId(true);
        changeSupport.firePropertyChange(TARGET_CHANGE_MESSAGE, old, target);
    }

    /**
     * @return true - Arcs are always selectable
     */
    @Override
    public boolean isSelectable() {
        return true;
    }

    /**
     *
     * @return true since arcs can always be dragged
     */
    @Override
    public boolean isDraggable() {
        return true;
    }

    /**
     *
     * @return arc id
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     *
     * @param id new id for arc
     */
    @Override
    public void setId(String id) {
        String old = this.id;
        this.id = id;
        changeSupport.firePropertyChange(ID_CHANGE_MESSAGE, old, id);
    }

    /**
     *
     * @return id of arc
     */
    //TODO: Not sure if arcs should have names
    @Override
    public String getName() {
        return id;
    }

    /**
     *
     * @return true if arc is tagged
     */
    @Override
    public boolean isTagged() {
        return tagged;
    }

    /**
     *
     * @param tagged new tagged value
     */
    @Override
    public void setTagged(boolean tagged) {
        this.tagged = tagged;
    }

    /**
     *
     * @param token to evaluate
     * @return the functional expression for a single token which is equivalent to that tokens weight on the arc
     */
    @Override
    public String getWeightForToken(String token) {
        if (tokenWeights.containsKey(token)) {
            return tokenWeights.get(token);
        } else {
            return "0";
        }
    }

    /**
     *
     * Sets the weight of the arc to the token id and new weight.
     * Overwrites any old token weight for the specified token id
     *
     * @param tokenId to set weight for
     * @param weight to assign
     */
    @Override
    public void setWeight(String tokenId, String weight) {
        Map<String, String> old = new HashMap<>(tokenWeights);
        tokenWeights.put(tokenId, weight);
        changeSupport.firePropertyChange(WEIGHT_CHANGE_MESSAGE, old, tokenWeights);
    }

    /**
     * @return true if any of the weights are functional
     */
    @Override
    public boolean hasFunctionalWeight() {
        for (String weight : tokenWeights.values()) {

            try {
                Integer.parseInt(weight);
            } catch (NumberFormatException e) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @return arc type
     */
    @Override
    public ArcType getType() {
        return type;

    }

    /**
     * Add intermediate points to the arc
     * @param points to be added
     */
    @Override
    public void addIntermediatePoints(Iterable<ArcPoint> points) {
        for (ArcPoint point : points) {
            addIntermediatePoint(point);
        }
    }

    /**
     * Add an intermediate point to the arc
     *
     * @param point to be added
     */
    @Override
    public void addIntermediatePoint(ArcPoint point) {
        int penultimateIndex = arcPoints.size() - 1;
        arcPoints.add(penultimateIndex, point);
        point.addPropertyChangeListener(intermediateListener);
        recalculateStartPoint();
        recalculateEndPoint();
        changeSupport.firePropertyChange(NEW_INTERMEDIATE_POINT_CHANGE_MESSAGE, null, point);
    }

    private void recalculateStartPoint() {
        Point2D startCoords = getStartPoint();
        sourcePoint.setPoint(startCoords);
    }

    private void recalculateEndPoint() {
        Point2D lastPoint = arcPoints.get(arcPoints.size() - 2).getPoint();
        double angle = getAngleBetweenTwoPoints(lastPoint, target.getCentre());
        Point2D newPoint = target.getArcEdgePoint(angle);
        targetPoint.setPoint(newPoint);
    }

    /**
     *
     * @return all intermediate arc points
     */
    @Override
    public List<ArcPoint> getArcPoints() {
        return arcPoints;
    }

    /**
     * Remove the intermediate arc point
     * @param point to be removed
     */
    @Override
    public void removeIntermediatePoint(ArcPoint point) {
        arcPoints.remove(point);
        point.removePropertyChangeListener(intermediateListener);
        recalculateStartPoint();
        recalculateEndPoint();
        changeSupport.firePropertyChange(DELETE_INTERMEDIATE_POINT_CHANGE_MESSAGE, point, null);
    }

    /**
     *
     * @param arcPoint to evaluate
     * @return the arcPoint following this one
     */
    @Override
    public ArcPoint getNextPoint(ArcPoint arcPoint) {
        int index = arcPoints.indexOf(arcPoint);
        if (index == arcPoints.size() - 1 || index < 0) {
            throw new RuntimeException("No next point");
        }

        return arcPoints.get(index + 1);
    }

    /**
     * @return The start coordinate of the arc
     */
    @Override
    public final Point2D.Double getStartPoint() {
        double angle;
        if (arcPoints.size() > 1) {
            angle = getAngleBetweenTwoPoints(arcPoints.get(1).getPoint(), source.getCentre());
        } else {
            angle = getAngleBetweenTwoPoints(target.getCentre(), source.getCentre());
        }
        return source.getArcEdgePoint(angle);
    }

    /**
     * @return The end coordinate of the arc
     */
    @Override
    public final Point2D getEndPoint() {
        return target.getArcEdgePoint(getEndAngle());
    }

    /**
     *
     * @return the angle at which this arc connects to the target
     */
    @Override
    public double getEndAngle() {
        if (arcPoints.size() > 1) {
            return getAngleBetweenTwoPoints(arcPoints.get(arcPoints.size() - 2).getPoint(), target.getCentre());
        } else {
            return getAngleBetweenTwoPoints(source.getCentre(), target.getCentre());
        }
    }

    /**
     * @return angle in radians between first and second
     */
    private double getAngleBetweenTwoPoints(Point2D first, Point2D second) {
        double deltax = second.getX() - first.getX();
        double deltay = second.getY() - first.getY();
        return Math.atan2(deltay, deltax);
    }

    @Override
    public int hashCode() {
        int result = source.hashCode();
        result = 31 * result + target.hashCode();
        result = 31 * result + id.hashCode();
        result = 31 * result + (tagged ? 1 : 0);
        result = 31 * result + tokenWeights.hashCode();
        result = 31 * result + arcPoints.hashCode();
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

        AbstractArc arc = (AbstractArc) o;

        if (tagged != arc.tagged) {
            return false;
        }
        if (!id.equals(arc.id)) {
            return false;
        }
        if (!arcPoints.equals(arc.arcPoints)) {
            return false;
        }
        if (!source.equals(arc.source)) {
            return false;
        }
        if (!target.equals(arc.target)) {
            return false;
        }
        //TODO:
        //        if (!tokenWeights.equals(arc.tokenWeights)) {
        //            return false;
        //        }

        return true;
    }

    /**
     * Removes the weight associated with the token from this arc
     * @param tokenId to remove weights for
     */
    @Override
    public void removeAllTokenWeights(String tokenId) {
        tokenWeights.remove(tokenId);
    }

    public PropertyChangeListener getIntermediateListener() {
        return intermediateListener;
    }

    private class ArcPointChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String name = evt.getPropertyName();
            if (name.equals(ArcPoint.UPDATE_LOCATION_CHANGE_MESSAGE)) {
                recalculateEndPoint();
                recalculateStartPoint();
            }
        }
    }
}
