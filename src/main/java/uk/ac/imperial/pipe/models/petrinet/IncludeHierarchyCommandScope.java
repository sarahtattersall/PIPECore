package uk.ac.imperial.pipe.models.petrinet;



public interface IncludeHierarchyCommandScope {

	public <T> Result<T> execute(IncludeHierarchyCommand<T> command);

}
