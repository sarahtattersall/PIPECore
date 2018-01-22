package uk.ac.imperial.pipe.runner;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Map.Entry;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParsingException;

public class JsonParameters {

    private static final String TRANSITIONS = "transitions";
    private static final String VERIFY_NOT_EMPTY_OR_NULL_STRING_IS_EMPTY_OR_NULL = "verifyNotEmptyOrNull:  string is empty or null.";
    private static final String BUILD_TRANSITIONS_EXPECTING_A_TRANSITIONS_KEY = "buildTransitions:  expecting a Transitions key.";
    private static final String BUILD_JSON_INPUT_NOT_JSON = "buildJson:  input string is not in Json format.";
    private static final String JSON_PARAMETERS = "JsonParameters.";
    private static final String ACTIVE_TRANSACTION_DOES_NOT_EXIST = "setActiveTransition:  transition does not exist: ";
    private String jsonString;
    private JsonReader reader;
    private JsonObject jsonObject;
    private JsonObject transitions;
    private JsonTransition activeTransition;

    //TODO add getter to return the entire JsonObject
    public JsonParameters(String jsonString) {
        this.jsonString = jsonString;
        verifyNotEmptyOrNull();
        buildJson();
        buildTransitions();
    }

    private void buildTransitions() {
        if (!jsonObject.containsKey(TRANSITIONS)) {
            throw new IllegalArgumentException(JSON_PARAMETERS + BUILD_TRANSITIONS_EXPECTING_A_TRANSITIONS_KEY);
        }
        transitions = jsonObject.getJsonObject(TRANSITIONS);
    }

    private void buildJson() {
        reader = Json.createReader(new ByteArrayInputStream(jsonString.getBytes()));
        try {
            jsonObject = reader.readObject();
            reader.close();
        } catch (JsonParsingException e) {
            throw new IllegalArgumentException(JSON_PARAMETERS + BUILD_JSON_INPUT_NOT_JSON + "\n" +
                    e.getClass().getName() + "\n" + e.getMessage());

        }
    }

    protected void verifyNotEmptyOrNull() {
        if ((jsonString == null) || (jsonString.trim().isEmpty())) {
            throw new IllegalArgumentException(JSON_PARAMETERS + VERIFY_NOT_EMPTY_OR_NULL_STRING_IS_EMPTY_OR_NULL);
        }
    }

    public void setActiveTransition(String transitionId) {
        if (transitions.containsKey(transitionId)) {
            activeTransition = new JsonTransition(transitionId, transitions);
        } else {
            throw new IllegalArgumentException(JSON_PARAMETERS + ACTIVE_TRANSACTION_DOES_NOT_EXIST + transitionId);
        }
    }

    public JsonTransition getActiveTransition() {
        return activeTransition;
    }

    public void updateTransition(JsonObject newObject) {
        JsonObject newTransitions;
        try {
            newTransitions = rebuildJsonObject(transitions, activeTransition.getTransitionId(), newObject);
            jsonObject = rebuildJsonObject(jsonObject, TRANSITIONS, newTransitions);
        } catch (IOException e) {
            e.printStackTrace();
        }
        buildTransitions();
        setActiveTransition(activeTransition.getTransitionId());
    }

    private JsonObject rebuildJsonObject(JsonObject parentObject, String key, JsonObject newObject) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Writer writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
        JsonGenerator generator = Json.createGenerator(writer);
        generator.writeStartObject();
        Set<Entry<String, JsonValue>> childObjects = parentObject.entrySet();
        for (Entry<String, JsonValue> entry : childObjects) {
            if (entry.getKey().equals(key)) {
                generator.write(entry.getKey(), newObject);
            } else {
                generator.write(entry.getKey(), entry.getValue());
            }
        }
        generator.writeEnd().close();
        JsonReader reader = Json
                .createReader(new InputStreamReader(new ByteArrayInputStream(out.toByteArray()), "UTF-8"));
        JsonObject rebuiltObject = reader.readObject();
        reader.close();
        return rebuiltObject;
    }

    @Override
    public String toString() {
        return jsonObject.toString();
    }
}
