package uk.ac.imperial.pipe.exceptions;

/**
 * This exception represents that the {@link uk.ac.imperial.pipe.models.component.rate.FunctionalRateParameter}
 * has an invalid rate for the {@link uk.ac.imperial.pipe.models.petrinet.PetriNet}
 */
public class InvalidRateException extends Exception {
    public InvalidRateException(String invalidRate) {
        super("Rate of " + invalidRate + " is invalid");
    }
}
