package uk.ac.imperial.pipe.visitor.connectable.arc;

import uk.ac.imperial.pipe.models.petrinet.Connectable;
import uk.ac.imperial.pipe.models.petrinet.PlaceVisitor;
import uk.ac.imperial.pipe.models.petrinet.TransitionVisitor;

/**
 * A tiny type interface to determine if the connectable is allowed to be an arc source
 */
public interface ArcSourceVisitor extends PlaceVisitor, TransitionVisitor {
    /**
     * @param connectable parameter to try and start arc from
     * @return true if we can start the type of arc at this connectable
     */
    boolean canStart(Connectable connectable);
}
