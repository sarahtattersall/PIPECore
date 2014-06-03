package uk.ac.imperial.pipe.models.petrinet;

/**
 * For petri net components that have an idea of location
 */
public interface PlaceablePetriNetComponent extends PetriNetComponent {

    /**
     * Message fired when the x attribute is changed
     */
    String X_CHANGE_MESSAGE = "x";

    /**
     * Message fired when the y attribute is changed
     */
    String Y_CHANGE_MESSAGE = "y";

    /**
     * Message fired when the width attribute is changed
     */
    String WIDTH_CHANGE_MESSAGE = "width";

    /**
     * Message fired when the height attribute is changed
     */
    String HEIGHT_CHANGE_MESSAGE = "height";

    /**
     *
     * @return x location of Petri net component
     */
    int getX();

    /**
     *
     * @param x new x location of Petri net component
     */
    void setX(int x);

    /**
     *
     * @return y location of Petri net component
     */
    int getY();

    /**
     *
     * @param y new y location of Petri net component
     */
    void setY(int y);

    /**
     *
     * @return height of Petri net component
     */
    int getHeight();

    /**
     *
     * @return width of Petri net component
     */
    int getWidth();
}
