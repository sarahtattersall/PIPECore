package uk.ac.imperial.pipe.visitor.connectable.arc;

import uk.ac.imperial.pipe.models.component.place.PlaceVisitor;
import uk.ac.imperial.pipe.models.component.transition.TransitionVisitor;

/**
 * tiny type, an arc creator
 */
public interface ArcCreatorVisitor extends PlaceVisitor, TransitionVisitor {
}
