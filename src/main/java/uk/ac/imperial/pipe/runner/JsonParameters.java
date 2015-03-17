package uk.ac.imperial.pipe.runner;

import java.io.ByteArrayInputStream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.stream.JsonParsingException;

public class JsonParameters {

	private static final String TRANSITIONS = "transitions";
	private static final String VERIFY_NOT_EMPTY_OR_NULL_STRING_IS_EMPTY_OR_NULL = "verifyNotEmptyOrNull:  string is empty or null.";
	private static final String BUILD_TRANSITIONS_EXPECTING_A_TRANSITIONS_KEY = "buildTransitions:  expecting a Transitions key.";
	private static final String BUILD_JSON_INPUT_NOT_JSON = "buildJson:  input string is not in Json format.";
	private static final String JSON_PARAMETERS = "JsonParameters.";
	private String jsonString;
	private JsonReader reader;
	private JsonObject jsonObject;
	private JsonObject transitions;

	public JsonParameters(String jsonString) {
		this.jsonString = jsonString;
		verifyNotEmptyOrNull();
		buildJson(); 
		buildTransitions(); 
	}

	private void buildTransitions() {
		if (!jsonObject.containsKey(TRANSITIONS)) {
			throw new IllegalArgumentException(JSON_PARAMETERS+BUILD_TRANSITIONS_EXPECTING_A_TRANSITIONS_KEY);
		}
		transitions = jsonObject.getJsonObject(TRANSITIONS); 
	}

	private void buildJson() {
		reader = Json.createReader(new ByteArrayInputStream(jsonString.getBytes())); 
		try {
			jsonObject = reader.readObject(); 
		} catch (JsonParsingException e) {
			throw new IllegalArgumentException(JSON_PARAMETERS+BUILD_JSON_INPUT_NOT_JSON+"\n"+e.getClass().getName()+"\n"+e.getMessage());
			
		}
	}

	protected void verifyNotEmptyOrNull() {
		if ((jsonString == null) || (jsonString.trim().isEmpty())) {
			throw new IllegalArgumentException(JSON_PARAMETERS+VERIFY_NOT_EMPTY_OR_NULL_STRING_IS_EMPTY_OR_NULL);
		}
	}

//	public <J> J getTransition(Class<J> clazz, String transitionId) {
//		transitions.
//		return null;
//	}

	public JsonTransition getTransition(String transitionId) {
		return new JsonTransition(transitionId, transitions);
	}
	class JsonTransition {
		private JsonObject transitions; 
		private String transitionId ;
		public JsonTransition(String transitionId, JsonObject transitions) {
			this.transitionId = transitionId;
			this.transitions = transitions; 
		}
		public JsonObject getJsonObject() {
			return (JsonObject) transitions.get(transitionId); 
		}
		public JsonArray getJsonArray() {
			return (JsonArray) transitions.get(transitionId); 
		}
		public JsonString getJsonString() {
			return (JsonString) transitions.get(transitionId); 
		}
		public JsonNumber getJsonNumber() {
			return (JsonNumber) transitions.get(transitionId); 
		}
	}
}
