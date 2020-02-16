package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.exceptions.IncludeException;

public interface IncludeHierarchyCommandScope {

    public <T> Result<T> execute(IncludeHierarchyCommand<T> command) throws IncludeException;

}
