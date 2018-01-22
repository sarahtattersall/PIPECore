package uk.ac.imperial.pipe.io.adapters.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * Adapted token for marshalling into PNML format
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class AdaptedToken {
    /**
     * Token id
     */
    @XmlAttribute
    private String id;

    /**
     * Red component of token color in range (0,255)
     */
    @XmlAttribute
    private int red;
    /**
     * Green component of token color in range (0,255)
     */
    @XmlAttribute
    private int green;
    /**
     * Blue component of token color in range (0,255)
     */
    @XmlAttribute
    private int blue;

    /**
     *
     * @return token id
     */
    public final String getId() {
        return id;
    }

    /**
     *
     * @param id token id
     */
    public final void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @return Red component of token color in range (0,255)
     */
    public final int getRed() {
        return red;
    }

    /**
     *
     * @param red component of token color in range (0,255)
     */
    public final void setRed(int red) {
        this.red = red;
    }

    /**
     *
     * @return Green component of token color in range (0,255)
     */
    public final int getGreen() {
        return green;
    }

    /**
     *
     * @param green component of token color in range (0,255)
     */
    public final void setGreen(int green) {
        this.green = green;
    }

    /**
     *
     * @return Blue component of token color in range (0,255)
     */
    public final int getBlue() {
        return blue;
    }

    /**
     *
     * @param blue component of token color in range (0,255)
     */
    public final void setBlue(int blue) {
        this.blue = blue;
    }
}
