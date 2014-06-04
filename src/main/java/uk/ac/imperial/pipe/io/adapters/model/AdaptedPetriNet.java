package uk.ac.imperial.pipe.io.adapters.model;

import uk.ac.imperial.pipe.io.adapters.modelAdapter.*;
import uk.ac.imperial.pipe.models.petrinet.*;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Collection;

/**
 * Adapted Petri net, whose XmlElement name represents the name given to them in PNML.
 * items are written in propOrder
 */
@XmlType(propOrder = {"tokens", "annotations", "rateParameters", "places", "transitions", "arcs"})
public class AdaptedPetriNet {
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
    public Collection<Place> places;

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

}
