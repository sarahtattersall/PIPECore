package uk.ac.imperial.pipe.models.petrinet;

public class TestingExternalTransition implements ExternalTransition {

	
	private ExecutablePetriNet executablePetriNet;
	private TestingContext context;

	@Override
	public void setContext(Object context) {
		this.context = (TestingContext) context; 
	}

	@Override
	public void setExecutablePetriNet(ExecutablePetriNet executablePetriNet) {
		this.executablePetriNet = executablePetriNet; 
	}

	@Override
	public void fire() {
		context.setContent(executablePetriNet.getPetriNet().getNameValue()); 
	}

}
