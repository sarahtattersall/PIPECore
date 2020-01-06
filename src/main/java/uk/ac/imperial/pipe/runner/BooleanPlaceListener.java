package uk.ac.imperial.pipe.runner;

import java.beans.PropertyChangeEvent;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.pipe.models.petrinet.Place;

public class BooleanPlaceListener extends PlaceTokensListener {

    protected static Logger logger = LogManager.getLogger(BooleanPlaceListener.class);
    public static final String PLACE_TRUE = "place true";
    public static final String PLACE_FALSE = "place false";
    private boolean bothEvents;

    public BooleanPlaceListener(String placeId) {
        //        super(placeId);
        this(placeId, null, false, false);
    }

    public BooleanPlaceListener(String placeId, Runner runner, boolean acknowledgement) {
        this(placeId, runner, acknowledgement, false);
    }

    public BooleanPlaceListener(String placeId, Runner runner, boolean acknowledgement, boolean bothEvents) {
        super(placeId, runner, acknowledgement);
        this.bothEvents = bothEvents;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        logger.debug("Property Change event for PlaceId: " + placeId + " with acknowledgement: " + acknowledgement +
                " : " + evt);
        if (checkTokensEventReceivedAndProcess(evt)) {
            if (zeroToNonZeroCounts()) {
                logger.debug("received non-zero tokens; about to fire \"place true\" property change");
                changeSupport.firePropertyChange(PLACE_TRUE, new Boolean(false), new Boolean(true));
            } else if (bothEvents && nonZeroToZeroCounts()) {
                logger.debug("received zero tokens and notification requested for both events; " +
                        "about to fire \"place false\" property change");
                changeSupport.firePropertyChange(PLACE_FALSE, new Boolean(true), new Boolean(false));
            } else {
                if (acknowledgement) {
                    runner.acknowledge(placeId);
                    Entry<String, Integer> entry = counts.entrySet().iterator().next();
                    logger.debug("Automatic acknowledgment of " + Place.TOKEN_CHANGE_MESSAGE +
                            " event for update of place " +
                            placeId +
                            " with token count " + entry.getKey() + " = " + entry.getValue());
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

    private boolean nonZeroToZeroCounts() {
        boolean nonZeroToZero = false;
        for (String token : counts.keySet()) {
            if ((counts.get(token) == 0) && ((oldCounts.get(token) != null) && (oldCounts.get(token) > 0))) {
                nonZeroToZero = true;
            }
        }
        return nonZeroToZero;
    }

    protected boolean isBothEvents() {
        return bothEvents;
    }

}
