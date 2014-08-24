package uk.ac.imperial.pipe.models.petrinet;

import java.util.List;


public class AllCommandScope implements IncludeHierarchyCommandScope {

	private IncludeHierarchy includes;

	public AllCommandScope(IncludeHierarchy includes) {
		this.includes = includes; 
	}

	@Override
	public List<String> execute(IncludeHierarchyCommand command) {
		return includes.all(command);
	}

}
