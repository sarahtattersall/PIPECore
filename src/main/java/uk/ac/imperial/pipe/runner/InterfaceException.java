package uk.ac.imperial.pipe.runner;

public class InterfaceException extends Exception {

    private static final long serialVersionUID = 1L;

    public InterfaceException() {
    }

    public InterfaceException(String message) {
        super(message);
    }

    public InterfaceException(Throwable cause) {
        super(cause);
    }

    public InterfaceException(String message, Throwable cause) {
        super(message, cause);
    }

    public InterfaceException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
