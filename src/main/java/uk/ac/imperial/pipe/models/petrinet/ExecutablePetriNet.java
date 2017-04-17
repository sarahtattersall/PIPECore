package uk.ac.imperial.pipe.models.petrinet;



import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.pipe.exceptions.InvalidRateException;
import uk.ac.imperial.pipe.parsers.FunctionalWeightParser;
import uk.ac.imperial.pipe.parsers.PetriNetWeightParser;
import uk.ac.imperial.pipe.parsers.StateEvalVisitor;
import uk.ac.imperial.pipe.visitor.ClonePetriNet;
import uk.ac.imperial.state.HashedStateBuilder;
import uk.ac.imperial.state.State;

import com.google.common.collect.HashMultimap;

/**
 * Makes a PetriNet available for execution, that is, animation or analysis by a module.  
 * The complete state of the Petri net is a set of collections of its constituent components.
 * For efficiency of processing the marking of the Petri net is saved as State  
 * <p>
 * If the Petri net is a composite Petri net, each import statement has been replaced with the components 
 * that comprise the imported Petri net, resulting in a single Petri net, 
 * with corresponding collections of all the constituent components.  
 * </p><p>
 * If this executable Petri net is animated, the markings that result from firing 
 * enabled transitions will be populated in the affected places.  
 * If the affected places are components in an imported Petri net, the markings in the updated places in the 
 * executable Petri net are mirrored to the corresponding imported Petri net. </p>
 */
// * In the PIPE 5.0 gui, each imported Petri net is displayed in its own tab, and may be edited and persisted separately.  
// * Expanded Petri nets are not visible in the gui; their updated markings are visible in the tabs of the corresponding imported Petri net. 
public class ExecutablePetriNet extends AbstractPetriNet implements PropertyChangeListener {

	public static final String PETRI_NET_REFRESHED_MESSAGE = "executable Petri net refreshed";
	private PetriNet petriNet;
	private boolean refreshRequired;
	private State state;
	// Wrapping the state with time
	private TimedState timedState;
	
	//protected long timeStep = 10; // Is done in milliseconds.
	//protected long initialTime = 0;
	//public long currentTime= this.initialTime;
	
    /**
     * Functional weight parser
     */
    private FunctionalWeightParser<Double> functionalWeightParser;

    /**
     * Creates a new executable Petri net based upon a source Petri net.  Performs an immediate 
     * {@link #refreshRequired() refreshRequired} and {@link #refresh() refresh} to synchronize 
     * the structure of the two Petri nets.
	 * @param petriNet -- the source Petri net whose structure this executable Petri net mirrors. 
	 */
	
	public ExecutablePetriNet(PetriNet petriNet, long initTime) {
		this.petriNet = petriNet;
		includeHierarchy = petriNet.getIncludeHierarchy(); 
		refreshRequired = true;
		//this.initialTime = initTime;
		//this.currentTime = this.initialTime;
		refresh(); 
		timedState = new HashedTimedState(this, state, initTime);
	}
	
	public ExecutablePetriNet(PetriNet petriNet) {
		this(petriNet, 0);
	}
	
	/**
	 * This will cause the executable Petri net to be immediately re-built from the underlying 
	 * source Petri net, using {@link uk.ac.imperial.pipe.visitor.ClonePetriNet} 
	 * Assumes that {@link #refreshRequired() refreshRequired} has been called since the last refresh.  
	 * <p>
	 * In addition to cloning the source Petri net, a listener is added for each place in the 
	 * source Petri net to update its token counts whenever they 
	 * change in the executable Petri net.
	 * </p><p>
	 * Finally, a representation of the marking of this executable Petri net is saved 
	 * as a {@link uk.ac.imperial.state.State}.  This can be retrieved with {@link #getState()}</p>
	 */
	//TODO currently only called when state changes; consider calling refreshRequired() on any PN structure change
	public void refresh() {
		if (isRefreshRequired()) {
			notifyListenersToRemovePlaces(); 
			initializeMaps(); 
			refreshIncludeHierarchyComponents(); 
			addSelfAsListenerForPlaceTokenCountChanges(); 
			buildState(); 
			refreshRequired = false;
		    changeSupport.firePropertyChange(PETRI_NET_REFRESHED_MESSAGE, null, null);
		}
	}

	private void notifyListenersToRemovePlaces() {
		for (Place place : places.values()) {
			place.removeSelfFromListeners(); 
		}
	}

