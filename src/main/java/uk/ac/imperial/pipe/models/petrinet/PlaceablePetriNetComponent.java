package uk.ac.imperial.pipe.models.petrinet;

/**
 * For petri net components that have an idea of location
 */
public interface PlaceablePetriNetComponent extends PetriNetComponent {

    /**
     * Message fired when the x attribute is changed
     */
    static final String X_CHANGE_MESSAGE = "x";

    /**
     * Message fired when the y attribute is changed
     */
    static final String Y_CHANGE_MESSAGE = "y";

    /**
     * Message fired when the width attribute is changed
     */
    static final String WIDTH_CHANGE_MESSAGE = "width";

    /**
     * Message fired when the height attribute is changed
     */
    static final String HEIGHT_CHANGE_MESSAGE = "height";

    /**
     *
     * @return x location of Petri net component
     */
    abstract int getX();

    /**
     *
     * @param x new x location of Petri net component
     */
    abstract void setX(int x);

    /**
     *
     * @return y location of Petri net component
     */
    abstract int getY();

    /**
     *
     * @param y new y location of Petri net component
     */
    abstract void setY(int y);

    /**
     *
     * @return height of Petri net component
     */
    abstract int getHeight();

    /**
     *
     * @return width of Petri net component
     * @return width of Petri net component
     */
    abstract int getWidth();
}
