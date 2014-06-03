package uk.ac.imperial.pipe.io.adapters.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class AdaptedArcPoint {
    @XmlAttribute
    private String id = "";

    @XmlAttribute
    private double x;

    @XmlAttribute
    private double y;

    @XmlAttribute(name = "curvePoint")
    private boolean curved;

    public final double getX() {
        return x;
    }

    public final void setX(double x) {
        this.x = x;
    }

    public final double getY() {
        return y;
    }

    public final void setY(double y) {
        this.y = y;
    }

    public final boolean isCurved() {
        return curved;
    }

    public final void setCurved(boolean curved) {
        this.curved = curved;
    }

    public final String getId() {
        return id;
    }

    public final void setId(String id) {
        this.id = id;
    }
}
