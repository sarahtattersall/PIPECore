package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

public interface PlaceVisitor extends PetriNetComponentVisitor {
    void visit(Place place);
}
