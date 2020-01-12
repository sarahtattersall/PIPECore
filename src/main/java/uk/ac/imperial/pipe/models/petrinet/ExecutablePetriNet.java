package uk.ac.imperial.pipe.models.petrinet;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;

import uk.ac.imperial.pipe.exceptions.IncludeException;
import uk.ac.imperial.pipe.exceptions.InvalidRateException;
import uk.ac.imperial.pipe.parsers.FunctionalWeightParser;
import uk.ac.imperial.pipe.parsers.PetriNetWeightParser;
import uk.ac.imperial.pipe.parsers.StateEvalVisitor;
import uk.ac.imperial.pipe.runner.Runner;
import uk.ac.imperial.pipe.tuple.Tuple;
import uk.ac.imperial.pipe.visitor.ExecutablePetriNetCloner;
import uk.ac.imperial.state.HashedStateBuilder;
import uk.ac.imperial.state.State;

/**
 * Makes a {@link PetriNet} available for execution, i.e., animation or analysis.
 * The complete state of the executable Petri net is a set of collections of its constituent components.
 * For efficiency of processing the marking of the executable Petri net is saved as its {@link State}
 * <p>
 * Broadly, an executable Petri net is defined by its structure (places, transitions, arcs) and its state (marking),
 * and possibly, its timing. The structure of an executable Petri net is built from an {@link IncludeHierarchy}
 * of one or more {@link PetriNet}(s).  The {@link IncludeHierarchy} of this Petri net is expanded to create a single
 * Petri net consisting of all the included Petri nets connected by the arcs defined by their {@link MergeInterfaceStatus}
 * The result is a single Petri net, with corresponding collections of all the constituent components.
 * </p><p>
 * Any change to the include hierarchy or Petri nets will result in refreshing ({@link #refresh()}) the executable Petri net.
 * During execution, the structure of the executable Petri net is fixed; what changes is its marking.
 * The current state (marking) of the executable Petri net is defined by its {@link State} ({@link #getState()}).
 * For Petri nets with timed transitions, the order of execution of timed transitions is defined by the
 * {@link TimingQueue} ({@link #getTimingQueue()}).  The current state and timing of the Petri net are used
 * by the PIPE GUI, and {@link Runner}.  Analysis of the Petri net, however, may require processing multiple
 * different States, other than the current State.  Therefore, many methods have two flavors:</p>
 * <ul>
 * <li> someProcessing():  performs some processing against the current State.  This may include updating the current State.
 * <li> someProcessing(State state):  performs some processing against the given state.  The current State is neither referenced nor updated.
 * </ul>
 * <p>
 * Processing of the executable Petri net may take different forms, defined as different implementations of {@link AnimationLogic}.
 * Step-by-step execution (firing of individual transitions) is controlled by the {@link Animator}.
 * </p><p>
 * If this executable Petri net is animated ({@link Animator}), the markings that result from firing enabled
 * transitions will be populated in the affected places. If the affected places are components in an included Petri
 * net, the markings in the updated places in the executable Petri net are mirrored to the corresponding
 * included Petri net.
 * </p>
 * @see PetriNet
 * @see AbstractPetriNet
 * @see IncludeHierarchy
 * @see State
 * @see TimingQueue
 * @see Runner
 * @see AnimationLogic
 * @see Animator
 */
public class ExecutablePetriNet extends AbstractPetriNet implements PropertyChangeListener {

    public static final String PETRI_NET_REFRESHED_MESSAGE = "executable Petri net refreshed";
    private PetriNet petriNet;
    private boolean refreshRequired;
    private State state;
    private TimingQueue timingQueue;
    /**
     * List of names of tokens to be used for validation by {@link Runner}
     * built once and only refreshed explicitly, so implies stability of tokens
     * Used during Runner processing; will not be useful during UI processing.
     */
    private List<String> validationTokenNames;
    /**
     * Map of names of places and their external accessibility to be used for validation by {@link Runner}
     * built once and only refreshed explicitly, so implies stability of tokens
     * Used during Runner processing; will not be useful during UI processing.
     */
    private Map<String, Boolean> validationPlaceMap;

