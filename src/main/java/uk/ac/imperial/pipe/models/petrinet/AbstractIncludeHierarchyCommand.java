package uk.ac.imperial.pipe.models.petrinet;

import java.util.ArrayList;
import java.util.List;

/**
 *  Abstract class that all classes that implement {@link IncludeHierarchyCommand} should extend.
 *  The constructor creates a list of error messages, which is returned either unmodified, by {@link #getMessages()}
 *  or as a result of {@link #execute(IncludeHierarchy)}. 
 */
public abstract class AbstractIncludeHierarchyCommand implements IncludeHierarchyCommand {

	protected List<String> messages;
	
	public AbstractIncludeHierarchyCommand() {
		messages = new ArrayList<>(); 
	}
	
	@Override
	public abstract List<String> execute(IncludeHierarchy includeHierarchy);
	@Override
	public List<String> getMessages() {
		return messages;
	}

}
