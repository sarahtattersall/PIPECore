package uk.ac.imperial.pipe.io.adapters.valueAdapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Wraps integers in a field called value
 */
public final class IntValueAdapter extends XmlAdapter<IntValueAdapter.IntAdapter, Integer> {
    /**
     *
     * @param intAdapter to unmarshal
     * @return unwrapped int
     */
    @Override
    public Integer unmarshal(IntAdapter intAdapter) {
        return intAdapter.value;
    }

    /**
     *
     * @param integer to marshal 
     * @return wrapped int
     */
    @Override
    public IntAdapter marshal(Integer integer) {
        IntAdapter adapter = new IntAdapter();
        adapter.value = integer;
        return adapter;
    }

    /**
     * Saves the integer within a field called value
     */
    public static class IntAdapter {
        /**
         * integer value
         */
        public int value;
    }
}
