package uk.ac.imperial.pipe.io.adapters.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;


@XmlAccessorType(XmlAccessType.FIELD)
public class AdaptedConnectable {

    @XmlElement
    private PositionGraphics graphics;

    @XmlAttribute
    private String id;

    @XmlElement(name = "name")
    private NameDetails name = new NameDetails();

    public final String getId() {
        return id;
    }

    public final void setId(String id) {
        this.id = id;
    }

    public final PositionGraphics getGraphics() {
        return graphics;
    }

    public final void setGraphics(PositionGraphics graphics) {
        this.graphics = graphics;
    }

    public final NameDetails getName() {
        return name;
    }

    public final void setNameDetails(NameDetails name) {
        this.name = name;
    }


}
