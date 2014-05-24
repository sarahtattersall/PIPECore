package uk.ac.imperial.pipe.io.adapters.modelAdapter;

import uk.ac.imperial.pipe.io.adapters.model.AdaptedRateParameter;
import uk.ac.imperial.pipe.models.component.rate.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.component.rate.RateParameter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.HashMap;
import java.util.Map;

public class RateParameterAdapter extends XmlAdapter<AdaptedRateParameter, RateParameter> {
    private final Map<String, FunctionalRateParameter> rateParameters;

    public RateParameterAdapter() {
        this.rateParameters = new HashMap<>();
    }

    public RateParameterAdapter(Map<String, FunctionalRateParameter> rateParameters) {

        this.rateParameters = rateParameters;
    }

    @Override
    public FunctionalRateParameter unmarshal(AdaptedRateParameter adaptedRateParameter) {
        FunctionalRateParameter rateParameter = new FunctionalRateParameter(adaptedRateParameter.getExpression(), adaptedRateParameter.getId(), adaptedRateParameter.getName());
        rateParameters.put(rateParameter.getId(), rateParameter);
        return rateParameter;
    }

    @Override
    public AdaptedRateParameter marshal(RateParameter rateParameter)  {
        AdaptedRateParameter adaptedRateParameter = new AdaptedRateParameter();
        adaptedRateParameter.setExpression(rateParameter.getExpression());
        adaptedRateParameter.setId(rateParameter.getId());
        adaptedRateParameter.setName(rateParameter.getId());
        return adaptedRateParameter;
    }
}
