package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.pipe.includeCommands.DummyCommand;

public class IncludeHierarchyCommandScopeTest {

	private PetriNet net1;
	private IncludeHierarchy includes;
	private IncludeHierarchyCommandScope parentScope;
	private IncludeHierarchyCommandScope parentsSiblingsScope;
	private IncludeHierarchyCommandScope allScope;

	@Before
	public void setUp() throws Exception {
		net1 = new PetriNet(); 
		includes = net1.getIncludeHierarchy(); 
		parentScope = IncludeHierarchyCommandScopeEnum.PARENTS.buildScope(includes);
		parentsSiblingsScope = IncludeHierarchyCommandScopeEnum.PARENTS_AND_SIBLINGS.buildScope(includes);
		allScope = IncludeHierarchyCommandScopeEnum.ALL.buildScope(includes);
	}
	@Test
	public void enumReturnsScope() {
		IncludeHierarchyCommandScope scope = IncludeHierarchyCommandScopeEnum.PARENTS.buildScope(includes); 
		assertTrue(scope instanceof ParentsCommandScope); 
		scope = IncludeHierarchyCommandScopeEnum.PARENTS_AND_SIBLINGS.buildScope(includes); 
		assertTrue(scope instanceof ParentsSiblingsCommandScope); 
		scope = IncludeHierarchyCommandScopeEnum.ALL.buildScope(includes);
		assertTrue(scope instanceof AllCommandScope); 
	}
	@Test
	public void scopeInvokesCommandAgainstExpectedPortionOfIncludeHierarchy() throws Exception {
//		fail("nothing yet...."); 
		List<String> messages = parentScope.execute(new DummyCommand()); 
		// mockito...
		// verify parents is invoked 
		// return include.parents(commmand)
	}
	private class TestingIncludeHierarchy extends IncludeHierarchy {

		public TestingIncludeHierarchy(PetriNet net, String name) {
			super(net, name);
		}
		
	}
}
