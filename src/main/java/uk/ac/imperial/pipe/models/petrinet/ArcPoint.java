package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

import java.awt.geom.Point2D;

/**
 * Represents a point on the arc
 */
public class ArcPoint extends AbstractPetriNetPubSub implements PlaceablePetriNetComponent {

    /**
     * Message fired when the curved attribute changes
     */
    public static final String UPDATE_CURVED_CHANGE_MESSAGE = "updateCurved";

    /**
     * Message fired when the location attribute changes
     */
    public static final String UPDATE_LOCATION_CHANGE_MESSAGE = "updateLocation";

    /**
     * x location
     */
    private int x;

    /**
     * y location
     */
    private int y;

    /**
     * If curved is true it implies this point is a Bezier curve
     */
    private boolean curved;

    /**
     * Denotes if the arc point is draggable in GUI mode
     */
    private final boolean draggable;

    /**
     * Constructor, sets draggable to true by default
     * @param point on the arc
     * @param curved true if arc is curved 
     */
    public ArcPoint(Point2D point, boolean curved) {
        this(point, curved, true);
    }

    /**
     * Constructor that allows you to choose whether the point is draggable
     * @param point on the arc
     * @param curved true if arc is curved
     * @param draggable true if arc is draggable
     */
    public ArcPoint(Point2D point, boolean curved, boolean draggable) {
        setPoint(point);
        this.curved = curved;
        this.draggable = draggable;
    }

    /**
     * Copy constructor
     * @param arcPoint to copy
     */
    public ArcPoint(ArcPoint arcPoint) {
        this.x = arcPoint.x;
        this.y = arcPoint.y;
        this.curved = arcPoint.curved;
        this.draggable = arcPoint.draggable;
    }

    /**
     *
     * @return location of point
     */
    public Point2D getPoint() {
        return new Point2D.Double(x, y);
    }

    /**
     *
     * @param point new location for the arc point
     */
    public void setPoint(Point2D point) {
        Point2D old = new Point2D.Double(this.x, this.y);
        this.x = (int) Math.round(point.getX());
        this.y = (int) Math.round(point.getY());
        changeSupport.firePropertyChange(UPDATE_LOCATION_CHANGE_MESSAGE, old, point);
    }

    /**
     *
     * @return x coordinate of the point
     */
    @Override
    public int getX() {
        return x;
    }

    /**
     *
     * @param x new x location of Petri net component
     */
    @Override
    public void setX(int x) {
        setPoint(new Point2D.Double(x, this.y));
    }

    /**
     *
     * @return y coordiate of the point
     */
    public int getY() {
        return y;
    }

    /**
     *
     * @param y new y location of Petri net component
     */
    @Override
    public void setY(int y) {
        setPoint(new Point2D.Double(this.x, y));
    }

    /**
     *
     * @return height of the arc point
     */
    @Override
    public int getHeight() {
        return 0;
    }

    /**
     *
     * @return width of the displayed arc point
     */
    @Override
    public int getWidth() {
        return 0;
    }

    /**
     *
     * @return false
     */
    @Override
    public boolean isSelectable() {
        return false;
    }

    /**
     *
     * @return true if the arc point is draggable, source and target arc points should not be draggable
     */
    @Override
    public boolean isDraggable() {
        return draggable;
    }

    /**
     * visit {@link uk.ac.imperial.pipe.models.petrinet.ArcPointVisitor}
     * @param visitor to be accepted
     */
    @Override
    public void accept(PetriNetComponentVisitor visitor) {
        if (visitor instanceof ArcPointVisitor) {
            ((ArcPointVisitor) visitor).visit(this);
        }
    }

    /**
     *
     * @return empty string sine an arc point doesn't yet have an id
     */
    @Override
    public String getId() {
        return "";
    }

    /**
     * Performs noop since an arc point doesn't yet have an id
     * @param id of the arc point
     */
    @Override
    public void setId(String id) {
        //TODO: Should arc points have an id?
    }

    /**
     *
     * @return true if arc point should be part of a Bezier curve
     */
    public boolean isCurved() {
        return curved;
    }

    /**
     *
     * @param curved true if an arc point should be part of a bezier curve
     */
    public void setCurved(boolean curved) {
        boolean old = this.curved;
        this.curved = curved;
        changeSupport.firePropertyChange(UPDATE_CURVED_CHANGE_MESSAGE, old, curved);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (curved ? 1 : 0);
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

        ArcPoint arcPoint = (ArcPoint) o;

        if (curved != arcPoint.curved) {
            return false;
        }
        if (Double.compare(arcPoint.x, x) != 0) {
            return false;
        }
        if (Double.compare(arcPoint.y, y) != 0) {
            return false;
        }

        return true;
    }
}
