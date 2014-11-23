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
     * @param adaptedPlaceStatus
     * @return unmarshaled PlaceStatus
     */
    @Override
    public PlaceStatus unmarshal(AdaptedPlaceStatus adaptedPlaceStatus) {
    	PlaceStatus status = new PlaceStatusInterface(); 
    	status.setMergeStatus(adaptedPlaceStatus.getMerge()); 
    	status.setExternalStatus(adaptedPlaceStatus.getExternal()); 
    	status.setInputOnlyStatus(adaptedPlaceStatus.getInputOnly());
    	status.setOutputOnlyStatus(adaptedPlaceStatus.getOutputOnly());
    	return status;
    }
    /**
     *
     * @param place status
     * @return marshaled place status
     */
    @Override
    public AdaptedPlaceStatus marshal(PlaceStatus placeStatus) {
    	AdaptedPlaceStatus adaptedStatus = new AdaptedPlaceStatus(); 
    	adaptedStatus.setMerge(placeStatus.isMergeStatus());
    	adaptedStatus.setExternal(placeStatus.isExternalStatus()); 
    	adaptedStatus.setInputOnly(placeStatus.isInputOnlyStatus());
    	adaptedStatus.setOutputOnly(placeStatus.isOutputOnlyStatus()); 
        return adaptedStatus;
    }
}
