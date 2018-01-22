package uk.ac.imperial.pipe.exceptions;

public class IncludeException extends Exception {

    private static final long serialVersionUID = 1L;

    public IncludeException() {
    }

    public IncludeException(String message) {
        super(message);
    }

    public IncludeException(Throwable cause) {
        super(cause);
    }

    public IncludeException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncludeException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
