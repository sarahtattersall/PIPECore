package uk.ac.imperial.pipe.runner;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;

//TODO consider whether this is needed, given that TransitionJsonParameters only uses JsonObject
public class JsonTransition {
    private JsonObject transitions;
    private String transitionId;

    public JsonTransition(String transitionId, JsonObject transitions) {
        if (!transitions.containsKey(transitionId)) {
            throw new IllegalArgumentException(
                    "JsonTransition.constructor:  Transitions does not contain individual transition: " + transitionId);
        }
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

    public String getTransitionId() {
        return transitionId;
    }
}
