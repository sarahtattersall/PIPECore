package uk.ac.imperial.pipe.models.petrinet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
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
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;
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
	@SuppressWarnings("unused")
	private PetriNet net4, net5, net6 ;
	private IncludeHierarchyCommandScopeEnum parents;
	private IncludeHierarchyCommandScopeEnum parentsSibs;
	private IncludeHierarchyCommandScopeEnum all;

	private Map<String, IncludeHierarchy> includeMap;

	@Before
	public void setUp() throws Exception {
		net1 = createSimpleNet(1);
		net2 = createSimpleNet(2);
		net3 = createSimpleNet(3);
		net4 = createSimpleNet(4);
		net5 = createSimpleNet(5);
		net6 = createSimpleNet(6);
		includes = new IncludeHierarchy(net1, "top"); 
		parents = IncludeHierarchyCommandScopeEnum.PARENTS; 
		parentsSibs = IncludeHierarchyCommandScopeEnum.PARENTS_AND_SIBLINGS; 
		all = IncludeHierarchyCommandScopeEnum.ALL; 
	}
	@Test
	public void includeHierarchyForRootLevelHasNoParentAndNoChildren() {
		assertNull(includes.parent()); 
		assertThat(includes.includeMap()).hasSize(0);
	}
	@Test
	public void verifyIncludeHierarchyForFirstIncludedLevel() throws Exception {
		includes.include(net2, "first-child");
		includes.include(net2, "second-child");
		assertThat(includes.includeMap()).hasSize(2); 
		assertThat(includes.getChildInclude("first-child").includeMap()).hasSize(0); 
		assertEquals(includes, includes.getChildInclude("first-child").parent()); 
	}
	@Test
	public void lowerLevelsKnowTheRootLevel() throws Exception {
		includes.include(net2, "b").include(net3, "c"); 
		assertEquals(includes, includes.getChildInclude("b").getChildInclude("c").getRoot());
	}
	@Test
	public void eachIncludeKnowsItsLevelRelativeToRoot() throws Exception {
		includes.include(net2, "b").include(net3, "c"); 
		includes.include(net2, "bb"); 
		assertEquals(0,includes.getLevelRelativeToRoot()); 
		assertEquals(1,includes.getChildInclude("b").getLevelRelativeToRoot()); 
		assertEquals(1,includes.getChildInclude("bb").getLevelRelativeToRoot()); 
		assertEquals(2,includes.getChildInclude("b").getChildInclude("c").getLevelRelativeToRoot()); 
	}
	@Test
	public void nameIsIndependentForEachLevelButFullyQualifiedNameBuildsByLevel() throws Exception {
		includes.include(net2, "first-child").include(net3, "grand-child");
		assertEquals("top",includes.getName());
		assertEquals("first-child",includes.getChildInclude("first-child").getName());
		assertEquals("grand-child",includes.getChildInclude("first-child").getChildInclude("grand-child").getName());
		assertEquals("top",includes.getFullyQualifiedName());
		assertEquals("top.first-child",includes.getChildInclude("first-child").getFullyQualifiedName());
		assertEquals("top.first-child.grand-child",includes.getChildInclude("first-child").getChildInclude("grand-child").getFullyQualifiedName());
	}
	@Test
	public void verifyRenameOfHigherLevelCascadedIntoAllFullyQualifiedNames() throws Exception {
		includes.include(net2, "first-child").include(net3, "grand-child");
		includes.rename("newtop"); 
		assertEquals("newtop",includes.getName());
		assertEquals("newtop",includes.getFullyQualifiedName());
		assertEquals("newtop.first-child",includes.getChildInclude("first-child").getFullyQualifiedName());
		assertEquals("newtop.first-child.grand-child",includes.getChildInclude("first-child").getChildInclude("grand-child").getFullyQualifiedName());
		includes.getChildInclude("first-child").rename("fred");
		assertEquals("fred",includes.getChildInclude("fred").getName());
		assertEquals("newtop.fred",includes.getChildInclude("fred").getFullyQualifiedName());
		assertEquals("newtop.fred.grand-child",includes.getChildInclude("fred").getChildInclude("grand-child").getFullyQualifiedName());
	}
    @Test
    public void childHearsThatParentHasRenamed() throws Exception {
    	PropertyChangeListener mockListener = mock(PropertyChangeListener.class);
    	includes.include(net2, "child");
        includes.getChildInclude("child").addPropertyChangeListener(mockListener);
        includes.rename("root");
        verify(mockListener).propertyChange(any(PropertyChangeEvent.class));
    }
    @Test
	public void throwsIfNameDoesNotExistAtChildLevel() throws Exception {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage(IncludeHierarchy.INCLUDE_ALIAS_NOT_FOUND_AT_LEVEL+"top: fred");
        includes.include(net2, "child");
        includes.getChildInclude("fred"); 
	}
    @Test
    public void throwsIfNameDoesNotExistAtAnyLevel() throws Exception {
    	expectedException.expect(RuntimeException.class);
    	expectedException.expectMessage(IncludeHierarchy.INCLUDE_ALIAS_NOT_FOUND_AT_ANY_LEVEL+"fred");
    	includes.include(net2, "child");
    	includes.getInclude("child").include(net3, "anotherChild");
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
    	includes.include(net2, "child");
    	includes.include(net2, "child");
    }
    @Test
    public void throwsIfChildNameIsBlankOrNull() throws Exception {
    	expectedException.expect(IllegalArgumentException.class);
    	expectedException.expectMessage(IncludeHierarchy.INCLUDE_ALIAS_NAME_MAY_NOT_BE_BLANK_OR_NULL);
    	includes.include(net2, " ");
    	includes.include(net2, null);
    }
    @Test
	public void sameAliasMayAppearAtDifferentLevels() throws Exception {
    	includes.include(net2, "child").include(net3, "child");
    	assertEquals("top.child.child",includes.getChildInclude("child").getChildInclude("child").getFullyQualifiedName()); 
	}
    @Test
    public void throwsIfRenameWouldCauseDuplicateAtParentLevel() throws Exception {
    	expectedException.expect(RuntimeException.class);
    	expectedException.expectMessage("IncludeHierarchy attempted rename at level top would cause duplicate: child");
    	includes.include(net2, "child");
    	includes.include(net2, "second-child");
    	includes.getChildInclude("second-child").rename("child"); 
    }
    @Test
	public void netMayNotBeNullForConstructor() throws Exception {
    	expectedException.expect(IllegalArgumentException.class);
    	expectedException.expectMessage(IncludeHierarchy.INCLUDE_HIERARCHY_PETRI_NET_MAY_NOT_BE_NULL);
    	includes = new IncludeHierarchy(null, "fred"); 
	}
    @Test
    public void netMayNotBeNullForImplicitCreation() throws Exception {
    	expectedException.expect(IllegalArgumentException.class);
    	expectedException.expectMessage(IncludeHierarchy.INCLUDE_HIERARCHY_PETRI_NET_MAY_NOT_BE_NULL);
    	includes.include(null, "child"); 
    }
    @Test
	public void savesAndReturnsNet() throws Exception {
    	assertEquals(net1, includes.getPetriNet()); 
	}
	public PetriNet createSimpleNet(int i) {
		PetriNet net = 
				APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
				APlace.withId("P1")).and(AnImmediateTransition.withId("T0")).and(
				AnImmediateTransition.withId("T1")).and(
				ANormalArc.withSource("T1").andTarget("P1")).andFinally(
				ANormalArc.withSource("T0").andTarget("P0").with("#(P0)", "Default").token());
		net.setName(new NormalPetriNetName("net"+i));
		return net; 
	}
	@Test
	public void topLevelPetriNetIsNotNull() throws Exception
	{
		expectedException.expect(IllegalArgumentException.class);
		includes = new IncludeHierarchy(null, "top");
	}
    @Test
    public void includedNetsAddedSubordinateToTop() throws Exception {
      assertEquals("hierarchy is everything below the root",0, includes.includeMap().size()); 
      includes.include(net2, "some-function"); 
      includes.include(net3, "another-function"); 
      assertEquals("size is only for a given level",2, includes.includeMap().size());
      assertEquals(net2, includes.getChildInclude("some-function").getPetriNet());
    }
    @Test
	public void verifyIncludedNetsHaveFullyQualifiedAliasNameBuiltByLevel() throws Exception {
		includes.include(net2, "left-function");
		includes.include(net2, "right-function").include(net3,"lowlevel-function"); 
		assertEquals("top", includes.getName()); 
		assertEquals("top", includes.getFullyQualifiedName()); 
  	    assertEquals("left-function", includes.getChildInclude("left-function").getName()); 
  	    assertEquals("lowlevel-function", includes.getChildInclude("right-function")
  			.getChildInclude("lowlevel-function").getName()); 
		assertEquals("top", includes.getFullyQualifiedName()); 
  	    assertEquals("top.left-function", includes.getChildInclude("left-function").getFullyQualifiedName()); 
  	    assertEquals("top.right-function.lowlevel-function", includes.getChildInclude("right-function")
  			.getChildInclude("lowlevel-function").getFullyQualifiedName()); 
	}
    @Test
    public void idPrefixIsSuffixedWithDot() throws Exception {
		includes = new IncludeHierarchy(net1, "top"); 
		includes.include(net2, "right-function").include(net3,"lowlevel-function"); 
		assertEquals("top.", includes.getFullyQualifiedNameAsPrefix()); 
		assertEquals("top.right-function.", includes.getChildInclude("right-function").getFullyQualifiedNameAsPrefix()); 
		assertEquals("top.right-function.lowlevel-function.", includes.getChildInclude("right-function")
				.getChildInclude("lowlevel-function").getFullyQualifiedNameAsPrefix()); 
		assertEquals("lowlevel-function.", includes.getInclude("lowlevel-function").getUniqueNameAsPrefix()); 
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
    	assertEquals(".right-function.", includes.getChildInclude("right-function").getFullyQualifiedNameAsPrefix()); 
    	assertEquals(".right-function.lowlevel-function.", includes.getChildInclude("right-function")
    			.getChildInclude("lowlevel-function").getFullyQualifiedNameAsPrefix()); 
    }
    
    @Test
	public void aNetCanBeIncludedMultipleTimesUnderDifferentAliases() throws Exception {
	  	includes.include(net2, "left-function"); 
	  	includes.include(net2, "right-function"); 
	  	assertEquals(includes.getChildInclude("left-function").getPetriNet(), includes.getChildInclude("right-function").getPetriNet()); 
	}
    @Test
	public void includeCanBeReferencedByMinimallyUniqueName() throws Exception {
    	includes.include(net2, "a").include(net3, "b"); 
    	includes.getChildInclude("a").include(net3, "bb"); 
    	assertEquals(includes, includes.getInclude("top")); 
    	assertEquals(includes.getChildInclude("a"), includes.getInclude("a")); 
    	assertEquals(includes.getChildInclude("a").getChildInclude("b"), includes.getInclude("b")); 
    	assertEquals(includes.getChildInclude("a").getChildInclude("bb"), includes.getInclude("bb")); 
	}
    @Test
    public void minimallyUniqueNameForSameNamedChildIsPrefixedWithNamesUpThroughSameNamedParent() throws Exception {
    	includes.include(net2, "a").include(net3, "b").include(net4, "a"); 
    	assertEquals("parent keeps minimal name",includes.getChildInclude("a"), includes.getInclude("a")); 
    	assertEquals("childs name includes path to parent",includes.getChildInclude("a").getChildInclude("b").getChildInclude("a"), includes.getInclude("a.b.a")); 
    }
    @Test
	public void minimallyUniqueNameForLowerLevelIncludeWhereDuplicateIsNotAParentWillBeFullyQualifiedName() throws Exception {
    	includes.include(net2, "b"); 
    	includes.include(net2, "a").include(net3, "b"); 
    	assertEquals("minimially unique name for lower level include that is not child will just be fully qualified name",
    			includes.getChildInclude("a").getChildInclude("b"), includes.getInclude("top.a.b")); 
    	includes.include(net2, "c").include(net3, "d"); 
    	includes.include(net2, "d"); 
    	assertEquals("same result if nets added in the opposite order",
    			includes.getChildInclude("c").getChildInclude("d"), includes.getInclude("top.c.d")); 
	}
    @Test
    public void renamesAreReflectedInMinimallyUniqueNames() throws Exception {
    	includes.include(net2, "a").include(net3, "b").include(net4, "c").include(net5, "d").include(net6, "a"); 
    	assertEquals(includes.getChildInclude("a").getChildInclude("b").getChildInclude("c")
    			.getChildInclude("d").getChildInclude("a"), includes.getInclude("a.b.c.d.a")); 
    	includes.getChildInclude("a").getChildInclude("b").getChildInclude("c").rename("a"); 
    	assertEquals("highest level blissfully unaware",includes.getChildInclude("a"), includes.getInclude("a")); 
    	assertEquals("was c now a.b.a",includes.getChildInclude("a").getChildInclude("b").getChildInclude("a"), includes.getInclude("a.b.a")); 
    	assertEquals("still references highest level, but reflects intermediate rename",
    			includes.getChildInclude("a").getChildInclude("b").getChildInclude("a").getChildInclude("d").getChildInclude("a"), 
    			includes.getInclude("a.b.a.d.a"));
    	includes.getChildInclude("a").rename("x"); 
    	assertEquals(includes.getChildInclude("x"), includes.getInclude("x")); 
    	assertEquals("mid-level was a.b.a now a because unique",includes.getChildInclude("x").getChildInclude("b").getChildInclude("a"), includes.getInclude("a")); 
    	assertEquals("unique name shortened to start at mid-level",
    			includes.getChildInclude("x").getChildInclude("b").getChildInclude("a").getChildInclude("d").getChildInclude("a"), 
    			includes.getInclude("a.d.a"));
    }
    @Test
	public void childKnowsIfIncludeIsParent() throws Exception {
    	includes.include(net2, "a").include(net3, "b"); 
    	assertTrue(includes.getInclude("b").hasParent(includes.getInclude("a"))); 
    	assertFalse(includes.getInclude("a").hasParent(includes.getInclude("b"))); 
    	includes.include(net2, "aa");  
    	assertFalse("but a and aa are only siblings",
    			includes.getInclude("a").hasParent(includes.getInclude("aa"))); 
    	assertFalse(includes.getInclude("aa").hasParent(includes.getInclude("a"))); 
	}
    @Test
	public void interfacePlaceCanBeAddedButOnceOnly() throws Exception {
    	Place place = net1.getComponent("P0", Place.class); 
    	includes.addToInterface(place); 
    	assertThat(includes.getInterfacePlaces()).hasSize(1);
    	includes.addToInterface(place); 
    	assertThat(includes.getInterfacePlaces()).hasSize(1);
    	assertEquals("P0-I", includes.getInterfacePlace("P0-I").getId()); 
	}
    @Test
	public void interfacePlaceHasFullyQualifiedPrefixOfItsHierarchy() throws Exception {
    	buildHierarchyWithInterfacePlaces(); 
    	InterfacePlace interfacePlace = includes.getInterfacePlace("P0-I"); 
    	assertEquals("", interfacePlace.getFullyQualifiedPrefix()); 
    	InterfacePlace interfacePlace2 = includes.getChildInclude("right-function").getInterfacePlace("P0-I"); 
    	assertEquals(".right-function", interfacePlace2.getFullyQualifiedPrefix()); 
	}
