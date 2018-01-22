package uk.ac.imperial.pipe.models.petrinet;

import java.util.List;

/**
 *  Abstract class that all classes that implement {@link IncludeHierarchyCommand} should extend.
 *  The constructor creates a list of error messages, which may be updated
 *  as a result of {@link #execute(IncludeHierarchy)}.
 */
public abstract class AbstractIncludeHierarchyCommand<T> implements IncludeHierarchyCommand<T> {

    public static final String EXECUTE_REQUIRES_NON_NULL_INCLUDE_HIERARCHY = "AbstractIncludeHierarchyCommand.execute:  requires non-null include hierarchy.";
    protected List<String> messages;
    protected Result<T> result;

    //TODO constructor takes T.class and any result entries are verified to be of the expected class type.
    public AbstractIncludeHierarchyCommand() {
        result = new Result<>();
    }

    @Override
    public abstract Result<T> execute(IncludeHierarchy includeHierarchy);

    @Override
    public Result<T> getResult() {
        return result;
    }

    protected void validate(IncludeHierarchy includeHierarchy) {
        if (includeHierarchy == null) {
            throw new IllegalArgumentException(EXECUTE_REQUIRES_NON_NULL_INCLUDE_HIERARCHY);
        }
    }

}
