package uk.ac.imperial.pipe.models.petrinet.name;

/**
 * Visit different types of file name
 */
public interface FileNameVisitor extends NameVisitor {
    /**
     *
     * Visit the Petri net file name
     *
     * @param name to be visited 
     */
    void visit(PetriNetFileName name);
}
