package uk.ac.imperial.pipe.models.petrinet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.exceptions.IncludeException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;

@RunWith(MockitoJUnitRunner.class)
public class IncludeHierarchyTest extends AbstractMapEntryTest {

    //    @Mock
    //    private PropertyChangeListener listener;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private IncludeHierarchy includes;
    private PetriNet net1, net2, net3, net4, net5, net6;
    private IncludeHierarchyCommandScopeEnum parents;
    private IncludeHierarchyCommandScopeEnum parentsSibs;
    private IncludeHierarchyCommandScopeEnum all;
    private IncludeHierarchyCommandScopeEnum parent;
    private Place placeTop;
    private Place placeA;
    private Place placeB;
    private boolean called;

    private Map<String, IncludeHierarchy> includeMapAll;

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
        parent = IncludeHierarchyCommandScopeEnum.PARENT;
        called = false;
    }

    @Test
    public void savesAndReturnsNet() throws Exception {
        assertEquals(net1, includes.getPetriNet());
    }

    @Test
    public void topLevelPetriNetIsNotNull() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        includes = new IncludeHierarchy(null, "top");
    }

    @Test
    public void includeHierarchyForRootLevelHasNoParentAndNoChildren() {
        assertNull(includes.getParent());
        assertThat(includes.includeMap()).hasSize(0);
    }

    @Test
    public void verifyRootLevel() throws Exception {
        assertEquals("top", includes.getName());
        assertEquals("top", includes.getFullyQualifiedName());
        assertEquals("top.", includes.getFullyQualifiedNameAsPrefix());
        assertEquals("top", includes.getUniqueName());
        assertEquals("top.", includes.getUniqueNameAsPrefix());
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
        assertEquals(0, includes.getLevelRelativeToRoot());
        assertEquals(1, includes.getChildInclude("b").getLevelRelativeToRoot());
        assertEquals(1, includes.getChildInclude("bb").getLevelRelativeToRoot());
        assertEquals(2, includes.getChildInclude("b").getChildInclude("c").getLevelRelativeToRoot());
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
        assertEquals("top", includes.getName());
        assertEquals("first-child", includes.getChildInclude("first-child").getName());
        assertEquals("grand-child", includes.getChildInclude("first-child").getChildInclude("grand-child").getName());
        assertEquals("top", includes.getFullyQualifiedName());
        assertEquals("top.first-child", includes.getChildInclude("first-child").getFullyQualifiedName());
        assertEquals("top.first-child.grand-child", includes.getChildInclude("first-child")
                .getChildInclude("grand-child").getFullyQualifiedName());
    }

    @Test
    public void renameChangesOnlyImmediateParentMap() throws Exception {
        IncludeHierarchy aInclude = includes.include(net2, "a");
        IncludeHierarchy bInclude = aInclude.include(net3, "b");
        checkAllIncludesMapEntries(new ME("top", includes), new ME("a", aInclude), new ME("b", bInclude));
        assertFalse(aInclude.renameBare("c").hasResult());
        checkAllIncludesMapEntries(new ME("top", includes), new ME("c", aInclude), new ME("b", bInclude));
        assertFalse(aInclude.renameBare("a").hasResult());
        checkAllIncludesMapEntries("rename back to original name", new ME("top", includes), new ME("a",
                aInclude), new ME("b", bInclude));
        assertFalse(bInclude.renameBare("d").hasResult());
        checkAllIncludesMapEntries(new ME("top", includes), new ME("a", aInclude), new ME("d", bInclude));
        assertFalse(bInclude.renameBare("a").hasResult());
        checkAllIncludesMapEntries("same name ok if at different levels", new ME("top", includes), new ME("a",
                aInclude), new ME("a", bInclude));
        assertFalse(includes.renameBare("root").hasResult());
        checkAllIncludesMapEntries("root level has no parent, but renames ok", new ME("root", includes), new ME("a",
                aInclude), new ME("a", bInclude));
        assertFalse(includes.renameBare("").hasResult());
        checkAllIncludesMapEntries("root level can also be blank", new ME("", includes), new ME("a",
                aInclude), new ME("a", bInclude));
    }

    @Test
    public void renameReturnsErrorOnlyIfNameConflictsWithAnotherIncludeAtSameLevel() throws Exception {
        IncludeHierarchy aInclude = includes.include(net2, "a");
        IncludeHierarchy aaInclude = includes.include(net2, "aa");
        IncludeHierarchy aaaInclude = includes.include(net2, "aaa");
        IncludeHierarchy bInclude = aaInclude.include(net3, "b");
        IncludeHierarchy cInclude = aaaInclude.include(net4, "c");
        IncludeHierarchy ccInclude = aaaInclude.include(net4, "cc");
        ME[][] originalMap = new ME[][] { new ME[] { new ME("top", includes) },
                new ME[] { new ME("a", aInclude), new ME("aa", aaInclude), new ME("aaa", aaaInclude) },
                new ME[] { new ME("b", bInclude), new ME("c", cInclude), new ME("cc", ccInclude) } };
        checkAllIncludesMapEntries(false, "", originalMap);
        Result<UpdateResultEnum> result = aInclude.renameBare("aa");
        assertTrue("duplicate, so error", result.hasResult());
        checkAllIncludesMapEntries(false, "conflict at level 1, so no changes to map", originalMap);
        result = cInclude.renameBare("cc");
        assertTrue(result.hasResult());
        checkAllIncludesMapEntries(false, "conflict at level 2, so no changes to map", originalMap);
        result = cInclude.renameBare("ccc");
        assertFalse(result.hasResult());
        checkAllIncludesMapEntries(false, "no conflict, so map changes", new ME[] {
                new ME("top", includes) }, new ME[] { new ME("a", aInclude), new ME("aa", aaInclude),
                        new ME("aaa", aaaInclude) }, new ME[] { new ME("b", bInclude), new ME("ccc", cInclude),
                                new ME("cc", ccInclude) });
    }

    @Test
    public void throwsIfRenameEncountersError() throws Exception {
        IncludeHierarchy aInclude = includes.include(net2, "a");
        @SuppressWarnings("unused")
        IncludeHierarchy aaInclude = includes.include(net2, "aa");
        verifyRenameThrowsIncludeException("duplicate", aInclude, "aa");
        verifyRenameThrowsIncludeException("only root can be blank", aInclude, "");
        verifyRenameThrowsIncludeException("alias must be non-null", aInclude, null);
    }

    protected void verifyRenameThrowsIncludeException(String comment, IncludeHierarchy include, String newname) {
        try {
            include.rename(newname);
            fail("should throw: " + comment);
        } catch (IncludeException e) {
        }
    }

    @Test
    public void renameCascadesFullyQualifiedNameChangesThroughChildrenButNotPeers() throws Exception {
        IncludeHierarchy aInclude = includes.include(net2, "a");
        IncludeHierarchy aaInclude = includes.include(net2, "aa");
        IncludeHierarchy bInclude = aaInclude.include(net3, "b");
        IncludeHierarchy cInclude = aInclude.include(net4, "c");
        IncludeHierarchy ccInclude = aInclude.include(net4, "cc");
        checkFullyQualifiedNameEntries(false, "initial names", new FQNME[] {
                new FQNME("top", includes, "top") }, new FQNME[] { new FQNME("a", aInclude, "top.a"),
                        new FQNME("aa", aaInclude, "top.aa") }, new FQNME[] { new FQNME("b", bInclude, "top.aa.b"),
                                new FQNME("c", cInclude, "top.a.c"), new FQNME("cc", ccInclude, "top.a.cc") });
        aInclude.rename("d");
        checkFullyQualifiedNameEntries(false, "rename affects the 'a' hierarchy only", new FQNME[] {
                new FQNME("top", includes, "top") }, new FQNME[] { new FQNME("d", aInclude, "top.d"),
                        new FQNME("aa", aaInclude, "top.aa") }, new FQNME[] { new FQNME("b", bInclude, "top.aa.b"),
                                new FQNME("c", cInclude, "top.d.c"), new FQNME("cc", ccInclude, "top.d.cc") });
        cInclude.rename("ccc");
        checkFullyQualifiedNameEntries(false, "bottom only renames self", new FQNME[] {
                new FQNME("top", includes, "top") }, new FQNME[] { new FQNME("d", aInclude, "top.d"),
                        new FQNME("aa", aaInclude, "top.aa") }, new FQNME[] { new FQNME("b", bInclude, "top.aa.b"),
                                new FQNME("ccc", cInclude, "top.d.ccc"), new FQNME("cc", ccInclude, "top.d.cc") });
        aInclude.rename("a");
        checkFullyQualifiedNameEntries(false, "rename back", new FQNME[] {
                new FQNME("top", includes, "top") }, new FQNME[] { new FQNME("a", aInclude, "top.a"),
                        new FQNME("aa", aaInclude, "top.aa") }, new FQNME[] { new FQNME("b", bInclude, "top.aa.b"),
                                new FQNME("ccc", cInclude, "top.a.ccc"), new FQNME("cc", ccInclude, "top.a.cc") });
        includes.rename("root");
        checkFullyQualifiedNameEntries(false, "rename top level", new FQNME[] {
                new FQNME("root", includes, "root") }, new FQNME[] { new FQNME("a", aInclude, "root.a"),
                        new FQNME("aa", aaInclude, "root.aa") }, new FQNME[] { new FQNME("b", bInclude, "root.aa.b"),
                                new FQNME("ccc", cInclude, "root.a.ccc"), new FQNME("cc", ccInclude, "root.a.cc") });
        includes.rename("");
        checkFullyQualifiedNameEntries(false, "top level is blank", new FQNME[] {
                new FQNME("", includes, "") }, new FQNME[] { new FQNME("a", aInclude, ".a"),
                        new FQNME("aa", aaInclude, ".aa") }, new FQNME[] { new FQNME("b", bInclude, ".aa.b"),
                                new FQNME("ccc", cInclude, ".a.ccc"), new FQNME("cc", ccInclude, ".a.cc") });
    }

    @Test
    public void throwsIfNameDoesNotExistAtChildLevel() throws Exception {
        expectedException.expect(IncludeException.class);
        expectedException.expectMessage(IncludeHierarchy.INCLUDE_NAME_NOT_FOUND_AT_LEVEL + "top: fred");
        includes.include(net2, "child");
        includes.getChildInclude("fred");
    }

    @Test
    public void throwsIfChildNameIsDuplicate() throws Exception {
        expectedException.expect(IncludeException.class);
        expectedException
                .expectMessage("UpdateMapEntryCommand:  map entry not added to IncludeMap in IncludeHierarchy top because another entry already exists with key: child");
        includes.include(net2, "child");
        includes.include(net2, "child");
    }

    @Test
    public void throwsIfChildNameIsBlankOrNull() throws Exception {
        verifyIncludeThrowsIncludeException(net2, " ");
        verifyIncludeThrowsIncludeException(net2, null);
    }

    protected void verifyIncludeThrowsIncludeException(PetriNet petriNet, String alias)
            throws IncludeException {
        try {
            includes.include(petriNet, alias);
            fail("should throw");
        } catch (IncludeException e) {
            assertEquals(IncludeHierarchy.INCLUDE_NAME_MAY_NOT_BE_BLANK_OR_NULL, e.getMessage());
        }
    }

    @Test
    public void sameAliasMayAppearAtDifferentLevels() throws Exception {
        includes.include(net2, "child").include(net3, "child");
        assertEquals("top.child.child", includes.getChildInclude("child").getChildInclude("child")
                .getFullyQualifiedName());
    }

    @Test
    public void netMayNotBeNullForConstructor() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(IncludeHierarchy.INCLUDE_HIERARCHY_PETRI_NET_MAY_NOT_BE_NULL);
        includes = new IncludeHierarchy(null, "fred");
    }

    @Test
    public void netMayNotBeNullForImplicitCreation() throws Exception {
        expectedException.expect(IncludeException.class);
        expectedException.expectMessage(IncludeHierarchy.INCLUDE_HIERARCHY_PETRI_NET_MAY_NOT_BE_NULL);
        includes.include(null, "child");
    }

    @Test
    public void throwsIfNameDoesNotExistAtAnyLevel() throws Exception {
        expectedException.expect(IncludeException.class);
        expectedException.expectMessage(IncludeHierarchy.INCLUDE_NAME_NOT_FOUND_AT_ANY_LEVEL + "fred");
        includes.include(net2, "child");
        includes.getInclude("child").include(net3, "anotherChild");
        includes.getInclude("fred");
    }

    @Test
    public void aNetCanBeIncludedMultipleTimesUnderDifferentAliases() throws Exception {
        includes.include(net2, "left-function");
        includes.include(net2, "right-function");
        assertEquals(includes.getChildInclude("left-function").getPetriNet(), includes.getChildInclude("right-function")
                .getPetriNet());
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
        assertEquals("parent keeps minimal name", includes.getChildInclude("a"), includes.getInclude("a"));
        assertEquals("childs name includes path to parent", includes.getChildInclude("a").getChildInclude("b")
                .getChildInclude("a"), includes.getInclude("a.b.a"));
    }

    @Test
    public void childKnowsIfIncludeIsParent() throws Exception {
        includes.include(net2, "a").include(net3, "b");
        assertTrue(includes.getInclude("b").hasParent(includes.getInclude("a")));
        assertFalse(includes.getInclude("a").hasParent(includes.getInclude("b")));
        includes.include(net2, "aa");
        assertFalse("but a and aa are only siblings", includes.getInclude("a").hasParent(includes.getInclude("aa")));
        assertFalse(includes.getInclude("aa").hasParent(includes.getInclude("a")));
    }

    @Test
    public void throwsIfPlaceIsNotPartOfNetOfIncludeHierarchyWhenAddedToInterface() throws Exception {
        expectedException.expect(IncludeException.class);
        expectedException
                .expectMessage("IncludeHierarchy.addToInterface: place P99 does not exist in the PetriNet of IncludeHierarchy top");
        Place place = new DiscretePlace("P99");
        includes.addToInterface(place, false, false, false, false);
    }

    @Test
    public void placeAddedToInterfaceChangesStatusFromNormalToInterface() throws Exception {
        Place place = net1.getComponent("P0", Place.class);
        assertTrue(place.getStatus() instanceof PlaceStatusNormal);
        includes.addToInterface(place, false, false, false, false);
        assertTrue(place.getStatus() instanceof PlaceStatusInterface);
    }

    @Test
    public void placeAddedToInterfaceHasMergeInterfaceStatusHomeWithOriginalPlace() throws Exception {
        Place place = net1.getComponent("P0", Place.class);
        includes.addToInterface(place, true, false, false, false);
        assertTrue(place.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusHome);
        assertEquals(place, place.getStatus().getMergeInterfaceStatus().getHomePlace());
    }

    @Test
    public void placeAddedToInterfaceWithMergeStatusAndExternalIsAccessibleInHomeNetButNotAwayNetUnlessOverridden()
            throws Exception {
        includes.include(net2, "a");
        IncludeHierarchy include2 = includes.getInclude("a");
        Place place = net2.getComponent("P0", Place.class);
        include2.addToInterface(place, true, true, false, false);
        Place topPlace = includes.getInterfacePlace("a.P0");
        includes.addAvailablePlaceToPetriNet(topPlace);
        assertFalse(topPlace.getStatus().isExternal());
        //TODO consider whether change indicator should be part of the API
        assertTrue("indicates that status has changed from original home place", ((PlaceStatusInterface) topPlace
                .getStatus()).hasExternalChanged());
        assertTrue(place.getStatus().isExternal());
        topPlace.getStatus().setExternal(true);
        assertTrue(topPlace.getStatus().isExternal());
    }

    @Test
    public void placeAddedToInterfaceWithMergeStatusHasArcConstraintInAwayNetButNotHomeNet() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Place has an inputOnly ArcConstraint, and will only accept InboundArcs: a.P0");

        includes.include(net2, "a");
        IncludeHierarchy include2 = includes.getInclude("a");
        Place place = net2.getComponent("P0", Place.class);
        Transition transition = net2.getComponent("T0", Transition.class);
        include2.addToInterface(place, true, false, true, false);
        OutboundArc arc = new OutboundNormalArc(transition, place, new HashMap<String, String>());
        assertEquals("P0", arc.getTarget().getId());
        Place topPlace = includes.getInterfacePlace("a.P0");
        includes.addAvailablePlaceToPetriNet(topPlace);
        assertTrue(topPlace.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAway);
        @SuppressWarnings("unused")
        OutboundArc arcFail = new OutboundNormalArc(transition, topPlace, new HashMap<String, String>());
    }

    @Test
    public void placeAddedToInterfacePlaceCollection() throws Exception {
        Place place = net1.getComponent("P0", Place.class);
        includes.addToInterface(place, false, false, false, false);
        assertEquals(1, includes.getInterfacePlaceMap().size());
    }

    @Test
    public void availableInterfacePlaceAddedToPetriNet() throws Exception {
        includes.include(net2, "a");
        IncludeHierarchy include2 = includes.getInclude("a");
        Place place = net2.getComponent("P0", Place.class);
        include2.addToInterface(place, true, false, false, false);
        Place topPlace = includes.getInterfacePlace("a.P0");
        assertTrue(topPlace.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAvailable);
        includes.addAvailablePlaceToPetriNet(topPlace);
        assertTrue(topPlace.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAway);
        assertEquals("a.P0", topPlace.getId());
    }

    @Test
    public void throwsIfInterfacePlaceBeingAddedToPetriNetIsNotAvailable() throws Exception {
        includes.include(net2, "a");
        IncludeHierarchy include2 = includes.getInclude("a");
        Place place = net2.getComponent("P0", Place.class);
        include2.addToInterface(place, true, false, false, false);
        Place topPlace = includes.getInterfacePlace("a.P0");
        checkExceptionCantAddBecauseAlreadyPresent("Home place already in net", include2, place);
        includes.addAvailablePlaceToPetriNet(topPlace);
        checkExceptionCantAddBecauseAlreadyPresent("First add works but second doesn't; Away place previously added", includes, topPlace);
    }

    protected void checkExceptionCantAddBecauseAlreadyPresent(String comment, IncludeHierarchy include, Place place) {
        try {
            include.addAvailablePlaceToPetriNet(place);
            fail("should throw");
        } catch (IncludeException e) {
            assertEquals(comment, "IncludeHierarchy.addAvailablePlaceToPetriNet: place " + place.getId() +
                    " cannot be added to Petri net " +
                    include.getPetriNet().getNameValue() + " because it is already present.", e.getMessage());
        }
    }

    @Test
    public void throwsIfAddedToInterfaceMoreThanOnce() throws Exception {
        expectedException.expect(IncludeException.class);
        expectedException
                .expectMessage("IncludeHierarchy.addToInterface: place P0 may not be added more than once to IncludeHierarchy top");
        Place place = net1.getComponent("P0", Place.class);
        includes.addToInterface(place, false, false, false, false);
        includes.addToInterface(place, false, false, false, false);
    }

    @Test
    public void interfacePlaceCreatedFollowingItsPlaceInHierarchyAndScope() throws Exception {
        checkInterfaceNames("no children or aunts", "c", parents, new String[] { "d", "a2", "b2" }, new IPN[] {
                new IPN("c", "P0"), new IPN("b", "c.P0"), new IPN("a", "c.P0"), new IPN("top", "c.P0") });
        checkInterfaceNames("no sibs, so same set", "c", parentsSibs, new String[] { "d", "a2", "b2" }, new IPN[] {
                new IPN("c", "P0"), new IPN("b", "c.P0"), new IPN("a", "c.P0"), new IPN("top", "c.P0") });
        checkInterfaceNames("no children, cousins don't count", "b", parentsSibs, new String[] { "c", "d", "a2",
                "b2" }, new IPN[] { new IPN("b", "P0"), new IPN("a", "b.P0"), new IPN("top", "b.P0") });
        checkInterfaceNames("no children or nieces", "a", parentsSibs, new String[] { "b", "c", "d", "b2" }, new IPN[] {
                new IPN("a", "P0"), new IPN("top", "a.P0"), new IPN("a2", "a.P0") });
        checkInterfaceNames("ayso rules--everybody plays", "b", all, new String[] {}, new IPN[] { new IPN("b", "P0"),
                new IPN("top", "b.P0"), new IPN("a", "b.P0"), new IPN("c", "b.P0"),
                new IPN("d", "b.P0"), new IPN("a2", "b.P0"), new IPN("b2", "b.P0") });
        checkInterfaceNames("just mom; is there really a use case for this?", "c", parent, new String[] { "top", "a",
                "d", "a2", "b2" }, new IPN[] { new IPN("c", "P0"), new IPN("b", "c.P0") });
    }

    private void checkInterfaceNames(String comment, String homeInclude, IncludeHierarchyCommandScopeEnum scopeEnum,
            String[] noInterfacePlaces, IPN... names) throws IncludeException, PetriNetComponentNotFoundException {
        includes = new IncludeHierarchy(net1, "top");
        includes.setInterfacePlaceAccessScope(scopeEnum);
        includes = buildTestHierarchy();
        includeMapAll = includes.getIncludeMapAll();
        IncludeHierarchy targetInclude = includeMapAll.get(homeInclude);
        Place p0 = targetInclude.getPetriNet().getComponent("P0", Place.class);
        targetInclude.addToInterface(p0, true, false, false, false);
        IncludeHierarchy include = null;
        for (int i = 0; i < names.length; i++) {
            include = includeMapAll.get(names[i].include);
            Iterator<Place> it = include.getInterfacePlaces().iterator();
            Place interfacePlace = (it.hasNext()) ? it.next() : null;
            if (interfacePlace == null)
                fail("No interface places for include " + names[i].include);
            assertEquals(comment, names[i].interfacePlaceName, interfacePlace.getId());
        }
        for (int i = 0; i < noInterfacePlaces.length; i++) {
            assertEquals(comment, 0, includeMapAll.get(noInterfacePlaces[i]).getInterfacePlaces().size());
        }
    }

    private IncludeHierarchy buildTestHierarchy() throws IncludeException {
        includes.include(net2, "a").include(net3, "b").include(net4, "c").include(net5, "d");
        includes.include(net2, "a2").include(net3, "b2");
        return includes;
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
        includes.getChildInclude("mom").include(net4, "4sis");
        includes.getChildInclude("mom").getChildInclude("4sis").include(net6, "niece");
        includes.getChildInclude("mom").include(net5, "5other-sis");
        Result<Object> result = includes.getChildInclude("mom").getChildInclude("me").siblings(command);
        assertEquals(2, result.getEntries().size());
        //    	System.out.println("commandExecutesForPeersButNotAuntsOrNieces"+result.getEntries().get(0).message);  
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
        assertTrue("default", includes.getInterfacePlaceAccessScope() instanceof ParentsCommandScope);
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

    //TODO implement equals and make consistent with this
    @Test
    public void twoIncludesSortByNameAlphabeticallyAsRequiredByIncludeIterator() throws Exception {
        includes = new IncludeHierarchy(net1, "top");
        IncludeHierarchy includeb = new IncludeHierarchy(net1, "bottom");
        assertTrue(includes.compareTo(includeb) > 0);
        assertTrue(includeb.compareTo(includes) < 0);
        assertTrue(includeb.compareTo(includeb) == 0);

    }

    @Test
    public void notifiesListenersUponStructureChange() throws Exception {
        PropertyChangeListener listener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                assertEquals(IncludeHierarchy.INCLUDE_HIERARCHY_STRUCTURE_CHANGE, evt.getPropertyName());
                IncludeHierarchy newIncludes = (IncludeHierarchy) evt.getNewValue();
                assertEquals(includes, newIncludes);
                try {
                    assertEquals("include2", newIncludes.getChildInclude("include2").getName());
                } catch (IncludeException e) {
                    e.printStackTrace();
                }
                called = true;
            }
        };
        includes.addPropertyChangeListener(listener);
        includes.include(net2, "include2");
        assertTrue("expected propertyChange to be called", called);
    }

    private PetriNet createSimpleNet(int i) throws PetriNetComponentException {
        PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0"))
                .and(APlace.withId("P1")).and(AnImmediateTransition.withId("T0"))
                .and(AnImmediateTransition.withId("T1")).and(ANormalArc.withSource("T1").andTarget("P1"))
                .andFinally(ANormalArc.withSource("T0").andTarget("P0").with("#(P0)", "Default").token());
        net.setName(new NormalPetriNetName("net" + i));
        return net;
    }

    private class IPN {
        public String include;
        public String interfacePlaceName;

        public IPN(String include, String interfacePlaceName) {
            this.include = include;
            this.interfacePlaceName = interfacePlaceName;
        }
    }

    //TODO testAddingToInterfaceMultipleTimes
    //TODO changeAccessScopeAffectsExistingInterfacePlaces...how? 
    //TODO removeInterfacePlaceReturnsResultForEachInUseInterfacePlaces
    //TODO remove include
}
