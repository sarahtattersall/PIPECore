package uk.ac.imperial.pipe.io.adapters.modelAdapter;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import uk.ac.imperial.pipe.io.adapters.model.AdaptedPlace;
import uk.ac.imperial.pipe.io.adapters.model.NameDetails;
import uk.ac.imperial.pipe.io.adapters.model.OffsetGraphics;
import uk.ac.imperial.pipe.io.adapters.model.Point;
import uk.ac.imperial.pipe.io.adapters.utils.ConnectableUtils;
import uk.ac.imperial.pipe.models.petrinet.DiscretePlace;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.PlaceStatusInterface;

import com.google.common.base.Joiner;

/**
 * Marhsals Places into and out of their PNML format
 */
public final class PlaceAdapter extends XmlAdapter<AdaptedPlace, Place> {
    private final Map<String, Place> places;

    /**
     * Empty constructor needed for marshalling. Since the method to marshal does not actually
     * use these fields it's ok to initialize them as empty/null.
     */
    public PlaceAdapter() {
        places = new HashMap<>();
    }

    /**
     * Constructor 
     * @param places to marshal  
     */
    public PlaceAdapter(Map<String, Place> places) {
        this.places = places;
    }

    /**
     *
     * @param adaptedPlace to unmarshal
     * @return unmarshaled place
     */
    @Override
    public Place unmarshal(AdaptedPlace adaptedPlace) {
        NameDetails nameDetails = adaptedPlace.getName();
        Place place = new DiscretePlace(adaptedPlace.getId(), nameDetails.getName());
        place.setCapacity(adaptedPlace.getCapacity());
        ConnectableUtils.setConnectablePosition(place, adaptedPlace);
        ConnectableUtils.setConntactableNameOffset(place, adaptedPlace);
        place.setTokenCounts(stringToWeights(adaptedPlace.getInitialMarking().getTokenCounts()));
        if (adaptedPlace.getToolSpecificPlace() != null) {
            place.setStatus(adaptedPlace.getToolSpecificPlace().getStatus());
            place.getStatus().setPlace(place);
        }
        places.put(place.getId(), place);
        return place;
    }

    /**
     *
     * @param place to marshal
     * @return marshaled place
     */
    @Override
    public AdaptedPlace marshal(Place place) {
        AdaptedPlace adapted = new AdaptedPlace();
        adapted.setId(place.getId());
        ConnectableUtils.setAdaptedName(place, adapted);
        ConnectableUtils.setPosition(place, adapted);

        adapted.setCapacity(place.getCapacity());
        adapted.getInitialMarking().setTokenCounts(weightToString(place.getTokenCounts()));

        OffsetGraphics offsetGraphics = new OffsetGraphics();
        offsetGraphics.point = new Point();
        offsetGraphics.point.setX(place.getMarkingXOffset());
        offsetGraphics.point.setY(place.getMarkingYOffset());
        adapted.getInitialMarking().setGraphics(offsetGraphics);
        if (place.getStatus() instanceof PlaceStatusInterface) {
            AdaptedPlace.ToolSpecificPlace toolSpecific = new AdaptedPlace.ToolSpecificPlace();
            toolSpecific.setStatus(place.getStatus());
            adapted.setToolSpecificPlace(toolSpecific);
        }

        return adapted;
    }

    /**
     * @param weights to marshal
     * @return comma separated weights string
     */
    private String weightToString(Map<String, Integer> weights) {
        return Joiner.on(",").withKeyValueSeparator(",").join(weights);
    }

    /**
     *
     * @param value to unmarshal
     * @return map from comma separated weights string
     */
    public Map<String, Integer> stringToWeights(String value) {
        Map<String, Integer> tokenWeights = new HashMap<>();
        if (value.isEmpty()) {
            return tokenWeights;
        }

        String[] commaSeparatedMarkings = value.split(",");
        if (commaSeparatedMarkings.length == 1) {
            Integer weight = Integer.valueOf(commaSeparatedMarkings[0]);
            tokenWeights.put("Default", weight);
        } else {
            for (int i = 0; i < commaSeparatedMarkings.length; i += 2) {
                Integer weight = Integer.valueOf(commaSeparatedMarkings[i + 1].replace("@", ","));
                String tokenName = commaSeparatedMarkings[i];
                tokenWeights.put(tokenName, weight);
            }
        }
        return tokenWeights;
    }

}
