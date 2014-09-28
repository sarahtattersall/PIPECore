package uk.ac.imperial.pipe.models.petrinet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
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
import uk.ac.imperial.pipe.exceptions.IncludeException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;
@RunWith(MockitoJUnitRunner.class)
public class IncludeHierarchyTest extends AbstractMapEntryTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private PropertyChangeListener mockListener;

	private IncludeHierarchy includes;
	private PetriNet net1, net2, net3, net4, net5, net6 ;
	private IncludeHierarchyCommandScopeEnum parents;
	private IncludeHierarchyCommandScopeEnum parentsSibs;
	private IncludeHierarchyCommandScopeEnum all;
	private Place placeTop;
	private Place placeA;
	private Place placeB;

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
		//TODO test or delete
		parentsSibs = IncludeHierarchyCommandScopeEnum.PARENTS_AND_SIBLINGS; 
		all = IncludeHierarchyCommandScopeEnum.ALL; 
	}
    @Test
	public void savesAndReturnsNet() throws Exception {
    	assertEquals(net1, includes.getPetriNet()); 
	}
    @Test
    public void topLevelPetriNetIsNotNull() throws Exception
    {
    	expectedException.expect(IllegalArgumentException.class);
    	includes = new IncludeHierarchy(null, "top");
    }
	@Test
	public void includeHierarchyForRootLevelHasNoParentAndNoChildren() {
		assertNull(includes.getParent()); 
		assertThat(includes.includeMap()).hasSize(0);
	}
	@Test
	public void verifyIncludeHierarchyForFirstIncludedLevel() throws Exception {
		includes.include(net2, "first-child");
		includes.include(net2, "second-child");
		assertThat(includes.includeMap()).hasSize(2); 
		assertThat(includes.getChildInclude("first-child").includeMap()).hasSize(0); 
		assertEquals(includes, includes.getChildInclude("first-child").getParent()); 
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
	public void topNameDefaultsToBlankIfNotProvided() throws Exception {
		includes = new IncludeHierarchy(net1, null); 
		assertEquals("", includes.getName()); 
		includes = new IncludeHierarchy(net1, " "); 
		assertEquals("", includes.getName()); 
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
	public void renameChangesOnlyImmediateParentMap() throws Exception {
	 	IncludeHierarchy aInclude = includes.include(net2, "a"); 
    	IncludeHierarchy bInclude = aInclude.include(net3, "b"); 
    	checkAllIncludesMapEntries(new ME("top", includes), new ME("a", aInclude), new ME("b", bInclude));  
		assertFalse(aInclude.renameBare("c").hasResult()); 
		checkAllIncludesMapEntries(new ME("top", includes), new ME("c", aInclude), new ME("b", bInclude));  
		assertFalse(aInclude.renameBare("a").hasResult()); 
		checkAllIncludesMapEntries("rename back to original name",
				new ME("top", includes), new ME("a", aInclude), new ME("b", bInclude));  
		assertFalse(bInclude.renameBare("d").hasResult()); 
		checkAllIncludesMapEntries(new ME("top", includes), new ME("a", aInclude), new ME("d", bInclude));  
		assertFalse(bInclude.renameBare("a").hasResult()); 
		checkAllIncludesMapEntries("same name ok if at different levels",
				new ME("top", includes), new ME("a", aInclude), new ME("a", bInclude));  
		assertFalse(includes.renameBare("root").hasResult()); 
		checkAllIncludesMapEntries("root level has no parent, but renames ok",
				new ME("root", includes), new ME("a", aInclude), new ME("a", bInclude));  
		assertFalse(includes.renameBare("").hasResult()); 
		checkAllIncludesMapEntries("root level can also be blank",
				new ME("", includes), new ME("a", aInclude), new ME("a", bInclude));  
	}
	@Test
	public void renameReturnsErrorOnlyIfNameConflictsWithAnotherIncludeAtSameLevel() throws Exception {
		IncludeHierarchy aInclude = includes.include(net2, "a"); 
		IncludeHierarchy aaInclude = includes.include(net2, "aa"); 
		IncludeHierarchy aaaInclude = includes.include(net2, "aaa"); 
		IncludeHierarchy bInclude = aaInclude.include(net3, "b"); 
		IncludeHierarchy cInclude = aaaInclude.include(net4, "c"); 
		IncludeHierarchy ccInclude = aaaInclude.include(net4, "cc"); 
		ME[][] originalMap = new ME[][] {new ME[] {new ME("top", includes)}, 
				new ME[] {new ME("a", aInclude), new ME("aa", aaInclude), new ME("aaa", aaaInclude)}, 
				new ME[] {new ME("b", bInclude), new ME("c", cInclude), new ME("cc", ccInclude)}};
		checkAllIncludesMapEntries(false, "", originalMap);
		Result<UpdateResultEnum> result = aInclude.renameBare("aa"); 
		assertTrue("duplicate, so error",result.hasResult()); 
		checkAllIncludesMapEntries(false, "conflict at level 1, so no changes to map", originalMap);  
		result = cInclude.renameBare("cc"); 
		assertTrue(result.hasResult()); 
		checkAllIncludesMapEntries(false, "conflict at level 2, so no changes to map", originalMap);  
		result = cInclude.renameBare("ccc"); 
		assertFalse(result.hasResult()); 
		checkAllIncludesMapEntries(false, "no conflict, so map changes", 
				new ME[] {new ME("top", includes)}, 
				new ME[] {new ME("a", aInclude), new ME("aa", aaInclude), new ME("aaa", aaaInclude)}, 
				new ME[] {new ME("b", bInclude), new ME("ccc", cInclude), new ME("cc", ccInclude)});  
	}
	@Test
	public void renameCascadesFullyQualifiedNameChangesThroughChildrenButNotPeers() throws Exception {
		IncludeHierarchy aInclude = includes.include(net2, "a"); 
		IncludeHierarchy aaInclude = includes.include(net2, "aa"); 
		IncludeHierarchy bInclude = aaInclude.include(net3, "b"); 
		IncludeHierarchy cInclude = aInclude.include(net4, "c"); 
		IncludeHierarchy ccInclude = aInclude.include(net4, "cc"); 
		
	}
    @Test
	public void throwsIfNameDoesNotExistAtChildLevel() throws Exception {
        expectedException.expect(IncludeException.class);
        expectedException.expectMessage(IncludeHierarchy.INCLUDE_ALIAS_NOT_FOUND_AT_LEVEL+"top: fred");
        includes.include(net2, "child");
        includes.getChildInclude("fred"); 
	}
    @Test
    public void throwsIfChildNameIsDuplicate() throws Exception {
    	expectedException.expect(IncludeException.class);
    	expectedException.expectMessage("UpdateMapEntryCommand:  map entry for IncludeHierarchy child not added to IncludeMap in IncludeHierarchy top because another entry already exists with key: child");
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
	public void throwsIfNameDoesNotExistAtAnyLevel() throws Exception {
		expectedException.expect(IncludeException.class);
		expectedException.expectMessage(IncludeHierarchy.INCLUDE_ALIAS_NOT_FOUND_AT_ANY_LEVEL+"fred");
		includes.include(net2, "child");
		includes.getInclude("child").include(net3, "anotherChild");
		includes.getInclude("fred"); 
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
    	assertEquals("top.P0", includes.getInterfacePlace("top.P0").getId()); 
	}
    @Test
	public void homeInterfacePlaceMinimalNameOfHomeIncludeAsPrefix() throws Exception {
    	buildHierarchyWithInterfacePlaces(); 
    	assertEquals("top.P0", includes.getInterfacePlace("top.P0").getId());  
    	assertEquals("a.P0", includes.getChildInclude("a").getInterfacePlace("a.P0").getId());  
    	assertEquals("b.P0", includes.getChildInclude("a").getChildInclude("b").getInterfacePlace("b.P0").getId());  
	}
    @Test
	public void interfacePlacesAreAvailableAsDefinedByAccessScope() throws Exception {
    	buildHierarchyWithInterfacePlaces(); 
    	assertEquals("top..b.P0", includes.getInterfacePlace("top..b.P0").getId());
    	assertEquals(placeB, includes.getInterfacePlace("top..b.P0").getPlace());
    	assertEquals(placeB, includes.getInclude("a").getInterfacePlace("a..b.P0").getPlace());
    	assertEquals(placeB, includes.getInclude("b").getInterfacePlace("b.P0").getPlace());
    	assertEquals("neither self nor parent, so doesn't see the interface place",
    			0, includes.getInclude("c").getInterfacePlaces().size());
	}
    @Test
	public void interfacePlacesArePartOfPlaceCollectionOnceUsed() throws Exception {
		includes = new IncludeHierarchy(net1, "top"); 
		includes.include(net2, "a");  
		assertEquals(2, includes.getPetriNet().getPlaces().size()); 
		assertEquals(2, includes.getInclude("a").getPetriNet().getPlaces().size()); 
		includes.getInclude("a").addToInterface(net2.getComponent("P0", Place.class)); 
		assertTrue(includes.getInterfacePlace("top..a.P0").getStatus() instanceof InterfacePlaceStatusAvailable); 
		assertTrue(includes.getInclude("a").getInterfacePlace("a.P0").getStatus() instanceof InterfacePlaceStatusHome); 
		assertEquals("available status interface places not added to places",2, includes.getPetriNet().getPlaces().size()); 
		assertEquals("home status interface places all not added",2, includes.getInclude("a").getPetriNet().getPlaces().size()); 
		includes.useInterfacePlace("top..a.P0"); 
		assertEquals("added to places in the away petri net",
				3, includes.getPetriNet().getPlaces().size()); 
		assertEquals("no change to home include",
				2, includes.getInclude("a").getPetriNet().getPlaces().size()); 
		assertTrue(includes.getInterfacePlace("top..a.P0").getStatus() instanceof InterfacePlaceStatusInUse); 
		assertEquals("top..a.P0", includes.getPetriNet().getComponent("top..a.P0", Place.class).getId()); 
	}
      
//    @Test  //TODO 
	public void interfacePlaceNamedFollowingItsPlaceInHierarchy() throws Exception {
    	checkInterfaceNames("c",parents,new ipn[]{new ipn("c","c.P0"), new ipn("b","b..c.P0"), new ipn("a","a..c.P0"), new ipn("top","top..c.P0")}); 
	}
    private void checkInterfaceNames(String homeInclude, IncludeHierarchyCommandScopeEnum scopeEnum, ipn... names) throws IncludeException, PetriNetComponentNotFoundException {
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
	@Test
	public void toAvoidRecursionIncludedPetriNetMustNotHaveSameNameAsItsParent() throws Exception {
    	expectedException.expect(IncludeException.class);
    	expectedException.expectMessage(IncludeHierarchy.INCLUDED_NET_MAY_NOT_EXIST_AS_PARENT_IN_HIERARCHY);
    	includes.include(net1, "fred");         
	}
    @Test
    public void toAvoidRecursionIncludedPetriNetMustNotHaveSameNameAsAnyParent() throws Exception {
    	expectedException.expect(IncludeException.class);
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
	private PetriNet createSimpleNet(int i) {
		PetriNet net = 
				APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
				APlace.withId("P1")).and(AnImmediateTransition.withId("T0")).and(
				AnImmediateTransition.withId("T1")).and(
				ANormalArc.withSource("T1").andTarget("P1")).andFinally(
				ANormalArc.withSource("T0").andTarget("P0").with("#(P0)", "Default").token());
		net.setName(new NormalPetriNetName("net"+i));
		return net; 
	}
   private class ipn {
    	public String include;
    	public String interfacePlaceName;
    	public ipn(String include, String interfacePlaceName) { this.include = include; this.interfacePlaceName = interfacePlaceName; }
    }
	private Map<String, IncludeHierarchy> buildTestMap() throws IncludeException {
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
	private IncludeHierarchy buildTestHierarchy() throws IncludeException {
		includes = new IncludeHierarchy(net1, "top"); 
		includes.include(net2, "a").include(net3, "b").include(net4, "c").include(net5, "d"); 
		includes.include(net2, "a2").include(net3, "b2");
		return includes;
	}

	private void buildHierarchyWithInterfacePlaces()
			throws PetriNetComponentNotFoundException, IncludeException {
		includes = new IncludeHierarchy(net1, "top"); 
    	includes.include(net2, "a").include(net3,"b").include(net4, "c"); 
    	includes.include(net2, "aa").include(net3, "bb"); 
    	placeTop = net1.getComponent("P0", Place.class); 
    	placeA = net2.getComponent("P0", Place.class); 
    	placeB = net3.getComponent("P0", Place.class); 
    	includes.addToInterface(placeTop); 
    	includes.getChildInclude("a").addToInterface(placeA);
    	includes.getChildInclude("a").getChildInclude("b").addToInterface(placeB);
	}
  //TODO verifyTopLevelCanBeRenamedToOrFromBlank
  //TODO testAddingToInterfaceMultipleTimes
	//TODO changeAccessScopeAffectsExistingInterfacePlaces...how? 
	//TODO removeInterfacePlaceReturnsResultForEachInUseInterfacePlaces
	//TODO remove include
}
