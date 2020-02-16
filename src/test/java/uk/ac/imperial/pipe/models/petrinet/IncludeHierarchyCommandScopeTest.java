package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IncludeHierarchyCommandScopeTest {

    private IncludeHierarchyCommandScope parentsScope;
    private IncludeHierarchyCommandScope parentsSiblingsScope;
    private IncludeHierarchyCommandScope allScope;
    private IncludeHierarchyCommandScope parentScope;
    private IncludeHierarchyCommand<Object> command;

    @Mock
    private IncludeHierarchy mockHierarchy;

    @Before
    public void setUp() throws Exception {
        command = new DummyCommand<Object>();
        parentScope = IncludeHierarchyCommandScopeEnum.PARENT.buildScope(mockHierarchy);
        parentsScope = IncludeHierarchyCommandScopeEnum.PARENTS.buildScope(mockHierarchy);
        parentsSiblingsScope = IncludeHierarchyCommandScopeEnum.PARENTS_AND_SIBLINGS.buildScope(mockHierarchy);
        allScope = IncludeHierarchyCommandScopeEnum.ALL.buildScope(mockHierarchy);
    }

    @Test
    public void enumReturnsScope() {
        IncludeHierarchyCommandScope scope = IncludeHierarchyCommandScopeEnum.PARENT.buildScope(mockHierarchy);
        assertTrue(scope instanceof ParentCommandScope);
        scope = IncludeHierarchyCommandScopeEnum.PARENTS.buildScope(mockHierarchy);
        assertTrue(scope instanceof ParentsCommandScope);
        scope = IncludeHierarchyCommandScopeEnum.PARENTS_AND_SIBLINGS.buildScope(mockHierarchy);
        assertTrue(scope instanceof ParentsSiblingsCommandScope);
        scope = IncludeHierarchyCommandScopeEnum.ALL.buildScope(mockHierarchy);
        assertTrue(scope instanceof AllCommandScope);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void scopeInvokesCommandAgainstIncludeHierarchyParents() throws Exception {
        parentsScope.execute(command);
        verify(mockHierarchy).parents(any(IncludeHierarchyCommand.class));
        //		verify(mockHierarchy).parents(any()); // JDK 1.8
    }

    @SuppressWarnings("unchecked")
    @Test
    public void scopeInvokesCommandAgainstIncludeHierarchyParentsAndSiblings() throws Exception {
        parentsSiblingsScope.execute(command);
        verify(mockHierarchy).parents(any(IncludeHierarchyCommand.class));
        verify(mockHierarchy).siblings(any(IncludeHierarchyCommand.class));
        //		verify(mockHierarchy).parents(any()); // JDK 1.8
        //		verify(mockHierarchy).siblings(any()); 
    }

    @SuppressWarnings("unchecked")
    @Test
    public void scopeInvokesCommandAgainstEntireInclude() throws Exception {
        allScope.execute(command);
        verify(mockHierarchy).all(any(IncludeHierarchyCommand.class));
        //		verify(mockHierarchy).all(any()); 
    }

    @SuppressWarnings("unchecked")
    @Test
    public void scopeInvokesCommandAgainstImmediateParentOnly() throws Exception {
        parentScope.execute(command);
        verify(mockHierarchy).parent(any(IncludeHierarchyCommand.class));
        //		verify(mockHierarchy).parent(any()); 
    }
}
