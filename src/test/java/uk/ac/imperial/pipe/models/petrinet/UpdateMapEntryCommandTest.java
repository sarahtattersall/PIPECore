package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class UpdateMapEntryCommandTest extends AbstractMapEntryTest {

    private PetriNet net;
    private IncludeHierarchy includes;
    private IncludeHierarchy childInclude;
    private IncludeHierarchyCommand<UpdateResultEnum> updateCommand;
    private IncludeHierarchy fredInclude;

    @Before
    public void setUp() throws Exception {
        net = new PetriNet();
        includes = new IncludeHierarchy(net, "top");
        childInclude = new IncludeHierarchy(net, "a");
        fredInclude = new IncludeHierarchy(net, "a");
    }

    @Test
    public void resolvesToCorrectMap() throws Exception {
        assertEquals(includes.getMap(IncludeHierarchyMapEnum.INCLUDE), includes.getIncludeMap());
        assertEquals(includes.getMap(IncludeHierarchyMapEnum.INCLUDE_ALL), includes.getIncludeMapAll());
        //		assertEquals(IncludeHierarchyMapEnum.INCLUDE.getMap(includes), includes.getIncludeMap()); 
        //		assertEquals(IncludeHierarchyMapEnum.INCLUDE_ALL.getMap(includes), includes.getIncludeMapAll()); 
    }

    @Test
    public void addsNewEntry() throws Exception {
        assertEquals(0, includes.getIncludeMap().size());
        IncludeHierarchyCommand<UpdateResultEnum> addCommand = new UpdateMapEntryCommand<IncludeHierarchy>(
                IncludeHierarchyMapEnum.INCLUDE, "a", childInclude);
        Result<UpdateResultEnum> result = addCommand.execute(includes);
        assertFalse(result.hasResult());
        checkIncludeMapEntries(includes, false, true, new ME("a", childInclude));
    }

    @Test
    public void existingEntryRenamed() throws Exception {
        assertFalse(new UpdateMapEntryCommand<IncludeHierarchy>(IncludeHierarchyMapEnum.INCLUDE, "a", childInclude)
                .execute(includes).hasResult());
        checkIncludeMapEntries(includes, false, true, new ME("a", childInclude));

        assertFalse(new UpdateMapEntryCommand<IncludeHierarchy>(IncludeHierarchyMapEnum.INCLUDE, "a", "b", childInclude)
                .execute(includes).hasResult());
        checkIncludeMapEntries("entry renamed in map (although internal name doesnt match -- still a)", includes, false, false, new ME(
                "b", childInclude));
    }

    @Test
    public void sameEntryCanBeAddedMultipleTimes() throws Exception {
        assertFalse(new UpdateMapEntryCommand<IncludeHierarchy>(IncludeHierarchyMapEnum.INCLUDE, "a", childInclude)
                .execute(includes).hasResult());
        assertFalse("same add done twice is ok", new UpdateMapEntryCommand<IncludeHierarchy>(
                IncludeHierarchyMapEnum.INCLUDE, "a", childInclude).execute(includes).hasResult());
        checkIncludeMapEntries(includes, false, true, new ME("a", childInclude));
    }

    @Test
    public void entryNotAddedIfAliasAlreadyExists() throws Exception {
        assertFalse(new UpdateMapEntryCommand<IncludeHierarchy>(IncludeHierarchyMapEnum.INCLUDE, "a", fredInclude)
                .execute(includes).hasResult());
        checkIncludeMapEntries(includes, false, true, new ME("a", fredInclude));

        Result<UpdateResultEnum> result = new UpdateMapEntryCommand<IncludeHierarchy>(IncludeHierarchyMapEnum.INCLUDE,
                "a", childInclude).execute(includes);
        assertTrue(result.hasResult());
        assertEquals(UpdateResultEnum.NAME_ALREADY_EXISTS, result.getEntry().value);
        assertEquals("UpdateMapEntryCommand:  map entry not added to IncludeMap in IncludeHierarchy top because another entry already exists with key: a", result
                .getMessage());
        checkIncludeMapEntries("only have the first include", includes, false, true, new ME("a", fredInclude));
    }

    @Test
    public void entryAddedIfForceRequestedReplacingExistingAlias() throws Exception {
        assertFalse(new UpdateMapEntryCommand<IncludeHierarchy>(IncludeHierarchyMapEnum.INCLUDE, "a", fredInclude)
                .execute(includes).hasResult());
        assertFalse(new UpdateMapEntryCommand<IncludeHierarchy>(IncludeHierarchyMapEnum.INCLUDE, "a", childInclude,
                true).execute(includes).hasResult());
        checkIncludeMapEntries("fred replaced by child when forced", includes, false, true, new ME("a", childInclude));
    }

    @Test
    public void entryNotRenamedIfNewnameAlreadyExists() throws Exception {
        assertFalse(new UpdateMapEntryCommand<IncludeHierarchy>(IncludeHierarchyMapEnum.INCLUDE, "a", childInclude)
                .execute(includes).hasResult());
        fredInclude = new IncludeHierarchy(net, "b");
        assertFalse(new UpdateMapEntryCommand<IncludeHierarchy>(IncludeHierarchyMapEnum.INCLUDE, "b", fredInclude)
                .execute(includes).hasResult());

        checkIncludeMapEntries("a and b entries", includes, false, true, new ME("a", childInclude), new ME("b",
                fredInclude));
        IncludeHierarchyCommand<UpdateResultEnum> renameCommand = new UpdateMapEntryCommand<IncludeHierarchy>(
                IncludeHierarchyMapEnum.INCLUDE, "a", "b", childInclude);
        Result<UpdateResultEnum> result = renameCommand.execute(includes);
        assertTrue(result.hasResult());
        assertEquals(UpdateResultEnum.NAME_ALREADY_EXISTS, result.getEntry().value);
        assertEquals("UpdateMapEntryCommand:  map entry in IncludeMap in IncludeHierarchy top for IncludeHierarchy with key a" +
                " not renamed to b; another entry by that name already exists.", result.getMessage());
        checkIncludeMapEntries("unchanged", includes, false, true, new ME("a", childInclude), new ME("b", fredInclude));
    }

    @Test
    public void entryRenamedIfForceRequestedReplacingExistingAlias() throws Exception {
        assertFalse(new UpdateMapEntryCommand<IncludeHierarchy>(IncludeHierarchyMapEnum.INCLUDE, "a", childInclude)
                .execute(includes).hasResult());
        fredInclude = new IncludeHierarchy(net, "b");
        assertFalse(new UpdateMapEntryCommand<IncludeHierarchy>(IncludeHierarchyMapEnum.INCLUDE, "b", fredInclude)
                .execute(includes).hasResult());
        checkIncludeMapEntries("a and b entries", includes, false, true, new ME("a", childInclude), new ME("b",
                fredInclude));

        assertFalse(new UpdateMapEntryCommand<IncludeHierarchy>(IncludeHierarchyMapEnum.INCLUDE, "a", "b", childInclude,
                true).execute(includes).hasResult());
        checkIncludeMapEntries("only 1 entry; b/fred replaced by b/child when forced", includes, false, false, new ME(
                "b", childInclude));
    }

    @Test
    public void entryAddedIfRenameRequestedButPreviousNameAndIncludeDontExist() throws Exception {
        assertFalse(new UpdateMapEntryCommand<IncludeHierarchy>(IncludeHierarchyMapEnum.INCLUDE, "a", childInclude)
                .execute(includes).hasResult());
        fredInclude = new IncludeHierarchy(net, "x");
        assertFalse("nothing under this key", includes.getIncludeMap().containsKey("b"));
        assertFalse("include doesn't exist either", includes.getIncludeMap().containsValue(fredInclude));
        checkIncludeMapEntries("only a is in the map, so far", includes, false, true, new ME("a", childInclude));
        assertFalse(new UpdateMapEntryCommand<IncludeHierarchy>(IncludeHierarchyMapEnum.INCLUDE, "b", "c", fredInclude)
                .execute(includes).hasResult());
        checkIncludeMapEntries("requested rename was mistaken, fred now added", includes, false, false, new ME("a",
                childInclude), new ME("c", fredInclude));
    }

    @Test
    public void entryNotAddedIfPreviousNameDoesntExistButIncludeExistsUnderAnotherNameAndReturnsResult()
            throws Exception {
        assertFalse(new UpdateMapEntryCommand<IncludeHierarchy>(IncludeHierarchyMapEnum.INCLUDE, "a", childInclude)
                .execute(includes).hasResult());
        assertFalse("nothing under this key", includes.getIncludeMap().containsKey("b"));
        assertTrue("but include exists", includes.getIncludeMap().containsValue(childInclude));
        Result<UpdateResultEnum> result = new UpdateMapEntryCommand<IncludeHierarchy>(IncludeHierarchyMapEnum.INCLUDE,
                "b", "c", childInclude).execute(includes);
        assertTrue(result.hasResult());
        assertEquals(UpdateResultEnum.INCLUDE_EXISTS_UNDER_DIFFERENT_OLDNAME, result.getEntry().value);
        assertEquals("UpdateMapEntryCommand:  no map entry found in IncludeMap in IncludeHierarchy top for IncludeHierarchy with key b." +
                " TargetHierarchy exists under different key: a.  Not renamed.  Probable logic error.", result
                        .getMessage());
        checkIncludeMapEntries("rename for existing include with wrong name has no effect", includes, false, true, new ME(
                "a", childInclude));
        assertFalse(includes.getIncludeMap().containsKey("b"));
        assertFalse(includes.getIncludeMap().containsKey("c"));
    }

    //	  java.lang.RuntimeException: UpdateMapEntryCommand:  no map entry found in IncludeMapAll in IncludeHierarchy a for IncludeHierarchy with key b.
    //	TargetHierarchy exists under different key: c.  Not renamed.  Probable logic error.
    @Test
    public void entryAddedIfForceRequestedWhenPreviousNameDoesntExistButIncludeExistsUnderAnotherNameAndReturnsResult()
            throws Exception {
        assertFalse(new UpdateMapEntryCommand<IncludeHierarchy>(IncludeHierarchyMapEnum.INCLUDE, "a", childInclude)
                .execute(includes).hasResult());
        assertFalse("nothing under this key", includes.getIncludeMap().containsKey("b"));
        assertTrue("but include exists", includes.getIncludeMap().containsValue(childInclude));
        Result<UpdateResultEnum> result = new UpdateMapEntryCommand<IncludeHierarchy>(IncludeHierarchyMapEnum.INCLUDE,
                "b", "c", childInclude, true).execute(includes);
        assertFalse(result.hasResult());
        checkIncludeMapEntries("renamed even though old name was not correct", includes, false, false, new ME("c",
                childInclude));
        assertFalse(includes.getIncludeMap().containsKey("a"));
        assertFalse(includes.getIncludeMap().containsKey("b"));
    }

    @Test
    public void entryAddedIfForceRequestedWhenPreviousNameIsNullButIncludeExistsUnderAnotherNameAndReturnsResult()
            throws Exception {
        assertFalse(new UpdateMapEntryCommand<IncludeHierarchy>(IncludeHierarchyMapEnum.INCLUDE, "a", childInclude)
                .execute(includes).hasResult());
        assertFalse("nothing under this key", includes.getIncludeMap().containsKey("b"));
        assertTrue("but include exists", includes.getIncludeMap().containsValue(childInclude));
        Result<UpdateResultEnum> result = new UpdateMapEntryCommand<IncludeHierarchy>(IncludeHierarchyMapEnum.INCLUDE,
                null, "c", childInclude, true).execute(includes);
        assertFalse(result.hasResult());
        checkIncludeMapEntries("renamed even though old name was null", includes, false, false, new ME("c",
                childInclude));
        assertFalse(includes.getIncludeMap().containsKey("a"));
        assertFalse(includes.getIncludeMap().containsKey("b"));
    }
}
