package uk.ac.imperial.pipe.models.petrinet;

public class TestingNotPublicConstructor implements ExternalTransition {

    private TestingNotPublicConstructor() {
    }

    @Override
    public void fire() {
    }

    @Override
    public void setExternalTransitionProvider(
            ExternalTransitionProvider externalTransitionProvider) {
    }

}
