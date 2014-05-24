package uk.ac.imperial.pipe.io.adapters.model;

import uk.ac.imperial.pipe.io.adapters.modelAdapter.*;
import uk.ac.imperial.pipe.models.component.Connectable;
import uk.ac.imperial.pipe.models.component.annotation.Annotation;
import uk.ac.imperial.pipe.models.component.arc.Arc;
import uk.ac.imperial.pipe.models.component.place.Place;
import uk.ac.imperial.pipe.models.component.rate.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.component.rate.RateParameter;
import uk.ac.imperial.pipe.models.component.token.Token;
import uk.ac.imperial.pipe.models.component.transition.Transition;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Collection;

@XmlType(propOrder = {"tokens", "annotations", "rateParameters", "places", "transitions", "arcs"})
public class AdaptedPetriNet {
    @XmlElement(name = "token")
    @XmlJavaTypeAdapter(TokenAdapter.class)
    public Collection<Token> tokens;

    @XmlElement(name = "labels")
    @XmlJavaTypeAdapter(AnnotationAdapter.class)
    public Collection<Annotation> annotations;


    @XmlElement(name = "definition")
    @XmlJavaTypeAdapter(RateParameterAdapter.class)
    public Collection<RateParameter> rateParameters;

    @XmlElement(name = "place")
    @XmlJavaTypeAdapter(PlaceAdapter.class)
    public Collection<Place> places;

    @XmlElement(name = "transition")
    @XmlJavaTypeAdapter(TransitionAdapter.class)
    public Collection<Transition> transitions;

    @XmlElement(name = "arc")
    @XmlJavaTypeAdapter(ArcAdapter.class)
    public Collection<Arc<? extends Connectable, ? extends Connectable>> arcs;

}
