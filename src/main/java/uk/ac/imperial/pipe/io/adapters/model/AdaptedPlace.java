package uk.ac.imperial.pipe.io.adapters.model;

import uk.ac.imperial.pipe.io.adapters.valueAdapter.IntValueAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * PNML adapted Petri net for marshalling with JAXB
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class AdaptedPlace extends AdaptedConnectable {

    /**
     * Place capacity
     */
    @XmlJavaTypeAdapter(IntValueAdapter.class)
    private Integer capacity = 0;

    /**
     * Verbose form of token counts
     */
    private InitialMarking initialMarking = new InitialMarking();

    /**
     *
     * @return verbose form of token counts
     */
    public final InitialMarking getInitialMarking() {
        return initialMarking;
    }

    /**
     *
     * @param initialMarking verbose form of token counts
     */
    public final void setInitialMarking(InitialMarking initialMarking) {
        this.initialMarking = initialMarking;
    }

    /**
     *
     * @return capacity
     */
    public final Integer getCapacity() {
        return capacity;
    }

    /**
     *
     * @param capacity place capacity
     */
    public final void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }


    /**
     * Verbose way of storing the token counts in PNML
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class InitialMarking {
        /**
         * Offset for showing tokens on the Petri net
         */
        private OffsetGraphics graphics;

        /**
         * Comma separated string representation of all token counts e.g. "Red 1, Blue 2"
         */
        @XmlElement(name = "value")
        private String tokenCounts = "";

        /**
         *
         * @return Comma separated string representation of all token counts e.g. "Red 1, Blue 2"
         */
        public final String getTokenCounts() {
            return tokenCounts;
        }

        /**
         *
         * @param tokenCounts Comma separated string representation of all token counts e.g. "Red 1, Blue 2"
         */
        public final void setTokenCounts(String tokenCounts) {
            this.tokenCounts = tokenCounts;
        }

        /**
         *
         * @return verbose x, y for token counts offset relevant to place
         */
        public final OffsetGraphics getGraphics() {
            return graphics;
        }

        /**
         *
         * @param graphics verbose x, y for token counts offset relevant to place
         */
        public final void setGraphics(OffsetGraphics graphics) {
            this.graphics = graphics;
        }
    }


}
