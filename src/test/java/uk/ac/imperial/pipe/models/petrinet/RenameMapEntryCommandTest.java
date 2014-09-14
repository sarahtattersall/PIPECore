package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class RenameMapEntryCommandTest {

	private PetriNet net;
	private IncludeHierarchy includes;
	private IncludeHierarchy childInclude;

	@Before
	public void setUp() throws Exception {
		net = new PetriNet(); 
		includes = new IncludeHierarchy(net, "top"); 
		childInclude = new IncludeHierarchy(net, "a");
	}
	@Test
	public void replacesExistingEntry() throws Exception {
		includes.getIncludeMap().put("a", childInclude); 
		assertEquals(1, includes.getIncludeMap().size()); 
		IncludeHierarchyCommand<Object> renameCommand = 
				new RenameMapEntryCommand<Object>(IncludeHierarchyMapEnum.INCLUDE,"a", "b", childInclude); 
		Result<Object> result = renameCommand.execute(includes); 
		assertFalse(result.hasResult()); 
		assertEquals(1, includes.getIncludeMap().size()); 
		assertEquals("b", includes.getIncludeMap().keySet().iterator().next()); 
		assertFalse(includes.getIncludeMap().containsKey("a")); 
		assertEquals(childInclude, includes.getIncludeMap().values().iterator().next()); 
	}
	@Test
	public void notReplacedIfNewnameIsDuplicate() throws Exception {
		includes.getIncludeMap().put("a", childInclude); 
		includes.getIncludeMap().put("b", new IncludeHierarchy(net, "fred")); 
		IncludeHierarchyCommand<Object> renameCommand = 
				new RenameMapEntryCommand<Object>(IncludeHierarchyMapEnum.INCLUDE,"a", "b", childInclude); 
		Result<Object> result = renameCommand.execute(includes); 
		assertTrue(result.hasResult()); 
		assertEquals("RenameMapEntryCommand:  map entry in IncludeMap in IncludeHierarchy top for IncludeHierarchy with key a" +
				" not renamed to b; another entry by that name already exists.",result.getMessage()); 
		assertEquals(2, includes.getIncludeMap().size()); 
		assertTrue(includes.getIncludeMap().containsKey("a")); 
		assertTrue(includes.getIncludeMap().containsKey("b")); 
		assertNotEquals(childInclude, includes.getIncludeMap().get("b")); 
	}
	@Test
	public void addsEntryIfPreviousNameDoesntExistAndIncludeDoesntExistUnderAnyNameButReturnsResult() throws Exception {
		includes.getIncludeMap().put("a", childInclude); 
		IncludeHierarchy fredInclude = new IncludeHierarchy(net, "fred"); 
		assertFalse("nothing under this key",includes.getIncludeMap().containsKey("b")); 
		assertFalse("include doesnt exist either",includes.getIncludeMap().containsValue(fredInclude)); 
		IncludeHierarchyCommand<Object> renameCommand = 
				new RenameMapEntryCommand<Object>(IncludeHierarchyMapEnum.INCLUDE,"b", "c", fredInclude); 
		Result<Object> result = renameCommand.execute(includes); 
		assertTrue(result.hasResult()); 
		assertEquals("RenameMapEntryCommand:  no map entry found in IncludeMap in IncludeHierarchy top for IncludeHierarchy with key b." +
				" Target IncludeHierarchy does not exist under any name.  Not renamed.  Probable logic error.",result.getMessage()); 
		assertEquals(1, includes.getIncludeMap().size()); 
		assertTrue(includes.getIncludeMap().containsKey("a")); 
		assertFalse(includes.getIncludeMap().containsKey("c")); 
		assertEquals(childInclude, includes.getIncludeMap().get("a")); 
	}
	@Test
	public void entryNotAddedIfPreviousNameDoesntExistButIncudeExistsUnderAnotherNameAndReturnsResult() throws Exception {
		includes.getIncludeMap().put("a", childInclude); 
		assertFalse("nothing under this key",includes.getIncludeMap().containsKey("b")); 
		assertTrue("but include exists",includes.getIncludeMap().containsValue(childInclude)); 
		IncludeHierarchyCommand<Object> renameCommand = 
				new RenameMapEntryCommand<Object>(IncludeHierarchyMapEnum.INCLUDE,"b", "c", childInclude); 
		Result<Object> result = renameCommand.execute(includes); 
		assertTrue(result.hasResult()); 
		assertEquals("RenameMapEntryCommand:  no map entry found in IncludeMap in IncludeHierarchy top for IncludeHierarchy with key b." +
				" TargetHierarchy exists under different key: a.  Not renamed.  Probable logic error.",result.getMessage()); 
		assertEquals(1, includes.getIncludeMap().size()); 
		assertTrue(includes.getIncludeMap().containsKey("a")); 
		assertFalse(includes.getIncludeMap().containsKey("b")); 
		assertFalse(includes.getIncludeMap().containsKey("c")); 
		assertEquals(childInclude, includes.getIncludeMap().get("a")); 
	}
}
