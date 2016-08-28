package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

/**
 * Annotation implementation visitor
 */
public interface AnnotationImplVisitor extends PetriNetComponentVisitor {
    /**
     * Visit the concrete implementation of an annotation
     *
     * @param annotation to be visited 
     */
    void visit(AnnotationImpl annotation);
}
