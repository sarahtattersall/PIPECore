package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.runner.InterfaceException;

public class TestingExternalTransition extends AbstractExternalTransition {

    @Override
    public void fire() {
        TestingContext context = (TestingContext) getExternalTransitionProvider().getContext();
        // CHANGED: The transition should not keep track of the whole PN,
        // therefore has not necessarily a current reference.
        //context.setContent(getExternalTransitionProvider().getExecutablePetriNet().getPetriNet().getNameValue());
        context.setContent("net");
        if (context.isMark()) {
            try {
                getExternalTransitionProvider().getPlaceMarker().markPlace(context.getPlaceId(), "Default", 2);
            } catch (InterfaceException e) {
                e.printStackTrace();
            }
        }
    }

}