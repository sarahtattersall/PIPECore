package uk.ac.imperial.pipe.models.petrinet;

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

}
