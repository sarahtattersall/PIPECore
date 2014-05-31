package uk.ac.imperial.pipe.io.adapters.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class AdaptedToken {
    @XmlAttribute
    private String id;

    @XmlAttribute
    private int red;

    @XmlAttribute
    private int green;

    @XmlAttribute
    private int blue;

    public final String getId() {
        return id;
    }

    public final void setId(String id) {
        this.id = id;
    }

    public final int getRed() {
        return red;
    }

    public final void setRed(int red) {
        this.red = red;
    }

    public final int getGreen() {
        return green;
    }

    public final void setGreen(int green) {
        this.green = green;
    }

    public final int getBlue() {
        return blue;
    }

    public final void setBlue(int blue) {
        this.blue = blue;
    }
}

