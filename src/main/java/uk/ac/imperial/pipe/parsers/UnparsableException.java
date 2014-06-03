package uk.ac.imperial.pipe.parsers;

/**
 * Exception thrown when parsing an expression cannot be
 * performed
 */
public class UnparsableException extends Exception {
    public UnparsableException(String message) {
        super(message);
    }

    public UnparsableException(String message, Throwable throwable) {
        super(message, throwable);
    }
}