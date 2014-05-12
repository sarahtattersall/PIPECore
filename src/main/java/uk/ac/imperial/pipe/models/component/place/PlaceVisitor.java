package uk.ac.imperial.pipe.models.component.place;

import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

public interface PlaceVisitor extends PetriNetComponentVisitor {
    void visit(Place place);
}
