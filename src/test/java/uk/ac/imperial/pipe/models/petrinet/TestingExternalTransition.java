package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.runner.InterfaceException;

public class TestingExternalTransition extends AbstractExternalTransition  {

	
	@Override
	public void fire() {
		TestingContext context = (TestingContext) getExternalTransitionProvider().getContext(); 
		context.setContent(getExternalTransitionProvider().getExecutablePetriNet().getPetriNet().getNameValue()); 
		if (context.isMark()) {
			try {
				getExternalTransitionProvider().getPlaceMarker().markPlace(context.getPlaceId(), "Default", 2);
			} catch (InterfaceException e) {
				e.printStackTrace();
			}
		}
	}


}
