package uk.ac.imperial.pipe.io.adapters.modelAdapter;

import uk.ac.imperial.pipe.io.adapters.model.AdaptedRateParameter;
import uk.ac.imperial.pipe.models.petrinet.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.petrinet.RateParameter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for marshalling rate parameters into and out of their PNML format
 */
public final class RateParameterAdapter extends XmlAdapter<AdaptedRateParameter, RateParameter> {
    /**
     * Rate paramter id -> rate paramter
     */
    private final Map<String, FunctionalRateParameter> rateParameters;

    /**
     * Constructor
     */
    public RateParameterAdapter() {
        this.rateParameters = new HashMap<>();
    }

    /**
     * Constructor
     * @param rateParameters to marshal
     */
    public RateParameterAdapter(Map<String, FunctionalRateParameter> rateParameters) {

        this.rateParameters = rateParameters;
    }

    /**
     *
     * @param adaptedRateParameter to unmarshal
     * @return unmarshaled rate parameter
    */
    @Override
    public FunctionalRateParameter unmarshal(AdaptedRateParameter adaptedRateParameter) {
        FunctionalRateParameter rateParameter = new FunctionalRateParameter(adaptedRateParameter.getExpression(),
                adaptedRateParameter.getId(), adaptedRateParameter.getName());
        rateParameters.put(rateParameter.getId(), rateParameter);
        return rateParameter;
    }

    /**
     *
     * @param rateParameter to marshal
     * @return marshaled rate parameter
     */
    @Override
    public AdaptedRateParameter marshal(RateParameter rateParameter) {
        AdaptedRateParameter adaptedRateParameter = new AdaptedRateParameter();
        adaptedRateParameter.setExpression(rateParameter.getExpression());
        adaptedRateParameter.setId(rateParameter.getId());
        adaptedRateParameter.setName(rateParameter.getId());
        return adaptedRateParameter;
    }
}
