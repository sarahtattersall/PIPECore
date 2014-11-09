package uk.ac.imperial.pipe.models.petrinet;



import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashMap;

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
 * <p>
 * If this executable Petri net is animated, the markings that result from firing 
 * enabled transitions will be populated in the affected places.  
 * If the affected places are components in an imported Petri net, the markings in the updated places in the 
 * executable Petri net are mirrored to the corresponding imported Petri net.
 */
// * In the PIPE 5.0 gui, each imported Petri net is displayed in its own tab, and may be edited and persisted separately.  
// * Expanded Petri nets are not visible in the gui; their updated markings are visible in the tabs of the corresponding imported Petri net. 
public class ExecutablePetriNet extends AbstractPetriNet implements PropertyChangeListener {

	private PetriNet petriNet;
	private boolean refreshRequired;
	private State state;
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

	public ExecutablePetriNet(PetriNet petriNet) {
		this.petriNet = petriNet;
		includeHierarchy = petriNet.getIncludeHierarchy(); 
		refreshRequired = true; 
		refresh(); 
	}

	/**
	 * This will cause the executable Petri net to be immediately re-built from the underlying 
	 * source Petri net, using {@link uk.ac.imperial.pipe.visitor.ClonePetriNet.clone(PetriNet)} 
	 * Assumes that {@link #refreshRequired() refreshRequired} has been called since the last refresh.  
	 * <p>
	 * In addition to cloning the source Petri net, a listener is added for each place in the 
	 * source Petri net to update its token counts whenever they 
	 * change in the executable Petri net.
	 * <p>
	 * Finally, a representation of the marking of this executable Petri net is saved 
	 * as a {@link uk.ac.imperial.state.State}.  This can be retrieved with {@link getState()}
	 */
	public void refresh() {
		if (isRefreshRequired()) {
			initializeMaps(); 
			refreshIncludeHierarchyComponents(); 
			addSelfAsListenerForPlaceTokenCountChanges(); 
			buildState(); 
			refreshRequired = false; 
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
	 * The refresh is done lazily, when the next "get" request is received. 
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
    * and then applying an updated State later {@see setState(State state)}
    *
    * @return the State of the executable Petri net.
    */

	public State getState() {
		refresh(); 
		return state;
	}
	/**
	 * Updates the State of the executable Petri net.  All places will be updated with 
	 * corresponding token counts, both in the 
	 * executable Petri net and the underlying source Petri net.
	 * <p>
	 * Note that if the structure of the underlying source Petri net has changed since 
	 * this state was originally saved, the results are undefined. 
	 * <p>
	 * @param state 
	 */
	public void setState(State state) {
		refreshRequired(); 
        for (Place place : places.values()) {
        	place.setTokenCounts(state.getTokens(place.getId()));
        }
	}
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		refreshRequired = true; 
	}
	/**
	 * @param String functional expression
	 * @return double result of the evaluation of the expression against the current state of 
	 * this executable petri net, or -1.0 if the expression is not valid. 
	 */
	public Double evaluateExpressionAgainstCurrentState(String expression) {
		return evaluateExpression(getState(), expression);
	}
	/**
	 * @param State representing a possible marking of the places in this executable Petri net.  
	 * <i>Note that the expression is evaluated against the given state, 
	 * not the current state.  If evaluation against the current state is needed, 
	 * invoke {@link #evaluateExpressionAgainstCurrentState(String)}</i>.  
	 * @param String functional expression
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