package uk.ac.imperial.pipe.visitor;

import uk.ac.imperial.pipe.models.component.annotation.AnnotationVisitor;
import uk.ac.imperial.pipe.models.component.arc.ArcVisitor;
import uk.ac.imperial.pipe.models.component.place.PlaceVisitor;
import uk.ac.imperial.pipe.models.component.rate.RateVisitor;
import uk.ac.imperial.pipe.models.component.token.TokenVisitor;
import uk.ac.imperial.pipe.models.component.transition.TransitionVisitor;

/**
 * Interface for visiting all Petri net components
 */
public interface AllComponentVisitor
        extends PlaceVisitor, TransitionVisitor, ArcVisitor, AnnotationVisitor, TokenVisitor, RateVisitor {
}
