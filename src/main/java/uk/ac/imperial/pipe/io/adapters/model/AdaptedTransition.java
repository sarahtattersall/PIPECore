package uk.ac.imperial.pipe.io.adapters.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.imperial.pipe.io.adapters.valueAdapter.BooleanValueAdapter;
import uk.ac.imperial.pipe.io.adapters.valueAdapter.IntValueAdapter;
import uk.ac.imperial.pipe.io.adapters.valueAdapter.StringValueAdapter;

/**
 * Adapted transition for PNML format
 */
public class AdaptedTransition extends AdaptedConnectable {

    /**
     * infinite server element
     */
    @XmlJavaTypeAdapter(BooleanValueAdapter.class)
    private Boolean infiniteServer = false;

    /**
     * timed element
     */
    @XmlJavaTypeAdapter(BooleanValueAdapter.class)
    private Boolean timed = false;

    /**
     * priority element
     */
    @XmlElement(name = "priority")
    @XmlJavaTypeAdapter(IntValueAdapter.class)
    private Integer priority = 0;

    /**
     * angle element
     */
    @XmlElement(name = "orientation")
    @XmlJavaTypeAdapter(IntValueAdapter.class)
    private Integer angle = 0;

    /**
     * rate element
     */
    @XmlElement(name = "rate")
    @XmlJavaTypeAdapter(StringValueAdapter.class)
    private String rate = "";

    /**
     * tool specified field for PIPE specific values
     */
    @XmlElement(name = "toolspecific")
    private ToolSpecific toolSpecific;

    /**
     *
     * @return timed boolean
     */
    public final Boolean getTimed() {
        return timed;
    }

    /**
     *
     * @param timed if this transition is timed or immediate
     */
    public final void setTimed(Boolean timed) {
        this.timed = timed;
    }

    /**
     *
     * @return true if this transition is infinite, false if it is a single server
     */
    public final Boolean getInfiniteServer() {
        return infiniteServer;
    }

    /**
     *
     * @param infiniteServer true if this transition supports infinite server semantics, false for single server semantics
     */
    public final void setInfiniteServer(Boolean infiniteServer) {
        this.infiniteServer = infiniteServer;
    }

    /**
     *
     * @return transition priority
     */
    public final int getPriority() {
        return priority;
    }

    /**
     *
     * @param priority transition priority
     */
    public final void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     *
     * @return transition angle
     */
    public final int getAngle() {
        return angle;
    }

    /**
     *
     * @param angle transition angle
     */
    public final void setAngle(int angle) {
        this.angle = angle;
    }

    /**
     *
     * @return transition rate
     */
    public final String getRate() {
        return rate;
    }

    /**
     *
     * @param rate transition weight
     */
    public final void setRate(String rate) {
        this.rate = rate;
    }

    /**
     *
     * @return tool specifid settings
     */
    public final ToolSpecific getToolSpecific() {
        return toolSpecific;
    }

    /**
     *
     * @param toolSpecific PIPE specific settings
     */
    public final void setToolSpecific(ToolSpecific toolSpecific) {
        this.toolSpecific = toolSpecific;
    }

    /**
     * PIPE specific settings
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ToolSpecific {
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
         * Rate definition
         */
        @XmlAttribute
        private String rateDefinition;

        /**
         * External class name
         */
        @XmlAttribute
        private String externalClass;

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
         * @return rate definition
         */
        public final String getRateDefinition() {
            return rateDefinition;
        }

        /**
         *
         * @param rateDefinition rate definition
         */
        public final void setRateDefinition(String rateDefinition) {
            this.rateDefinition = rateDefinition;
        }

        /**
         *
         * @return external class name
         */
        public String getExternalClass() {
            return externalClass;
        }

        /**
         *
         * @param externalClass Name of the class to be invoked
         */
        public void setExternalClass(String externalClass) {
            this.externalClass = externalClass;
        }
    }
}
