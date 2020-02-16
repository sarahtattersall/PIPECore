package uk.ac.imperial.pipe.models.petrinet;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;

import uk.ac.imperial.pipe.exceptions.InvalidRateException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;
import uk.ac.imperial.pipe.models.petrinet.name.PetriNetName;
import uk.ac.imperial.pipe.parsers.FunctionalResults;
import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

/**
 * Petri net class that houses Petri net components and performs the logic on their
 * insertion and deletion.
 */
public class PetriNet extends AbstractPetriNet {
    /**
     * Message fired when an annotation is added to the Petri net
     */
    public static final String NEW_ANNOTATION_CHANGE_MESSAGE = "newAnnotation";

    /**
     * Message fired when a transition is deleted from the Petri net
     */
    public static final String DELETE_TRANSITION_CHANGE_MESSAGE = "deleteTransition";

    /**
     * Message fired when an annotation is deleted from the Petri net
     */
    public static final String DELETE_ANNOTATION_CHANGE_MESSAGE = "deleteAnnotation";

    /**
     * Message fired when a Place is added to the Petri net
     */
    public static final String NEW_PLACE_CHANGE_MESSAGE = "newPlace";

    /**
     * Message fired when a transition is added to the Petri net
     */
    public static final String NEW_TRANSITION_CHANGE_MESSAGE = "newTransition";

    /**
     * Message fired when a token is added to the Petri net
     */
    public static final String NEW_TOKEN_CHANGE_MESSAGE = "newToken";

    /**
     * Message fired when a token is deleted
     */
    public static final String DELETE_TOKEN_CHANGE_MESSAGE = "deleteToken";

    /**
     * Message fired when a rate parameter is added
     */
    public static final String NEW_RATE_PARAMETER_CHANGE_MESSAGE = "newRateParameter";

    /**
     * Message fired when a rate parameter is deleted
     */
    public static final String DELETE_RATE_PARAMETER_CHANGE_MESSAGE = "deleteRateParameter";

    //    /**
    //     * Functional weight parser
    //     */
    //    protected FunctionalWeightParser<Double> functionalWeightParser = new PetriNetWeightParser(new EvalVisitor(this), this);

    /**
     * Visitor used to remove petri net components when the type is not directly known
     */
    //TODO: CYCLIC DEPENDENCY BETWEEN CREATING THIS AND PETRI NET/
    private final PetriNetComponentVisitor deleteVisitor = new PetriNetComponentRemovalVisitor(this);

    /**
     * Used to add Petri net components to the Petri net when their type is not directly known
     */
    private final PetriNetComponentVisitor addVisitor = new PetriNetComponentAddVisitor(this);

    /**
     * Name of the Petri net
     */
    //TODO: IS THIS USED?
    public String pnmlName = "";

    /**
     * Validated
     */
    //TODO: WHAT IS THIS
    private boolean validated = false;

    private ExecutablePetriNet executablePetriNet;

    /**
     * Constructor initializes the petri net components map, include hierarchy, and sets PetriNetName
     * @param name the name of the Petri net, it should be unique
     */
    public PetriNet(PetriNetName name) {
        super();
        includeHierarchy = new IncludeHierarchy(this, null);
        this.petriNetName = name;
    }

    /**
     * Default constructor sets name to blank
     */
    public PetriNet() {
        this(new NormalPetriNetName(""));
    }

    /**
     *
     * @param listener listens for changes on the Petri net
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     *
     * @param listener current listener listining to the Petri net
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    /**
     *
     * @return PNML name
     */
    @XmlTransient
    public String getPnmlName() {
        return pnmlName;
    }

    /**
     *
     * @param pnmlName file name
     */
    public void setPnmlName(String pnmlName) {
        this.pnmlName = pnmlName;
    }

    /**
     *
     * @return true if validated
     */
    @XmlTransient
    public boolean isValidated() {
        return validated;
    }

    /**
     *
     * @param validated new validated value
     */
    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    /**
     * Adds place to the Petri net
     *
     * @param place place to add to Petri net
     */
    @Override
    public void addPlace(Place place) {
        if (addComponentToMap(place, places)) {
            setInitialTokenCountsToZero(place);
            place.addPropertyChangeListener(Place.CAPACITY_CHANGE_MESSAGE, getExecutablePetriNetBare());
            place.addPropertyChangeListener(Place.ID_CHANGE_MESSAGE, getExecutablePetriNetBare());
            //            super.addAndNotifyListeners(place, places, NEW_PLACE_CHANGE_MESSAGE);
            addAndNotifyListeners(place, places, NEW_PLACE_CHANGE_MESSAGE);
        }
    }

