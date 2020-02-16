package uk.ac.imperial.pipe.models.petrinet;

public interface ExternalTransition {

    public void fire();

    public void setExternalTransitionProvider(ExternalTransitionProvider externalTransitionProvider);

}
