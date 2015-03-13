package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;
import uk.ac.imperial.pipe.visitor.TransitionCloner;

public class DiscreteExternalTransitionTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();
	private ExecutablePetriNet executablePetriNet;
	private Transition transition;
	
	@Before
	public void setUp() throws Exception {
		PetriNet net = new PetriNet(new NormalPetriNetName("net"));
		executablePetriNet = net.getExecutablePetriNet();  
		transition = new DiscreteExternalTransition("T1", "T1","uk.ac.imperial.pipe.models.petrinet.TestingExternalTransition"); 
	}
	
	@Test
	public void throwsIfClassDoesntExist() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("DiscreteExternalTransition.init:  client class does not exist: org.something.NonExistentClass");
		@SuppressWarnings("unused")
		Transition transition = new DiscreteExternalTransition("T1", "T1", "org.something.NonExistentClass"); 
	}
	@Test
	public void throwsIfClassDoesNotImplementExternalTransition() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("DiscreteExternalTransition.init:  client class does not implement uk.ac.imperial.pipe.models.petrinet.ExternalTransition: uk.ac.imperial.pipe.models.petrinet.TestingNotExternalTransition");
		@SuppressWarnings("unused")
		Transition transition = new DiscreteExternalTransition("T1", "T1","uk.ac.imperial.pipe.models.petrinet.TestingNotExternalTransition"); 
	}
	@Test
	public void throwsIfClassDoesNotHaveNullConstructor() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("DiscreteExternalTransition.init:  client class does not have a null constructor: uk.ac.imperial.pipe.models.petrinet.TestingNotNullConstructor");
		@SuppressWarnings("unused")
		Transition transition = new DiscreteExternalTransition("T1", "T1","uk.ac.imperial.pipe.models.petrinet.TestingNotNullConstructor"); 
	}
	@Test
	public void throwsIfClassDoesNotHavePublicConstructor() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("DiscreteExternalTransition.init:  client class does not have a public constructor: uk.ac.imperial.pipe.models.petrinet.TestingNotPublicConstructor");
		@SuppressWarnings("unused")
		Transition transition = new DiscreteExternalTransition("T1", "T1","uk.ac.imperial.pipe.models.petrinet.TestingNotPublicConstructor"); 
	}
	@Test
	public void throwsIfClassNotProvided() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("DiscreteExternalTransition.init:  client class name not specified.");
		@SuppressWarnings("unused")
		Transition transition = new DiscreteExternalTransition("T1", "T1",null); 
	}
	
	@Test
	public void externalTransitionLoadsClass() throws Exception {
		Transition transition = new DiscreteExternalTransition("T1", "T1","uk.ac.imperial.pipe.models.petrinet.TestingExternalTransition"); 
		ExternalTransition externalTransition = ((DiscreteExternalTransition) transition).getClient();  
		assertTrue(externalTransition instanceof TestingExternalTransition); 
//		transition.setContext("context");
//		transition.fire(); 
//		((Transition) transition).fire(); 
	}
	@Test
	public void externalTransitionFiresWithContextAndExecutablePetriNet() throws Exception {
		((DiscreteExternalTransition) transition).executablePetriNet = executablePetriNet;   
		ExternalTransition externalTransition = ((DiscreteExternalTransition) transition).getClient();  
		assertTrue(externalTransition instanceof TestingExternalTransition); 
		TestingContext test = new TestingContext(2);
		externalTransition.setContext(test);
		externalTransition.setExecutablePetriNet(executablePetriNet);
		externalTransition.fire();
		assertEquals("net2", test.getUpdatedContext()); 
	}
	@Test
	public void externalTransitionInvokedByDiscreteExternalTransition() throws Exception {
		((DiscreteExternalTransition) transition).executablePetriNet = executablePetriNet;   
		ExternalTransition externalTransition = ((DiscreteExternalTransition) transition).getClient();  
		assertTrue(externalTransition instanceof TestingExternalTransition); 
		TestingContext test = new TestingContext(2);
		((DiscreteExternalTransition) transition).setContextForClient(test);   
//		externalTransition.setContext(test);
//		externalTransition.setExecutablePetriNet(executablePetriNet);
//		externalTransition.fire();
		transition.fire(); 
		assertEquals("net2", test.getUpdatedContext()); 
	}
	@Test
	public void transitionClonerCreatesNewInstance() throws Exception {
		TransitionCloner cloner = new TransitionCloner(); 
		transition.accept(cloner); 
		Transition newTransition = cloner.cloned;
		assertEquals("T1", newTransition.getId()); 
		assertTrue(((DiscreteExternalTransition) newTransition).getClient() instanceof TestingExternalTransition); 
		assertEquals(transition, newTransition); 
	}
	@Test
	public void testEquals() throws Exception {
		Transition transition2 = new DiscreteExternalTransition("T1", "T1","uk.ac.imperial.pipe.models.petrinet.TestingExternalTransition"); 
		assertEquals(transition, transition2); 	
		transition2 = new DiscreteTransition("T1"); 
		assertNotEquals("different transition types",transition, transition2); 	
		transition2 = new DiscreteExternalTransition("T1", "T1","uk.ac.imperial.pipe.models.petrinet.TestingExternalTransition2"); 
		assertNotEquals("different client class",transition, transition2); 	
	}
}
