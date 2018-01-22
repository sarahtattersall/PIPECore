package uk.ac.imperial.pipe.models.petrinet;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

/**
 * This class is for labels that can be added to the Petri net
 */
public class AnnotationImpl extends AbstractPetriNetPubSub implements Annotation {

    /**
     * Message fired when annotation text is changed
     */
    public static final String TEXT_CHANGE_MESSAGE = "text";

    /**
     * Message fired when the border is toggled on/off
     */
    public static final String TOGGLE_BORDER_CHANGE_MESSAGE = "toggleBorder";

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AnnotationImpl)) {
            return false;
        }

        AnnotationImpl that = (AnnotationImpl) o;

        if (border != that.border) {
            return false;
        }
        if (height != that.height) {
            return false;
        }
        if (width != that.width) {
            return false;
        }
        if (x != that.x) {
            return false;
        }
        if (y != that.y) {
            return false;
        }
        if (!text.equals(that.text)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (border ? 1 : 0);
        result = 31 * result + x;
        result = 31 * result + y;
        result = 31 * result + text.hashCode();
        result = 31 * result + width;
        result = 31 * result + height;
        return result;
    }

    /**

     * Copy constructor
     * @param annotation to copy
     */
    public AnnotationImpl(AnnotationImpl annotation) {
        this(annotation.x, annotation.y, annotation.text, annotation.width, annotation.height, annotation.border);
    }

    /**
     * Constructor
     * @param x coordinate
     * @param y coordinate
     * @param text of the annotation
     * @param width of the annotation
     * @param height of the annotation
     * @param border of the annotation
     */
    public AnnotationImpl(int x, int y, String text, int width, int height, boolean border) {
        this.border = border;
        this.x = x;
        this.y = y;
        this.text = text;
        this.width = width;
        this.height = height;
    }

    /**
     * Setting border to true implies the annotation should be displayed with a boreder
     * @param border of the annotation
     */
    public final void setBorder(boolean border) {
        this.border = border;
    }

    /**
     *
     * @return true if the annotation has a border
     */
    @Override
    public final boolean hasBorder() {
        return border;
    }

    /**
     *
     * @return x location of the top left corner of the annotation
     */
    @Override
    public int getX() {
        return x;
    }

    /**
     *
     * @param x new x location of annotation
     */
    @Override
    public void setX(int x) {
        int old = this.x;
        this.x = x;
        changeSupport.firePropertyChange(X_CHANGE_MESSAGE, old, x);
    }

    /**
     *
     * @return y coordinate of top left corner
     */
    @Override
    public int getY() {
        return y;
    }

    /**
     *
     * @param y new y location of Petri net component
     */
    @Override
    public final void setY(int y) {
        int old = this.y;
        this.y = y;
        changeSupport.firePropertyChange(Y_CHANGE_MESSAGE, old, y);
    }

    /**
     *
     * @return annotation height
     */
    @Override
    public int getHeight() {
        return height;
    }

    /**
     *
     * @return annotation width
     */
    @Override
    public int getWidth() {
        return width;
    }

    /**
     *
     * @param width new annotation width
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     *
     * @param height new annotation height
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     *
     * @return text on annotation
     */
    @Override
    public String getText() {
        return text;
    }

    /**
     *
     * @param text new text for annotation
     */
    @Override
    public void setText(String text) {
        String old = this.text;
        this.text = text;
        changeSupport.firePropertyChange(TEXT_CHANGE_MESSAGE, old, text);
        changeSupport.firePropertyChange(ID_CHANGE_MESSAGE, old, text);
    }

    /**
     *
     * @return true because we can always select annotations
     */
    @Override
    public boolean isSelectable() {
        return true;
    }

    /**
     *
     * @return true because we can always drag annotations
     */
    @Override
    public boolean isDraggable() {
        return true;
    }

    /**
     * Accept visitor if its an annotation visitor
     * @param visitor to be accepted
     */
    @Override
    public void accept(PetriNetComponentVisitor visitor) {
        if (visitor instanceof AnnotationVisitor) {
            ((AnnotationVisitor) visitor).visit(this);
        }
        if (visitor instanceof AnnotationImplVisitor) {
            ((AnnotationImplVisitor) visitor).visit(this);
        }
    }

    /**
     *
     * @return annotation text
     */
    @Override
    public String getId() {
        return getText();
    }

    @Override
    public void setId(String id) {
        //TODO: WORK OUT WHAT THESE SHOULD DO
    }

    /**
     * Toggles the border on/off
     */
    public final void toggleBorder() {
        border = !border;
        changeSupport.firePropertyChange(TOGGLE_BORDER_CHANGE_MESSAGE, !border, border);
    }
}
