package uk.ac.imperial.pipe.io.adapters.valueAdapter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * This class differs from {@link uk.ac.imperial.pipe.io.adapters.valueAdapter.StringValueAdapter} only
 * by setting {@link uk.ac.imperial.pipe.io.adapters.valueAdapter.StringAttributeValueAdaptor.AdaptedAttributeString}
 * to be an XmlAttribute. The reason behind needed this is that PNML is a strict standard that is very verbose
 * and does not always follow a common pattern between different Petri net components.
 *
 * If there was a way to dynamically choose if it was an attribute/element
 * this would be better
 */
public final class StringAttributeValueAdaptor
        extends XmlAdapter<StringAttributeValueAdaptor.AdaptedAttributeString, String> {
    @Override
    public String unmarshal(AdaptedAttributeString adaptedString) {
        return adaptedString.value;
    }

    @Override
    public AdaptedAttributeString marshal(String s) {
        AdaptedAttributeString adaptedString = new AdaptedAttributeString();
        adaptedString.value = s;
        return adaptedString;
    }

    public static class AdaptedAttributeString {
        @XmlAttribute
        public String value;
    }
}
