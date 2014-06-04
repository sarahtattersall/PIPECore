package uk.ac.imperial.pipe.naming;

/**
 * Deals with the naming of multiple objects
 */
public interface MultipleNamer {
    /**
     *
     * @return a unique place name
     */
    String getPlaceName();

    /**
     *
     * @return a unique transition name
     */
    String getTransitionName();

    /**
     *
     * @return a unique arc name
     */
    String getArcName();
}
