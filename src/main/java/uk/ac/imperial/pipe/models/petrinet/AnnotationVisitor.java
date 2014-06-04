package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

/**
 * Visits interface annotation
 */
public interface AnnotationVisitor extends PetriNetComponentVisitor {
    void visit(Annotation annotation);
}