    @Override
    public void addArc(InboundArc inboundArc) {
        super.addArc(inboundArc);
        inboundArc.addPropertyChangeListener(Arc.SOURCE_CHANGE_MESSAGE, getExecutablePetriNetBare());
        inboundArc.addPropertyChangeListener(Arc.TARGET_CHANGE_MESSAGE, getExecutablePetriNetBare());
        inboundArc.addPropertyChangeListener(Arc.WEIGHT_CHANGE_MESSAGE, getExecutablePetriNetBare());
    }

    @Override
    public void addArc(OutboundArc outboundArc) {
        super.addArc(outboundArc);
        outboundArc.addPropertyChangeListener(Arc.SOURCE_CHANGE_MESSAGE, getExecutablePetriNetBare());
        outboundArc.addPropertyChangeListener(Arc.TARGET_CHANGE_MESSAGE, getExecutablePetriNetBare());
        outboundArc.addPropertyChangeListener(Arc.WEIGHT_CHANGE_MESSAGE, getExecutablePetriNetBare());
    }

    @Override
    public void removeArc(InboundArc arc) {
        super.removeArc(arc);
        changeSupport.firePropertyChange(DELETE_ARC_CHANGE_MESSAGE, arc, null);
    }

    @Override
    public void removeArc(OutboundArc arc) {
        super.removeArc(arc);
        changeSupport.firePropertyChange(DELETE_ARC_CHANGE_MESSAGE, arc, null);
    }

    private void setInitialTokenCountsToZero(Place place) {
        for (Token token : tokens.values()) {
            if (!place.getTokenCounts().containsKey(token.getId())) {
                place.setTokenCount(token.getId(), 0);
            }
        }
    }

    /**
     * Adds transition to the Petri net
     *
     * @param transition transition to add to the Petri net
     */
    @Override
    public void addTransition(Transition transition) {
        if (addComponentToMap(transition, transitions)) {
            transition.addPropertyChangeListener(new NameChangeArcListener());
            transition.addPropertyChangeListener(Transition.ID_CHANGE_MESSAGE, getExecutablePetriNetBare());
            transition.addPropertyChangeListener(Transition.PRIORITY_CHANGE_MESSAGE, getExecutablePetriNetBare());
            transition.addPropertyChangeListener(Transition.RATE_CHANGE_MESSAGE, getExecutablePetriNetBare());
            transition.addPropertyChangeListener(Transition.DELAY_CHANGE_MESSAGE, getExecutablePetriNetBare());
            transition.addPropertyChangeListener(Transition.INFINITE_SEVER_CHANGE_MESSAGE, getExecutablePetriNetBare());
            transition.addPropertyChangeListener(Transition.TIMED_CHANGE_MESSAGE, getExecutablePetriNetBare());
            super.addAndNotifyListeners(transition, transitions, NEW_TRANSITION_CHANGE_MESSAGE);
        }
    }

    /**
     * Removes transition from the petri net. Also removes any arcs connected
     * to this transition
     *
     * @param transition to remove
     */
    public void removeTransition(Transition transition) {
        this.transitions.remove(transition.getId());
        for (OutboundArc arc : outboundArcs(transition)) {
            removeArc(arc);
        }
        for (InboundArc arc : inboundArcs(transition)) {
            removeArc(arc);
        }
        transitionOutboundArcs.removeAll(transition.getId());
        transitionInboundArcs.removeAll(transition.getId());
        changeSupport.firePropertyChange(DELETE_TRANSITION_CHANGE_MESSAGE, transition, null);
    }

    /**
     * Adds the token to the Petri net
     *
     * @param token to be added
     */
    @Override
    public void addToken(Token token) {
        if (addComponentToMap(token, tokens)) {
            updateAllPlacesWithCountZeroForThisToken(token);
            token.addPropertyChangeListener(new TokenNameChanger());
            addAndNotifyListeners(token, tokens, NEW_TOKEN_CHANGE_MESSAGE);
        }
    }

    private void updateAllPlacesWithCountZeroForThisToken(Token token) {
        for (Place place : places.values()) {
            place.setTokenCount(token.getId(), 0);
        }
    }

