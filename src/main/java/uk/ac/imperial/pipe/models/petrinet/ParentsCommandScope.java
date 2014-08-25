package uk.ac.imperial.pipe.models.petrinet;



public class ParentsCommandScope implements IncludeHierarchyCommandScope {

	private IncludeHierarchy includes;

	public ParentsCommandScope(IncludeHierarchy includes) {
		this.includes = includes; 
	}


	@Override
	public <T> Result<T> execute(IncludeHierarchyCommand<T> command) {
		return includes.parents(command);
	}

}
