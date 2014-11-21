package uk.ac.imperial.pipe.models.petrinet;

import static java.lang.Math.floor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.pipe.parsers.FunctionalResults;
import uk.ac.imperial.pipe.parsers.FunctionalWeightParser;
import uk.ac.imperial.state.HashedStateBuilder;
import uk.ac.imperial.state.State;

public abstract class AbstractTransition extends AbstractConnectable implements Transition {

	protected ExecutablePetriNet executablePetriNet;
	private State state;
	private HashedStateBuilder builder;
	/**
	 * The priority of this transition, the transition(s) with the highest priority will be enabled
	 * when multiple transitions have the possiblity of being enabled
	 */
	protected int priority = 1;
	/**
	 * The rate/weight of the transition. It is considered to be the rate if the transition
	 * is timed and the weight otherwise
	 */
	protected Rate rate = new NormalRate("1");
	/**
	 * Defaults to an immediate transition
	 */
	protected boolean timed = false;
	/**
	 * Defaults to single server semantics
	 */
	protected boolean infiniteServer = false;
	/**
	 * Enabled
	 */
	boolean enabled = false;
	public AbstractTransition(String id, String name) {
		super(id, name);
	}

	public AbstractTransition(AbstractConnectable connectable) {
		super(connectable);
	}
	public HashedStateBuilder fire(ExecutablePetriNet executablePetriNet, State state,
			HashedStateBuilder builder) {
		//TODO instance of EPN should not be initialized here; move to setExecut.... or constructor
		this.executablePetriNet = executablePetriNet; 
		this.state = state; 
		this.builder = builder; 
		fire(); 
		consumeInboundTokens();

		State temporaryState = builder.build();

		produceOutboundTokens(temporaryState);
		return builder; 
	}


	protected void produceOutboundTokens(State temporaryState) {
		for (Arc<Transition, Place> arc : executablePetriNet.outboundArcs(this)) {
		    String placeId = arc.getTarget().getId();
		    Map<String, String> arcWeights = arc.getTokenWeights();
		    for (Map.Entry<String, String> entry : arcWeights.entrySet()) {
		        String tokenId = entry.getKey();
		        int currentCount = temporaryState.getTokens(placeId).get(tokenId);
		        int arcWeight = (int) getArcWeight(executablePetriNet, state, entry.getValue());
		        builder.placeWithToken(placeId, tokenId, addWeight(currentCount, arcWeight));
		    }
		}
	}

	protected void consumeInboundTokens() {
		for (Arc<Place, Transition> arc : executablePetriNet.inboundArcs(this)) {
		    String placeId = arc.getSource().getId();
		    Map<String, String> arcWeights = arc.getTokenWeights();
		    for (Map.Entry<String, Integer> entry : state.getTokens(placeId).entrySet()) {
		        String tokenId = entry.getKey();
		        if (arcWeights.containsKey(tokenId)) {
		            int currentCount = entry.getValue();
		            int arcWeight = (int) getArcWeight(executablePetriNet, state, arcWeights.get(tokenId));
		            builder.placeWithToken(placeId, tokenId, subtractWeight(currentCount, arcWeight));
		        }
		    }
		}
	}
    /**
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

    /**
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
    /**
     * @param state  petri net state to evaluate weight against
     * @param weight a functional weight
     * @return the evaluated weight for the given state
     */
    protected double getArcWeight(ExecutablePetriNet executablePetriNet, State state, String weight) {
    	double result =  executablePetriNet.evaluateExpression(state, weight); 
        if (result == -1.0) {
            //TODO:
            throw new RuntimeException("Could not parse arc weight");
        }
        return result; 
    }

	/**
	 *
	 * @return true
	 */
	@Override
	public boolean isEndPoint() {
	    return true;
	}

	/**
	 *
	 * Returns the priority of the transition, priorities are used in animation
	 * of a Petri net where the highest priority transitions are enabled
	 *
	 * @return the priority of the transition
	 */
	@Override
	public int getPriority() {
	    return priority;
	}

	/**
	 *
	 * @param priority the priority of this transition. Must be > 0.
	 */
	@Override
	public void setPriority(int priority) {
	    int old = this.priority;
	    this.priority = priority;
	    changeSupport.firePropertyChange(PRIORITY_CHANGE_MESSAGE, old, priority);
	}

	/**
	 *
	 * @return the rate at which the transition fires
	 */
	@Override
	public Rate getRate() {
	    return rate;
	}

	/**
	 *
	 * @param rate the new rate for the transitions firing rate
	 */
	@Override
	public void setRate(Rate rate) {
	    this.rate = rate;
	}

	/**
	 * Evaluate the transitions rate against the given state
	 * <p/>
	 * If an infinite server the transition will return its rate * enabling degree
	 *
	 * @return actual evaluated rate of the Petri net
	 */
	@Override
	public Double getActualRate(ExecutablePetriNet executablePetriNet) {
		Double rate = getRateGivenCurrentState(executablePetriNet);
		if (rate == -1) {
			//TODO:
			return rate;
		}
		
		if (!isInfiniteServer()) {
			return rate;
		}
		Map<String, Map<String, Double>> arcWeights = evaluateInboundArcWeights(executablePetriNet.getFunctionalWeightParserForCurrentState(), executablePetriNet.inboundArcs(this));
		int enablingDegree = getEnablingDegree(executablePetriNet.getState(), arcWeights);
		return rate * enablingDegree;
	}

