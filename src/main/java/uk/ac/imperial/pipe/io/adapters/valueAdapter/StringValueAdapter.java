package uk.ac.imperial.pipe.io.adapters.valueAdapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Used for elements which need a value field in their element
 * E.g.
 * <name>
 * <value>
 * value goes here
 * </value>
 * </name>
 */
public final class StringValueAdapter extends XmlAdapter<StringValueAdapter.AdaptedString, String> {
    /**
     *
     * @param adaptedString
     * @return unwrapped string
     */
    @Override
    public String unmarshal(AdaptedString adaptedString) {
        return adaptedString.value;
    }

    /**
     *
     * @param s
     * @return wrapped string
     */
    @Override
    public AdaptedString marshal(String s) {
        AdaptedString adaptedString = new AdaptedString();
        adaptedString.value = s;
        return adaptedString;
    }

    /**
     * Wraps the string in a field called value
     */
    public static class AdaptedString {
        /**
         * String element
         */
        public String value;
    }
}
