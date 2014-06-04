package uk.ac.imperial.pipe.parsers;

/**
 * Exception thrown when parsing an expression cannot be
 * performed
 */
public class UnparsableException extends Exception {
    /**
     *
     * @param message exception message
     */
    public UnparsableException(String message) {
        super(message);
    }

    /**
     *
     * @param message exception message
     * @param throwable cause of the exception
     */
    public UnparsableException(String message, Throwable throwable) {
        super(message, throwable);
    }
}