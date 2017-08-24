package uk.ac.imperial.pipe.models.petrinet;

import java.awt.geom.Point2D;

/**
 * This class is used for PetriNetComponents that can be connected
 * to and Connected from by an {@link Arc}
 */
public interface Connectable extends PlaceablePetriNetComponent {
    /**
     * Message fired when x name offset is changed
     */
    String NAME_X_OFFSET_CHANGE_MESSAGE = "nameXOffset";

    /**
     * Message fired when y name offset is changed
     */
    String NAME_Y_OFFSET_CHANGE_MESSAGE = "nameYOffset";

    /**
     *
     * @return name label x offset
     */
    double getNameXOffset();

    /**
     *
     * @param nameXOffset new name label x offset
     */
    void setNameXOffset(double nameXOffset);


    void setName(String name);

    String getName();

    /**
     *
     * @return name label y offset
     */
    double getNameYOffset();


    void setNameYOffset(double nameYOffset);


    Point2D.Double getCentre();

    /**
     * @return coords for an arc to connect to
     * <p>
     * x, y are the top left corner so A
     * would return (4, 1) and B would
     * return (14, 1) </p>
     * <p>
     * +---+         +---+
     * | A |--------&gt;| B |
     * +---+         +---+
     * </p>
     * @param angle at which to connect
     */
    Point2D.Double getArcEdgePoint(double angle);

    /**
     * @return true if the arc can finish at this point.
     * I.e it is not a temporary connectable
     */
     boolean isEndPoint();

	boolean isOriginal();

	Connectable getLinkedConnectable();

	void setLinkedConnectable(Connectable linkedConnectable);
	/**
	 * 
	 * @param connectable to be linked to this one
	 * @return true if this is the same Connectable, 
	 * or if it is this Connectable's linked Connectable ({@link #getLinkedConnectable()})
	 */
	public boolean isOrClonedFrom(Connectable connectable);

	public String getOriginalId();

	public String getUniqueId();
	/**
	 * tests whether this and connectable have the same position 
	 * @return true if X, Y, XOffset and YOffset are equal for this and connectable
	 */
	public boolean equalsPosition(Connectable connectable);
	/**
	 * tests whether this and connectable have the same structure 
	 * @return true if id and name are equal for this and connectable
	 */
	public boolean equalsStructure(Connectable connectable);

}
