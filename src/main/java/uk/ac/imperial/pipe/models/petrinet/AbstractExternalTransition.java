package uk.ac.imperial.pipe.models.petrinet;

public class AbstractExternalTransition implements ExternalTransition {

	protected ExecutablePetriNet executablePetriNet;
	protected Object context;

	@Override
	public void fire() {
	}

	@Override
	public void setContext(Object context) {
		this.context = context; 
	}

	@Override
	public void setExecutablePetriNet(ExecutablePetriNet executablePetriNet) {
		this.executablePetriNet = executablePetriNet; 
	}

}
