package uk.ac.imperial.pipe.models.component.annotation;

import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

public interface AnnotationVisitor extends PetriNetComponentVisitor {
    void visit(Annotation annotation);
}
