package uk.ac.imperial.pipe.io.adapters.model;

import uk.ac.imperial.pipe.io.adapters.valueAdapter.BooleanValueAdapter;
import uk.ac.imperial.pipe.io.adapters.valueAdapter.IntValueAdapter;
import uk.ac.imperial.pipe.io.adapters.valueAdapter.StringValueAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class AdaptedTransition extends AdaptedConnectable {

    @XmlJavaTypeAdapter(BooleanValueAdapter.class)
    private Boolean infiniteServer = false;

    @XmlJavaTypeAdapter(BooleanValueAdapter.class)
    private Boolean timed = false;

    @XmlElement(name = "priority")
    @XmlJavaTypeAdapter(IntValueAdapter.class)
    private Integer priority = 0;

    @XmlElement(name = "orientation")
    @XmlJavaTypeAdapter(IntValueAdapter.class)
    private Integer angle = 0;

    @XmlElement(name = "rate")
    @XmlJavaTypeAdapter(StringValueAdapter.class)
    private String rate = "";

    @XmlElement(name = "toolspecific")
    private ToolSpecific toolSpecific;

    public final Boolean getTimed() {
        return timed;
    }

    public final void setTimed(Boolean timed) {
        this.timed = timed;
    }

    public final Boolean getInfiniteServer() {
        return infiniteServer;
    }

    public final void setInfiniteServer(Boolean infiniteServer) {
        this.infiniteServer = infiniteServer;
    }

    public final int getPriority() {
        return priority;
    }

    public final void setPriority(int priority) {
        this.priority = priority;
    }

    public final int getAngle() {
        return angle;
    }

    public final void setAngle(int angle) {
        this.angle = angle;
    }

    public final String getRate() {
        return rate;
    }

    public final void setRate(String rate) {
        this.rate = rate;
    }

    public final ToolSpecific getToolSpecific() {
        return toolSpecific;
    }

    public final void setToolSpecific(ToolSpecific toolSpecific) {
        this.toolSpecific = toolSpecific;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ToolSpecific {
        @XmlAttribute
        private String tool = "PIPE";

        @XmlAttribute
        private String version = "2.5";

        @XmlAttribute
        private String rateDefinition;

        public final String getTool() {
            return tool;
        }

        public final void setTool(String tool) {
            this.tool = tool;
        }

        public final String getVersion() {
            return version;
        }

        public final void setVersion(String version) {
            this.version = version;
        }

        public final String getRateDefinition() {
            return rateDefinition;
        }

        public final void setRateDefinition(String rateDefinition) {
            this.rateDefinition = rateDefinition;
        }
    }
}