    /**
     * Tries to remove the token
     *
     * @param token token to remove
     * @throws PetriNetComponentException if places or transitions reference this token!
     */
    public void removeToken(Token token) throws PetriNetComponentException {
        Collection<Place> referencedPlaces = getPlacesContainingToken(token);
        Collection<Transition> referencedTransitions = getTransitionsReferencingToken(token);
        if (referencedPlaces.isEmpty() && referencedTransitions.isEmpty()) {
            tokens.remove(token.getId());
            cleanupZeroCountEntriesInPlaces(token);
            changeSupport.firePropertyChange(DELETE_TOKEN_CHANGE_MESSAGE, token, null);
            return;
        }
        StringBuilder message = new StringBuilder();
        message.append("Cannot remove ").append(token.getId()).append(" token");
        if (!referencedPlaces.isEmpty()) {
            message.append(" places: ");
            for (Place place : referencedPlaces) {
                message.append(place.getId());
            }
            message.append(" contains it\n");
        }
        if (!referencedTransitions.isEmpty()) {
            message.append(" transitions: ");
            for (Transition transition : referencedTransitions) {
                message.append(transition.getId());
            }
            message.append(" reference it\n");
        }

        throw new PetriNetComponentException(message.toString());
    }

    private void cleanupZeroCountEntriesInPlaces(Token token) {
        for (Place place : places.values()) {
            place.removeAllTokens(token.getId());
        }
    }

    /**
     * @param token
     * @return collection of Places that contain 1 or more of these tokens
     */
    private Collection<Place> getPlacesContainingToken(Token token) {
        Collection<Place> result = new LinkedList<>();
        for (Place place : places.values()) {
            if (place.getTokenCount(token.getId()) > 0) {
                result.add(place);
            }
        }
        return result;
    }

    /**
     * @param token
     * @return list of transitions that reference the token in their rate expression
     */
    private Collection<Transition> getTransitionsReferencingToken(Token token) {
        Collection<Transition> result = new LinkedList<>();
        for (Transition transition : transitions.values()) {
            FunctionalResults<Double> results = functionalWeightParser.evaluateExpression(transition.getRateExpr());
            if (results.getComponents().contains(token.getId())) {
                result.add(transition);
            }
        }
        return result;
    }

    /**
     * Adds the annotation to the Petri net
     *
     * @param annotation to be added
     */
    @Override
    public void addAnnotation(Annotation annotation) {
        if (addComponentToMap(annotation, annotations)) {
            addAndNotifyListeners(annotation, annotations, NEW_ANNOTATION_CHANGE_MESSAGE);
        }
    }

    /**
     * Removes the specified annotation from the Petri net
     *
     * @param annotation annotation to remove
     */
    public void removeAnnotation(Annotation annotation) {
        annotations.remove(annotation.getId());
        changeSupport.firePropertyChange(DELETE_ANNOTATION_CHANGE_MESSAGE, annotation, null);
    }

    /**
     * Adds the RateParameter to the Petri Net
     *
     * @param rateParameter to add to Petri net
     * @throws InvalidRateException if the rate is not parseable
     */
    @Override
    public void addRateParameter(RateParameter rateParameter) throws InvalidRateException {
        if (!validFunctionalExpression(rateParameter.getExpression())) {
            throw new InvalidRateException(rateParameter.getExpression());
        }
        //        if (!rateParameters.containsValue(rateParameter)) {
        if (addComponentToMap(rateParameter, rateParameters)) {
            addAndNotifyListeners(rateParameter, rateParameters, NEW_RATE_PARAMETER_CHANGE_MESSAGE);
        }
    }

    /**
     * Attempts to parse the expression of the rate
     *
     * @param expression functional expression to evaluate for Petri net
     * @return false if the rate's expression is invalid
     */
    public boolean validFunctionalExpression(String expression) {
        FunctionalResults<Double> result = functionalWeightParser.evaluateExpression(expression);
        return !result.hasErrors();
    }

    /**
     * Removes the rate parameter from the Petri net.
     * <p>
     * Any transitions referencing this rate parameter will have their rates
     * set to the last value of the rate parameter
     * </p>
     * @param parameter rate parameter to remove
     */
    public void removeRateParameter(RateParameter parameter) {
        removeRateParameterFromTransitions(parameter);
        rateParameters.remove(parameter.getId());
        changeSupport.firePropertyChange(DELETE_RATE_PARAMETER_CHANGE_MESSAGE, parameter, null);
    }

    /**
     * Removes the Rate Parameter from any transitions that refer to it
     * and replaces it with a {@link NormalRate} with the
     * same value
     *
     * @param parameter to remove
     */
    private void removeRateParameterFromTransitions(RateParameter parameter) {
        for (Transition transition : transitions.values()) {
            if (transition.getRate().equals(parameter)) {
                Rate rate = new NormalRate(parameter.getExpression());
                transition.setRate(rate);
            }
        }
    }

