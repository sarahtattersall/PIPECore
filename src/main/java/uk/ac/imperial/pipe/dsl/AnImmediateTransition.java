package uk.ac.imperial.pipe.dsl;

/**
 * Transition DSL to be used in conjunction with {@link uk.ac.imperial.pipe.dsl.APetriNet}
 *
 * Creates an immediate transition
 *
 * Example usage:
 * AnImmediateTransition.withID("T0').andASingle().server().andProbability("#(P0, Red)")
 *
 */
public final class AnImmediateTransition extends AnAbstractTransition<AnImmediateTransition> {

    /**
     * Private constructor
     * @param id
     */
    private AnImmediateTransition(String id) {
        super(id, false);
    }

    /**
     * Factory method for setting the transition id
     * @param id of the transition
     * @return builder for chaining
     */
    public static AnImmediateTransition withId(String id) {
        return new AnImmediateTransition(id);
    }

    /**
     *
     * @param probability functional expression for the transition probability
     * @return builder for chaining
     */
    public AnImmediateTransition andProbability(String probability) {
        rate = probability;
        return this;
    }

    /**
     *
     * @return class instance for chaining calls
     */
    @Override
    protected AnImmediateTransition getInstance() {
        return this;
    }
}
