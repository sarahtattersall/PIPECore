package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

public interface TokenVisitor extends PetriNetComponentVisitor {
    void visit(Token token) throws PetriNetComponentException;
}
