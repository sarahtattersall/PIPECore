package uk.ac.imperial.pipe.runner;

import static org.junit.Assert.*;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import uk.ac.imperial.pipe.runner.JsonParameters.JsonTransition;

public class JsonParametersTest {

	private JsonParameters json;
	private String simpleJson = "{\"name\":[\"Mary\",\"Ann\"],\"surname\":\"Lastname\"}"; 
	private String validJson = "{\"name\":[\"Mary\",\"Ann\"],\"surname\":\"Lastname\",\"transitions\":{\"T0\":{\"num\":1},\"T1\":[\"sam\", \"sally\"],\"T2\":1, \"T3\":\"someValue\"}}"; 
//	private String simpleJson = "{\"name\": \"Mary\",\"Ann\"],\"surname\": \"Lastname\"}";
	private JsonArray jsonArray;
	private JsonObject jsonObject;

	@Before
	public void setUp() throws Exception {
		jsonArray = Json.createArrayBuilder().add("Mary").add("Ann").build();
		jsonObject = Json.createObjectBuilder().add("name", jsonArray).add("surname", "Lastname").build();
	}
	
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void verifyJsonStringNotBlank() {
    	expectedException.expect(IllegalArgumentException.class); 
    	expectedException.expectMessage("JsonParameters.verifyNotEmptyOrNull:  string is empty or null."); 
    	json = new JsonParameters(""); 

	}
	@Test
	public void verifyJsonStringNotNull() {
		expectedException.expect(IllegalArgumentException.class); 
		expectedException.expectMessage("JsonParameters.verifyNotEmptyOrNull:  string is empty or null."); 
		json = new JsonParameters(null); 
	}
	@Test
	public void throwsIfStringIsNotJsonFormat() throws Exception {
		expectedException.expect(IllegalArgumentException.class); 
		expectedException.expectMessage("JsonParameters.buildJson:  input string is not in Json format."); 
		json = new JsonParameters("fred"); 
	}
	@Test
	public void throwsIfNoTransitionsKey() throws Exception {
		expectedException.expect(IllegalArgumentException.class); 
		expectedException.expectMessage("JsonParameters.buildTransitions:  expecting a Transitions key."); 
		json = new JsonParameters(simpleJson); 
	}
	@Test
	public void hasJsonForIndividualTransition() throws Exception {
		json = new JsonParameters(validJson); 
		JsonParameters.JsonTransition transition = json.getTransition("T0"); 
		assertEquals(1, transition.getJsonObject().getInt("num"));
		transition = json.getTransition("T1"); 
		assertEquals(2, transition.getJsonArray().size());
		assertEquals("sam", transition.getJsonArray().getString(0));
		transition = json.getTransition("T2");
		assertEquals(1, transition.getJsonNumber().intValue());
		transition = json.getTransition("T3");
		assertEquals("someValue", transition.getJsonString().getString());
	}

}