	private Double getRateGivenCurrentState(ExecutablePetriNet executablePetriNet) {
		return executablePetriNet.evaluateExpressionAgainstCurrentState(getRateExpr());
	}

	/**
	 *
	 * @return the unevaluated text representation of a transition weight
	 */
	@Override
	public String getRateExpr() {
	    return rate.getExpression();
	}

	/**
	 *
	 * @return true if the transition is an infinite sever, false if it is a single server
	 */
	@Override
	public boolean isInfiniteServer() {
	    return infiniteServer;
	}

	/**
	 * @param parser parser for a given state of Petri net
	 * @param arcs   set of inbound arcs to evaluate weight against the current state
	 * @return map of arc place id -> arc weights associated with it
	 */
	private Map<String, Map<String, Double>> evaluateInboundArcWeights(FunctionalWeightParser<Double> parser, Collection<InboundArc> arcs) {
	    Map<String, Map<String, Double>> result = new HashMap<>();
	    for (InboundArc arc : arcs) {
	        String placeId = arc.getSource().getId();
	        Map<String, String> arcWeights = arc.getTokenWeights();
	        Map<String, Double> weights = evaluateArcWeight(parser, arcWeights);
	        result.put(placeId, weights);
	    }
	
	    return result;
	}

	/**
	 * A Transition is enabled if all its input places are marked with at least one token
	 * This method calculates the minimum number of tokens needed in order for a transition to be enabled
	 * <p/>
	 * The enabling degree is the number of times that a transition is enabled
	 *
	 * @param state state of the petri net
	 * @param arcWeights evaluated arc weights for the given state
	 * @return number of times this transition is enabled for the given state
	 */
	protected int getEnablingDegree(State state, Map<String, Map<String, Double>> arcWeights) {
	    int enablingDegree = Integer.MAX_VALUE;
	
	    for (Map.Entry<String, Map<String, Double>> entry : arcWeights.entrySet()) {
	        String placeId = entry.getKey();
	        Map<String, Double> weights = entry.getValue();
	        for (Map.Entry<String, Double> weightEntry : weights.entrySet()) {
	            String tokenId = weightEntry.getKey();
	            Double weight = weightEntry.getValue();
	
	            int requiredTokenCount = (int) floor(weight);
	            if (requiredTokenCount == 0) {
	                enablingDegree = 0;
	            } else {
	                Map<String, Integer> tokenCount = state.getTokens(placeId);
	                int placeTokenCount = tokenCount.get(tokenId);
	                int currentDegree = placeTokenCount / requiredTokenCount;
	                if (currentDegree < enablingDegree) {
	                    enablingDegree = currentDegree;
	                }
	            }
	        }
	
	    }
	    return enablingDegree;
	}

	/**
	 * Parses a string representation of a weight with respect to the Petri net
	 *
	 * @param parser     parser for a given state of the Petri net
	 * @param arcWeights arc weights
	 * @return arc weights evaluated to the current state
	 */
	protected Map<String, Double> evaluateArcWeight(FunctionalWeightParser<Double> parser, Map<String, String> arcWeights) {
	    Map<String, Double> result = new HashMap<>();
	    for (Map.Entry<String, String> entry : arcWeights.entrySet()) {
	        String tokenId = entry.getKey();
	        double arcWeight = getArcWeight(parser, arcWeights.get(tokenId));
	        result.put(tokenId, arcWeight);
	    }
	    return result;
	}

	/**
	 * @param parser parser for a given state of the Petri net
	 * @param weight arc functional rate
	 * @return arc weight for a given state
	 */
	protected double getArcWeight(FunctionalWeightParser<Double> parser, String weight) {
	    FunctionalResults<Double> result = parser.evaluateExpression(weight);
	    if (result.hasErrors()) {
	        //TODO:
	        throw new RuntimeException("Could not parse arc weight");
	    }
	
	    return result.getResult();
	}

	/**
	 *
	 * @param infiniteServer true => infite server, false => single server
	 */
	@Override
	public void setInfiniteServer(boolean infiniteServer) {
	    boolean old = this.infiniteServer;
	    this.infiniteServer = infiniteServer;
	    changeSupport.firePropertyChange(INFINITE_SEVER_CHANGE_MESSAGE, old, infiniteServer);
	}

	/**
	 *
	 * @return true if the transition is timed, false for immediate
	 */
	@Override
	public boolean isTimed() {
	    return timed;
	}

	/**
	 *
	 * @param timed true => timed, false => immediate
	 */
	@Override
	public void setTimed(boolean timed) {
	    boolean old = this.timed;
	    this.timed = timed;
	    changeSupport.firePropertyChange(TIMED_CHANGE_MESSAGE, old, timed);
	}

	/**
	 * Enable the transition
	 */
	@Override
	public void enable() {
	    enabled = true;
	    changeSupport.firePropertyChange(ENABLED_CHANGE_MESSAGE, false, true);
	}

	/**
	 * Disable the transition
	 */
	@Override
	public void disable() {
	    enabled = false;
	    changeSupport.firePropertyChange(DISABLED_CHANGE_MESSAGE, true, false);
	}

	/**
	 *
	 * @return true if the transition has been enabled
	 */
	@Override
	public boolean isEnabled() {
	    return enabled;
	}

}
