package uk.ac.imperial.pipe.models.petrinet;

/**
 * Represents a normal rate value for a single transition to refer to
 */
public final class NormalRate implements Rate {
    /**
     * Functional expression matching the rate grammar
     */
    public final String rate;

    /**
     * Constructor
     * @param rate string to be converted to NormalRate
     */
    public NormalRate(String rate) {
        this.rate = rate;
    }

    /**
     *
     * @return functional expression matching the rate grammar
     */
    @Override
    public String getExpression() {
        return rate;
    }

    /**
     *
     * @return normal rate
     */
    @Override
    public RateType getRateType() {
        return RateType.NORMAL_RATE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NormalRate that = (NormalRate) o;

        if (!rate.equals(that.rate)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return rate.hashCode();
    }
}
