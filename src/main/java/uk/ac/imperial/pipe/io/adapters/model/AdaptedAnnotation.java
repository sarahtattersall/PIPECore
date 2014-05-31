package uk.ac.imperial.pipe.io.adapters.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * This class is a direct map of {@link uk.ac.imperial.pipe.models.petrinet.AnnotationImpl}
 * and is used in marshalling the annotation fields into/out of XML.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class AdaptedAnnotation {

    /**
     * True if display border for annotation box
     */
    @XmlAttribute
    private boolean border;

    /**
     * Top left x position
     */
    @XmlAttribute
    private int x;

    /**
     * Top left y position
     */
    @XmlAttribute
    private int y;

    /**
     * Text to be displayed
     */
    @XmlElement
    private String text;

    /**
     * Annotation box width
     */
    @XmlAttribute
    private int width;

    /**
     * Annotation box height
     */
    @XmlAttribute
    private int height;

    public final void setBorder(boolean border) {
        this.border = border;
    }

    public final boolean hasBoarder() {
        return border;
    }

    public final  int getX() {
        return x;
    }

    public final void setX(int x) {
        this.x = x;
    }

    public final int getY() {
        return y;
    }

    public final void setY(int y) {
        this.y = y;
    }

    public final int getHeight() {
        return height;
    }

    public final void setHeight(int height) {
        this.height = height;
    }

    public final int getWidth() {
        return width;
    }

    public final void setWidth(int width) {
        this.width = width;
    }

    public final String getText() {
        return text;
    }

    public final void setText(String text) {
        this.text = text;
    }
}
