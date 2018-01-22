package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;

public class ResultTest {

    private DummyCommand<Integer> command;
    private PetriNet net;
    private IncludeHierarchy includes;
    private Result<Integer> result;

    @Before
    public void setUp() throws Exception {
        command = new DummyCommand<>();
        net = new PetriNet(new NormalPetriNetName("net"));
        includes = net.getIncludeHierarchy();
    }

    @Test
    public void hasResultReturnsTrueWhenEntryListIsntEmptyAndFalseOtherwise() throws Exception {
        result = command.execute(includes);
        assertTrue(result.hasResult());
        assertFalse((new DummyCommand<>(0)).execute(includes).hasResult());
    }

    @Test
    public void returnsListOfResultEntries() throws Exception {
        result = (new DummyCommand<Integer>(1)).execute(includes);
        assertEquals(1, result.getEntries().size());
        result = (new DummyCommand<Integer>(3)).execute(includes);
        assertEquals(3, result.getEntries().size());
    }

    @Test
    public void returnsFirstEntryOrMessageAsSingleEntryIfHasResultsAndNullOtherwise() throws Exception {
        result = (new DummyCommand<Integer>(0)).execute(includes);
        assertNull(result.getEntry());
        assertNull(result.getMessage());
        result = (new DummyCommand<Integer>(1)).execute(includes);
        assertEquals(0, (int) result.getEntry().value);
        result = (new DummyCommand<Integer>(3)).execute(includes);
        assertEquals("retrieves first entry", 0, (int) result.getEntry().value);
        assertEquals("retrieves first entry", "dummy message for net", result.getEntry().message);
        assertEquals("convenience method", "dummy message for net", result.getMessage());
    }

    @Test
    public void accumulatesAllEntriesFromMultipleResult() throws Exception {
        result = new Result<>();
        Result<Integer> resultOne = buildResult(1);
        result.addResult(resultOne);
        assertEquals(2, result.getEntries().size());
        Result<Integer> resultTwo = buildResult(3);
        result.addResult(resultTwo);
        assertEquals(4, result.getEntries().size());
        assertEquals("result 1", result.getEntries().get(0).message);
        assertEquals("result 4", result.getEntries().get(3).message);
    }

    private Result<Integer> buildResult(int i) {
        Result<Integer> result = new Result<>();
        result.addEntry("result " + i, 1);
        result.addEntry("result " + (i + 1), 1);
        return result;
    }
}
