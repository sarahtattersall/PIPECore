package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.exceptions.IncludeException;

public class ParentsSiblingsCommandScope implements IncludeHierarchyCommandScope {
    private IncludeHierarchy includes;

    public ParentsSiblingsCommandScope(IncludeHierarchy includes) {
        this.includes = includes;
    }

    @Override
    public <T> Result<T> execute(IncludeHierarchyCommand<T> command) throws IncludeException {
        includes.siblings(command);
        return includes.parents(command);
    }

}
