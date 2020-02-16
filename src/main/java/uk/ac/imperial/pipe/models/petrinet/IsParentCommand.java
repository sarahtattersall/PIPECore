package uk.ac.imperial.pipe.models.petrinet;

public class IsParentCommand extends AbstractIncludeHierarchyCommand<Boolean> {

    private IncludeHierarchy include;

    public IsParentCommand(IncludeHierarchy include) {
        this.include = include;
    }

    @Override
    public Result<Boolean> execute(IncludeHierarchy includeHierarchy) {
        if (includeHierarchy.equals(include)) {
            result.addEntry(null, new Boolean(true));
        }
        return result;
    }

}
