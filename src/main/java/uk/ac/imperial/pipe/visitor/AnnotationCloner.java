package uk.ac.imperial.pipe.visitor;

import uk.ac.imperial.pipe.models.petrinet.Annotation;
import uk.ac.imperial.pipe.models.petrinet.AnnotationImpl;
import uk.ac.imperial.pipe.models.petrinet.AnnotationImplVisitor;

/**
 * Used to clone annotations
 */
public class AnnotationCloner implements AnnotationImplVisitor {
    /**
     * Cloned annotation, null before visit is called
     */
    public Annotation cloned;

    /**
     * Visits the annotation and clones it
     * @param annotation to be cloned 
     */
    @Override
    public void visit(AnnotationImpl annotation) {
        cloned = new AnnotationImpl(annotation);
    }
}
