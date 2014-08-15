package uk.ac.imperial.pipe.models.petrinet;

import java.util.List;

import uk.ac.imperial.pipe.includeCommands.IncludeHierarchyCommand;

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
