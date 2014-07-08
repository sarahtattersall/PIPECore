package uk.ac.imperial.pipe.naming;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
@RunWith(MockitoJUnitRunner.class)
public class AliasTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private PropertyChangeListener mockListener;

	private Alias alias;

	@Before
	public void setUp() throws Exception {
		alias = new Alias("top"); 
	}
	@Test
	public void aliasForRootLevelHasNoParentAndNoChildren() {
		assertNull(alias.parent()); 
		assertThat(alias.aliasMap()).hasSize(0);
	}
	@Test
	public void verifyAliasForFirstIncludedLevel() throws Exception {
		alias.buildAlias("first-child");
		alias.buildAlias("second-child");
		assertThat(alias.aliasMap()).hasSize(2); 
		assertThat(alias.getAlias("first-child").aliasMap()).hasSize(0); 
		assertEquals(alias, alias.getAlias("first-child").parent()); 
	}
	@Test
	public void nameIsIndependentForEachLevelButFullyQualifiedNameBuildsByLevel() throws Exception {
		alias.buildAlias("first-child").buildAlias("grand-child");
		assertEquals("top",alias.getName());
		assertEquals("first-child",alias.getAlias("first-child").getName());
		assertEquals("grand-child",alias.getAlias("first-child").getAlias("grand-child").getName());
		assertEquals("top",alias.getFullyQualifiedName());
		assertEquals("top.first-child",alias.getAlias("first-child").getFullyQualifiedName());
		assertEquals("top.first-child.grand-child",alias.getAlias("first-child").getAlias("grand-child").getFullyQualifiedName());
	}
	@Test
	public void verifyRenameOfHigherLevelCascadedIntoAllFullyQualifiedNames() throws Exception {
		alias.buildAlias("first-child").buildAlias("grand-child");
		alias.rename("newtop"); 
		assertEquals("newtop",alias.getName());
		assertEquals("newtop",alias.getFullyQualifiedName());
		assertEquals("newtop.first-child",alias.getAlias("first-child").getFullyQualifiedName());
		assertEquals("newtop.first-child.grand-child",alias.getAlias("first-child").getAlias("grand-child").getFullyQualifiedName());
		alias.getAlias("first-child").rename("fred");
		assertEquals("fred",alias.getAlias("fred").getName());
		assertEquals("newtop.fred",alias.getAlias("fred").getFullyQualifiedName());
		assertEquals("newtop.fred.grand-child",alias.getAlias("fred").getAlias("grand-child").getFullyQualifiedName());
	}
    @Test
    public void childHearsThatParentHasRenamed() {
    	PropertyChangeListener mockListener = mock(PropertyChangeListener.class);
    	alias.buildAlias("child");
        alias.getAlias("child").addPropertyChangeListener(mockListener);
        alias.rename("root");
        verify(mockListener).propertyChange(any(PropertyChangeEvent.class));
    }
    @Test
	public void throwsIfNameDoesNotExist() throws Exception {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Alias not found at level top: fred");
        alias.buildAlias("child");
        alias.getAlias("fred"); 
	}
    @Test
    public void topNameDefaultsToRootIfNotProvided() throws Exception {
    	alias = new Alias(null); 
    	assertEquals("root", alias.getName()); 
    	alias = new Alias(" "); 
    	assertEquals("root", alias.getName()); 
    }
    @Test
    public void throwsIfChildNameIsDuplicateOrBlankOrNull() throws Exception {
    	expectedException.expect(RuntimeException.class);
    	expectedException.expectMessage("Alias name duplicated at level top: child");
    	alias.buildAlias("child");
    	alias.buildAlias("child");
    	expectedException.expectMessage("Alias name may not be blank or null");
    	alias.buildAlias(" ");
    	alias.buildAlias(null);
    }
    @Test
	public void sameNameMayAppearAtDifferentLevels() throws Exception {
    	alias.buildAlias("child").buildAlias("child");
    	assertEquals("top.child.child",alias.getAlias("child").getAlias("child").getFullyQualifiedName()); 
	}
    @Test
    public void throwsIfRenameWouldCauseDuplicateAtParentLevel() throws Exception {
    	expectedException.expect(RuntimeException.class);
    	expectedException.expectMessage("Alias attempted rename at level top would cause duplicate: child");
    	alias.buildAlias("child");
    	alias.buildAlias("second-child");
    	alias.getAlias("second-child").rename("child"); 
    }
}
