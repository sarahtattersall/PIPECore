package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;
import uk.ac.imperial.pipe.runner.JsonParameters;
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
		transition = new DiscreteExternalTransition("T1", "T1", "org.something.NonExistentClass"); 
	}
	@Test
	public void throwsIfClassDoesNotImplementExternalTransition() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("DiscreteExternalTransition.init:  client class does not implement uk.ac.imperial.pipe.models.petrinet.ExternalTransition: uk.ac.imperial.pipe.models.petrinet.TestingNotExternalTransition");
		transition = new DiscreteExternalTransition("T1", "T1","uk.ac.imperial.pipe.models.petrinet.TestingNotExternalTransition"); 
	}
	@Test
	public void throwsIfClassDoesNotHaveNullConstructor() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("DiscreteExternalTransition.init:  client class does not have a null constructor: uk.ac.imperial.pipe.models.petrinet.TestingNotNullConstructor");
		transition = new DiscreteExternalTransition("T1", "T1","uk.ac.imperial.pipe.models.petrinet.TestingNotNullConstructor"); 
	}
	@Test
	public void throwsIfClassDoesNotHavePublicConstructor() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("DiscreteExternalTransition.init:  client class does not have a public constructor: uk.ac.imperial.pipe.models.petrinet.TestingNotPublicConstructor");
		transition = new DiscreteExternalTransition("T1", "T1","uk.ac.imperial.pipe.models.petrinet.TestingNotPublicConstructor"); 
	}
	@Test
	public void throwsIfClassNotProvided() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("DiscreteExternalTransition.init:  client class name not specified.");
		transition = new DiscreteExternalTransition("T1", "T1",null); 
	}
	
	@Test
	public void externalTransitionLoadsClass() throws Exception {
		transition = new DiscreteExternalTransition("T1", "T1","uk.ac.imperial.pipe.models.petrinet.TestingExternalTransition"); 
		ExternalTransition externalTransition = ((DiscreteExternalTransition) transition).getClient();  
		assertTrue(externalTransition instanceof TestingExternalTransition); 
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
		transition.fire(); 
		assertEquals("net2", test.getUpdatedContext()); 
	}
	@Test
	public void setsActiveTransitionIfContextIsJsonParameters() throws Exception {
		transition = new DiscreteExternalTransition("T0", "T0","uk.ac.imperial.pipe.models.petrinet.TestingTransitionJsonParameters"); 
		((DiscreteExternalTransition) transition).executablePetriNet = executablePetriNet;   
		ExternalTransition externalTransition = ((DiscreteExternalTransition) transition).getClient();  
		assertTrue(externalTransition instanceof TestingTransitionJsonParameters); 
		String validJson = "{\"name\":[\"Mary\",\"Ann\"],\"surname\":\"Lastname\",\"transitions\":{\"T0\":{\"num\":1},\"T1\":[\"sam\",\"sally\"],\"T2\":1,\"T3\":\"someValue\",\"T4\":[true,false,null]}}"; 
		JsonParameters test = new JsonParameters(validJson);
		((DiscreteExternalTransition) transition).setContextForClient(test);   
		transition.fire(); 
		TestingTransitionJsonParameters testingTransitionJsonParameters = (TestingTransitionJsonParameters) externalTransition;
		assertEquals(1, testingTransitionJsonParameters.getNum());
	}
	@Test
	public void contextIsJsonParametersIfClientIsTransitionJsonParameters() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("DiscreteExternalTransition.setContextForClient: Client is TransitionJsonParameters but context is: java.lang.String");
		transition = new DiscreteExternalTransition("T0", "T0","uk.ac.imperial.pipe.models.petrinet.TestingTransitionJsonParameters"); 
		((DiscreteExternalTransition) transition).setContextForClient("Not a Json Parameters");  
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
