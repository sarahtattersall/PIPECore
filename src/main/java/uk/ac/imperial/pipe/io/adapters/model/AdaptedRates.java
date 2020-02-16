package uk.ac.imperial.pipe.io.adapters.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "rates")
@XmlAccessorType(XmlAccessType.FIELD)
public class AdaptedRates {

    @XmlElement(name = "rate")
    private List<String> rates = null;

    public final List<String> getRates() {
        return rates;
    }

    public final void setRates(List<String> rates) {
        this.rates = rates;
    }

}
