package uk.ac.imperial.pipe.dsl;

import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.petrinet.NormalRate;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.DiscreteTransition;
import uk.ac.imperial.pipe.models.petrinet.Transition;

import java.util.Map;

/**
 * Transition DSL to be used in conjunction with {@link uk.ac.imperial.pipe.dsl.APetriNet}
 *
 * Example usage:
 * ATransition.withID("T0').whichIsTimed().andASingle().server().andRate("#(P0, Red)")
 *
 */
public final class ATransition implements DSLCreator<Transition> {
    /**
     * Transition id
     */
    private String id;

    /**
     * Transition priority
     */
    private int priority = 1;

    /**
     * True = timed transition
     */
    private boolean timed = false;

    /**
     * True = infinite server
     */
    private boolean infinite = false;

    /**
     * Functional rate value
     */
    private String rate = "";

    /**
     * Rate parameter for transition
     */
    private String rateParameter = "";

    /**
     * Transition x location
     */
    private int x = 0;

    /**
     * Transition y location
     */
    private int y = 0;

    /**
     * Private constructor
     * @param id
     */
    private ATransition(String id) {this.id = id;}

    /**
     * Factory method for setting the transition id
     * @param id
     * @return builder for chaining
     */
    public static ATransition withId(String id) {
        return new ATransition(id);
    }

    /**
     *
     * @param tokens map of created tokens with id -> Token
     * @param places map of created places with id -> Connectable
     * @param transitions
     * @param rateParameters
     * @return discrete transition
     */
    @Override
    public Transition create(Map<String, Token> tokens, Map<String, Place> places, Map<String, Transition> transitions,
                             Map<String, FunctionalRateParameter> rateParameters) {
        Transition transition = new DiscreteTransition(id, id);
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

        transitions.put(id, transition);
        return transition;
    }

    /**
     * Set the transition priority
     * @param priority
     * @return builder for chaining
     */
    public ATransition andPriority(int priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Sets the transition to be immediate
     * @return builder for chainign
     */
    public ATransition whichIsImmediate() {
        timed = false;
        return this;
    }

    /**
     *
     * Sets the transition to timed logic
     *
     * @return builder for chaining
     */
    public ATransition whichIsTimed() {
        timed = true;
        return this;
    }

    /**
     * Sets the transition to be infinite
     * @return builder for chaining
     */
    public ATransition andIsAnInfinite() {
        infinite = true;
        return this;
    }

    /**
     * Sets the transition to be a single server
     * @return builder for chaining
     */
    public ATransition andIsASingle() {
       infinite = false;
        return this;
    }

    /**
     * Added for readability e.g.
     * andASingle().server()
     */
    public ATransition server() {
        return this;
    }

    /**
     * Sets the transition rate
     * @param rate functional rate
     * @return builder for chaining
     */
    public ATransition andRate(String rate) {
        this.rate = rate;
        return this;
    }

    /**
     *
     * Factory constructor
     *
     * @param rateParameterName name for the rate parameter
     * @return builder for chaining
     */
    public ATransition withRateParameter(String rateParameterName) {
        this.rateParameter = rateParameterName;
        return this;
    }


    /**
     * Sets the location of the x, y locations
     * @param x
     * @param y
     * @return builder for chaining
     */
    public ATransition locatedAt(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }
}
