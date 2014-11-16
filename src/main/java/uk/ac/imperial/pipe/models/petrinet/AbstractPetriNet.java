package uk.ac.imperial.pipe.models.petrinet;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.collections.CollectionUtils;

import uk.ac.imperial.pipe.exceptions.InvalidRateException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.name.PetriNetName;
import uk.ac.imperial.pipe.parsers.EvalVisitor;
import uk.ac.imperial.pipe.parsers.FunctionalResults;
import uk.ac.imperial.pipe.parsers.FunctionalWeightParser;
import uk.ac.imperial.pipe.parsers.PetriNetWeightParser;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public abstract class AbstractPetriNet extends AbstractPetriNetPubSub {

    /**
	 * Message fired when Petri net name changes
	 */
	public static final String PETRI_NET_NAME_CHANGE_MESSAGE = "nameChange";
    /**
     * Message fired when an arc is added to the Petri net
     */
    public static final String NEW_ARC_CHANGE_MESSAGE = "newArc";
	/**
	 * Message fired when an arc is deleted from the Petri net
	 */
	public static final String DELETE_ARC_CHANGE_MESSAGE = "deleteArc";
    /**
     * Message fired when a place is deleted from the Petri net
     */
    public static final String DELETE_PLACE_CHANGE_MESSAGE = "deletePlace";

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
	protected Map<Class<? extends PetriNetComponent>, Map<String, ? extends PetriNetComponent>> componentMaps = new HashMap<>();
//	/**
//	 * Property change support used to fire messages and register listeners to
//	 */
//	protected final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
	/**
	 * Petri net name
	 */
	protected PetriNetName petriNetName;
	/**
	 *  Maps transition id -> outbound arcs out of the transition
	 */
	protected Multimap<String, OutboundArc> transitionOutboundArcs = HashMultimap.create();
	/**
	 * Maps transition id -> inbound arcs into the transition
	 */
	protected Multimap<String, InboundArc> transitionInboundArcs = HashMultimap.create();
	
    /**
     * Functional weight parser
     */
    protected FunctionalWeightParser<Double> functionalWeightParser = new PetriNetWeightParser(new EvalVisitor(this), this);


	protected IncludeHierarchy includeHierarchy;


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
	@Override
	public int hashCode() {
	        int result = transitions.hashCode();
	        result = 31 * result + places.hashCode();
	        result = 31 * result + tokens.hashCode();
	        result = 31 * result + inboundArcs.hashCode();
	        result = 31 * result + outboundArcs.hashCode();
	        result = 31 * result + annotations.hashCode();
	        result = 31 * result + rateParameters.hashCode();
	        result = 31 * result + (petriNetName != null ? petriNetName.hashCode() : 0);
	        return result;
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
    public void addArc(InboundArc inboundArc) {
        if (addComponentToMap(inboundArc, inboundArcs)) {
            transitionInboundArcs.put(inboundArc.getTarget().getId(), inboundArc);
            addAndNotifyListeners(inboundArc, inboundArcs, NEW_ARC_CHANGE_MESSAGE);
        }
    }
    
    /**
     * Adds this arc to the petri net
     * @param outboundArc outbound arc to include in the Petri net
     */
    public void addArc(OutboundArc outboundArc) {
        if (addComponentToMap(outboundArc, outboundArcs)) {
            transitionOutboundArcs.put(outboundArc.getSource().getId(), outboundArc);
            addAndNotifyListeners(outboundArc, outboundArcs, NEW_ARC_CHANGE_MESSAGE);
        }
    }
    protected <T extends PetriNetComponent> void addAndNotifyListeners(T component, Map<String, T> components, String newMessage) {
		component.addPropertyChangeListener(new NameChangeListener<>(component, components));
		changeSupport.firePropertyChange(newMessage, null, component);
	}
	
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

	/**
	 * An outbound arc of a transition is any arc that starts at the transition
	 * and connects elsewhere
	 *
	 * @param transition to find outbound arcs for
	 * @return arcs that are outbound from transition
	 */
	public Collection<OutboundArc> outboundArcs(Transition transition) {
	    return transitionOutboundArcs.get(transition.getId());
	}

	/**
	    *
	    * @return the IncludeHierarchy representing any PetriNets included directly by this net, or indirectly by includes done in included in Petri nets. 
	    */
	public IncludeHierarchy getIncludeHierarchy() {
		return includeHierarchy;
	}

	/**
	 * @param transition to calculate inbound arc for
	 * @return arcs that are inbound to transition, that is arcs that come into the transition
	 */
	public Collection<InboundArc> inboundArcs(Transition transition) {
	    return transitionInboundArcs.get(transition.getId());
	}

	/**
	 * @param id
	 * @return true if any component in the Petri net has this id
	 */
	public boolean containsComponent(String id) {
	    for (Map<String, ? extends PetriNetComponent> map : componentMaps.values()) {
	        if (map.containsKey(id)) {
	            return true;
	        }
	    }
	    return false;
	}
	/**
	 * @param place
	 * @return arcs that are outbound from place
	 */
	public Collection<InboundArc> outboundArcs(Place place) {
	    Collection<InboundArc> outbound = new LinkedList<>();
	    for (InboundArc arc : inboundArcs.values()) {
	        if (arc.getSource().equals(place)) {
	            outbound.add(arc);
	        }
	    }
	    return outbound;
	}
	/**
	 * @param place
	 * @return arcs that are inbound to place
	 */
	public Collection<OutboundArc> inboundArcs(Place place) {
		Collection<OutboundArc> inbound = new LinkedList<>();
		for (OutboundArc arc : outboundArcs.values()) {
			if (arc.getTarget().equals(place)) {
				inbound.add(arc);
			}
		}
		return inbound;
	}
	/**
	 * Removes the specified arc from the Petri net
	 *
	 * @param arc to remove from the Petri net
	 */
	public void removeArc(InboundArc arc) {
	    inboundArcs.remove(arc.getId());
	    transitionInboundArcs.remove(arc.getTarget().getId(), arc);
	}
	/**
	 * Removes the specified arc from the Petri net
	 *
	 * @param arc to remove from the Petri net
	 */
	public void removeArc(OutboundArc arc) {
        outboundArcs.remove(arc.getId());
        transitionOutboundArcs.remove(arc.getSource().getId(), arc);
	}
	public void convertArcsToUseNewPlace(Place oldPlace, Place newPlace) {
		convertInboundArcsToUseNewPlace(oldPlace, newPlace);
		convertOutboundArcsToUseNewPlace(oldPlace, newPlace); 
	}
	public void convertInboundArcsToUseNewPlace(Place oldPlace, Place newPlace) {
		for (InboundArc arc : outboundArcs(oldPlace)) {
			arc.setSource(newPlace); 
		}
	}
	public void convertOutboundArcsToUseNewPlace(Place oldPlace, Place newPlace) {
		for (OutboundArc arc : inboundArcs(oldPlace)) {
			arc.setTarget(newPlace); 
		}
	}
	//TODO may not be needed once PlaceStatus replaces InterfacePlace
	public void replacePlace(Place oldPlace, Place newPlace) throws PetriNetComponentException {
		convertArcsToUseNewPlace(oldPlace, newPlace); 
		removePlace(oldPlace);
		if (!places.containsKey(newPlace.getId())) {
			addPlace(newPlace); 
		}
	}
    /**
     * Removes the place and all arcs connected to the place from the
     * Petri net
     *
     * @param place to remove from Petri net
     */
    public void removePlace(Place place) throws PetriNetComponentException {
    	verifyPlaceNotInUseInInterface(place);
        removePlaceBare(place);
    }
	protected void removePlaceBare(Place place)
			throws PetriNetComponentException {
		Collection<String> components = getComponentsReferencingId(place.getId());
        if (!components.isEmpty()) {
            throw new PetriNetComponentException("Cannot delete " + place.getId() + " it is referenced in a functional expression!");
        }
        this.places.remove(place.getId());
        for (InboundArc arc : outboundArcs(place)) {
            removeArc(arc);
        }
        changeSupport.firePropertyChange(DELETE_PLACE_CHANGE_MESSAGE, place, null);
	}
	protected void verifyPlaceNotInUseInInterface(Place place)
			throws PetriNetComponentException {
		Result<InterfacePlaceAction> result = place.getStatus().getMergeInterfaceStatus().remove(getIncludeHierarchy());
    	if (result.hasResult()) {
    		StringBuffer sb = new StringBuffer(); 
    		for (String message : result.getMessages()) {
				sb.append(message); 
				sb.append("\n");
			}
    		throw new PetriNetComponentException("Cannot delete "+place.getId()+":\n"+sb.toString()); 
    	}
	}

	/**
    *
    * @param componentId component id to find
    * @return all components ids whose functional expression references the componentId
    */
   protected Collection<String> getComponentsReferencingId(String componentId) {
       Set<String> results = new HashSet<>();
       for (Transition transition : getTransitions()) {
           if (referencesId(transition.getRateExpr(), componentId)) {
               results.add(transition.getId());
           }
       }
       for (Arc<?, ?> arc : getArcs()) {
           for (String expr : arc.getTokenWeights().values()) {
               if (referencesId(expr, componentId)) {
                   results.add(arc.getId());
                   break;
               }
           }
       }
       for (RateParameter rateParameter : getRateParameters()) {
           if (referencesId(rateParameter.getExpression(), componentId)) {
               results.add(rateParameter.getId());
           }
       }
       return results;
   }

	/**
    *
    * @param expr
    * @param id
    * @return true if the component id is referenced in the functional expression
    */
   protected boolean referencesId(String expr, String id) {
       Collection<String> components = getComponents(expr);
       return components.contains(id);
   }

   /**
    *
    * @param expression
    * @return a list of components that the expression references
    */
   protected Collection<String> getComponents(String expression) {
       FunctionalResults<Double> results = parseExpression(expression);
       return results.getComponents();
   }

	/**
     * Parse the functional expression via the under lying Petri net state
     *
     * @param expr functional expression which conforms to the rate grammar
     * @return parsed expression
     */
    public FunctionalResults<Double> parseExpression(String expr) {
        return functionalWeightParser.evaluateExpression(expr);
    }

	  /**
     * Listener for changing a components name in the set it is referenced by
     * @param <T>
     */
    protected static class NameChangeListener<T extends PetriNetComponent> implements PropertyChangeListener {
        /**
         * Comoponent whose name will change
         */
        private final T component;

        /**
         * Component map that houses the component, needs to be updated on name change
         */
        private final Map<String, T> componentMap;

        /**
         * Constructor
         * @param component
         * @param componentMap
         */
        public NameChangeListener(T component, Map<String, T> componentMap) {
            this.component = component;
            this.componentMap = componentMap;
        }

        /**
         * If the name/id of the component changes then it is updated in the component map.
         * That is the old key is removed and the component is re-added with the new name.
         * @param evt
         */
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(PetriNetComponent.ID_CHANGE_MESSAGE)) {
                String oldId = (String) evt.getOldValue();
                String newId = (String) evt.getNewValue();
                componentMap.remove(oldId);
                componentMap.put(newId, component);
            }

        }
    }

}

