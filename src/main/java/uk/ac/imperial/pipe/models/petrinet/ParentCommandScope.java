package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.exceptions.IncludeException;

public class ParentCommandScope implements IncludeHierarchyCommandScope {

    private IncludeHierarchy includes;

    public ParentCommandScope(IncludeHierarchy includes) {
        this.includes = includes;
    }

    @Override
    public <T> Result<T> execute(IncludeHierarchyCommand<T> command) throws IncludeException {
        return includes.parent(command);
    }

}
