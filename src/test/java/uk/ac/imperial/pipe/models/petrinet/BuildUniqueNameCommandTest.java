package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.awt.Color;

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
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;

@RunWith(MockitoJUnitRunner.class)
public class BuildUniqueNameCommandTest extends AbstractMapEntryTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private IncludeHierarchy includes;
    private PetriNet net1;
    private PetriNet net2;
    private PetriNet net3;
    @SuppressWarnings("unused")
    private PetriNet net4, net5, net6;

    //	private IncludeHierarchyCommand<Object> uniqueNameCommand;
    private BuildUniqueNameCommand uniqueNameCommand;

    @Before
    public void setUp() throws Exception {
        net1 = createSimpleNet(1);
        net2 = createSimpleNet(2);
        net3 = createSimpleNet(3);
        net4 = createSimpleNet(4);
        net5 = createSimpleNet(5);
        net6 = createSimpleNet(6);
        includes = new IncludeHierarchy(net1, "top");
    }

    public PetriNet createSimpleNet(int i) throws PetriNetComponentException {
        PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0"))
                .and(APlace.withId("P1")).and(AnImmediateTransition.withId("T0"))
                .and(AnImmediateTransition.withId("T1")).and(ANormalArc.withSource("T1").andTarget("P1"))
                .andFinally(ANormalArc.withSource("T0").andTarget("P0").with("#(P0)", "Default").token());
        net.setName(new NormalPetriNetName("net" + i));
        return net;
    }

    @Test
    public void uniqueNamesCreatedAtEachLevel() throws Exception {
        assertEquals("top", includes.getUniqueName());
        checkIncludeMapAllEntries("top level knows itself", includes, new ME("top", includes));

        IncludeHierarchy aInclude = addBareInclude(includes, net2, "a");
        checkIncludeMapAllEntries("top doesnt know 'a' yet", includes, new ME("top", includes));
        checkIncludeMapAllEntries("'a' doesnt know itself yet", aInclude);

        uniqueNameCommand = new BuildUniqueNameCommand();
        Result<IncludeHierarchy> result = aInclude.self(uniqueNameCommand);
        assertFalse(result.hasResult());
        checkIncludeMapAllEntries(includes, new ME("top", includes), new ME("a", aInclude));
        checkIncludeMapAllEntries(aInclude, new ME("a", aInclude));

        IncludeHierarchy bInclude = addBareInclude(aInclude, net3, "b");
        assertFalse(bInclude.self(new BuildUniqueNameCommand()).hasResult());
        checkAllIncludesAllMapEntries(new ME("top", includes), new ME("a", aInclude), new ME("b", bInclude));
    }

    @Test
    public void minimallyUniqueNameForSameNamedChildIsPrefixedWithNamesUpThroughSameNamedParent() throws Exception {
        IncludeHierarchy aInclude = addIncludeAndBuildUniqueName(includes, net2, "a");
        IncludeHierarchy bInclude = addIncludeAndBuildUniqueName(aInclude, net3, "b");
        IncludeHierarchy abaInclude = addIncludeAndBuildUniqueName(bInclude, net4, "a");

        checkAllIncludesAllMapEntries(new ME("top", includes), new ME("a", aInclude), new ME("b",
                bInclude), new ME("a.b.a", abaInclude));

        assertEquals("parent keeps minimal name", includes.getChildInclude("a"), includes.getInclude("a"));
        assertEquals("childs name includes path to parent", includes.getChildInclude("a").getChildInclude("b")
                .getChildInclude("a"), includes.getInclude("a.b.a"));
    }

    @Test
    public void renamesAreReflectedInUniqueNames() throws Exception {
        IncludeHierarchy aInclude = addIncludeAndBuildUniqueName(includes, net2, "a");
        IncludeHierarchy bInclude = addIncludeAndBuildUniqueName(aInclude, net3, "b");
        IncludeHierarchy cInclude = addIncludeAndBuildUniqueName(bInclude, net4, "c");
        IncludeHierarchy dInclude = addIncludeAndBuildUniqueName(cInclude, net5, "d");
        IncludeHierarchy abcdaInclude = addIncludeAndBuildUniqueName(dInclude, net6, "a");

        checkAllIncludesAllMapEntries(new ME("top", includes), new ME("a", aInclude), new ME("b", bInclude), new ME("c",
                cInclude), new ME("d", dInclude), new ME("a.b.c.d.a", abcdaInclude));
        assertEquals(includes.getChildInclude("a").getChildInclude("b").getChildInclude("c")
                .getChildInclude("d").getChildInclude("a"), includes.getInclude("a.b.c.d.a"));
        assertFalse(cInclude.renameBare("a").hasResult());
        checkAllIncludesMapEntries(new ME("top", includes), new ME("a", aInclude), new ME("b", bInclude), new ME("a",
                cInclude), new ME("d", dInclude), new ME("a", abcdaInclude));
        uniqueNameCommand = new BuildUniqueNameCommand();
        cInclude.self(uniqueNameCommand);
        assertFalse(uniqueNameCommand.getResult().hasResult());

        checkAllIncludesAllMapEntries(new ME("top", includes), new ME("a", aInclude), new ME("b",
                bInclude), new ME("a.b.a", cInclude), new ME("d", dInclude), new ME("a.b.a.d.a", abcdaInclude));
        assertEquals("highest level blissfully unaware", includes.getChildInclude("a"), includes.getInclude("a"));
        assertEquals("was c now a.b.a", includes.getChildInclude("a").getChildInclude("b")
                .getChildInclude("a"), includes.getInclude("a.b.a"));
        assertEquals("still references highest level, but reflects intermediate rename", includes.getChildInclude("a")
                .getChildInclude("b").getChildInclude("a").getChildInclude("d")
                .getChildInclude("a"), includes.getInclude("a.b.a.d.a"));

        assertFalse(aInclude.renameBare("x").hasResult());
        uniqueNameCommand = new BuildUniqueNameCommand();
        aInclude.self(uniqueNameCommand);
        assertFalse(uniqueNameCommand.getResult().hasResult());
        checkAllIncludesAllMapEntries(new ME("top", includes), new ME("x", aInclude), new ME("b", bInclude), new ME("a",
                cInclude), new ME("d", dInclude), new ME("a.d.a", abcdaInclude));
        assertEquals(includes.getChildInclude("x"), includes.getInclude("x"));
        assertEquals("mid-level was a.b.a now a because unique", includes.getChildInclude("x").getChildInclude("b")
                .getChildInclude("a"), includes.getInclude("a"));
        assertEquals("unique name shortened to start at mid-level", includes.getChildInclude("x").getChildInclude("b")
                .getChildInclude("a").getChildInclude("d").getChildInclude("a"), includes.getInclude("a.d.a"));
    }

    @Test
    public void uniqueNameForLowerLevelIncludeWhereDuplicateIsNotAParentWillBeFullyQualifiedName() throws Exception {
        IncludeHierarchy bInclude = addIncludeAndBuildUniqueName(includes, net2, "b");
        IncludeHierarchy aInclude = addIncludeAndBuildUniqueName(includes, net2, "a");
        IncludeHierarchy abInclude = addIncludeAndBuildUniqueName(aInclude, net3, "b");
        assertEquals("minimially unique name for lower level include that is not child will just be fully qualified name", includes
                .getChildInclude("a").getChildInclude("b"), includes.getInclude("top.a.b"));
        checkIncludeMapAllEntries(includes, new ME("top", includes), new ME("a", aInclude), new ME("b",
                bInclude), new ME("top.a.b", abInclude));
        checkIncludeMapAllEntries(bInclude, new ME("b", bInclude));
        checkIncludeMapAllEntries(aInclude, new ME("a", aInclude), new ME("top.a.b", abInclude));
        checkIncludeMapAllEntries(abInclude, new ME("top.a.b", abInclude));
        IncludeHierarchy cInclude = addIncludeAndBuildUniqueName(includes, net2, "c");
        IncludeHierarchy cdInclude = addIncludeAndBuildUniqueName(cInclude, net3, "d");
        checkIncludeMapAllEntries(includes, new ME("top", includes), new ME("a", aInclude), new ME("b",
                bInclude), new ME("top.a.b", abInclude), new ME("c", cInclude), new ME("d", cdInclude));
        IncludeHierarchy dInclude = addIncludeAndBuildUniqueName(includes, net2, "d");
        assertEquals("same result if nets added in the opposite order", includes.getChildInclude("c")
                .getChildInclude("d"), includes.getInclude("top.c.d"));
        checkIncludeMapAllEntries(includes, new ME("top", includes), new ME("a", aInclude), new ME("b",
                bInclude), new ME("top.a.b",
                        abInclude), new ME("c", cInclude), new ME("top.c.d", cdInclude), new ME("d", dInclude));

        checkIncludeMapAllEntries(cInclude, new ME("c", cInclude), new ME("top.c.d", cdInclude));
        checkIncludeMapAllEntries(dInclude, new ME("d", dInclude));
    }

    @Test
    public void renameToUnclesNameGeneratesFullyQualifiedNameForNephew() throws Exception {
        IncludeHierarchy bInclude = addIncludeAndBuildUniqueName(includes, net2, "b");
        IncludeHierarchy aInclude = addIncludeAndBuildUniqueName(includes, net2, "a");
        IncludeHierarchy cInclude = addIncludeAndBuildUniqueName(aInclude, net3, "c");
        checkIncludeMapAllEntries(includes, new ME("top", includes), new ME("a", aInclude), new ME("b",
                bInclude), new ME("c", cInclude));
        checkIncludeMapAllEntries(aInclude, new ME("a", aInclude), new ME("c", cInclude));
        checkIncludeMapAllEntries(bInclude, new ME("b", bInclude));
        checkIncludeMapAllEntries(cInclude, new ME("c", cInclude));
        assertFalse(cInclude.renameBare("b").hasResult());
        cInclude.buildFullyQualifiedName();
        uniqueNameCommand = new BuildUniqueNameCommand();
        cInclude.self(uniqueNameCommand);
        checkIncludeMapAllEntries(includes, new ME("top", includes), new ME("a", aInclude), new ME("b",
                bInclude), new ME("top.a.b", cInclude));
        checkIncludeMapAllEntries(aInclude, new ME("a", aInclude), new ME("top.a.b", cInclude));
        checkIncludeMapAllEntries("uncle unaffected", bInclude, new ME("b", bInclude));
        checkIncludeMapAllEntries("nephew's unique name is fully qualified name", cInclude, new ME("top.a.b",
                cInclude));
    }

    @Test
    public void renameBackToUnusedNameFromFullyQualifiedNameReturnsToMinimalUniqueName() throws Exception {
        IncludeHierarchy bInclude = addIncludeAndBuildUniqueName(includes, net2, "b");
        IncludeHierarchy aInclude = addIncludeAndBuildUniqueName(includes, net2, "a");
        IncludeHierarchy topabInclude = addIncludeAndBuildUniqueName(aInclude, net3, "b");
        checkIncludeMapAllEntries(includes, new ME("top", includes), new ME("a", aInclude), new ME("b",
                bInclude), new ME("top.a.b", topabInclude));
        checkIncludeMapAllEntries(aInclude, new ME("a", aInclude), new ME("top.a.b", topabInclude));
        checkIncludeMapAllEntries(bInclude, new ME("b", bInclude));
        checkIncludeMapAllEntries(topabInclude, new ME("top.a.b", topabInclude));
        assertFalse(topabInclude.renameBare("c").hasResult());
        //    	topabInclude.buildFullyQualifiedName(); // not strictly needed for this case, since we're moving away from it.  
        uniqueNameCommand = new BuildUniqueNameCommand();
        topabInclude.self(uniqueNameCommand);

        checkIncludeMapAllEntries(includes, new ME("top", includes), new ME("a", aInclude), new ME("b",
                bInclude), new ME("c", topabInclude));
        checkIncludeMapAllEntries(aInclude, new ME("a", aInclude), new ME("c", topabInclude));
        checkIncludeMapAllEntries("uncled still unaffected", bInclude, new ME("b", bInclude));
        checkIncludeMapAllEntries("nephew drops fully qualified name, back to c", topabInclude, new ME("c",
                topabInclude));
    }

    @Test
    public void renameToNephewsNameForcesNephewToSwitchToFullyQualifiedNames() throws Exception {
        IncludeHierarchy bInclude = addIncludeAndBuildUniqueName(includes, net2, "b");
        IncludeHierarchy aInclude = addIncludeAndBuildUniqueName(includes, net2, "a");
        IncludeHierarchy cInclude = addIncludeAndBuildUniqueName(aInclude, net3, "c");
        checkIncludeMapAllEntries(includes, new ME("top", includes), new ME("a", aInclude), new ME("b",
                bInclude), new ME("c", cInclude));
        checkIncludeMapAllEntries(aInclude, new ME("a", aInclude), new ME("c", cInclude));
        checkIncludeMapAllEntries(bInclude, new ME("b", bInclude));
        checkIncludeMapAllEntries(cInclude, new ME("c", cInclude));
        assertFalse(bInclude.renameBare("c").hasResult());
        uniqueNameCommand = new BuildUniqueNameCommand();
        bInclude.self(uniqueNameCommand);
        checkIncludeMapAllEntries(includes, new ME("top", includes), new ME("a", aInclude), new ME("c",
                bInclude), new ME("top.a.c", cInclude));
        checkIncludeMapAllEntries(aInclude, new ME("a", aInclude), new ME("top.a.c", cInclude));
        checkIncludeMapAllEntries(bInclude, new ME("c", bInclude));
        checkIncludeMapAllEntries(cInclude, new ME("top.a.c", cInclude));
    }

    @Test
    public void renameOfRootLevelToConflictingNameCausesLowerLevelToUseFullyQualifiedName() throws Exception {
        IncludeHierarchy aInclude = addIncludeAndBuildUniqueName(includes, net2, "a");
        IncludeHierarchy bInclude = addIncludeAndBuildUniqueName(aInclude, net3, "b");
        checkIncludeMapAllEntries(includes, new ME("top", includes), new ME("a", aInclude), new ME("b", bInclude));
        checkIncludeMapAllEntries(aInclude, new ME("a", aInclude), new ME("b", bInclude));
        checkIncludeMapAllEntries(bInclude, new ME("b", bInclude));

        assertFalse(includes.renameBare("b").hasResult());
        uniqueNameCommand = new BuildUniqueNameCommand();
        includes.self(uniqueNameCommand);
        checkIncludeMapAllEntries(includes, new ME("b", includes), new ME("a", aInclude), new ME("b.a.b", bInclude));
        checkIncludeMapAllEntries(aInclude, new ME("a", aInclude), new ME("b.a.b", bInclude));
        checkIncludeMapAllEntries(bInclude, new ME("b.a.b", bInclude));
    }

    private IncludeHierarchy addBareInclude(IncludeHierarchy parentInclude, PetriNet net,
            String alias) throws IncludeException {
        IncludeHierarchy newInclude = new IncludeHierarchy(net, parentInclude, alias);
        Result<UpdateResultEnum> result = parentInclude
                .self(new UpdateMapEntryCommand<IncludeHierarchy>(IncludeHierarchyMapEnum.INCLUDE, alias, newInclude));
        if (result.hasResult())
            throw new IncludeException(result.getMessage());
        return newInclude;
    }

    //TODO reconcile with IncludeHierarchy
    protected IncludeHierarchy addIncludeAndBuildUniqueName(IncludeHierarchy parentInclude, PetriNet net, String name)
            throws IncludeException {
        IncludeHierarchy include = addBareInclude(parentInclude, net, name);
        include.buildFullyQualifiedName();
        assertFalse(include.self(new BuildUniqueNameCommand()).hasResult());
        return include;
    }
}
