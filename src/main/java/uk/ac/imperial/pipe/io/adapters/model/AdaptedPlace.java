package uk.ac.imperial.pipe.io.adapters.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.imperial.pipe.io.adapters.modelAdapter.PlaceStatusAdapter;
import uk.ac.imperial.pipe.io.adapters.valueAdapter.IntValueAdapter;
import uk.ac.imperial.pipe.models.petrinet.PlaceStatus;

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
     * tool specified field for PIPE specific values
     */
    @XmlElement(name = "toolspecificplace")
    private ToolSpecificPlace toolSpecificPlace;

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
    *
    * @return tool specifid settings
    */
    public final ToolSpecificPlace getToolSpecificPlace() {
        return toolSpecificPlace;
    }

    /**
    *
    * @param toolSpecific PIPE specific settings
    */
    public final void setToolSpecificPlace(ToolSpecificPlace toolSpecific) {
        this.toolSpecificPlace = toolSpecific;
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

    /**
     * PIPE specific settings
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ToolSpecificPlace {
        /**
         * PIPE attribute
         */
        @XmlAttribute
        private String tool = "PIPE";

        /**
         * Version attribute
         */
        @XmlAttribute
        private String version = "5";

        /**
         * priority element
         */
        @XmlElement(name = "status")
        @XmlJavaTypeAdapter(PlaceStatusAdapter.class)
        private PlaceStatus status;

        /**
         *
         * @return tool type
         */
        public final String getTool() {
            return tool;
        }

        /**
         *
         * @param tool tool type
         */
        public final void setTool(String tool) {
            this.tool = tool;
        }

        /**
         *
         * @return version
         */
        public final String getVersion() {
            return version;
        }

        /**
         *
         * @param version version
         */
        public final void setVersion(String version) {
            this.version = version;
        }

        /**
         *
         * @return place status interface
         */
        public final PlaceStatus getStatus() {
            return status;
        }

        /**
         *
         * @param status   place status interface
         */
        public final void setStatus(PlaceStatus status) {
            this.status = status;
        }
    }
}