    /**
     * Functional weight parser
     */
    private FunctionalWeightParser<Double> functionalWeightParser;

    /**
     * Creates a new executable Petri net based upon a source Petri net.  Performs an immediate
     * {@link #refreshRequired() refreshRequired} and {@link #refresh() refresh} to synchronize
     * the structure of the two Petri nets.
     * @param petriNet -- the source Petri net whose structure this executable Petri net mirrors.
     * @param initTime time in milliseconds to be set as current time.
     */

    public ExecutablePetriNet(PetriNet petriNet, long initTime) {
        this.petriNet = petriNet;
        getIncludeHierarchyFromPetriNet(petriNet);
        refreshRequired = true;
        refresh();
        timingQueue = buildTimingQueue(initTime);
    }

    /**
     * Creates a new executable Petri net based upon a source Petri net.  Performs an immediate
     * {@link #refreshRequired() refreshRequired} and {@link #refresh() refresh} to synchronize
     * the structure of the two Petri nets.  Defaults the initial time of the EPN to 0.
     * @param petriNet -- the source Petri net whose structure this executable Petri net mirrors.
     */
    public ExecutablePetriNet(PetriNet petriNet) {
        this(petriNet, 0);
    }

    protected TimingQueue buildTimingQueue(long initTime) {
        return new TimingQueue(this, initTime);
    }

    /**
     * This will cause the executable Petri net to be immediately re-built from the underlying
     * source Petri net, using {@link uk.ac.imperial.pipe.visitor.PetriNetCloner}
     * Assumes that {@link #refreshRequired() refreshRequired} has been called since the last refresh.
     * <p>
     * In addition to cloning the source Petri net, a listener is added for each place in the
     * source Petri net to update its token counts whenever they
     * change in the executable Petri net.
     * </p><p>
     * Finally, a representation of the marking of this executable Petri net is saved
     * as a {@link uk.ac.imperial.state.State}.  This can be retrieved with {@link #getState()}</p>
     */
    //TODO rework to minimize refreshes; consider calling on any PN structure change
    public synchronized void refresh() {
        if (isRefreshRequired()) {
            notifyListenersToRemovePlaces();
            removeListenersToCurrentComponents();
            initializeMaps();
            refreshIncludeHierarchyComponents();
            addSelfAsListenerForPlaceTokenCountChanges();
            buildState();
            refreshRequired = false;
            changeSupport.firePropertyChange(PETRI_NET_REFRESHED_MESSAGE, null, null);
        }
    }

    private void removeListenersToCurrentComponents() {
        for (Place place : places.values()) {
            ((AbstractPetriNetPubSub) place).removeAllListeners();
        }
        for (Transition transition : transitions.values()) {
            ((AbstractPetriNetPubSub) transition).removeAllListeners();
        }
        for (InboundArc inboundArc : inboundArcs.values()) {
            ((AbstractPetriNetPubSub) inboundArc).removeAllListeners();
        }
        for (OutboundArc outboundArc : outboundArcs.values()) {
            ((AbstractPetriNetPubSub) outboundArc).removeAllListeners();
        }
        for (Token token : tokens.values()) {
            ((AbstractPetriNetPubSub) token).removeAllListeners();
        }
        for (RateParameter rateParameter : rateParameters.values()) {
            ((AbstractPetriNetPubSub) rateParameter).removeAllListeners();
        }
        for (Annotation annotation : annotations.values()) {
            ((AbstractPetriNetPubSub) annotation).removeAllListeners();
        }
    }

    private void notifyListenersToRemovePlaces() {
        for (Place place : places.values()) {
            place.removeSelfFromListeners();
        }
    }

    private void refreshIncludeHierarchyComponents() {
        getIncludeHierarchyFromPetriNet(petriNet);
        try {
            ExecutablePetriNetCloner.refreshFromIncludeHierarchy(this);
        } catch (IncludeException e) {
            throw new RuntimeException("Failure attempting to refresh executable petri net.  Details:\n" +
                    e.getMessage());
        }
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

        placeCloneMap = new HashMap<>();

        componentMaps = new HashMap<>();
        initialiseIdMap();
    }

