package uk.ac.imperial.pipe.runner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class JsonParametersTest {

    private JsonParameters jsonParameters;
    private String simpleJson = "{\"name\":[\"Mary\",\"Ann\"],\"surname\":\"Lastname\"}";
    private String validJson = "{\"name\":[\"Mary\",\"Ann\"],\"surname\":\"Lastname\",\"transitions\":{\"T0\":{\"num\":1},\"T1\":[\"sam\",\"sally\"],\"T2\":1,\"T3\":\"someValue\",\"T4\":[true,false,null]}}";
    private String afterJson = "{\"name\":[\"Mary\",\"Ann\"],\"surname\":\"Lastname\",\"transitions\":{\"T0\":{\"newKey\":\"newValue\"},\"T1\":[\"sam\",\"sally\"],\"T2\":1,\"T3\":\"someValue\",\"T4\":[true,false,null]}}";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void verifyJsonStringNotBlank() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("JsonParameters.verifyNotEmptyOrNull:  string is empty or null.");
        jsonParameters = new JsonParameters("");

    }

    @Test
    public void verifyJsonStringNotNull() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("JsonParameters.verifyNotEmptyOrNull:  string is empty or null.");
        jsonParameters = new JsonParameters(null);
    }

    @Test
    public void throwsIfStringIsNotJsonFormat() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("JsonParameters.buildJson:  input string is not in Json format.");
        jsonParameters = new JsonParameters("fred");
    }

    @Test
    public void throwsIfNoTransitionsKey() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("JsonParameters.buildTransitions:  expecting a Transitions key.");
        jsonParameters = new JsonParameters(simpleJson);
    }

    @Test
    public void setsActiveTransitionMakingIndividualTransitionAvailable() throws Exception {
        jsonParameters = new JsonParameters(validJson);
        jsonParameters.setActiveTransition("T0"); // called by DiscreteExternalTransition  
        JsonTransition transition = jsonParameters.getActiveTransition(); // called by ExternalTransition 
        assertEquals(1, transition.getJsonObject().getInt("num"));
        jsonParameters.setActiveTransition("T1");
        transition = jsonParameters.getActiveTransition();
        assertEquals(2, transition.getJsonArray().size());
        assertEquals("sam", transition.getJsonArray().getString(0));
        jsonParameters.setActiveTransition("T2");
        transition = jsonParameters.getActiveTransition();
        assertEquals(1, transition.getJsonNumber().intValue());
        jsonParameters.setActiveTransition("T3");
        transition = jsonParameters.getActiveTransition();
        assertEquals("someValue", transition.getJsonString().getString());
        jsonParameters.setActiveTransition("T4");
        transition = jsonParameters.getActiveTransition();
        assertEquals(true, transition.getJsonArray().getBoolean(0));
        assertEquals(false, transition.getJsonArray().getBoolean(1));
        assertEquals(JsonValue.ValueType.NULL, transition.getJsonArray().get(2).getValueType());
    }

    @Test
    public void throwsIfIndividualTransitionDoesNotExist() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("JsonParameters.setActiveTransition:  transition does not exist: T99");
        jsonParameters = new JsonParameters(validJson);
        jsonParameters.setActiveTransition("T99");
    }

    @Test
    public void updatesActiveTransition() throws Exception {
        jsonParameters = new JsonParameters(validJson);
        jsonParameters.setActiveTransition("T0"); // called by DiscreteExternalTransition  
        JsonObject newObject = Json.createObjectBuilder()
                .add("newKey", "newValue")
                .build();
        jsonParameters.updateTransition(newObject);
        JsonTransition transition = jsonParameters.getActiveTransition(); // called by ExternalTransition 
        assertTrue(transition.getJsonObject().containsKey("newKey"));
        assertEquals("newValue", transition.getJsonObject().getString("newKey"));
        assertEquals(afterJson, jsonParameters.toString());
    }
}
