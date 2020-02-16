package uk.ac.imperial.pipe.runner;

public interface PlaceMarker {

    public void markPlace(String placeId, String token, int count)
            throws InterfaceException;

}