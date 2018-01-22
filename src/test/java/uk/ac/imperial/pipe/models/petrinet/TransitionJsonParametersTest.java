package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import uk.ac.imperial.pipe.runner.JsonParameters;

public class TransitionJsonParametersTest {

    private String validJson = "{\"name\":[\"Mary\",\"Ann\"],\"surname\":\"Lastname\",\"transitions\":{\"T0\":{\"num\":1},\"T1\":[\"sam\",\"sally\"],\"T2\":1,\"T3\":\"someValue\",\"T4\":[true,false,null]}}";
    private JsonParameters parms;
    private TransitionJsonParameters transition;
    private JsonObject transitionParameters;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private TestingExternalTransitionProvider provider;

    @Before
    public void setUp() throws Exception {
        parms = new JsonParameters(validJson);
        parms.setActiveTransition("T0"); // DiscreteExternalTransition will do this
        transition = new TestingTransitionJsonParameters();
        provider = new TestingExternalTransitionProvider();
        provider.setContext(parms);
        transition.setExternalTransitionProvider(provider);
    }

    @Test
    public void getsJsonParameters() {
        transitionParameters = transition.getParameters();
        assertEquals(1, transitionParameters.getInt("num"));
    }

    @Test
    public void updatesJsonParameters() {
        JsonObject newObject = Json.createObjectBuilder()
                .add("newKey", "newValue")
                .build();
        transition.updateParameters(newObject);
        transitionParameters = transition.getParameters();
        assertTrue(transitionParameters.containsKey("newKey"));
        assertEquals("newValue", transitionParameters.getString("newKey"));
        assertFalse(transitionParameters.containsKey("num"));

    }

    @Test
    public void throwsIllegalArgumentExceptionIfContextNotJsonParameters() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException
                .expectMessage("AbstractTransitionJsonParameters.setContext:  expected JsonParameters, found: " +
                        java.lang.String.class.getName());
        provider.setContext("Not a JsonParameters");
        transition.setExternalTransitionProvider(provider);
    }

}
