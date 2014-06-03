package uk.ac.imperial.pipe.models.petrinet.name;

/**
 * Visit different types of file name
 */
public interface FileNameVisitor extends NameVisitor {
    void visit(PetriNetFileName name);
}
