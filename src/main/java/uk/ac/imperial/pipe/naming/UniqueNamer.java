package uk.ac.imperial.pipe.naming;

/**
 * Namer is used to find unique names for {@link uk.ac.imperial.pipe.models.petrinet.PetriNetComponent}'s within a
 * {@link uk.ac.imperial.pipe.models.petrinet.PetriNet}
 */
public interface UniqueNamer {
    /**
     * @return a unique name for the {@link uk.ac.imperial.pipe.models.petrinet.PetriNetComponent}
     */
    String getName();

    /**
     *
     * @param name to be checked for duplicates 
     * @return true if name doesn't exist anywhere else in the {@link uk.ac.imperial.pipe.models.petrinet.PetriNetComponent}
     */
    boolean isUniqueName(String name);
}
