package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.runner.InterfaceException;
import uk.ac.imperial.pipe.runner.JsonParameters;
import uk.ac.imperial.pipe.runner.PlaceMarker;
import uk.ac.imperial.pipe.visitor.TransitionCloner;

public class DiscreteExternalTransitionTest implements PlaceMarker {

    @Rule
    public ExpectedException exception = ExpectedException.none();
    private ExecutablePetriNet executablePetriNet;
    private DiscreteExternalTransition transition;

    @Before
    public void setUp() throws Exception {
        PetriNet net = buildNet();
        executablePetriNet = net.getExecutablePetriNet();
        transition = new DiscreteExternalTransition("T1", "T1",
                "uk.ac.imperial.pipe.models.petrinet.TestingExternalTransition");
    }

    @Test
    public void throwsIfClassDoesntExist() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception
                .expectMessage("DiscreteExternalTransition.init:  client class does not exist: org.something.NonExistentClass");
        transition = new DiscreteExternalTransition("T1", "T1", "org.something.NonExistentClass");
    }

    @Test
    public void throwsIfClassDoesNotImplementExternalTransition() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception
                .expectMessage("DiscreteExternalTransition.init:  client class does not implement uk.ac.imperial.pipe.models.petrinet.ExternalTransition: uk.ac.imperial.pipe.models.petrinet.TestingNotExternalTransition");
        transition = new DiscreteExternalTransition("T1", "T1",
                "uk.ac.imperial.pipe.models.petrinet.TestingNotExternalTransition");
    }

    @Test
    public void throwsIfClassDoesNotHaveNullConstructor() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception
                .expectMessage("DiscreteExternalTransition.init:  client class does not have a null constructor: uk.ac.imperial.pipe.models.petrinet.TestingNotNullConstructor");
        transition = new DiscreteExternalTransition("T1", "T1",
                "uk.ac.imperial.pipe.models.petrinet.TestingNotNullConstructor");
    }

    @Test
    public void throwsIfClassDoesNotHavePublicConstructor() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception
                .expectMessage("DiscreteExternalTransition.init:  client class does not have a public constructor: uk.ac.imperial.pipe.models.petrinet.TestingNotPublicConstructor");
        transition = new DiscreteExternalTransition("T1", "T1",
                "uk.ac.imperial.pipe.models.petrinet.TestingNotPublicConstructor");
    }

    @Test
    public void throwsIfClassNotProvided() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("DiscreteExternalTransition.init:  client class name not specified.");
        transition = new DiscreteExternalTransition("T1", "T1", null);
    }

    @Test
    public void throwsIfAccessToContextIsAttemptedButContextWasNotSet() throws Exception {
        exception.expect(IllegalStateException.class);
        exception
                .expectMessage("ExternalTransitionProvider.getContext:  client uk.ac.imperial.pipe.models.petrinet.TestingExternalTransition attempted to use Context but none was provided.  Use Runner.setTransitionContext(String transitionId, Object object).");
        transition.fire();
    }

    @Test
    public void externalTransitionLoadsClass() throws Exception {
        transition = new DiscreteExternalTransition("T1", "T1",
                "uk.ac.imperial.pipe.models.petrinet.TestingExternalTransition");
        ExternalTransition externalTransition = transition.getClient();
        assertTrue(externalTransition instanceof TestingExternalTransition);
    }

    @Test
    public void externalTransitionFiresWithContextAndExecutablePetriNet() throws Exception {
        transition.executablePetriNet = executablePetriNet;
        ExternalTransition externalTransition = transition.getClient();
        assertTrue(externalTransition instanceof TestingExternalTransition);
        TestingContext test = new TestingContext(2);
        transition.setContextForClient(test);
        transition.setPlaceMarker(this);
        externalTransition.setExternalTransitionProvider(transition);
        externalTransition.fire();
        assertEquals("net2", test.getUpdatedContext());
    }

    @Test
    public void externalTransitionInvokedByDiscreteExternalTransition() throws Exception {
        transition.executablePetriNet = executablePetriNet;
        ExternalTransition externalTransition = transition.getClient();
        assertTrue(externalTransition instanceof TestingExternalTransition);
        TestingContext test = new TestingContext(2);
        transition.setContextForClient(test);
        transition.setPlaceMarker(this);
        transition.fire();
        assertEquals("net2", test.getUpdatedContext());
    }

    @Test
    public void canUpdateAnotherPlaceInExecutablePetriNet() throws Exception {
        transition.executablePetriNet = executablePetriNet;
        ExternalTransition externalTransition = transition.getClient();
        assertTrue(externalTransition instanceof TestingExternalTransition);
        TestingContext test = new TestingContext(2);
        transition.setContextForClient(test);
        transition.setPlaceMarker(this);
        assertEquals(0, executablePetriNet.getComponent("P1", Place.class).getTokenCount("Default"));
        transition.fire();
        assertEquals(2, executablePetriNet.getComponent("P1", Place.class).getTokenCount("Default"));
    }

    @Test
    public void setsActiveTransitionIfContextIsJsonParameters() throws Exception {
        transition = new DiscreteExternalTransition("T0", "T0",
                "uk.ac.imperial.pipe.models.petrinet.TestingTransitionJsonParameters");
        transition.executablePetriNet = executablePetriNet;
        ExternalTransition externalTransition = transition.getClient();
        assertTrue(externalTransition instanceof TestingTransitionJsonParameters);
        String validJson = "{\"name\":[\"Mary\",\"Ann\"],\"surname\":\"Lastname\",\"transitions\":{\"T0\":{\"num\":1},\"T1\":[\"sam\",\"sally\"],\"T2\":1,\"T3\":\"someValue\",\"T4\":[true,false,null]}}";
        JsonParameters test = new JsonParameters(validJson);
        transition.setContextForClient(test);
        transition.fire();
        TestingTransitionJsonParameters testingTransitionJsonParameters = (TestingTransitionJsonParameters) externalTransition;
        assertEquals(1, testingTransitionJsonParameters.getNum());
    }

    @Test
    public void contextIsJsonParametersIfClientIsTransitionJsonParameters() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception
                .expectMessage("DiscreteExternalTransition.setContextForClient: Client is TransitionJsonParameters but context is: java.lang.String");
        transition = new DiscreteExternalTransition("T0", "T0",
                "uk.ac.imperial.pipe.models.petrinet.TestingTransitionJsonParameters");
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
        Transition transition2 = new DiscreteExternalTransition("T1", "T1",
                "uk.ac.imperial.pipe.models.petrinet.TestingExternalTransition");
        assertEquals(transition, transition2);
        transition2 = new DiscreteTransition("T1");
        assertNotEquals("different transition types", transition, transition2);
        transition2 = new DiscreteExternalTransition("T1", "T1",
                "uk.ac.imperial.pipe.models.petrinet.TestingExternalTransition2");
        assertNotEquals("different client class", transition, transition2);
    }

    @Override
    public void markPlace(String placeId, String token, int count)
            throws InterfaceException {
        try {
            executablePetriNet.getComponent(placeId, Place.class).setTokenCount(token, count);
        } catch (PetriNetComponentNotFoundException e) {
            e.printStackTrace();
        }
    }

    private PetriNet buildNet() throws PetriNetComponentException {
        PetriNet net = APetriNet.named("net").and(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P0").containing(1, "Default").token())
                .and(APlace.withId("P1").externallyAccessible()).and(AnImmediateTransition.withId("T0"))
                .and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token())
                .andFinally(ANormalArc.withSource("T0").andTarget("P1").with("1", "Default").token());
        return net;
    }
}
