package uk.ac.imperial.pipe.models.petrinet;

import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.collections.CollectionUtils;

import uk.ac.imperial.pipe.exceptions.InvalidRateException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.PetriNet.NameChangeArcListener;
import uk.ac.imperial.pipe.models.petrinet.PetriNet.NameChangeListener;
import uk.ac.imperial.pipe.models.petrinet.PetriNet.TokenNameChanger;
import uk.ac.imperial.pipe.models.petrinet.name.PetriNetName;

public abstract class AbstractPetriNet  {

    /**
	 * Message fired when Petri net name changes
	 */
	public static final String PETRI_NET_NAME_CHANGE_MESSAGE = "nameChange";
	/**
	 * Maps transition id -> transition
	 */
	protected  Map<String, Transition> transitions = new HashMap<>();
	/**
	 * Maps place id -> place
	 */
	protected  Map<String, Place> places = new HashMap<>();
	/**
	 * Maps token id -> token
	 */
	protected  Map<String, Token> tokens = new HashMap<>();
	/**
	 * Maps inbound arc id -> inbound arc
	 */
	protected  Map<String, InboundArc> inboundArcs = new HashMap<>();
	/**
	 * Maps outbound arc id -> outbound arc
	 */
	protected  Map<String, OutboundArc> outboundArcs = new HashMap<>();
	/**
	 * Maps rate paramter id -> rate paramter
	 */
	protected  Map<String, RateParameter> rateParameters = new HashMap<>();
	/**
	 * Maps annotation id -> annotation
	 */
	protected  Map<String, Annotation> annotations = new HashMap<>();
	/**
	 * A tokens that will contain the maps specified above.
	 * It's ID is the class type to tokens
	 * Sadly need to cast to get the exact tokens back out of it. If you know of a better way to
	 * do this then please change it. It is used to easily get a Petri net component of type T
	 * by id.
	 */
	protected final Map<Class<? extends PetriNetComponent>, Map<String, ? extends PetriNetComponent>> componentMaps = new HashMap<>();
	/**
	 * Property change support used to fire messages and register listeners to
	 */
	protected final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
	/**
	 * Petri net name
	 */
	protected PetriNetName petriNetName;

	/**
     * @param id
     * @return true if any component in the Petri net has this id
     */
    public  abstract boolean containsComponent(String id);

    public AbstractPetriNet() {
    	initialiseIdMap();
    }
	/**
	 * Initialises the petri net components map for addtion and retreivals
	 * by mapping the component interface class to the map that contains the components
	 */
	protected void initialiseIdMap() {
	    componentMaps.put(Place.class, places);
	    componentMaps.put(Transition.class, transitions);
	    componentMaps.put(InboundArc.class, inboundArcs);
	    componentMaps.put(OutboundArc.class, outboundArcs);
	    componentMaps.put(Token.class, tokens);
	    componentMaps.put(RateParameter.class, rateParameters);
	    componentMaps.put(Annotation.class, annotations);
	}

	/**
	 * @return all Places currently in the Petri net
	 */
	public Collection<Place> getPlaces() {
	    return places.values();
	}

	/**
	 * @return all transitions in the Petri net
	 */
	public Collection<Transition> getTransitions() {
	    return transitions.values();
	}

	/**
	 * @return Petri net's collection of arcs
	 */
	public Collection<Arc<? extends Connectable, ? extends Connectable>> getArcs() {
	    Collection<Arc<? extends Connectable, ? extends Connectable>> arcs = new LinkedList<>();
	    arcs.addAll(getOutboundArcs());
	    arcs.addAll(getInboundArcs());
	    return arcs;
	}

	/**
	 *
	 * @return all outbound arcs in the Petri net
	 */
	public Collection<OutboundArc> getOutboundArcs() {
	    return outboundArcs.values();
	}

	/**
	 *
	 * @return all inbound arcs in the Petri net
	 */
	public Collection<InboundArc> getInboundArcs() {
	    return inboundArcs.values();
	}

	/**
	 * @return Petri net's list of tokens
	 */
	public Collection<Token> getTokens() {
	    return tokens.values();
	}

	/**
	 * @return annotations stored in the Petri net
	 */
	public Collection<Annotation> getAnnotations() {
	    return annotations.values();
	}

	/**
	 * @return rate parameters stored in the Petri net
	 */
	public Collection<RateParameter> getRateParameters() {
	    return rateParameters.values();
	}

