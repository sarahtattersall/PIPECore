package uk.ac.imperial.pipe.models.petrinet;

/**
 * Annotation is used
 */
public interface Annotation extends PlaceablePetriNetComponent {
    /**
     *
     * @return text in the annotation
     */
    String getText();

    /**
     *
     * @param text new text for annotation
     */
    void setText(String text);

    /**
     *
     * @return true if the annotation should have a border outline
     */
    boolean hasBorder();
}
