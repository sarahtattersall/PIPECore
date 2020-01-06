package uk.ac.imperial.pipe.runner;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.pipe.models.petrinet.AbstractPetriNetPubSub;
import uk.ac.imperial.pipe.models.petrinet.Place;

public class PlaceTokensListener extends AbstractPetriNetPubSub implements PropertyChangeListener {

    protected static Logger logger = LogManager.getLogger(PlaceTokensListener.class);
    protected String placeId;

    protected PropertyChangeEvent event;
    protected Map<String, Integer> counts;
    protected Map<String, Integer> oldCounts;
    protected Runner runner;
    protected boolean acknowledgement = false;
    private String propertyName;

    public PlaceTokensListener(String placeId) {
        this.placeId = placeId;
    }

    public PlaceTokensListener(String placeId, Runner runner, boolean acknowledgement) {
        this(placeId);
        this.runner = runner;
        this.acknowledgement = acknowledgement;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        checkTokensEventReceivedAndProcess(evt);
    }

    public boolean checkTokensEventReceivedAndProcess(PropertyChangeEvent evt) {
        verifyPlace(evt);
        boolean tokenEvent = false;
        if (evt.getPropertyName().equals(Place.TOKEN_CHANGE_MESSAGE)) {
            tokenEvent = true;
            this.event = evt;
            this.counts = (Map<String, Integer>) evt.getNewValue();
            this.oldCounts = (Map<String, Integer>) evt.getOldValue();
            logger.debug("received tokens event for place " + placeId + ": " + evt.toString());
            perhapsAcknowledge();
        }
        return tokenEvent;
    }

    /**
     * sub-class may override this to suppress acknowledgement in some cases; see {@link BooleanPlaceListener}
     */
    protected void perhapsAcknowledge() {
        if (acknowledgement) {
            runner.acknowledge(placeId);
        }
    }

    private void verifyPlace(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof Place) {
            Place source = (Place) evt.getSource();
            if (!(placeId.equals(source.getId()))) {
                throw new IllegalArgumentException("Logic error:  expected event for place " + placeId +
                        ", but received event for " + source.getId());
            }
        } else {
            throw new IllegalArgumentException(
                    "Logic error:  expected event for a place, but received event for another type");
        }

    }

    public PropertyChangeEvent getEventForTesting() {
        return event;
    }

    public String getPlaceId() {
        return placeId;
    }

    public Map<String, Integer> getCounts() {
        return counts;
    }

    public Map<String, Integer> getOldCounts() {
        return oldCounts;
    }

}
