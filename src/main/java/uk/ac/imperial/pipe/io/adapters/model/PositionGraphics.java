package uk.ac.imperial.pipe.io.adapters.model;

import javax.xml.bind.annotation.XmlElement;

/**
 * Verbose way of storing x, y, that needs to be called "position"
 * for PNML compatibility issues
 */
public class PositionGraphics {
    /**
     * x, y coordinate
     */
    @XmlElement(name = "position")
    public Point point;
}
