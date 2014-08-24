package uk.ac.imperial.pipe.models.petrinet;

import java.util.List;


public class ParentsCommandScope implements IncludeHierarchyCommandScope {

	private IncludeHierarchy includes;

	public ParentsCommandScope(IncludeHierarchy includes) {
		this.includes = includes; 
	}

	@Override
	public List<String> execute(IncludeHierarchyCommand command) {
		return includes.parents(command);
	}

}
