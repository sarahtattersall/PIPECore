package uk.ac.imperial.pipe.models.petrinet;

public class RecursiveIncludeException extends Exception {

    private static final long serialVersionUID = 1L;

    public RecursiveIncludeException() {
    }

    public RecursiveIncludeException(String message) {
        super(message);
    }

    public RecursiveIncludeException(Throwable cause) {
        super(cause);
    }

    public RecursiveIncludeException(String message, Throwable cause) {
        super(message, cause);
    }

    public RecursiveIncludeException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
