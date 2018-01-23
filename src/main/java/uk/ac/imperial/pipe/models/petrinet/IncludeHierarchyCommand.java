package uk.ac.imperial.pipe.models.petrinet;

/**
 * Used to implement the same command for multiple levels in an @link {@link IncludeHierarchy}
 * Each level is its own IncludeHierarchy
 * and each level may report errors as a separate entry of type T in a single cumulative @link {@link Result}
 * Used to simplify the process whereby an {@link IncludeHierarchy} requests
 * that all its parents or children execute a command.  Continued processing depends on whether any of the receivers encountered an error.
 * @see AbstractIncludeHierarchyCommand
 * @see Result
 */
public interface IncludeHierarchyCommand<T> {

    /**
     * @param includeHierarchy upon which this command is to be performed
     * @return Result including a list of type T that resulted from the command
     */

    Result<T> execute(IncludeHierarchy includeHierarchy);

    /**
     * @return current Result from this command
     */
    Result<T> getResult();

}
