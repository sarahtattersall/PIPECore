package uk.ac.imperial.pipe.models.petrinet;

public class TestingExternalTransition extends AbstractExternalTransition  {

	
	@Override
	public void fire() {
		((TestingContext) context).setContent(executablePetriNet.getPetriNet().getNameValue()); 
	}

}