//    @Test
	public void interfacePlaceNamedFollowingItsPlaceInHierarchy() throws Exception {
    	checkInterfaceNames("c",parents,new ipn[]{new ipn("c","c.P0"), new ipn("b","b..c.P0"), new ipn("a","a..c.P0"), new ipn("top","top..c.P0")}); 
	}
    private void checkInterfaceNames(String homeInclude, IncludeHierarchyCommandScopeEnum scopeEnum, ipn... names) throws RecursiveIncludeException, PetriNetComponentNotFoundException {
    	includes = buildTestHierarchy(); 
    	includes.setInterfacePlaceAccessScope(scopeEnum); 
    	includeMap = buildTestMap(); 
    	IncludeHierarchy targetInclude = includeMap.get(homeInclude);  
    	Place p0 = targetInclude.getPetriNet().getComponent("P0", Place.class); 
    	targetInclude.addToInterface(p0); 
    	for (int i = 0; i < names.length; i++) {
			assertEquals(names[i].interfacePlaceName, includeMap.get(names[i].include).getInterfacePlaces().iterator().next().getId()); 
		}
	}
    private class ipn {
    	public String include;
    	public String interfacePlaceName;
    	public ipn(String include, String interfacePlaceName) { this.include = include; this.interfacePlaceName = interfacePlaceName; }
    }
	private Map<String, IncludeHierarchy> buildTestMap() {
		includeMap = new HashMap<String, IncludeHierarchy>(); 
		includeMap.put("top", includes); 
		includeMap.put("a", includes.getChildInclude("a")); 
		includeMap.put("b", includes.getChildInclude("a").getChildInclude("b")); 
		includeMap.put("c", includes.getChildInclude("a").getChildInclude("b").getChildInclude("c")); 
		includeMap.put("d", includes.getChildInclude("a").getChildInclude("b").getChildInclude("c").getChildInclude("d")); 
		includeMap.put("a2", includes.getChildInclude("a2")); 
		includeMap.put("b2", includes.getChildInclude("a2").getChildInclude("b2")); 
		return includeMap;
	}
	private IncludeHierarchy buildTestHierarchy() throws RecursiveIncludeException {
		includes = new IncludeHierarchy(net1, "top"); 
		includes.include(net2, "a").include(net3, "b").include(net4, "c").include(net5, "d"); 
		includes.include(net2, "a2").include(net3, "b2");
		return includes;
	}
	@Test
	public void toAvoidRecursionIncludedPetriNetMustNotHaveSameNameAsItsParent() throws Exception {
    	expectedException.expect(RecursiveIncludeException.class);
    	expectedException.expectMessage(IncludeHierarchy.INCLUDED_NET_MAY_NOT_EXIST_AS_PARENT_IN_HIERARCHY);
    	includes.include(net1, "fred");         
	}
    @Test
    public void toAvoidRecursionIncludedPetriNetMustNotHaveSameNameAsAnyParent() throws Exception {
    	expectedException.expect(RecursiveIncludeException.class);
    	expectedException.expectMessage(IncludeHierarchy.INCLUDED_NET_MAY_NOT_EXIST_AS_PARENT_IN_HIERARCHY);
    	includes.include(net2, "fred").include(net1, "mary");         
    }	
    @Test
	public void commandExecutesAtEachParentLevel() throws Exception {
		IncludeHierarchyCommand<Object> command = new DummyCommand<>(); 
		includes.include(net2, "2nd").include(net3, "3rd"); 
		Result<Object> result = includes.getChildInclude("2nd").getChildInclude("3rd").parents(command); 
		assertEquals(2, result.getEntries().size());
		assertTrue(result.getEntries().get(0).message.endsWith("2")); 
		assertTrue(result.getEntries().get(1).message.endsWith("1")); 
	}
    @Test
    public void commandExecutesAtEachChildLevel() throws Exception {
    	IncludeHierarchyCommand<Object> command = new DummyCommand<>(); 
    	includes.include(net2, "2a").include(net3, "3rd"); 
    	includes.include(net4, "2b"); 
    	Result<Object> result = includes.children(command); 
    	assertEquals(3, result.getEntries().size());
    	assertTrue(result.getEntries().get(0).message.endsWith("2")); 
    	assertTrue(result.getEntries().get(1).message.endsWith("3")); 
    	assertTrue(result.getEntries().get(2).message.endsWith("4")); 
    }
    @Test
    public void commandExecutesForPeersButNotAuntsOrNieces() throws Exception {
    	IncludeHierarchyCommand<Object> command = new DummyCommand<>(); 
    	includes.include(net2, "aunt"); 
    	includes.include(net2, "mom").include(net3, "me"); 
    	includes.getChildInclude("mom").include(net4, "sis"); 
    	includes.getChildInclude("mom").getChildInclude("sis").include(net6, "niece"); 
    	includes.getChildInclude("mom").include(net5, "other-sis"); 
    	Result<Object> result = includes.getChildInclude("mom").getChildInclude("me").siblings(command); 
    	assertEquals(2, result.getEntries().size());
    	assertTrue(result.getEntries().get(0).message.endsWith("4")); 
    	assertTrue(result.getEntries().get(1).message.endsWith("5")); 
    }
    @Test
    public void commandExecutesForAllLevelsInHierarchy() throws Exception {
    	IncludeHierarchyCommand<Object> command = new DummyCommand<>(); 
    	includes.include(net2, "aunt"); 
    	includes.include(net2, "mom").include(net3, "me"); 
    	includes.getChildInclude("mom").include(net4, "sis"); 
    	includes.getChildInclude("mom").getChildInclude("sis").include(net6, "niece"); 
    	includes.getChildInclude("mom").include(net5, "other-sis"); 
    	Result<Object> result = includes.getChildInclude("mom").getChildInclude("me").all(command); 
    	assertEquals(7, result.getEntries().size());
    }
    @Test
	public void hasInterfacePlaceAccessScopeWithParentsCommandScopeAsDefault() throws Exception {
    	assertTrue("default",includes.getInterfacePlaceAccessScope() instanceof ParentsCommandScope); 
    	includes.setInterfacePlaceAccessScope(IncludeHierarchyCommandScopeEnum.PARENTS_AND_SIBLINGS);
    	assertTrue(includes.getInterfacePlaceAccessScope() instanceof ParentsSiblingsCommandScope); 
    	includes.setInterfacePlaceAccessScope(IncludeHierarchyCommandScopeEnum.ALL);
    	assertTrue(includes.getInterfacePlaceAccessScope() instanceof AllCommandScope); 
	}
    @Test
	public void accessScopeCascadesDownward() throws Exception {
    	includes.setInterfacePlaceAccessScope(IncludeHierarchyCommandScopeEnum.PARENTS_AND_SIBLINGS);
    	includes.include(net2, "a").include(net3, "b"); 
    	assertTrue(includes.getChildInclude("a").getChildInclude("b")
    			.getInterfacePlaceAccessScope() instanceof ParentsSiblingsCommandScope); 
	}
	private void buildHierarchyWithInterfacePlaces()
			throws PetriNetComponentNotFoundException, RecursiveIncludeException {
		includes = new IncludeHierarchy(net1, null); 
    	includes.include(net2, "right-function").include(net3,"lowlevel-function"); 
    	Place place = net1.getComponent("P0", Place.class); 
    	Place place2 = net2.getComponent("P0", Place.class); 
    	Place place3 = net3.getComponent("P0", Place.class); 
    	includes.addToInterface(place); 
    	includes.getChildInclude("right-function").addToInterface(place2);
    	includes.getChildInclude("right-function").getChildInclude("lowlevel-function").addToInterface(place3);
	}
    //TODO fullyqualified name renamed when hierarchy is renamed
    //TODO interfacePlace mirrors to EPN and vice versa
    //TODO InterfacePlaces from one include are visible under another include appropriately prefixed
  //TODO verifyTopLevelCanBeRenamedToOrFromBlank
  //TODO verifyDuplicateAliasIsSuffixedToEnsureUniqueness
  //TODO verifyAliasesAreStackedAsImportsAreAdded
  //TODO consider whether different access scopes should be permitted at different levels     
}
