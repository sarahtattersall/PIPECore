package uk.ac.imperial.pipe.io.adapters.modelAdapter;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import uk.ac.imperial.pipe.io.adapters.model.AdaptedTransition;
import uk.ac.imperial.pipe.io.adapters.model.NameDetails;
import uk.ac.imperial.pipe.io.adapters.utils.ConnectableUtils;
import uk.ac.imperial.pipe.models.petrinet.DiscreteExternalTransition;
import uk.ac.imperial.pipe.models.petrinet.DiscreteTransition;
import uk.ac.imperial.pipe.models.petrinet.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.petrinet.NormalRate;
import uk.ac.imperial.pipe.models.petrinet.Rate;
import uk.ac.imperial.pipe.models.petrinet.Transition;

/**
 * Used to marshal transitions to and from their PNML representation
 */
public final class TransitionAdapter extends XmlAdapter<AdaptedTransition, Transition> {
    private final Map<String, Transition> transitions;
    private final Map<String, FunctionalRateParameter> rateParameters;

    /**
     * Empty constructor needed for marshalling. Since the method to marshall does not actually
     * use these fields it's ok to initialise them as empty/null.
     */
    public TransitionAdapter() {
        transitions = new HashMap<>();
        rateParameters = new HashMap<>();
    }

    /**
     * Constructor
     * @param transitions to marshal
     * @param rateParameters to marshal
     */
    public TransitionAdapter(Map<String, Transition> transitions, Map<String, FunctionalRateParameter> rateParameters) {

        this.transitions = transitions;
        this.rateParameters = rateParameters;
    }

    /**
     *
     * @param adaptedTransition to unmarshal
     * @return unmarshaled transition
     */
    @Override
    public Transition unmarshal(AdaptedTransition adaptedTransition) {
        NameDetails nameDetails = adaptedTransition.getName();
        AdaptedTransition.ToolSpecific toolSpecific = adaptedTransition.getToolSpecific();
        Transition transition;
        Rate rate;
        if (toolSpecific == null) {
            transition = new DiscreteTransition(adaptedTransition.getId(), nameDetails.getName());
            rate = new NormalRate(adaptedTransition.getRate());
        } else {
            if (toolSpecific.getExternalClass() == null) {
                transition = new DiscreteTransition(adaptedTransition.getId(), nameDetails.getName());
                rate = rateParameters.get(toolSpecific.getRateDefinition()); // implies only two parameters: externalClass & rateDefinition
            } else {
                transition = new DiscreteExternalTransition(adaptedTransition.getId(), nameDetails.getName(),
                        toolSpecific.getExternalClass());
                if (toolSpecific.getRateDefinition() == null) {
                    rate = new NormalRate(adaptedTransition.getRate());
                } else {
                    rate = rateParameters.get(toolSpecific.getRateDefinition());
                }
            }
        }
        transition.setRate(rate);
        ConnectableUtils.setConntactableNameOffset(transition, adaptedTransition);
        ConnectableUtils.setConnectablePosition(transition, adaptedTransition);
        transition.setAngle(adaptedTransition.getAngle());
        transition.setPriority(adaptedTransition.getPriority());

        transition.setTimed(adaptedTransition.getTimed());
        transition.setInfiniteServer(adaptedTransition.getInfiniteServer());
        transitions.put(transition.getId(), transition);
        return transition;
    }

    /**
     *
     * @param transition to marshal
     * @return marshaled transition
     */
    @Override
    public AdaptedTransition marshal(Transition transition) {
        AdaptedTransition adaptedTransition = new AdaptedTransition();
        ConnectableUtils.setAdaptedName(transition, adaptedTransition);

        adaptedTransition.setId(transition.getId());
        ConnectableUtils.setPosition(transition, adaptedTransition);
        adaptedTransition.setPriority(transition.getPriority());
        adaptedTransition.setAngle(transition.getAngle());
        adaptedTransition.setRate(transition.getRateExpr());
        adaptedTransition.setInfiniteServer(transition.isInfiniteServer());
        adaptedTransition.setTimed(transition.isTimed());

        Rate rate = transition.getRate();
        AdaptedTransition.ToolSpecific toolSpecific = new AdaptedTransition.ToolSpecific();
        boolean toolSpecificNeeded = false;
        if (rate instanceof FunctionalRateParameter) {
            FunctionalRateParameter rateParameter = (FunctionalRateParameter) rate;
            toolSpecific.setRateDefinition(rateParameter.getId());
            toolSpecificNeeded = true;
        }
        if (transition instanceof DiscreteExternalTransition) {
            toolSpecific.setExternalClass(((DiscreteExternalTransition) transition).getClassName());
            toolSpecificNeeded = true;
        }
        if (toolSpecificNeeded) {
            adaptedTransition.setToolSpecific(toolSpecific);
        }
        return adaptedTransition;
    }
}
