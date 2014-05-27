package uk.ac.imperial.pipe.visitor;

import uk.ac.imperial.pipe.models.petrinet.AnnotationVisitor;
import uk.ac.imperial.pipe.models.petrinet.ArcVisitor;
import uk.ac.imperial.pipe.models.petrinet.PlaceVisitor;
import uk.ac.imperial.pipe.models.petrinet.RateVisitor;
import uk.ac.imperial.pipe.models.petrinet.TokenVisitor;
import uk.ac.imperial.pipe.models.petrinet.TransitionVisitor;

/**
 * Interface for visiting all Petri net components
 */
public interface AllComponentVisitor
        extends PlaceVisitor, TransitionVisitor, ArcVisitor, AnnotationVisitor, TokenVisitor, RateVisitor {
}
