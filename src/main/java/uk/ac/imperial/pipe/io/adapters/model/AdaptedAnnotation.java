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

    /**
     * Sets the border property
     * @param border of the annotation
     */
    public final void setBorder(boolean border) {
        this.border = border;
    }

    /**
     *
     * @return true if has border
     */
    public final boolean hasBoarder() {
        return border;
    }

    /**
     *
     * @return x coordinate
     */
    public final int getX() {
        return x;
    }

    /**
     *
     * @param x new x coordinate
     */
    public final void setX(int x) {
        this.x = x;
    }

    /**
     *
     * @return y coordinate
     */
    public final int getY() {
        return y;
    }

    /**
     *
     * @param y new y coordinate
     */
    public final void setY(int y) {
        this.y = y;
    }

    /**
     *
     * @return height
     */
    public final int getHeight() {
        return height;
    }

    /**
     *
     * @param height new height
     */
    public final void setHeight(int height) {
        this.height = height;
    }

    /**
     *
     * @return width
     */
    public final int getWidth() {
        return width;
    }

    /**
     *
     * @param width new width
     */
    public final void setWidth(int width) {
        this.width = width;
    }

    /**
     *
     * @return annotation message
     */
    public final String getText() {
        return text;
    }

    /**
     *
     * @param text new annotation message
     */
    public final void setText(String text) {
        this.text = text;
    }
}
