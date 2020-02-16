package uk.ac.imperial.pipe.io.adapters.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.imperial.pipe.io.adapters.valueAdapter.BooleanValueAdapter;

/**
 * Adapted transition for PNML format
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class AdaptedPlaceStatus {

    /**
     * merge interface status
     */
    @XmlElement(name = "merge")
    private MergeStatus mergeStatus;

    /**
     * external interface status
     */
    @XmlJavaTypeAdapter(BooleanValueAdapter.class)
    private Boolean external = false;

    /**
     * external interface status
     */
    @XmlJavaTypeAdapter(BooleanValueAdapter.class)
    private Boolean inputOnly = false;

    /**
     * external interface status
     */
    @XmlJavaTypeAdapter(BooleanValueAdapter.class)
    private Boolean outputOnly = false;

    /**
     *
     * @return true if place has external interface status
     */
    public final Boolean getExternal() {
        return external;
    }

    /**
     *
     * @param external true if place has external interface status
     */
    public final void setExternal(Boolean external) {
        this.external = external;
    }

    /**
     *
     * @return true if place interface accepts input only
     */
    public final Boolean getInputOnly() {
        return inputOnly;
    }

    /**
     *
     * @param inputOnly true if place interface accepts input only
     */
    public final void setInputOnly(Boolean inputOnly) {
        this.inputOnly = inputOnly;
    }

    /**
     *
     * @return true if place interface accepts output only
     */
    public final Boolean getOutputOnly() {
        return outputOnly;
    }

    /**
     *
     * @param outputOnly true if place interface accepts output only
     */
    public final void setOutputOnly(Boolean outputOnly) {
        this.outputOnly = outputOnly;
    }

    public final MergeStatus getMergeStatus() {
        return mergeStatus;
    }

    public final void setMergeStatus(MergeStatus mergeStatus) {
        this.mergeStatus = mergeStatus;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class MergeStatus {
        /**
         * merge type attribute
         */
        @XmlAttribute
        private String type;

        public final String getType() {
            return type;
        }

        public final void setType(String type) {
            this.type = type;
        }

    }
}
