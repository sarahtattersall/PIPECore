package uk.ac.imperial.pipe.dsl;

/**
 * Transition DSL to be used in conjunction with {@link uk.ac.imperial.pipe.dsl.APetriNet}
 *
 * Creates a timed transition
 *
 * Example usage:
 * ATimedTransition.withID("T0').andASingle().server().andRate("#(P0, Red)")
 *
 */
public final class ATimedTransition extends AnAbstractTransition<ATimedTransition> {
    /**
     * Private constructor
     * @param id transition id
     */
    private ATimedTransition(String id) {
        super(id, true);
    }

    /**
     * Factory method for setting the transition id
     * @param id of the transition
     * @return builder for chaining
     */
    public static ATimedTransition withId(String id) {
        return new ATimedTransition(id);
    }

    /**
     *
     * @return class instance for chaining
     */
    @Override
    protected ATimedTransition getInstance() {
        return this;
    }

    /**
     *
     * Factory constructor
     *
     * @param rateParameterName name for the rate parameter
     * @return builder for chaining
     */
    public ATimedTransition withRateParameter(String rateParameterName) {
        rateParameter = rateParameterName;
        return getInstance();
    }

    /**
     * Sets the transition rate
     * @param rate functional rate
     * @return builder for chaining
     */
    public ATimedTransition andRate(String rate) {
        this.rate = rate;
        return getInstance();
    }

    /**
     * Sets the delay in milliseconds
     * @param delay to wait before firing
     * @return builder for chaining
     */
    public ATimedTransition andDelay(int delay) {
        this.delay = delay;
        return getInstance();
    }

}
