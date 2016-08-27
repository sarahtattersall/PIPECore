package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.state.State;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;

/**
 * Arc connecting components together. Places can connect to transitions and transitions to places in
 * a bipartite graph
 * @param <S> component source type
 * @param <T> component target type
 */
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

    /**
     *
     * @return the target this arc connects to
     */
    T getTarget();

    /**
     *
     * @param target new target for the arc
     */
    void setTarget(T target);

    //TODO: Not sure if arcs should have names
    String getName();

    /**
     *
     * @return true if the arc is tagged
     */
    boolean isTagged();

    /**
     *
     * @param tagged new tagged status for the arc
     */
    void setTagged(boolean tagged);

    /**
     *
     * @param token token id
     * @return the weight of the specific token on the arc
     */
    String getWeightForToken(String token);

    /**
     *
     * Assign add to the arcs weight requirements a token with the specified weight.
     *
     * It the token id already exists it will get replaced.
     *
     * @param tokenId to set weight for
     * @param weight weight for the token id
     */
    void setWeight(String tokenId, String weight);

    /**
     * @return true if any of the weights are functional
     */
    boolean hasFunctionalWeight();

    /**
     *
     * @return arc type e.g. normal, inhibitor etc.
     */
    ArcType getType();

    /**
     *
     * @param points points to feature along the arc
     */
    void addIntermediatePoints(Iterable<ArcPoint> points);

    /**
     *
     * @param point add this point to the end of the arc
     */
    void addIntermediatePoint(ArcPoint point);

    /**
     *
     * @return all intermediate points
     */
    List<ArcPoint> getArcPoints();

    /**
     *
     * Removes the given point from this arcs intermediate points
     *
     * @param point to be removed
     */
    void removeIntermediatePoint(ArcPoint point);

    /**
     *
     * @param arcPoint to evaluate
     * @return the point after the specified one in the path
     */
    ArcPoint getNextPoint(ArcPoint arcPoint);

    /**
     * @return The start coordinate of the arc
     */
    Point2D.Double getStartPoint();

    /**
     * @return The end coordinate of the arc
     */
    Point2D getEndPoint();

    /**
     *
     * @return angle at which the arc connects to its target
     */
    double getEndAngle();

    /**
     *
     * @param executablePetriNet to evaluate
     * @param state of the Petri net
     * @return true if given the current state the arc can fire
     */
    //TODO: Don't pass in Petri net, get around this with better design
    boolean canFire(ExecutablePetriNet executablePetriNet, State state);

    /**
     * Removes the weight associated with the token from this arc
     * @param tokenId to remove weights for 
     */
    void removeAllTokenWeights(String tokenId);

}
