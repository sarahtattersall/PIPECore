package uk.ac.imperial.pipe.models.petrinet;

/**
 * Rate interface for normal rates and rate parameters
 */
public interface Rate {
    /**
     *
     * @return string representation of the rate
     */
    String getExpression();

    /**
     *
     * @return type of rate e.g. rate paramter or normal rate
     */
    RateType getRateType();
}
