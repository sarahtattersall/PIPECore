package uk.ac.imperial.pipe.visitor;

import uk.ac.imperial.pipe.models.petrinet.Annotation;
import uk.ac.imperial.pipe.models.petrinet.AnnotationImpl;
import uk.ac.imperial.pipe.models.petrinet.AnnotationImplVisitor;

public class AnnotationCloner implements AnnotationImplVisitor {
    public Annotation cloned;
    @Override
    public void visit(AnnotationImpl annotation) {
        cloned = new AnnotationImpl(annotation);
    }
}
