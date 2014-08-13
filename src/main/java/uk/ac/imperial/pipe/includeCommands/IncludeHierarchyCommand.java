package uk.ac.imperial.pipe.includeCommands;

import java.util.List;

import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
/**
 * Used to implement the same command for multiple levels in an {@link uk.ac.imperial.models.petrinet.IncludeHierarchy}.  Each level is its own IncludeHierarchy
 * and each level may report errors.  Used to simplify the process whereby an {@link uk.ac.imperial.models.petrinet.IncludeHierarchy} requests  
 * that all its parents or children execute a command.  Continued processing depends on whether any of the receivers encountered an error.    
 * <p>
 * Implementations are expected to provide a constructor that passes a List<String> of messages, that will be returned 
 * by each command execution.  The command requester interprets an empty list of messages as meaning no errors were encountered.  
 * A non-empty list can be presented to the user as a list of errors to be addressed.  
 *
 */
public interface IncludeHierarchyCommand {
	
	/**
	 * 
	 * @param includeHierarchy upon which this command is to be performed
	 * @return list of error messages that resulted from the command 
	 */
	List<String> execute(IncludeHierarchy includeHierarchy);
	/**
	 * @return list of current error messages 
	 */
	List<String> getMessages();
}
