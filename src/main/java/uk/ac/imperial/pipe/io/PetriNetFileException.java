package uk.ac.imperial.pipe.io;

@SuppressWarnings("serial")
public class PetriNetFileException extends Exception {

    public PetriNetFileException() {
    }

    public PetriNetFileException(String message) {
        super(message);
    }

    public PetriNetFileException(Throwable cause) {
        super(cause);
    }

    public PetriNetFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public PetriNetFileException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
