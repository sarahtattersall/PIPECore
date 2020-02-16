package uk.ac.imperial.pipe.dsl;

import uk.ac.imperial.pipe.models.petrinet.DiscreteExternalTransition;
import uk.ac.imperial.pipe.models.petrinet.Transition;

/**
 * Transition DSL to be used in conjunction with {@link uk.ac.imperial.pipe.dsl.APetriNet}
 *
 * Creates an external transition
 *
 * Example usage:
 * AnExternalTransition.withID("T0').andExternalClass("org.some.SomeExternalTransition").andASingle().server().andProbability("#(P0, Red)")
 *
 */
public final class AnExternalTransition extends AnAbstractTransition<AnExternalTransition> {

    private String className;

    /**
     * Private constructor
     * @param id of the transition
     */
    private AnExternalTransition(String id) {
        super(id, false);
    }

    /**
     * Factory method for setting the transition id
     * @param id of the transition
     * @return builder for chaining
     */
    public static AnExternalTransition withId(String id) {
        return new AnExternalTransition(id);
    }

    /**
     * Factory method for setting the external class this transition's fire method will invoke
     * @param className of the external class to be invoked
     * @return builder for chaining
     */
    public AnExternalTransition andExternalClass(String className) {
        this.className = className;
        return this;
    }

    /**
     *
     * @param probability functional expression for the transition probability
     * @return builder for chaining
     */
    public AnExternalTransition andProbability(String probability) {
        rate = probability;
        return this;
    }

    /**
     *
     * @return class instance for chaining calls
     */
    @Override
    protected AnExternalTransition getInstance() {
        return this;
    }

    @Override
    protected Transition buildTransition() {
        return new DiscreteExternalTransition(id, id, className);
    }
}
