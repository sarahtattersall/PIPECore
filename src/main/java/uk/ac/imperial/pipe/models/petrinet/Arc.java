package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.state.State;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;

public interface Arc<S extends Connectable, T extends Connectable> extends PetriNetComponent {
    /**
     * Message fired when the arc source is changed
     */
    String SOURCE_CHANGE_MESSAGE = "source";
    /**
     * Message fired when the arc target is changed
     */
    String TARGET_CHANGE_MESSAGE = "target";
    /**
     * Message fired when the arc weight is changed
     */
    String WEIGHT_CHANGE_MESSAGE = "weight";
    /**
     * Message fired when an intermediate point is deleted
     */
    String DELETE_INTERMEDIATE_POINT_CHANGE_MESSAGE = "deleteIntermediatePoint";
    /**
     * Message fired when an intermediate point is created
     */
    String NEW_INTERMEDIATE_POINT_CHANGE_MESSAGE = "newIntermediatePoint";

    Map<String, String> getTokenWeights();

    /**
     *
     * @return the source the arc connects to
     */
    S getSource();

    /**
     *
     * @param source new source of arc
     */
    void setSource(S source);

    T getTarget();

    void setTarget(T target);

    //TODO: Not sure if arcs should have names
    String getName();

    boolean isTagged();

    void setTagged(boolean tagged);

    String getWeightForToken(String token);

    void setWeight(String tokenId, String weight);

    /**
     * @return true if any of the weights are functional
     */
    boolean hasFunctionalWeight();

    ArcType getType();

    void addIntermediatePoints(Iterable<ArcPoint> points);

    void addIntermediatePoint(ArcPoint point);

    List<ArcPoint> getArcPoints();

    void removeIntermediatePoint(ArcPoint point);

    ArcPoint getNextPoint(ArcPoint arcPoint);

    /**
     * @return The start coordinate of the arc
     */
    Point2D.Double getStartPoint();

    /**
     * @return The end coordinate of the arc
     */
    Point2D getEndPoint();

    double getEndAngle();

    /**
     *
     * @param petriNet
     * @param state
     * @return true if given the current state the arc can fire
     */
    //TODO: Don't pass in Petri net, get around this with better design
    boolean canFire(PetriNet petriNet, State state);

    /**
     * Removes the weight associated with the token from this arc
     * @param tokenId
     */
    void removeAllTokenWeights(String tokenId);
}
