package uk.ac.imperial.pipe.io.adapters.valueAdapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Used for elements which need a value field in their element
 * E.g.
 * {@code <name>}
 * {@code <value>}
 * value goes here
 * {@code </value>}
 * {@code </name>}
 */
public final class StringValueAdapter extends XmlAdapter<StringValueAdapter.AdaptedString, String> {
    /**
     *
     * @param adaptedString to unmarshal
     * @return unwrapped string
     */
    @Override
    public String unmarshal(AdaptedString adaptedString) {
        return adaptedString.value;
    }

    /**
     *
     * @param s  string to marshal
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
