package uk.ac.imperial.pipe.runner;

import java.beans.PropertyChangeEvent;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.pipe.models.petrinet.Place;

public class BooleanPlaceListener extends PlaceTokensListener {

    protected static Logger logger = LogManager.getLogger(BooleanPlaceListener.class);
    public static final String PLACE_TRUE = "place true";

    public BooleanPlaceListener(String placeId) {
        super(placeId);
    }

    public BooleanPlaceListener(String placeId, Runner runner, boolean acknowledgement) {
        super(placeId, runner, acknowledgement);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        logger.debug("Property Change event for PlaceId: " + placeId + " with acknowledgement: " + acknowledgement +
                " : " + evt);
        if (checkTokensEventReceivedAndProcess(evt)) {
            if (zeroToNonZeroCounts()) {
                logger.debug("received non-zero tokens; about to fire \"place true\" property change");
                changeSupport.firePropertyChange(PLACE_TRUE, new Boolean(false), new Boolean(true));
            } else {
                if (acknowledgement) {
                    runner.acknowledge(placeId);
                    Entry<String, Integer> entry = counts.entrySet().iterator().next();
                    logger.debug("acknowledging " + Place.TOKEN_CHANGE_MESSAGE + " event for update of place " +
                            placeId +
                            " with zero tokens" + entry.getKey() + " = " + entry.getValue());
                }
            }
        }

    }

    /**
     * suppress automatic acknowledgement
     */
    @Override
    protected void perhapsAcknowledge() {
    }

    private boolean zeroToNonZeroCounts() {
        boolean zeroToNonZero = false;
        for (String token : counts.keySet()) {
            if ((counts.get(token) > 0) && ((oldCounts.get(token) == null) || (oldCounts.get(token) == 0))) {
                zeroToNonZero = true;
            }
        }
        return zeroToNonZero;
    }

}
