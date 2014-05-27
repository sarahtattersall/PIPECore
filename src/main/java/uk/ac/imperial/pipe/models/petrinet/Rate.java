package uk.ac.imperial.pipe.models.petrinet;

/**
 * Wraps RateParameters and NormalRates
 */
public interface Rate {
    String getExpression();
    RateType getRateType();
}