    /**
     * Add any Petri net component to this Petri net
     *
     * @param component to be added
     * @throws PetriNetComponentException if component already exists or other logic error
     */
    public void add(PetriNetComponent component) throws PetriNetComponentException {
        component.accept(addVisitor);
    }

    /**
     * Remove any Petri net component from the Petri net
     *
     * @param component component to remove
     * @throws PetriNetComponentException if component does not exist in the Petri net
     */
    public void remove(PetriNetComponent component) throws PetriNetComponentException {
        if (contains(component.getId())) {
            component.accept(deleteVisitor);
        }
    }

    /**
     * @return true if the Petri net contains a default token
     */
    public boolean containsDefaultToken() {
        return tokens.containsKey("Default");
    }

    /**
    *
    * @return a set of all component id's contained within this Petri net
    */
    public Set<String> getComponentIds() {
        Set<String> results = new HashSet<>();
        for (Map<String, ? extends PetriNetComponent> entry : componentMaps.values()) {
            results.addAll(entry.keySet());
        }
        return results;
    }

    /**
    *
    * @param id of component
    * @return true if a component with the given id exists in the Petri net
    */
    public boolean contains(String id) {
        return getComponentIds().contains(id);
    }

    @Override
    protected <T extends PetriNetComponent> void addAndNotifyListeners(T component, Map<String, T> components,
            String newMessage) {
        //	   component.addPropertyChangeListener(getExecutablePetriNetBare()); //TODO drop this when each component is separately listening
        getExecutablePetriNetBare().refreshRequired();
        super.addAndNotifyListeners(component, components, newMessage);
    }

    public ExecutablePetriNet getExecutablePetriNet() {
        getExecutablePetriNetBare().refresh();
        //TODO move this into ExecutablePetriNet
        getExecutablePetriNetBare().getTimingQueue().rebuild(getExecutablePetriNetBare().getState());
        return executablePetriNet;
    }

    protected ExecutablePetriNet getExecutablePetriNetBare() {
        if (executablePetriNet == null) {
            executablePetriNet = new ExecutablePetriNet(this);
            addPropertyChangeListener(executablePetriNet);
        }
        return executablePetriNet;
    }

    /**
     *
     * @return string representation of the Petri net name
     */
    public String getNameValue() {
        return petriNetName.getName();
    }

    /**
     * This class is responsible for changing inbound and outbound arc references from
     * a transition id change
     */
    private class NameChangeArcListener implements PropertyChangeListener {

        /**
         * If a transition changes name then this is updated in the maps by removing the key
         * and replacing the inbound/outbound arcs with the new name as the key.
         * @param evt
         */
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(PetriNetComponent.ID_CHANGE_MESSAGE)) {
                String oldId = (String) evt.getOldValue();
                String newId = (String) evt.getNewValue();
                Collection<InboundArc> inbound = transitionInboundArcs.removeAll(oldId);
                Collection<OutboundArc> outbound = transitionOutboundArcs.removeAll(oldId);
                transitionInboundArcs.putAll(newId, inbound);
                transitionOutboundArcs.putAll(newId, outbound);
            }
        }
    }

    /**
     * Listens for name changes of a token
     */
    private class TokenNameChanger implements PropertyChangeListener {

        /**
         * When a tokens name changes then the maps in the places and arc need adjusting
         * @param evt
         */
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(PetriNetComponent.ID_CHANGE_MESSAGE)) {
                String oldId = (String) evt.getOldValue();
                String newId = (String) evt.getNewValue();
                changePlaceTokens(oldId, newId);
                changeArcTokens(oldId, newId);
            }
        }

        /**
         *
         * Changes references of token counts in place containing old id to new id
         *
         * @param oldId old token id
         * @param newId new token id
         */
        private void changePlaceTokens(String oldId, String newId) {
            for (Place place : getPlaces()) {
                int count = place.getTokenCount(oldId);
                place.removeAllTokens(oldId);
                place.setTokenCount(newId, count);
            }
        }

        /**
         *
         * Changes references of token weights in arcs from old id to new id
         *
         * @param oldId old token id
         * @param newId new token id
         */
        private void changeArcTokens(String oldId, String newId) {
            for (Arc<? extends Connectable, ? extends Connectable> arc : getArcs()) {
                if (arc.getTokenWeights().containsKey(oldId)) {
                    String weight = arc.getWeightForToken(oldId);
                    arc.removeAllTokenWeights(oldId);
                    arc.setWeight(newId, weight);
                }
            }
        }
    }

    public void setIncludeHierarchy(IncludeHierarchy includeHierarchy) {
        this.includeHierarchy = includeHierarchy;
    }

}
