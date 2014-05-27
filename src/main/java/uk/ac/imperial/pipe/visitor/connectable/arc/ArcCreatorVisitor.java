package uk.ac.imperial.pipe.visitor.connectable.arc;

import uk.ac.imperial.pipe.models.petrinet.PlaceVisitor;
import uk.ac.imperial.pipe.models.petrinet.TransitionVisitor;

/**
 * tiny type, an arc creator
 */
public interface ArcCreatorVisitor extends PlaceVisitor, TransitionVisitor {
}
