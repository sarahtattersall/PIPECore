package uk.ac.imperial.pipe.visitor.component;

/**
 * Degenerate visitor interface for visiting {@link uk.ac.imperial.pipe.models.petrinet.PetriNetComponent}
 * Used to implement the acyclic visitor pattern
 * This pattern is used to break dependency cycles and allows
 * for visitors to only implement those classes that they're actually
 * interested in.
 */
public interface PetriNetComponentVisitor {

}
