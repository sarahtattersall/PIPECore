package uk.ac.imperial.pipe.models.petrinet;

import javax.json.JsonObject;

public interface TransitionJsonParameters extends ExternalTransition {

    public JsonObject getParameters();

    public void updateParameters(JsonObject jsonObject);

}
