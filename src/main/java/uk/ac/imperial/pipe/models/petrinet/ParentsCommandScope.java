package uk.ac.imperial.pipe.models.petrinet;

import java.util.List;

import uk.ac.imperial.pipe.includeCommands.IncludeHierarchyCommand;

public class ParentsCommandScope implements IncludeHierarchyCommandScope {

	public ParentsCommandScope(IncludeHierarchy includes) {
	}

	@Override
	public List<String> execute(IncludeHierarchyCommand command) {
		return null;
	}

}
