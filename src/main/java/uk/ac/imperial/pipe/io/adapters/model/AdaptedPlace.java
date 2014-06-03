package uk.ac.imperial.pipe.io.adapters.model;

import uk.ac.imperial.pipe.io.adapters.valueAdapter.IntValueAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public class AdaptedPlace extends AdaptedConnectable {

    @XmlJavaTypeAdapter(IntValueAdapter.class)
    private Integer capacity = 0;

    private InitialMarking initialMarking = new InitialMarking();

    public final InitialMarking getInitialMarking() {
        return initialMarking;
    }

    public final void setInitialMarking(InitialMarking initialMarking) {
        this.initialMarking = initialMarking;
    }

    public final Integer getCapacity() {
        return capacity;
    }

    public final void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    public static class InitialMarking {
        private OffsetGraphics graphics;

        @XmlElement(name = "value")
        private String tokenCounts = "";

        public final String getTokenCounts() {
            return tokenCounts;
        }

        public final void setTokenCounts(String tokenCounts) {
            this.tokenCounts = tokenCounts;
        }

        public final OffsetGraphics getGraphics() {
            return graphics;
        }

        public final void setGraphics(OffsetGraphics graphics) {
            this.graphics = graphics;
        }
    }


}
