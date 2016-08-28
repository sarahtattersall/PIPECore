package uk.ac.imperial.pipe.io.adapters.valueAdapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adapts booleans into pnml value format
 */
public class BooleanValueAdapter extends XmlAdapter<BooleanValueAdapter.AdaptedBoolean, Boolean> {

    /**
     *
     * @param adaptedBoolean to unmarshal
     * @return unwrapped boolean
     */
    @Override
    public Boolean unmarshal(AdaptedBoolean adaptedBoolean) {
        return adaptedBoolean.value;
    }

    /**
     *
     * @param aBoolean to marshal
     * @return wrapped boolean
     */
    @Override
    public AdaptedBoolean marshal(Boolean aBoolean) {
        AdaptedBoolean adapted = new AdaptedBoolean();
        adapted.value = aBoolean;
        return adapted;
    }

    /**
     * Saves the boolean value within a field called value
     */
    public static class AdaptedBoolean {
        /**
         * boolean value
         */
        public boolean value;
    }
}