	/**
	 * @param id    component name
	 * @param clazz PetriNetComponent class
	 * @param <T>   type of Petri net component required
	 * @return component with the specified id if it exists in the Petri net
	 * @throws PetriNetComponentNotFoundException if component does not exist in Petri net
	 */
	public <T extends PetriNetComponent> T getComponent(String id,
			Class<T> clazz) throws PetriNetComponentNotFoundException {
			    Map<String, T> map = getMapForClass(clazz);
			    if (map.containsKey(id)) {
			        return map.get(id);
			    }
			    throw new PetriNetComponentNotFoundException("No component " + id + " exists in Petri net.");
			}

	/**
	 *
	 * @param clazz component map type, this should be the interface of the component
	 * @param <T> componennt class
	 * @return the map that corresponds to the clazz type.
	 */
	@SuppressWarnings("unchecked")
	public <T extends PetriNetComponent> Map<String, T> getMapForClass(Class<T> clazz) {
	    return (Map<String, T>) componentMaps.get(clazz);
	}


	@Override
	public boolean equals(Object o) {
	    if (this == o) {
	        return true;
	    }
	    if (!(o instanceof AbstractPetriNet)) {
	        return false;
	    }
	
	    AbstractPetriNet petriNet = (AbstractPetriNet) o;
	
	
	    if (!CollectionUtils.isEqualCollection(annotations.values(), petriNet.annotations.values())) {
	        return false;
	    }
	    if (!CollectionUtils.isEqualCollection(inboundArcs.values(), petriNet.inboundArcs.values())) {
	        return false;
	    }
	    if (!CollectionUtils.isEqualCollection(outboundArcs.values(), petriNet.outboundArcs.values())) {
	        return false;
	    }
	    if (petriNetName != null ? !petriNetName.equals(petriNet.petriNetName) : petriNet.petriNetName != null) {
	        return false;
	    }
	    if (!CollectionUtils.isEqualCollection(places.values(), petriNet.places.values())) {
	        return false;
	    }
	    if (!CollectionUtils.isEqualCollection(rateParameters.values(), petriNet.rateParameters.values())) {
	        return false;
	    }
	    if (!CollectionUtils.isEqualCollection(tokens.values(), petriNet.tokens.values())) {
	        return false;
	    }
	    if (!CollectionUtils.isEqualCollection(transitions.values(), petriNet.transitions.values())) {
	        return false;
	    }
	
	    return true;
	}


	/**
	 *
	 * @return petri net name
	 */
	@XmlTransient
	public PetriNetName getName() {
	    return petriNetName;
	}


	/**
	 * Give the petri net a new name
	 * @param name name to replace the existing name with
	 */
	public void setName(PetriNetName name) {
	    PetriNetName old = this.petriNetName;
	    this.petriNetName = name;
	    changeSupport.firePropertyChange(PETRI_NET_NAME_CHANGE_MESSAGE, old, name);
	}

	/**
	 * Adds the annotation to the Petri net
	 *
	 * @param annotation
	 */
	public abstract void addAnnotation(Annotation annotation); 

	/**
	 * Adds place to the Petri net
	 *
	 * @param place place to add to Petri net
	 */
	public abstract void addPlace(Place place);

	/**
	 * Adds transition to the Petri net
	 *
	 * @param transition transition to add to the Petri net
	 */
	public abstract void addTransition(Transition transition) ;
	/**
	 *
	 * Adds this arc to the petri net
	 *
	 * @param inboundArc inbound arc to include in the Petri net
	 */
	public abstract void addArc(InboundArc inboundArc);

	/**
	 * Adds this arc to the petri net
	 * @param outboundArc outbound arc to include in the Petri net
	 */
	public abstract void addArc(OutboundArc outboundArc);
	
	/**
	 * Adds the token to the Petri net
	 *
	 * @param token
	 */
	public abstract void addToken(Token token);
	/**
	 * Adds the RateParameter to the Petri Net
	 *
	 * @param rateParameter to add to Petri net
	 * @throws InvalidRateException if the rate is not parseable
	 */
	public abstract void addRateParameter(RateParameter rateParameter) throws InvalidRateException; 

	protected <T extends PetriNetComponent> boolean addComponentToMap(T component, Map<String, T> components) {
		if (!components.containsKey(component.getId())) {
			components.put(component.getId(), component);
			return true;
		}
		else return false; 
	}
}

