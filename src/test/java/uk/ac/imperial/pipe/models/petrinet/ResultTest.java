package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class ResultTest {

	
	private DummyCommand<Integer> command;
	private PetriNet net;
	private IncludeHierarchy includes;
	private Result<Integer> result;

	@Before
	public void setUp() throws Exception {
		command = new DummyCommand<>(); 
		net = new PetriNet(); 
		includes = net.getIncludeHierarchy(); 
	}
	@Test
	public void hasResultReturnsTrueWhenEntryListIsntEmptyAndFalseOtherwise() throws Exception {
		result = command.execute(includes);  
		assertTrue(result.hasResult()); 
		assertFalse((new DummyCommand<>(0)).execute(includes).hasResult()); 
	}
	@Test
	public void returnsListOfResultEntries() throws Exception {
		result = (new DummyCommand<Integer>(1)).execute(includes); 
		assertEquals(1, result.getEntries().size()); 
		result = (new DummyCommand<Integer>(3)).execute(includes); 
		assertEquals(3, result.getEntries().size()); 
	}
	@Test
	public void returnsFirstEntryAsSingleEntryIfHasResultsAndNullOtherwise() throws Exception {
		result = (new DummyCommand<Integer>(0)).execute(includes); 
		assertNull(result.getEntry()); 
		result = (new DummyCommand<Integer>(1)).execute(includes); 
		assertEquals(0, (int) result.getEntry().value); 
		result = (new DummyCommand<Integer>(3)).execute(includes); 
		assertEquals("retrieves first entry",0, (int) result.getEntry().value); 
	}
}
