package uk.ac.imperial.pipe.models.petrinet;

import java.util.List;

/**
 *  Abstract class that all classes that implement {@link IncludeHierarchyCommand} should extend.
 *  The constructor creates a list of error messages, which is returned either unmodified, by {@link #getMessages()}
 *  or as a result of {@link #executeOld(IncludeHierarchy)}. 
 */
public abstract class AbstractIncludeHierarchyCommand<T> implements IncludeHierarchyCommand<T> {

	protected List<String> messages;
	protected Result<T> result;
	
	public AbstractIncludeHierarchyCommand() {
		result = new Result<T>(); 
		
	}
	
	@Override
	public abstract Result<T> execute(IncludeHierarchy includeHierarchy);
	@Override
	public Result<T> getResult() {
		return result;
	}

}