	private void refreshIncludeHierarchyComponents() {
		ClonePetriNet.refreshFromIncludeHierarchy(this);
	}
	private void initializeMaps() {
		transitions = new HashMap<>();
		places = new HashMap<>();
		tokens = new HashMap<>();
		inboundArcs = new HashMap<>();
		outboundArcs = new HashMap<>();
		rateParameters = new HashMap<>();
		annotations = new HashMap<>();
		transitionOutboundArcs = HashMultimap.create();
		transitionInboundArcs = HashMultimap.create();
		
		
		componentMaps = new HashMap<>();
		initialiseIdMap(); 
	}

	private void addSelfAsListenerForPlaceTokenCountChanges() {
		for (Place place: places.values()) {
			place.addPropertyChangeListener(this);  // force refresh 
		}
	}
	/**
	 * This will cause the executable Petri net to be re-built from the underlying source Petri net.  
	 * Used when the structure of the underlying source Petri net has 
	 * changed, although most changes are detected automatically.  
	 * <p>
	 * The refresh is done lazily, when the next "get" request is received. </p>
	 */
	public void refreshRequired() {
		refreshRequired = true; 
	}
	private void buildState() {
		HashedStateBuilder builder = new HashedStateBuilder();
		for (Place place : places.values()) {
			for (Token token : tokens.values()) {
				builder.placeWithToken(place.getId(), token.getId(), place.getTokenCount(token.getId()));
			}
		}
		state = builder.build();
	}
	
    /**
    * Supports calculating State independently of this executable petri net, 
    * and then applying an updated State later
    * @see #setState
    * @return the State of the executable Petri net.
    */
	public State getState() {
		refresh(); 
		return state;
	}
	
	public TimedState getTimedState() {
		// Refresh State - TODO: not sure necessary to reassign all the time
		timedState.setState( getState() );
		return timedState;
	}
	
	/**
	 * Updates the State of the executable Petri net.  All places will be updated with 
	 * corresponding token counts, both in the 
	 * executable Petri net and the underlying source Petri net.
	 * <p>
	 * Note that if the structure of the underlying source Petri net has changed since 
	 * this state was originally saved, the results are undefined. 
	 * </p>
	 * @param state the updated state
	 */
	public void setState(State state) {
		refreshRequired(); 
        for (Place place : places.values()) {
        	place.setTokenCounts(state.getTokens(place.getId()));
        }
	}
	
	public void setTimedState(TimedState timedState) {
		setState(timedState.getState());
		this.timedState = timedState;
	}
    /**
     * @return all the currently enabled immediate transitions in the petri net
     */
	//TODO calculate enabled transitions for other than current State
	//  getEnabledImmediateTransitions(state)....calls isEnabled(transition, state)
    public Set<Transition> getEnabledImmediateTransitions() {

        Set<Transition> enabledTransitions = new HashSet<>();
        for (Transition transition : getTransitions()) {
            if (isEnabled(transition) && !transition.isTimed()) {
                enabledTransitions.add(transition);
            }
        }
        return enabledTransitions;
    }
    /**
     * @return all the currently enabled timed transitions in the petri net
     */
    public Set<Transition> getEnabledTimedTransitions() {
        Set<Transition> enabledTransitions = new HashSet<>();
        for (Transition transition : getTransitions()) {
            if (isEnabled(transition) & transition.isTimed()) {
                enabledTransitions.add(transition);
            }
        }
        return enabledTransitions;
    }

