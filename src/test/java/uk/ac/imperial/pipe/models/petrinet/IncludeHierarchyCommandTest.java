package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import uk.ac.imperial.pipe.exceptions.IncludeException;
import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchyCommand;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;

public class IncludeHierarchyCommandTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void commandExecutesPossiblyReturningMessages() throws IncludeException {
        IncludeHierarchyCommand<Object> command = new DummyCommand<>();
        assertEquals(0, command.getResult().getEntries().size());
        Result<Object> result = command.execute(new IncludeHierarchy(
                new PetriNet(new NormalPetriNetName("net1")), "fred"));
        assertEquals(1, result.getEntries().size());
        assertEquals(1, command.getResult().getEntries().size());
    }

    @Test
    public void throwsIfExecuteIncludeHierarchyIsNull() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(AbstractIncludeHierarchyCommand.EXECUTE_REQUIRES_NON_NULL_INCLUDE_HIERARCHY);
        IncludeHierarchyCommand<Object> command = new DummyCommand<>();
        command.execute(null);
    }
}
