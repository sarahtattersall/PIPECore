package uk.ac.imperial.pipe.naming;

/**
 * Namer is used to find unique names for {@link uk.ac.imperial.pipe.models.component.PetriNetComponent}'s within a
 * {@link uk.ac.imperial.pipe.models.petrinet.PetriNet}
 */
public interface UniqueNamer {
    /**
     * @return a unique name for the {@link uk.ac.imperial.pipe.models.component.PetriNetComponent}
     */
    String getName();

    /**
     *
     * @param name
     * @return true if name doesn't exist anywhere else in the {@link uk.ac.imperial.pipe.models.component.PetriNetComponent}
     */
    boolean isUniqueName(String name);
}
