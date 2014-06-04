package uk.ac.imperial.pipe.io.adapters.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Verbose way to store a Petri net components id in PNML format
 */
@XmlAccessorType(XmlAccessType.FIELD)
public final class NameDetails {
    /**
     * Id
     */
    @XmlElement(name = "value")
    private String name;

    /**
     * Offset graphics
     */
    @XmlElement
    private OffsetGraphics graphics = new OffsetGraphics();

    /**
     *
     * @return x, y offset
     */
    public OffsetGraphics getGraphics() {
        return graphics;
    }

    /**
     *
     * @param graphics x, y offset
     */
    public void setGraphics(OffsetGraphics graphics) {
        this.graphics = graphics;
    }

    /**
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name component name
     */
    public void setName(String name) {
        this.name = name;
    }
}