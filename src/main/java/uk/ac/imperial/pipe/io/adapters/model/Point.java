package uk.ac.imperial.pipe.io.adapters.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * Verbose way of storing a coordinate in PNML
 */
@XmlAccessorType(XmlAccessType.FIELD)
public final class Point {

    /**
     * x coordinate
     */
    @XmlAttribute
    public double x;

    /**
     * y coordinate
     */
    @XmlAttribute
    public double y;

    /**
     *
     * @return x coordinate
     */
    public double getX() {
        return x;
    }

    /**
     *
     * @param x x coordinate
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     *
     * @return y coordinate
     */
    public double getY() {
        return y;
    }

    /**
     *
     * @param y y coordinate
     */
    public void setY(double y) {
        this.y = y;
    }
}
