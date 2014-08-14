package uk.ac.imperial.pipe.models.petrinet;

import java.util.List;

import uk.ac.imperial.pipe.includeCommands.IncludeHierarchyCommand;

public class ParentsSiblingsCommandScope implements IncludeHierarchyCommandScope {

	public ParentsSiblingsCommandScope(IncludeHierarchy includes) {
	}

	@Override
	public List<String> execute(IncludeHierarchyCommand command) {
		return null;
	}

}
