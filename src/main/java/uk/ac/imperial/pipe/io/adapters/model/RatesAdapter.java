package uk.ac.imperial.pipe.io.adapters.model;

import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class RatesAdapter extends XmlAdapter<AdaptedRates, List<String>> {

    @Override
    public List<String> unmarshal(AdaptedRates v) throws Exception {
        return null;
    }

    @Override
    public AdaptedRates marshal(List<String> v) throws Exception {
        return null;
    }

}
