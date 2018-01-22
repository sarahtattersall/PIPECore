package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.runner.PlaceMarker;

public interface ExternalTransitionProvider {

    public PlaceMarker getPlaceMarker();

    public ExecutablePetriNet getExecutablePetriNet();

    public Object getContext();

}
