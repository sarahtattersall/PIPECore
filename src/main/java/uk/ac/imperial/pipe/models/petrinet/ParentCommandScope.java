package uk.ac.imperial.pipe.models.petrinet;

public class ParentCommandScope implements IncludeHierarchyCommandScope {

	private IncludeHierarchy includes;

	public ParentCommandScope(IncludeHierarchy includes) {
		this.includes = includes; 
	}

	@Override
	public <T> Result<T> execute(IncludeHierarchyCommand<T> command) {
		return includes.parent(command);
	}

}