    /**
     * Works out if an transition is enabled. This means that it checks if
     * a) places connected by an incoming arc to this transition have enough tokens to fire
     * b) places connected by an outgoing arc to this transition have enough space to fit the
     * new tokens (that is enough capacity).
     *
     * @param transition to see if it is enabled
     * @return true if transition is enabled
     */
    public boolean isEnabled(Transition transition) {
    	return isEnabled(transition, this.state);
    }
    public boolean isEnabled(Transition transition, State state) {
    	for (Arc<Place, Transition> arc : inboundArcs(transition)) {
    		if (!arc.canFire(this, state)) {
    			return false;
    		}
    	}
    	for (Arc<Transition, Place> arc : outboundArcs(transition)) {
    		if (!arc.canFire(this, state)) {
    			return false;
    		}
    	}
    	return true;
    }

	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		refreshRequired = true; 
	}
	/**
	 * @param expression to evaluate
	 * @return double result of the evaluation of the expression against the current state of 
	 * this executable petri net, or -1.0 if the expression is not valid. 
	 */
	public Double evaluateExpressionAgainstCurrentState(String expression) {
		return evaluateExpression(getState(), expression);
	}
	/**
	 * <i>Note that the expression is evaluated against the given state, 
	 * not the current state.  If evaluation against the current state is needed, 
	 * invoke {@link #evaluateExpressionAgainstCurrentState(String)}</i>.  
	 * @param state representing a possible marking of the places in this executable Petri net.  
	 * @param expression to evaluate
	 * @return double result of the evaluation of the expression against the given state, 
	 * or -1.0 if the expression is not valid. 
	 */
	public Double evaluateExpression(State state, String expression) {
		return buildFunctionalWeightParser(state).evaluateExpression(expression).getResult();
	}
	public FunctionalWeightParser<Double> getFunctionalWeightParserForCurrentState() {
		functionalWeightParser = buildFunctionalWeightParser(getState());
		return functionalWeightParser;
	}
	private FunctionalWeightParser<Double> buildFunctionalWeightParser(State state) {
		return new PetriNetWeightParser(new StateEvalVisitor(this, state), this);
	}

	
	/**
	 * @return all Places currently in the Petri net
	 */
	@Override
	public Collection<Place> getPlaces() {
		refresh(); 
		return super.getPlaces();
	}
	/**
	 * An outbound arc of a transition is any arc that starts at the transition
	 * and connects elsewhere
	 *
	 * @param transition to find outbound arcs for
	 * @return arcs that are outbound from transition
	 */
	@Override
	public Collection<OutboundArc> outboundArcs(Transition transition) {
		refresh(); 
		return super.outboundArcs(transition);
	}
	/**
	 * @return all transitions in the Petri net
	 */
	@Override
	public Collection<Transition> getTransitions() {
		refresh(); 
		return super.getTransitions();
	}
	/**
	 * @return Petri net's collection of arcs
	 */
	@Override
	public Collection<Arc<? extends Connectable, ? extends Connectable>> getArcs() {
		refresh(); 
		return super.getArcs();
	}
	/**
	 *
	 * @return all outbound arcs in the Petri net
	 */
	@Override
	public Collection<OutboundArc> getOutboundArcs() {
		refresh(); 
		return super.getOutboundArcs();
	}
	/**
	 *
	 * @return all inbound arcs in the Petri net
	 */
	@Override
	public Collection<InboundArc> getInboundArcs() {
		refresh(); 
		return super.getInboundArcs();
	}
	/**
	 * @return Petri net's list of tokens
	 */	
	@Override
	public Collection<Token> getTokens() {
		refresh(); 
		return super.getTokens();
	}
	/**
	 * @return annotations stored in the Petri net
	 */
	@Override 
	public Collection<Annotation> getAnnotations() {
		refresh(); 
		return super.getAnnotations();
	}
	/**
	 * @return rate parameters stored in the Petri net
	 */
	public Collection<RateParameter> getRateParameters() {
		refresh(); 
		return super.getRateParameters();
	}

	/**
	 * Fire a specific transition for the given TimedState.
	 */
	public void fireTransition(Transition transition, TimedState timedState) {
		//TODO: Clean up - should the timedState be copied first to the network
		// then the transition fired - and then the timedState set again?
		transition.fire(); 
		//TODO: shouldn't this go into fire?
		consumeInboundTokens(transition, timedState);
		produceOutboundTokens(transition, timedState);
		timedState.setState( this.getState() );
		if (transition.isTimed()) {
			timedState.unregisterTimedTransition(transition, timedState.getCurrentTime() );
    	}
    	timedState.registerEnabledTimedTransitions( getEnabledTimedTransitions() );
//    	timedState.registerEnabledTimedTransitions( timedState.getEnabledTimedTransitions() );
	}

	protected void consumeInboundTokens(Transition transition, TimedState timedState) {
		/*for (Arc<Place, Transition> arc : this.inboundArcs(transition)) {
		    String placeId = arc.getSource().getId();
		    Map<String, String> arcWeights = arc.getTokenWeights();
		    Map<String, Integer> tokens = timedState.getState().getTokens(placeId);
		    for (Map.Entry<String, Integer> entry : tokens.entrySet()) {
		        String tokenId = entry.getKey();
		        if (arcWeights.containsKey(tokenId)) {
		            int currentCount = entry.getValue();
		            int arcWeight = (int) getArcWeight(arcWeights.get(tokenId), timedState);
		            // Write to current state map
		            // TODO: right now it is handled through the 
		            //tokens.put(tokenId, subtractWeight(currentCount, arcWeight));
		            // TODO: This is still strange as a place has also always a marking associated.
		            //arc.getSource().setTokenCount(tokenId, subtractWeight(currentCount, arcWeight));
		            //builder.placeWithToken(placeId, tokenId, subtractWeight(currentCount, arcWeight));
		        }
		    }
		}*/
		for (Arc<Place, Transition> arc : this.inboundArcs(transition)) {
	        Place place = arc.getSource();
	        for (Map.Entry<String, String> entry : arc.getTokenWeights().entrySet()) {
	        	if (arc.getType() == ArcType.NORMAL) {
	        		String tokenId = entry.getKey();
	        		String functionalWeight = entry.getValue();
	        		double weight = getArcWeight(functionalWeight, timedState);
	        		int currentCount = place.getTokenCount(tokenId);
	        		//int newCount = currentCount + (int) weight;
	        		// TODO: This is still strange as a place has also always a marking associated.
	        		place.setTokenCount(tokenId, subtractWeight(currentCount, (int) weight));
	        		//timedState.setState( this.getState() );
	        	}
	        }
	    }
	}
	
	protected void produceOutboundTokens(Transition transition, TimedState timedState) {
		/*for (Arc<Transition, Place> arc : this.outboundArcs(transition)) {
		    String placeId = arc.getTarget().getId();
		    Map<String, String> arcWeights = arc.getTokenWeights();
		    Map<String, Integer> tokens = timedState.getState().getTokens(placeId);
		    for (Map.Entry<String, String> entry : arcWeights.entrySet()) {
		        String tokenId = entry.getKey();
		        int currentCount = timedState.getState().getTokens(placeId).get(tokenId);
		        int arcWeight = (int) getArcWeight(entry.getValue(), timedState);
		        //tokens.put(tokenId, addWeight(currentCount, arcWeight) );
		        //builder.placeWithToken(placeId, tokenId, addWeight(currentCount, arcWeight));
		        ((Place) arc.getTarget()).setTokenCount(tokenId, addWeight(currentCount, arcWeight ));
		    }
		}*/
	    //Increment new places
	    for (Arc<Transition, Place> arc : this.outboundArcs(transition)) {
	        Place place = arc.getTarget(); 
	        for (Map.Entry<String, String> entry : arc.getTokenWeights().entrySet()) {
	            String tokenId = entry.getKey();
	            String functionalWeight = entry.getValue();
	            double weight = getArcWeight(functionalWeight, timedState);
	            int currentCount = place.getTokenCount(tokenId);
	            //int newCount = oldCount - (int) weight;
	            place.setTokenCount(tokenId, addWeight(currentCount, (int) weight ));
	        }
	    }
	}
	
	/** MOVED FROM ABSTRACTTRANSITION
     * Treats Integer.MAX_VALUE as infinity and so will not subtract the weight
     * from it if this is the case
     *
     * @param currentWeight
     * @param arcWeight
     * @return subtracted weight
     */
    protected int subtractWeight(int currentWeight, int arcWeight) {
        if (currentWeight == Integer.MAX_VALUE) {
            return currentWeight;
        }
        return currentWeight - arcWeight;
    }

    /** MOVED FROM ABSTRACT TRANSITION
     * Treats Integer.MAX_VALUE as infinity and so will not add the weight
     * to it if this is the case
     *
     * @param currentWeight
     * @param arcWeight
     * @return added weight
     */
    protected int addWeight(int currentWeight, int arcWeight) {
        if (currentWeight == Integer.MAX_VALUE) {
            return currentWeight;
        }
        return currentWeight + arcWeight;
    }
    //MOVED FROM ABSTRACT TRANSITION
    /** 
     * @param state  petri net state to evaluate weight against
     * @param weight a functional weight
     * @return the evaluated weight for the given state
     */
    public double getArcWeight(String weight, TimedState timedState) {
    	double result =  this.evaluateExpression(timedState.getState(), weight); 
        if (result == -1.0) {
            //TODO:
            throw new RuntimeException("Could not parse arc weight");
        }
        return result; 
    }
	
	/**
	 * @param transition to calculate inbound arc for
	 * @return arcs that are inbound to transition, that is arcs that come into the transition
	 */
	@Override
	public Collection<InboundArc> inboundArcs(Transition transition) {
		refresh(); 
		return super.inboundArcs(transition); 
	}

	/**
	 * @return petriNet from which this executable petri net was built. 
	 */
	public PetriNet getPetriNet() {
		return petriNet;
	}

	@Override
	public void addAnnotation(Annotation annotation) {
		addComponentToMap(annotation, annotations);
	}

	@Override
	public void addPlace(Place place) {
		addComponentToMap(place, places);
	}

	@Override
	public void addTransition(Transition transition) {
		addComponentToMap(transition, transitions);
	}

	@Override
	public void addToken(Token token) {
		addComponentToMap(token, tokens);
	}

	@Override
	public void addRateParameter(RateParameter rateParameter)
			throws InvalidRateException {
		addComponentToMap(rateParameter, rateParameters);
	}

	public boolean isRefreshRequired() {
		return refreshRequired;
	}

}