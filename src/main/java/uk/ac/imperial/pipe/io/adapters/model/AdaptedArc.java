package uk.ac.imperial.pipe.io.adapters.model;

import uk.ac.imperial.pipe.io.adapters.modelAdapter.ArcPointAdapter;
import uk.ac.imperial.pipe.io.adapters.valueAdapter.StringAttributeValueAdaptor;
import uk.ac.imperial.pipe.models.petrinet.ArcPoint;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapted arc for PNML verbose fomat
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class AdaptedArc {
    /**
     * arc id
     */
    @XmlAttribute
    private String id;

    /**
     * arc source id
     */
    @XmlAttribute
    private String source;

    /**
     * arc target id
     */
    @XmlAttribute
    private String target;

    /**
     * arc intermediate points
     */
    @XmlElement(name = "arcpath")
    @XmlJavaTypeAdapter(ArcPointAdapter.class)
    private List<ArcPoint> arcPoints = new ArrayList<>();

    /**
     * arc type, defaults to normal, can also be "inhib"
     */
    @XmlElement
    @XmlJavaTypeAdapter(StringAttributeValueAdaptor.class)
    private String type = "normal";

    /**
     * arc weights inscription, it's PNML verbose which is why its not in the actual arc class
     */
    private Inscription inscription = new Inscription();

    /**
     *
     * @return arc id
     */
    public final String getId() {
        return id;
    }

    /**
     *
     * @param id new arc id
     */
    public final void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @return arc source id
     */
    public final String getSource() {
        return source;
    }

    /**
     *
     * @param source arc source id
     */
    public final void setSource(String source) {
        this.source = source;
    }

    /**
     *
     * @return arc target id
     */
    public final String getTarget() {
        return target;
    }

    /**
     *
     * @param target arc target id
     */
    public final void setTarget(String target) {
        this.target = target;
    }

    /**
     *
     * @return type of arc. I.e. normal or inhib
     */
    public final String getType() {
        return type;
    }

    /**
     *
     * @param type type of arc I.e. normal or inhib
     */
    public final void setType(String type) {
        this.type = type;
    }

    /**
     *
     * @return arc weightings
     */
    public final Inscription getInscription() {
        return inscription;
    }

    /**
     *
     * @param inscription arc weightings
     */
    public final void setInscription(Inscription inscription) {
        this.inscription = inscription;
    }

    /**
     *
     * @return arc intermediate points
     */
    public final List<ArcPoint> getArcPoints() {
        return arcPoints;
    }

    /**
     *
     * @param arcPoints arc intermediate points
     */
    public final void setArcPoints(List<ArcPoint> arcPoints) {
        this.arcPoints = arcPoints;
    }

    /**
     * PNML verbose way of arc weightings
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Inscription {
        /**
         * token counts string, contains all tokens to their counts e.g. "Red 1, Default 2, Black 3"
         */
        @XmlElement(name = "value")
        private String tokenCounts = "";

        /**
         * Graphics offsets for the display of tokens
         */
        private OffsetGraphics graphics;

        /**
         *
         * @return token counts string
         */
        public String getTokenCounts() {
            return tokenCounts;
        }

        /**
         *
         * @param tokenCounts token count string, containing all tokens and their count comma separeted e.g.
         *                    "Red 1, Default 2, Black 3"
         */
        public void setTokenCounts(String tokenCounts) {
            this.tokenCounts = tokenCounts;
        }

        /**
         *
         * @return graphics offset for weights
         */
        public OffsetGraphics getGraphics() {
            return graphics;
        }

        /**
         *
         * @param graphics offset for arc weights
         */
        public void setGraphics(OffsetGraphics graphics) {
            this.graphics = graphics;
        }
    }
}
