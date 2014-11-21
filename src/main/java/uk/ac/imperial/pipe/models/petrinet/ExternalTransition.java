package uk.ac.imperial.pipe.models.petrinet;

public interface ExternalTransition {

	public void setContext(Object object);

	public void setExecutablePetriNet(ExecutablePetriNet executablePetriNet);

	public void fire();

}
