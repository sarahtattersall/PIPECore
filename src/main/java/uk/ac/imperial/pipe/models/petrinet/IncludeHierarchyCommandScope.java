package uk.ac.imperial.pipe.models.petrinet;

import java.util.List;

import uk.ac.imperial.pipe.includeCommands.IncludeHierarchyCommand;

public interface IncludeHierarchyCommandScope {

	public List<String> execute(IncludeHierarchyCommand command);

}
