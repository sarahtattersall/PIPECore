package uk.ac.imperial.pipe.models.petrinet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.models.petrinet.DiscretePlace;
import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.Place;
@RunWith(MockitoJUnitRunner.class)
public class IncludeHierarchyTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private PropertyChangeListener mockListener;

	private IncludeHierarchy includes;
	private PetriNet net1;
	private PetriNet net2; 
	private PetriNet net3; 

	@Before
	public void setUp() throws Exception {
		net1 = createSimpleNet();
		net2 = createSimpleNet();
		net3 = createSimpleNet();
		includes = new IncludeHierarchy(net1, "top"); 
	}
	@Test
	public void includeHierarchyForRootLevelHasNoParentAndNoChildren() {
		assertNull(includes.parent()); 
		assertThat(includes.includeMap()).hasSize(0);
	}
	@Test
	public void verifyIncludeHierarchyForFirstIncludedLevel() throws Exception {
		includes.include(net1, "first-child");
		includes.include(net1, "second-child");
		assertThat(includes.includeMap()).hasSize(2); 
		assertThat(includes.getInclude("first-child").includeMap()).hasSize(0); 
		assertEquals(includes, includes.getInclude("first-child").parent()); 
	}
	@Test
	public void nameIsIndependentForEachLevelButFullyQualifiedNameBuildsByLevel() throws Exception {
		includes.include(net1, "first-child").include(net1, "grand-child");
		assertEquals("top",includes.getName());
		assertEquals("first-child",includes.getInclude("first-child").getName());
		assertEquals("grand-child",includes.getInclude("first-child").getInclude("grand-child").getName());
		assertEquals("top",includes.getFullyQualifiedName());
		assertEquals("top.first-child",includes.getInclude("first-child").getFullyQualifiedName());
		assertEquals("top.first-child.grand-child",includes.getInclude("first-child").getInclude("grand-child").getFullyQualifiedName());
	}
	@Test
	public void verifyRenameOfHigherLevelCascadedIntoAllFullyQualifiedNames() throws Exception {
		includes.include(net1, "first-child").include(net1, "grand-child");
		includes.rename("newtop"); 
		assertEquals("newtop",includes.getName());
		assertEquals("newtop",includes.getFullyQualifiedName());
		assertEquals("newtop.first-child",includes.getInclude("first-child").getFullyQualifiedName());
		assertEquals("newtop.first-child.grand-child",includes.getInclude("first-child").getInclude("grand-child").getFullyQualifiedName());
		includes.getInclude("first-child").rename("fred");
		assertEquals("fred",includes.getInclude("fred").getName());
		assertEquals("newtop.fred",includes.getInclude("fred").getFullyQualifiedName());
		assertEquals("newtop.fred.grand-child",includes.getInclude("fred").getInclude("grand-child").getFullyQualifiedName());
	}
    @Test
    public void childHearsThatParentHasRenamed() {
    	PropertyChangeListener mockListener = mock(PropertyChangeListener.class);
    	includes.include(net1, "child");
        includes.getInclude("child").addPropertyChangeListener(mockListener);
        includes.rename("root");
        verify(mockListener).propertyChange(any(PropertyChangeEvent.class));
    }
    @Test
	public void throwsIfNameDoesNotExist() throws Exception {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage(IncludeHierarchy.INCLUDE_ALIAS_NOT_FOUND_AT_LEVEL+"top: fred");
        includes.include(net1, "child");
        includes.getInclude("fred"); 
	}
    @Test
    public void topNameDefaultsToBlankIfNotProvided() throws Exception {
    	includes = new IncludeHierarchy(net1, null); 
    	assertEquals("", includes.getName()); 
    	includes = new IncludeHierarchy(net1, " "); 
    	assertEquals("", includes.getName()); 
    }
    @Test
    public void throwsIfChildNameIsDuplicate() throws Exception {
    	expectedException.expect(RuntimeException.class);
    	expectedException.expectMessage(IncludeHierarchy.INCLUDE_ALIAS_NAME_DUPLICATED_AT_LEVEL+"top: child");
    	includes.include(net1, "child");
    	includes.include(net1, "child");
    }
    @Test
    public void throwsIfChildNameIsBlankOrNull() throws Exception {
    	expectedException.expect(IllegalArgumentException.class);
    	expectedException.expectMessage(IncludeHierarchy.INCLUDE_ALIAS_NAME_MAY_NOT_BE_BLANK_OR_NULL);
    	includes.include(net1, " ");
    	includes.include(net1, null);
    }
    @Test
	public void sameNameMayAppearAtDifferentLevels() throws Exception {
    	includes.include(net1, "child").include(net1, "child");
    	assertEquals("top.child.child",includes.getInclude("child").getInclude("child").getFullyQualifiedName()); 
	}
    @Test
    public void throwsIfRenameWouldCauseDuplicateAtParentLevel() throws Exception {
    	expectedException.expect(RuntimeException.class);
    	expectedException.expectMessage("IncludeHierarchy attempted rename at level top would cause duplicate: child");
    	includes.include(net1, "child");
    	includes.include(net1, "second-child");
    	includes.getInclude("second-child").rename("child"); 
    }
    @Test
	public void netMayNotBeNull() throws Exception {
    	expectedException.expect(IllegalArgumentException.class);
    	expectedException.expectMessage(IncludeHierarchy.INCLUDE_HIERARCHY_PETRI_NET_MAY_NOT_BE_NULL);
    	includes.include(null, "child"); 
    	expectedException.expectMessage(IncludeHierarchy.INCLUDE_HIERARCHY_PETRI_NET_MAY_NOT_BE_NULL);
    	includes = new IncludeHierarchy(null, "fred"); 
	}
    @Test
	public void savesAndReturnsNet() throws Exception {
    	assertEquals(net1, includes.getPetriNet()); 
	}
	public PetriNet createSimpleNet() {
		return APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
				APlace.withId("P1")).and(AnImmediateTransition.withId("T0")).and(
				AnImmediateTransition.withId("T1")).and(
				ANormalArc.withSource("T1").andTarget("P1")).andFinally(
				ANormalArc.withSource("T0").andTarget("P0").with("#(P0)", "Default").token());
	}
	@Test
	public void topLevelPetriNetIsNotNull() throws Exception
	{
		expectedException.expect(IllegalArgumentException.class);
		includes = new IncludeHierarchy(null, "top");
	}
    @Test
    public void includedNetsAddedSubordinateToTop() {
      assertEquals("hierarchy is everything below the root",0, includes.includeMap().size()); 
      includes.include(net2, "some-function"); 
      includes.include(net3, "another-function"); 
      assertEquals("size is only for a given level",2, includes.includeMap().size());
      assertEquals(net2, includes.getInclude("some-function").getPetriNet());
    }
    @Test
	public void verifyIncludedNetsHaveFullyQualifiedAliasNameBuiltByLevel() throws Exception {
		includes.include(net2, "left-function");
		includes.include(net2, "right-function").include(net3,"lowlevel-function"); 
		assertEquals("top", includes.getName()); 
		assertEquals("top", includes.getFullyQualifiedName()); 
  	    assertEquals("left-function", includes.getInclude("left-function").getName()); 
  	    assertEquals("lowlevel-function", includes.getInclude("right-function")
  			.getInclude("lowlevel-function").getName()); 
		assertEquals("top", includes.getFullyQualifiedName()); 
  	    assertEquals("top.left-function", includes.getInclude("left-function").getFullyQualifiedName()); 
  	    assertEquals("top.right-function.lowlevel-function", includes.getInclude("right-function")
  			.getInclude("lowlevel-function").getFullyQualifiedName()); 
	}
    @Test
    public void idPrefixIsSuffixedWithDot() throws Exception {
		includes = new IncludeHierarchy(net1, "top"); 
		includes.include(net2, "right-function").include(net3,"lowlevel-function"); 
		assertEquals("top.", includes.getFullyQualifiedNameAsPrefix()); 
		assertEquals("top.right-function.", includes.getInclude("right-function").getFullyQualifiedNameAsPrefix()); 
		assertEquals("top.right-function.lowlevel-function.", includes.getInclude("right-function")
				.getInclude("lowlevel-function").getFullyQualifiedNameAsPrefix()); 
    }
    @Test
    public void blankTopLevelGivesBlankPrefix() throws Exception {
    	includes = new IncludeHierarchy(net1, null); 
    	assertEquals("", includes.getFullyQualifiedNameAsPrefix()); 
    }
    @Test
    public void lowerLevelPrefixIsPrefixedWithDotIfRootLevelIsBlank() throws Exception {
    	includes = new IncludeHierarchy(net1, null); 
    	includes.include(net2, "right-function").include(net3,"lowlevel-function"); 
    	assertEquals(".right-function.", includes.getInclude("right-function").getFullyQualifiedNameAsPrefix()); 
    	assertEquals(".right-function.lowlevel-function.", includes.getInclude("right-function")
    			.getInclude("lowlevel-function").getFullyQualifiedNameAsPrefix()); 
    }
    
    @Test
	public void aNetCanBeIncludedMultipleTimesUnderDifferentAliases() throws Exception {
	  	includes.include(net1, "left-function"); 
	  	includes.include(net1, "right-function"); 
	  	assertEquals(includes.getInclude("left-function").getPetriNet(), includes.getInclude("right-function").getPetriNet()); 
	}
    @Test
	public void returnsAllComponentsWithIdPrefixedWithAlias() throws Exception {
	  	includes.include(net2, "a-function");
	  	Map<String, Place> places = includes.getPlaces();
	  	assertEquals("P0", places.get("P0").getName());
	  	assertEquals("P0", places.get("top.a-function.P0").getName()); 
	}
  //TODO AliasInclude class
  //TODO generic getMap function.  use Id change 
  //TODO verifyImportsAreNotRecursive or verifyNumberOfCascadedImportsIsLessThanSomeConstant
  //TODO verifyDuplicateAliasIsSuffixedToEnsureUniqueness
  //TODO verifyDefaultAssignedWhenAliasIsBlank
  //TODO verifyAliasesAreStackedAsImportsAreAdded
	//TODO verifyAliasIsOptionalForRoot
    
}
