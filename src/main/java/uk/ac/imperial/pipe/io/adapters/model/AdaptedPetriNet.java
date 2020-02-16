package uk.ac.imperial.pipe.io.adapters.model;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.imperial.pipe.io.adapters.modelAdapter.AnnotationAdapter;
import uk.ac.imperial.pipe.io.adapters.modelAdapter.ArcAdapter;
import uk.ac.imperial.pipe.io.adapters.modelAdapter.PlaceAdapter;
import uk.ac.imperial.pipe.io.adapters.modelAdapter.RateParameterAdapter;
import uk.ac.imperial.pipe.io.adapters.modelAdapter.TokenAdapter;
import uk.ac.imperial.pipe.io.adapters.modelAdapter.TransitionAdapter;
import uk.ac.imperial.pipe.models.petrinet.Annotation;
import uk.ac.imperial.pipe.models.petrinet.Arc;
import uk.ac.imperial.pipe.models.petrinet.Connectable;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.RateParameter;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.Transition;

/**
 * Adapted Petri net, whose XmlElement name represents the name given to them in PNML.
 * items are written in propOrder
 */
@XmlType(propOrder = { "tokens", "annotations", "rateParameters", "places", "transitions", "arcs" })
public class AdaptedPetriNet {

    /**
     * id will be marshalled to name
     */
    @XmlAttribute
    public String id;

    /**
     * Petri net tokens
     */
    @XmlElement(name = "token")
    @XmlJavaTypeAdapter(TokenAdapter.class)
    public Collection<Token> tokens;

    /**
     * Petri net annotations
     */
    @XmlElement(name = "labels")
    @XmlJavaTypeAdapter(AnnotationAdapter.class)
    public Collection<Annotation> annotations;

    /**
     * Petri net rate parameters
     */
    @XmlElement(name = "definition")
    @XmlJavaTypeAdapter(RateParameterAdapter.class)
    public Collection<RateParameter> rateParameters;

    /**
     * Petri net places
     */
    @XmlElement(name = "place")
    @XmlJavaTypeAdapter(PlaceAdapter.class)
    public Collection<Place> places = new TreeSet<Place>(new PlaceComparator());

    /**
     * Petri net transitions
     */
    @XmlElement(name = "transition")
    @XmlJavaTypeAdapter(TransitionAdapter.class)
    public Collection<Transition> transitions;

    /**
     * Petri net arcs
     */
    @XmlElement(name = "arc")
    @XmlJavaTypeAdapter(ArcAdapter.class)
    public Collection<Arc<? extends Connectable, ? extends Connectable>> arcs;

    private class PlaceComparator implements Comparator<Place> {

        public PlaceComparator() {
        }

        @Override
        public int compare(Place place1, Place place2) {
            return place1.getId().compareTo(place2.getId());
        }
    }
}
