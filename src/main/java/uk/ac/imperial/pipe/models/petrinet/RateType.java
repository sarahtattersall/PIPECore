package uk.ac.imperial.pipe.models.petrinet;

/**
 * The type of rate that a transition can reference
 *
 * Normal rates are just a single value that is stored in the transition
 * Rate parameters can be referenced by multiple transitions
 */
public enum RateType {
    NORMAL_RATE, RATE_PARAMETER
}
