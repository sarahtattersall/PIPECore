package uk.ac.imperial.pipe.dsl;

import uk.ac.imperial.pipe.models.petrinet.*;

import java.util.Map;

/**
 * Abstract class for creating timed and immediate transitions
 * subclasses will determine if the transition is immediate or timed.
 */
public abstract class AnAbstractTransition<T extends AnAbstractTransition> implements DSLCreator<Transition> {
    /**
     * Transition id
     */
    protected String id;

    /**
     * Transition priority
     */
    private int priority = 1;

    /**
     * True = timed transition
     */
    private final boolean timed;

    /**
     * True = infinite server
     */
    private boolean infinite = false;

    /**
     * Functional rate value
     */
    protected String rate = "";

    /**
     * Rate parameter for transition
     */
    protected String rateParameter = "";

    /**
     * Transition x location
     */
    private int x = 0;

    /**
     * Transition y location
     */
    private int y = 0;
    /**
     * Delay in milliseconds
     */
    protected int delay;

    public AnAbstractTransition(String id, boolean timed) {
        this.id = id;
        this.timed = timed;
    }

    /**
     *
     * @param tokens map of created tokens with id of Token
     * @param places map of created places with id of Connectable
     * @param transitions map of created transitions with id of Transition
     * @param rateParameters map of created rate parameters with id of rateParameter
     * @return discrete transition
     */
    @Override
    public Transition create(Map<String, Token> tokens, Map<String, Place> places, Map<String, Transition> transitions,
            Map<String, FunctionalRateParameter> rateParameters) {
        Transition transition = buildTransition();
        transition.setPriority(priority);
        transition.setTimed(timed);
        transition.setInfiniteServer(infinite);
        transition.setX(x);
        transition.setY(y);

        if (!rate.isEmpty()) {
            transition.setRate(new NormalRate(rate));
        } else if (!rateParameter.isEmpty()) {
            transition.setRate(rateParameters.get(rateParameter));
        }
        if (timed) {
            transition.setDelay(delay);
        }

        transitions.put(id, transition);
        return transition;
    }

    protected Transition buildTransition() {
        Transition transition = new DiscreteTransition(id, id);
        return transition;
    }

    /**
     * Set the transition priority
     * @param priority of the transition
     * @return builder for chaining
     */
    public T andPriority(int priority) {
        this.priority = priority;
        return getInstance();
    }

    /**
     * Sets the transition to be infinite
     * @return builder for chaining
     */
    public T andIsAnInfinite() {
        infinite = true;
        return getInstance();
    }

    /**
     * Sets the transition to be a single server
     * @return builder for chaining
     */
    public T andIsASingle() {
        infinite = false;
        return getInstance();
    }

    /**
     * Added for readability e.g.
     * andASingle().server()
     * @return builder for chaining
     */
    public T server() {
        return getInstance();
    }

    /**
     * Sets the location of the x, y locations
     * @param x coordinate
     * @param y coordinate
     * @return builder for chaining
     */
    public T locatedAt(int x, int y) {
        this.x = x;
        this.y = y;
        return getInstance();
    }

    protected abstract T getInstance();
}
