package uk.ac.imperial.pipe.models.petrinet.name;

/**
 * Used in acyclic visitor pattern to visit {@link uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName} class
 */
public interface NormalNameVisitor extends NameVisitor {
    /**
     *
     * Visits name and performs any necessary actions on it
     * @param name name to visit
     */
    void visit(NormalPetriNetName name);
}
