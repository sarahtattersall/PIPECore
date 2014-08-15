package uk.ac.imperial.pipe.models.petrinet;

import java.util.List;

import uk.ac.imperial.pipe.includeCommands.IncludeHierarchyCommand;

public class ParentsSiblingsCommandScope implements IncludeHierarchyCommandScope {
	private IncludeHierarchy includes;

	public ParentsSiblingsCommandScope(IncludeHierarchy includes) {
		this.includes = includes; 
	}

	@Override
	public List<String> execute(IncludeHierarchyCommand command) {
		includes.siblings(command);
		return includes.parents(command);
	}

}
