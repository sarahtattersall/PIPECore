package uk.ac.imperial.pipe.models.component;

import java.awt.geom.Point2D;

/**
 * This class is used for PetriNetComponents that can be connected
 * to and Connected from by an {@link uk.ac.imperial.pipe.models.component.arc.Arc}
 */
public interface Connectable extends PlaceablePetriNetComponent {
    /**
     * Message fired when x name offset is changed
     */
    public static final String NAME_X_OFFSET_CHANGE_MESSAGE = "nameXOffset";

    /**
     * Message fired when y name offset is changed
     */
    public static final String NAME_Y_OFFSET_CHANGE_MESSAGE = "nameYOffset";

    /**
     *
     * @return name label x offset
     */
    public double getNameXOffset();

    /**
     *
     * @param nameXOffset new name label x offset
     */
    public void setNameXOffset(double nameXOffset);


    void setName(String name);

    String getName();

    /**
     *
     * @return name label y offset
     */
    public double getNameYOffset();


    void setNameYOffset(double nameYOffset);


    Point2D.Double getCentre();

    /**
     * @return coords for an arc to connect to
     * <p/>
     * x, y are the top left corner so A
     * would return (4, 1) and B would
     * return (14, 1)
     * <p/>
     * +---+         +---+
     * | A |-------->| B |
     * +---+         +---+
     */
    Point2D.Double getArcEdgePoint(double angle);

    /**
     * @return true if the arc can finish at this point.
     * I.e it is not a temporary connectable
     */
     boolean isEndPoint();
}
