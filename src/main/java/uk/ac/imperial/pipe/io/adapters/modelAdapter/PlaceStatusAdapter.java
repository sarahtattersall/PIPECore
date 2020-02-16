package uk.ac.imperial.pipe.io.adapters.modelAdapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import uk.ac.imperial.pipe.io.adapters.model.AdaptedPlaceStatus;
import uk.ac.imperial.pipe.models.petrinet.PlaceStatus;
import uk.ac.imperial.pipe.models.petrinet.PlaceStatusInterface;

/**
 * Marshalls Petri net arc points in and out of their PNML format
 */
public final class PlaceStatusAdapter extends XmlAdapter<AdaptedPlaceStatus, PlaceStatus> {

    /**
     * null constructor required for marshalling
     */
    public PlaceStatusAdapter() {
    }

    /**
     *
     * @param adaptedPlaceStatus to be unmarshaled
     * @return unmarshaled PlaceStatus 
     */
    @Override
    public PlaceStatus unmarshal(AdaptedPlaceStatus adaptedPlaceStatus) {
        PlaceStatus status = new PlaceStatusInterface();
        if (adaptedPlaceStatus.getMergeStatus() != null) {
            status.buildMergeStatus(adaptedPlaceStatus.getMergeStatus().getType());
        }
        status.setExternal(adaptedPlaceStatus.getExternal());
        status.setInputOnlyArcConstraint(adaptedPlaceStatus.getInputOnly());
        status.setOutputOnlyArcConstraint(adaptedPlaceStatus.getOutputOnly());
        return status;
    }

    /**
     *
     * @param placeStatus to be marshaled
     * @return marshaled place status
     */
    @Override
    public AdaptedPlaceStatus marshal(PlaceStatus placeStatus) {
        AdaptedPlaceStatus adaptedStatus = new AdaptedPlaceStatus();
        String merge = placeStatus.getMergeXmlType();
        if (merge != null) {
            AdaptedPlaceStatus.MergeStatus mergeStatus = new AdaptedPlaceStatus.MergeStatus();
            mergeStatus.setType(merge);
            adaptedStatus.setMergeStatus(mergeStatus);
        }
        adaptedStatus.setExternal(placeStatus.isExternal());
        adaptedStatus.setInputOnly(placeStatus.isInputOnlyArcConstraint());
        adaptedStatus.setOutputOnly(placeStatus.isOutputOnlyArcConstraint());
        return adaptedStatus;
    }
}