    private void addSelfAsListenerForPlaceTokenCountChanges() {
        for (Place place : places.values()) {
            place.addPropertyChangeListener(this); // force refresh
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
     * Return the current State of this executable Petri net.
     * Enables analyzing and computing State independently of this executable Petri net,
     * and then applying an updated State later
     * @see #setState
     * @return current State of the executable Petri net.
    */
    public State getState() {
        refresh();
        return state;
    }

    public TimingQueue getTimingQueue() {
        return timingQueue;
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

    /**
     * Returns the enabled immediate Transitions for the current State ({@link #getState()})
     * of this executable Petri net, evaluated against the structure of this executable Petri net.
     * To retrieve the enabled immediate Transitions for a given State,
     * use {@link #getEnabledImmediateTransitions(State)}
     * @see #getEnabledTimedTransitions()
     * @see #getEnabledImmediateAndTimedTransitions()
     * @return all the enabled immediate transitions in the executable petri net for the current state
     */
    public Set<Transition> getEnabledImmediateTransitions() {
        return getEnabledImmediateAndTimedTransitions().tuple1;
    }

    /**
     * Returns the enabled immediate Transitions for the given State evaluated against the structure
     * of this executable Petri net, but ignoring the current state of this executable Petri net.
     * To retrieve the enabled immediate Transitions for the current State, use {@link #getEnabledImmediateTransitions()}.
     * If the current State ({@link #getState()}) of the executable Petri net is passed to this method,
     * it will return results identical to {@link #getEnabledImmediateTransitions()}.
     * @param state to be evaluated
     * @return all the enabled immediate transitions in the executable petri net for the given state
     * @see #getEnabledTimedTransitions(State)
     * @see #getEnabledImmediateAndTimedTransitions(State)
     */
    public Set<Transition> getEnabledImmediateTransitions(State state) {
        return getEnabledImmediateAndTimedTransitions(state).tuple1;
    }

    /**
     * Returns the enabled timed Transitions for the current State ({@link #getState()})
     * of this executable Petri net, evaluated against the structure of this executable Petri net.
     * This method ignores the current time ({@link #getCurrentTime()}); to retrieve only the subset of the enabled
     * timed Transitions that could fire at the current time, use {@link #getCurrentlyEnabledTimedTransitions()}.
     * To retrieve the enabled timed Transitions for a given State,
     * use {@link #getEnabledTimedTransitions(State)}.
     * @see #getCurrentlyEnabledTimedTransitions()
     * @see #getEnabledImmediateTransitions()
     * @see #getEnabledImmediateAndTimedTransitions()
     * @return all the enabled timed transitions in the executable petri net for the current state
     */
    public Set<Transition> getEnabledTimedTransitions() {
        return getEnabledImmediateAndTimedTransitions().tuple2;
    }

    /**
     * Returns the enabled timed Transitions for the given State evaluated against the structure
     * of this executable Petri net, but ignoring the current state of this executable Petri net.
     * To retrieve the enabled timed Transitions for the current State, use {@link #getEnabledTimedTransitions()}.
     * If the current State ({@link #getState()}) of the executable Petri net is passed to this method,
     * it will return results identical to {@link #getEnabledTimedTransitions()}.
     * This method ignores the current time ({@link #getCurrentTime()}); to retrieve only the subset of the enabled
     * timed Transitions that could fire at the current time and with the current state,
     * use {@link #getCurrentlyEnabledTimedTransitions()}.
     * @param state to be evaluated
     * @return all the enabled timed transitions in the executable petri net for the given state
     * @see #getCurrentlyEnabledTimedTransitions()
     * @see #getEnabledImmediateTransitions(State)
     * @see #getEnabledImmediateAndTimedTransitions(State)
     */
    public Set<Transition> getEnabledTimedTransitions(State state) {
        return getEnabledImmediateAndTimedTransitions(state).tuple2;
    }

    /**
     * Returns both the enabled immediate and timed Transitions for the current State
     * ({@link #getState()}) of this executable Petri net, evaluated against the structure of this executable Petri net.
     * The two sets are returned as a {@link Tuple}.
     * To retrieve the enabled immediate and timed Transitions for a given State,
     * use {@link #getEnabledImmediateAndTimedTransitions(State)}
     * @see #getEnabledImmediateTransitions()
     * @see #getEnabledTimedTransitions()
     * @return tuple: immediate enabled transitions, timed enabled transitions
     */
    public Tuple<Set<Transition>, Set<Transition>> getEnabledImmediateAndTimedTransitions() {
        return getEnabledImmediateAndTimedTransitions(this.state);
    }

    /**
     * Returns both the enabled immediate and timed Transitions for a given State
     * evaluated against the structure of this executable Petri net, but ignoring the current state
     * of this executable Petri net.  The two sets are returned as a {@link Tuple}.
     * To retrieve the enabled immediate and timed Transitions for the current State ({@link #getState()}),
     * use {@link #getEnabledImmediateAndTimedTransitions()}.  If the current State of the executable Petri net
     * is passed to this method, it will return results identical to {@link #getEnabledImmediateAndTimedTransitions()}.
     * @param state to be evaluated
     * @return a Tuple: immediate enabled transitions, timed enabled transitions
     * @see #getEnabledImmediateTransitions()
     * @see #getEnabledTimedTransitions()
     */
    public Tuple<Set<Transition>, Set<Transition>> getEnabledImmediateAndTimedTransitions(State state) {
        Set<Transition> immediateTransitions = new HashSet<>();
        Set<Transition> timedTransitions = new HashSet<>();
        for (Transition transition : getTransitions()) {
            if (isEnabled(transition, state)) {
                if (transition.isTimed()) {
                    timedTransitions.add(transition);
                } else {
                    immediateTransitions.add(transition);
                }
            }
        }
        return new Tuple<>(immediateTransitions, timedTransitions);
    }

    /**
     * Returns the subset of the enabled timed Transitions that could fire at the current time
     * and with the current state.  Normal processing, as defined in {@link AnimationLogic}, would consist of
     * firing all the transitions returned from this method, then advancing the current time of this
     * ExecutablePetriNet to the next available time, and then invoking this method again.
     * @see AnimationLogic
     * @see PetriNetAnimationLogic
     * @return set of transitions that are enabled to fire at the current time
     */
    public Set<Transition> getCurrentlyEnabledTimedTransitions() {
        return timingQueue.getCurrentlyEnabledTimedTransitions();
    }

    public long getCurrentTime() {
        return timingQueue.getCurrentTime();
    }

    public void setCurrentTime(long time) {
        timingQueue.setCurrentTime(time);
    }

    /**
     * determines the maximum priority of a collection of transitions, and only returns the transitions with
     * that maximum priority
     * @param transitions collection to be evaluated
     * @return set of transitions that have the maximum priority of all transitions evaluated
     */
    public Set<Transition> maximumPriorityTransitions(
            Collection<Transition> transitions) {
        int maxPriority = getMaxPriority(transitions);
        Set<Transition> maximumPriorityTransitions = new HashSet<>();
        for (Transition transition : transitions) {
            if (transition.getPriority() == maxPriority) {
                maximumPriorityTransitions.add(transition);
            }
        }
        return maximumPriorityTransitions;
    }

    private int getMaxPriority(Iterable<Transition> transitions) {
        int maxPriority = 0;
        for (Transition transition : transitions) {
            maxPriority = Math.max(maxPriority, transition.getPriority());
        }
        return maxPriority;
    }

    /**
     * Determines if a transition is enabled for the current State ({@link #getState()}.
     * Checks if:
     * a) places connected by an incoming arc to this transition have enough tokens to fire
     * b) places connected by an outgoing arc to this transition have enough space to fit the
     * new tokens (i.e., enough capacity).
     *
     * @param transition to see if it is enabled
     * @return true if transition is enabled
     */
    public boolean isEnabled(Transition transition) {
        return isEnabled(transition, getState());
    }

    /**
     * Determines if a transition is enabled for the given State, ignoring the current State.
     * Checks if:
     * a) places connected by an incoming arc to this transition have enough tokens to fire
     * b) places connected by an outgoing arc to this transition have enough space to fit the
     * new tokens (i.e., enough capacity).
     *
     * @param transition to see if it is enabled
     * @param state against which enabled status is to be determined
     * @return true if transition is enabled
     */
    public boolean isEnabled(Transition transition, State state) {
        Collection<InboundArc> inboundArcs = inboundArcs(transition);
        if (inboundArcs.isEmpty())
            return false;
        for (Arc<Place, Transition> arc : inboundArcs) {
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
    public void propertyChange(PropertyChangeEvent event) {
        //TODO token changes aren't structural, so probably shouldn't force a refresh...
        //        if (!event.getPropertyName().equals(Place.TOKEN_CHANGE_MESSAGE)) {
        refreshRequired = true;
        //        }
    }

    /**
     * Evaluates an expression against the current State ({@link #getState()}.
     * @param expression to evaluate
     * @return double result of the evaluation of the expression against the current state of
     * this executable petri net, or -1.0 if the expression is not valid.
     */
    public Double evaluateExpression(String expression) {
        return evaluateExpression(getState(), expression);
    }

    /**
     * Evaluates an expression against the given State, ignoring the current State.
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
     * Fire a transition, returning the State resulting from consuming tokens from the sources of the
     * inbound arcs and producing tokens on the targets of the outbound arcs.
     * <p>
     * This method updates the state of this executable Petri net.  To fire a transition without changing
     * the state of the executable Petri net, use {@link #fireTransition(Transition, State)}.
     * Both methods will return the same State.  This method will also result in an executable Petri net
     * whose State ({@link #getState()}) is identical to the State returned.
     * </p><p>
     * The caller ({@link Animator} or {@link AnimationLogic}) is responsible for ensuring the transition
     * is in an appropriate state for firing, having selected the transition from the results of
     * {@link #getEnabledImmediateTransitions()}, {@link #getEnabledTimedTransitions()},
     * {@link #getEnabledImmediateAndTimedTransitions()}, or {@link #getCurrentlyEnabledTimedTransitions()}.
     * Otherwise, results are undefined.
     * </p><p>
     * The order of operations is as follows:
     * <ul>
     * <li>inbound tokens are consumed, updating the source places
     * <li>outbound tokens are produced, updating the target places
     * <li>timing queue is updated to reflect any changes to the state of timed transitions
     * <li>the transition is fired
     * </ul>
     * <p>If the transition is an {@link ExternalTransition} it will have access to the executable petri net
     * after the inbound tokens are consumed and the outbound tokens are produced.  This does not match
     * a common sense interpretation of the semantics of "firing" (consume, fire, produce), but has the practical effect
     * of ensuring the executable petri net is in a consistent state when external transition is given
     * access to it. </p>
     * <p>Calculation of token counts during token production depends on both the original state
     * and on the state after tokens are consumed.</p>
     * <ul>
     * <li>Token production depends on the original state when the outbound arc has a functional expression.
     * Suppose we have a net: P0 -&gt; T0 -$gt; P1 and T0 -$gt; P1 has a weight of #(P0).
     * We expect #(P0) to refer to the number of tokens before firing.
     * <li>Token production depends on the state after tokens are consumed for calculation of capacity.
     * Suppose we have a net: P0 -&gt; T0 -$gt; P0 and P0 has capacity 1.
     * The token in P0 must be consumed before the capacity calculation to allow the outbound arc to produce a token.
     * </ul>
     * @param transition to be fired
     * @return state that results from firing the transition
     */
    public State fireTransition(Transition transition) {
        return fireTransition(transition, this.state, true);
    }

    /**
     * Fire a transition, returning the State resulting from consuming tokens from the sources of the
     * inbound arcs and producing tokens on the targets of the outbound arcs.
     * <p>
     * This method does not update the state of this executable Petri net.  Instead, it operates <i>as if</i>
     * the input State was the state of this executable Petri net, and returns the State that would result
     * if that were the case.
     * To fire a transition and change the corresponding state of the executable Petri net,
     * use {@link #fireTransition(Transition)}.  Both methods will return the same State.
     * </p><p>
     * The caller ({@link Animator} or {@link AnimationLogic}) is responsible for ensuring the transition
     * is in an appropriate state for firing, having selected the transition from the results of {@link #getEnabledImmediateTransitions()},
     * {@link #getEnabledTimedTransitions()}, {@link #getEnabledImmediateAndTimedTransitions()}, or {@link #getCurrentlyEnabledTimedTransitions()}.
     * Otherwise, results are undefined.
     * </p><p>
     * The order of operations is as follows:
     * <ul>
     * <li>inbound tokens are consumed, updating the source places
     * <li>outbound tokens are produced, updating the target places
     * <li>timing queue is updated to reflect any changes to the state of timed transitions
     * <li>the transition is fired
     * </ul>
     * <p>If the transition is an {@link ExternalTransition} it will have access to the executable petri net
     * after the inbound tokens are consumed and the outbound tokens are produced.  This does not match
     * a common sense interpretation of the semantics of "firing" (consume, fire, produce), but has the practical effect
     * of ensuring the executable petri net is in a consistent state when external transition is given
     * access to it. </p>
     * <p>Calculation of token counts during token production depends on both the original state
     * and on the state after tokens are consumed.</p>
     * <ul>
     * <li>Token production depends on the original state when the outbound arc has a functional expression.
     * Suppose we have a net: P0 -&gt; T0 -$gt; P1 and T0 -$gt; P1 has a weight of #(P0).
     * We expect #(P0) to refer to the number of tokens before firing.
     * <li>Token production depends on the state after tokens are consumed for calculation of capacity.
     * Suppose we have a net: P0 -&gt; T0 -$gt; P0 and P0 has capacity 1.
     * The token in P0 must be consumed before the capacity calculation to allow the outbound arc to produce a token.
     * </ul>
     * @param transition to be fired
     * @param state prior to the firing of the transition
     * @return state that results from firing the transition
     */
    public State fireTransition(Transition transition, State state) {
        return fireTransition(transition, state, false);
    }

    protected State fireTransition(Transition transition, State state, boolean updateState) {
        HashedStateBuilder builder = new HashedStateBuilder(state);
        consumeInboundTokens(builder, transition, state, updateState);
        produceOutboundTokens(builder, transition, state, updateState);
        State stateProduced = builder.build();
        if (updateState) {
            getTimingQueue().dequeueAndRebuild(transition, stateProduced);
        }
        transition.fire();
        return stateProduced;
    }

    protected State consumeInboundTokens(HashedStateBuilder builder, Transition transition, State state,
            boolean updatePlace) {
        return inboundTokens(builder, transition, state, updatePlace, false);
    }

    protected State produceInboundTokens(HashedStateBuilder builder, Transition transition, State state,
            boolean updatePlace) {
        return inboundTokens(builder, transition, state, updatePlace, true);
    }

    protected State inboundTokens(HashedStateBuilder builder,
            Transition transition, State state, boolean updatePlace, boolean add) {
        for (Arc<Place, Transition> arc : this.inboundArcs(transition)) {
            Place place = arc.getSource();
            if (arc.getType() == ArcType.NORMAL) {
                updateTokensInStateAndPerhapsPlace(arc, state, state, updatePlace, builder, place, add);
            }
        }
        return builder.build();
    }

    protected State produceOutboundTokens(HashedStateBuilder builder, Transition transition, State originalState,
            boolean updatePlace) {
        return outboundTokens(builder, transition, originalState, updatePlace, true);
    }

    protected State consumeOutboundTokens(HashedStateBuilder builder, Transition transition, State originalState,
            boolean updatePlace) {
        return outboundTokens(builder, transition, originalState, updatePlace, false);
    }

    protected State outboundTokens(HashedStateBuilder builder,
            Transition transition, State originalState, boolean updatePlace, boolean add) {
        for (Arc<Transition, Place> arc : this.outboundArcs(transition)) {
            Place place = arc.getTarget();
            updateTokensInStateAndPerhapsPlace(arc, originalState, builder.build(), updatePlace, builder, place, add);
        }
        return builder.build();
    }

    protected void updateTokensInStateAndPerhapsPlace(
            Arc<? extends Connectable, ? extends Connectable> arc,
            State originalState, State consumedState, boolean updatePlace, HashedStateBuilder builder,
            Place place, boolean add) {
        for (Map.Entry<String, String> entry : arc.getTokenWeights().entrySet()) {
            String tokenId = entry.getKey();
            String functionalWeight = entry.getValue();
            double weight = getArcWeight(functionalWeight, originalState);
            int currentCount = consumedState.getTokens(place.getId()).get(tokenId);
            int newCount = adjustWeight(currentCount, (int) weight, add);
            builder.placeWithToken(place.getId(), tokenId, newCount);
            if (updatePlace) {
                place.setTokenCount(tokenId, newCount);
            }
        }
    }

    /**
     * Returns the State of the executable Petri net that generated the current State through the firing of the
     * specified transition.  Note that if the transition is an external transition, it is fired, but no current
     * mechanism exists to alert the external transition that it is being fired "backwards".  Note also that the
     * timing queue is rebuilt with its state as of the state prior to the firing of the transition; behavior
     * if the current time has changed since the transition was originally fired is undefined.
     *
     * @param transition to be "unfired"
     * @return State prior to the firing of the transition
     */
    //TODO:  handle external transition firing backwards
    //TODO:  test impact on timing queue
    public State fireTransitionBackwards(Transition transition) {
        HashedStateBuilder builder = new HashedStateBuilder(state);
        produceInboundTokens(builder, transition, state, true);
        consumeOutboundTokens(builder, transition, state, true);
        State stateProduced = builder.build();
        getTimingQueue().dequeueAndRebuild(transition, stateProduced);
        transition.fire();
        return stateProduced;

    }

    /**
     * Treats Integer.MAX_VALUE as infinity and so will not subtract the weight
     * from it if this is the case
     *
     * @param currentWeight of the arc
     * @param arcWeight to be subtracted
     * @return subtracted weight
     */
    protected int subtractWeight(int currentWeight, int arcWeight) {
        return adjustWeight(currentWeight, arcWeight, false);
    }

    /**
     * Treats Integer.MAX_VALUE as infinity and so will not add the weight
     * to it if this is the case
     *
     * @param currentWeight of the arc
     * @param arcWeight to be added
     * @return added weight
     */
    protected int addWeight(int currentWeight, int arcWeight) {
        return adjustWeight(currentWeight, arcWeight, true);
    }

    private int adjustWeight(int currentWeight, int arcWeight, boolean add) {
        if (currentWeight == Integer.MAX_VALUE) {
            return currentWeight;
        }
        int adjust = (add) ? 1 : -1;
        return currentWeight + (adjust * arcWeight);
    }

    /**
     * @param state  petri net state to evaluate weight against
     * @param weight a functional weight
     * @return the evaluated weight for the given state
     */
    public double getArcWeight(String weight, State state) {
        double result = this.evaluateExpression(state, weight);
        if (result == -1.0) {
            //TODO:
            throw new RuntimeException("Could not parse arc weight: " + weight);
        }
        return result;
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
    @Override
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

    protected void setRefreshNotRequiredForTesting() {
        refreshRequired = false;
    }

    private void getIncludeHierarchyFromPetriNet(PetriNet petriNet) {
        includeHierarchy = petriNet.getIncludeHierarchy();
    }

    public List<String> getTokenNamesForValidation() {
        if (validationTokenNames == null) {
            validationTokenNames = new ArrayList<>();
            for (String tokenName : tokens.keySet()) {
                validationTokenNames.add(tokenName);
            }
        }
        return validationTokenNames;
    }

    public Map<String, Boolean> getPlaceMapForValidation() {
        if (validationPlaceMap == null) {
            validationPlaceMap = new HashMap<>();
            for (Place place : places.values()) {
                validationPlaceMap.put(place.getId(), place.getStatus().isExternal());
            }
        }
        return validationPlaceMap;
    }

}