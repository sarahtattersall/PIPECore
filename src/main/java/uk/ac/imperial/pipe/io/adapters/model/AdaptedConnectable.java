package uk.ac.imperial.pipe.io.adapters.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * PNML Adaption of a connectable object
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class AdaptedConnectable {

    /**
     * Where the object is located in the Petri net
     */
    @XmlElement
    private PositionGraphics graphics;

    /**
     * Component id
     */
    @XmlAttribute
    private String id;

    /**
     * Name details in vebose PNML format
     */
    @XmlElement(name = "name")
    private NameDetails name = new NameDetails();

    /**
     *
     * @return component unique id
     */
    public final String getId() {
        return id;
    }

    /**
     *
     * @param id component unique id
     */
    public final void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @return position of component
     */
    public final PositionGraphics getGraphics() {
        return graphics;
    }

    /**
     *
     * @param graphics position of component
     */
    public final void setGraphics(PositionGraphics graphics) {
        this.graphics = graphics;
    }

    /**
     *
     * @return verbose PNML component name
     */
    public final NameDetails getName() {
        return name;
    }

    /**
     *
     * @param name vebose PNML component name
     */
    public final void setNameDetails(NameDetails name) {
        this.name = name;
    }
}
