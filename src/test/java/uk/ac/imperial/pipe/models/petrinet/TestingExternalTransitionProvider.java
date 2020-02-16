package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.runner.PlaceMarker;

public class TestingExternalTransitionProvider implements
        ExternalTransitionProvider {

    private PlaceMarker placeMarker;
    private ExecutablePetriNet executablePetriNet;
    private Object context;

    @Override
    public PlaceMarker getPlaceMarker() {
        return placeMarker;
    }

    @Override
    public ExecutablePetriNet getExecutablePetriNet() {
        return executablePetriNet;
    }

    @Override
    public Object getContext() {
        return context;
    }

    public final void setPlaceMarker(PlaceMarker placeMarker) {
        this.placeMarker = placeMarker;
    }

    public final void setExecutablePetriNet(ExecutablePetriNet executablePetriNet) {
        this.executablePetriNet = executablePetriNet;
    }

    public final void setContext(Object context) {
        this.context = context;
    }

}
