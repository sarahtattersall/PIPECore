package uk.ac.imperial.pipe.models.petrinet;



public class AllCommandScope implements IncludeHierarchyCommandScope {

	private IncludeHierarchy includes;

	public AllCommandScope(IncludeHierarchy includes) {
		this.includes = includes; 
	}

	@Override
	public <T> Result<T> execute(IncludeHierarchyCommand<T> command) {
		return includes.all(command);
	}

}
