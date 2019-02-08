package uk.ac.imperial.pipe.runner;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.pipe.models.petrinet.AbstractPetriNetPubSub;
import uk.ac.imperial.pipe.models.petrinet.Place;

public class PlaceListener extends AbstractPetriNetPubSub implements PropertyChangeListener {

    protected static Logger logger = LogManager.getLogger(PlaceListener.class);
    private String placeId;

    protected PropertyChangeEvent event;
    protected Map<String, Integer> counts;
    protected Map<String, Integer> oldCounts;
    protected Runner runner;
    protected boolean acknowledgement = false;

    public PlaceListener(String placeId) {
        this.placeId = placeId;
    }

    public PlaceListener(String placeId, Runner runner, boolean acknowledgement) {
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
        boolean tokenEvent = false;
        if (evt.getPropertyName().equals(Place.TOKEN_CHANGE_MESSAGE)) {
            tokenEvent = true;
            this.event = evt;
            this.counts = (Map<String, Integer>) evt.getNewValue();
            this.oldCounts = (Map<String, Integer>) evt.getOldValue();
            logger.debug("received tokens event for place " + placeId + ": " + evt.toString());
        } else if (acknowledgement) {
            runner.acknowledge();
        }
        return tokenEvent;
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
