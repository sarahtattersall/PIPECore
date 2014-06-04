package uk.ac.imperial.pipe.io.adapters.model;

import javax.xml.bind.annotation.XmlElement;

/**
 * Verbose way of storing the x, y offset of components and labels
 */
public class OffsetGraphics {
    /**
     * x, y offset
     */
    @XmlElement(name = "offset")
    public Point point;
}
