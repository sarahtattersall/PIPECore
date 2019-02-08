package uk.ac.imperial.pipe.runner;

import java.beans.PropertyChangeEvent;

public class BooleanPlaceListener extends PlaceListener {

    public static final String PLACE_TRUE = "place true";

    public BooleanPlaceListener(String placeId) {
        super(placeId);
    }

    public BooleanPlaceListener(String placeId, Runner runner, boolean acknowledgement) {
        super(placeId, runner, acknowledgement);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (checkTokensEventReceivedAndProcess(evt)) {
            if (zeroToNonZeroCounts()) {
                changeSupport.firePropertyChange(PLACE_TRUE, new Boolean(false), new Boolean(true));
            } else if (acknowledgement)
                runner.acknowledge();
        }

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
