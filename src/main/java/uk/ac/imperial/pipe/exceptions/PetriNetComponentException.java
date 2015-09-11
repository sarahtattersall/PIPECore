package uk.ac.imperial.pipe.exceptions;

/**
 * Represents an error that can be thrown by the {@link uk.ac.imperial.pipe.models.petrinet.PetriNet}
 * when modifying the components it stores
 * known subclasses:  {@link PetriNetComponentNotFoundException}
 */
public class PetriNetComponentException  extends Exception {
    /**
     *
     * @param message message of exception
     */
    public PetriNetComponentException(String message) {
        super(message);
    }

    /**
     *
     * @param throwable throwable exception
     */
    public PetriNetComponentException(Throwable throwable) {
        super(throwable);
    }
}