package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.exceptions.IncludeException;
import uk.ac.imperial.pipe.models.petrinet.Result.Entry;

/**
 * Used to implement the same command for multiple levels in an {@link uk.ac.imperial.models.petrinet.IncludeHierarchy}.  Each level is its own IncludeHierarchy
 * and each level may report errors as a separate {@link Entry} in a single cumulative {@link Result}.  
 * Used to simplify the process whereby an {@link uk.ac.imperial.models.petrinet.IncludeHierarchy} requests  
 * that all its parents or children execute a command.  Continued processing depends on whether any of the receivers encountered an error.    
 * @see AbstractIncludeHierarchyCommand
 * @see Result
 */
public interface IncludeHierarchyCommand<T> {
	
	/**
	 * @param includeHierarchy upon which this command is to be performed
	 * @return Result including a list of {@link Entry} that resulted from the command 
	 * @throws IncludeException 
	 */
	
	Result<T> execute(IncludeHierarchy includeHierarchy) ;

	/**
	 * @return current Result from this command 
	 */
	Result<T> getResult();
	
}
