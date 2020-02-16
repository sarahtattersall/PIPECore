package uk.ac.imperial.pipe.models.petrinet;

import javax.json.JsonObject;

import uk.ac.imperial.pipe.runner.JsonParameters;

public abstract class AbstractTransitionJsonParameters extends AbstractExternalTransition
        implements TransitionJsonParameters {

    private static final String SET_CONTEXT_EXPECTED_JSON_PARAMETERS_FOUND = "setContext:  expected JsonParameters, found: ";
    private static final String ABSTRACT_TRANSITION_JSON_PARAMETERS = "AbstractTransitionJsonParameters.";
    protected JsonParameters jsonParameters;

    @Override
    public void setExternalTransitionProvider(
            ExternalTransitionProvider externalTransitionProvider) {
        super.setExternalTransitionProvider(externalTransitionProvider);
        if (!(externalTransitionProvider.getContext() instanceof JsonParameters)) {
            throw new IllegalArgumentException(
                    ABSTRACT_TRANSITION_JSON_PARAMETERS + SET_CONTEXT_EXPECTED_JSON_PARAMETERS_FOUND +
                            externalTransitionProvider.getContext().getClass().getName());
        }
        jsonParameters = (JsonParameters) externalTransitionProvider.getContext();
    }

    //	@Override
    //	public void setContext(Object context) {
    //		super.setContext(context);
    //		if (!(context instanceof JsonParameters)) {
    //			throw new IllegalArgumentException(ABSTRACT_TRANSITION_JSON_PARAMETERS+SET_CONTEXT_EXPECTED_JSON_PARAMETERS_FOUND + context.getClass().getName());
    //		}
    //		jsonParameters = (JsonParameters) context;
    //	}

    @Override
    public JsonObject getParameters() {
        return jsonParameters.getActiveTransition().getJsonObject();
    }

    @Override
    public void updateParameters(JsonObject jsonObject) {
        jsonParameters.updateTransition(jsonObject);
    }

}
