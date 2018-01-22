package uk.ac.imperial.pipe.models.petrinet;

public class BuildFullyQualifiedNameCommand extends AbstractIncludeHierarchyCommand<Object> {

    @Override
    public Result<Object> execute(IncludeHierarchy includeHierarchy) {
        StringBuffer sb = new StringBuffer();
        sb.insert(0, includeHierarchy.getName());
        IncludeHierarchy parent = includeHierarchy.getParent();
        while (parent != null) {
            sb.insert(0, ".");
            sb.insert(0, parent.getName());
            parent = parent.getParent();
        }
        includeHierarchy.setFullyQualifiedName(sb.toString());
        includeHierarchy.buildFullyQualifiedNameAsPrefix();
        return result;
    }
}
