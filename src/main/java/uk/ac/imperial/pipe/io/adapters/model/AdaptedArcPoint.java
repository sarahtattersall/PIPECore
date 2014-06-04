package uk.ac.imperial.pipe.io.adapters.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * PNML adaption of an arc point
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class AdaptedArcPoint {
    /**
     * Id attribute
     */
    @XmlAttribute
    private String id = "";

    /**
     * x coordinate
     */
    @XmlAttribute
    private double x;

    /**
     * y coordinate
     */
    @XmlAttribute
    private double y;

    /**
     * curved attribute
     */
    @XmlAttribute(name = "curvePoint")
    private boolean curved;

    /**
     *
     * @return x coordinate
     */
    public final double getX() {
        return x;
    }

    /**
     *
     * @param x x coordinate
     */
    public final void setX(double x) {
        this.x = x;
    }

    /**
     *
     * @return y y coordinate
     */
    public final double getY() {
        return y;
    }

    /**
     *
     * @param y y coordinate
     */
    public final void setY(double y) {
        this.y = y;
    }

    /**
     *
     * @return true if the point is part of a Bezier curve
     */
    public final boolean isCurved() {
        return curved;
    }

    /**
     *
     * @param curved true if the point is part of a Bezier curve
     */
    public final void setCurved(boolean curved) {
        this.curved = curved;
    }

    /**
     *
     * @return arc point id
     */
    public final String getId() {
        return id;
    }

    /**
     *
     * @param id unique arc point id
     */
    public final void setId(String id) {
        this.id = id;
    }
}
