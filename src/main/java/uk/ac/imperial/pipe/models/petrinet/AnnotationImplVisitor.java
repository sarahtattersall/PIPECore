package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

/**
 * Annotation implementation visitor
 */
public interface AnnotationImplVisitor extends PetriNetComponentVisitor {
    /**
     * Visit the concrete implementation of an annotation
     *
     * @param annotation
     */
    void visit(AnnotationImpl annotation);
}
