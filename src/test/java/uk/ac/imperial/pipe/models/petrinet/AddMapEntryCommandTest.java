package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class AddMapEntryCommandTest {

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
	public void addsToCorrectMap() throws Exception {
		assertEquals(IncludeHierarchyMapEnum.INCLUDE.getMap(includes), includes.getIncludeMap()); 
		assertEquals(IncludeHierarchyMapEnum.INCLUDE_ALL.getMap(includes), includes.getIncludeMapAll()); 
	}
	@Test
	public void addsNewEntry() throws Exception {
		assertEquals(0, includes.getIncludeMap().size()); 
//		assertEquals("self always added",1, includes.getIncludeMapAll().size()); 
		IncludeHierarchyCommand<Object> addCommand = new AddMapEntryCommand<Object>(IncludeHierarchyMapEnum.INCLUDE,"a", childInclude); 
		Result<Object> result = addCommand.execute(includes); 
		assertFalse(result.hasResult()); 
		assertEquals(1, includes.getIncludeMap().size()); 
		assertEquals("a", includes.getIncludeMap().keySet().iterator().next()); 
		assertEquals(childInclude, includes.getIncludeMap().values().iterator().next()); 
	}
	@Test
	public void entryNotAddedIfAlreadyExistsAndResultReturned() throws Exception {
		includes.getIncludeMap().put("a", new IncludeHierarchy(net, "fred")); 
		assertEquals(1, includes.getIncludeMap().size()); 
		IncludeHierarchyCommand<Object> addCommand = new AddMapEntryCommand<Object>(IncludeHierarchyMapEnum.INCLUDE,"a", childInclude); 
		Result<Object> result = addCommand.execute(includes); 
		assertTrue(result.hasResult()); 
		assertEquals("AddMapEntryCommand:  map entry for IncludeHierarchy a not added to IncludeMap in IncludeHierarchy top because another entry already exists with key: a",
				result.getMessage()); 
		assertEquals(1, includes.getIncludeMap().size()); 
		assertEquals("a", includes.getIncludeMap().keySet().iterator().next()); 
		assertNotEquals(childInclude, includes.getIncludeMap().values().iterator().next()); 
	}
}
