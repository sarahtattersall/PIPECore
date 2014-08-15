package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.imperial.pipe.includeCommands.DummyCommand;
import uk.ac.imperial.pipe.includeCommands.IncludeHierarchyCommand;

@RunWith(MockitoJUnitRunner.class)
public class IncludeHierarchyCommandScopeTest {

	private PetriNet net1;
	private IncludeHierarchyCommandScope parentScope;
	private IncludeHierarchyCommandScope parentsSiblingsScope;
	private IncludeHierarchyCommandScope allScope;

	@Mock
    private IncludeHierarchy mockHierarchy;
	private IncludeHierarchyCommand command;

	@Before
	public void setUp() throws Exception {
		command = new DummyCommand(); 
		parentScope = IncludeHierarchyCommandScopeEnum.PARENTS.buildScope(mockHierarchy);
		parentsSiblingsScope = IncludeHierarchyCommandScopeEnum.PARENTS_AND_SIBLINGS.buildScope(mockHierarchy);
		allScope = IncludeHierarchyCommandScopeEnum.ALL.buildScope(mockHierarchy);
	}
	@Test
	public void enumReturnsScope() {
		IncludeHierarchyCommandScope scope = IncludeHierarchyCommandScopeEnum.PARENTS.buildScope(mockHierarchy); 
		assertTrue(scope instanceof ParentsCommandScope); 
		scope = IncludeHierarchyCommandScopeEnum.PARENTS_AND_SIBLINGS.buildScope(mockHierarchy); 
		assertTrue(scope instanceof ParentsSiblingsCommandScope); 
		scope = IncludeHierarchyCommandScopeEnum.ALL.buildScope(mockHierarchy);
		assertTrue(scope instanceof AllCommandScope); 
	}
	@Test
	public void scopeInvokesCommandAgainstIncludeHierarchyParents() throws Exception {
		parentScope.execute(command); 
		verify(mockHierarchy).parents(any(IncludeHierarchyCommand.class)); 
	}
	@Test
	public void scopeInvokesCommandAgainstIncludeHierarchyParentsAndSiblings() throws Exception {
		parentsSiblingsScope.execute(command); 
		verify(mockHierarchy).parents(any(IncludeHierarchyCommand.class)); 
		verify(mockHierarchy).siblings(any(IncludeHierarchyCommand.class)); 
	}
	@Test
	public void scopeInvokesCommandAgainstEntireInclude() throws Exception {
		allScope.execute(command); 
		verify(mockHierarchy).all(any(IncludeHierarchyCommand.class)); 
	}
}
