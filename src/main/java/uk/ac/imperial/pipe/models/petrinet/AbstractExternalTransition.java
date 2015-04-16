package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.runner.PlaceMarker;

public abstract class AbstractExternalTransition implements ExternalTransition {


	private ExternalTransitionProvider externalTransitionProvider;


	@Override
	public abstract void fire();


	@Override
	public void setExternalTransitionProvider(
			ExternalTransitionProvider externalTransitionProvider) {
		this.externalTransitionProvider = externalTransitionProvider; 
	}


	public final ExternalTransitionProvider getExternalTransitionProvider() {
		return externalTransitionProvider;
	}

//	@Override
//	public void setContext(Object context) {
//		this.context = context; 
//	}
//
//	@Override
//	public void setExecutablePetriNet(ExecutablePetriNet executablePetriNet) {
//		this.executablePetriNet = executablePetriNet; 
//	}
//
//	@Override
//	public void setPlaceMarker(PlaceMarker placeMarker) {
//		this.placeMarker = placeMarker; 
//	}

//	@Override
//	public PlaceMarker getPlaceMarker() {
//		return placeMarker;
//	}


}
