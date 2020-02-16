package uk.ac.imperial.pipe.runner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;

public class FiringWriterTest {

    private PetriNet net;
    private PetriNetRunner runner;
    private File file;
    private FiringWriter writer;
    private OutputStream out;
    private String filename;

    @Before
    public void setUp() throws PetriNetComponentException {
        net = buildNet();
        runner = new PetriNetRunner(net);
        runner.tryAfterNoEnabledTransitions = false;
        runner.setSeed(456327998101l);
        runner.setFiringLimit(10);
        filename = "firing.csv";
        file = new File(filename);
        if (file.exists())
            file.delete();
        out = new ByteArrayOutputStream();
    }

    @Test
    public void writesStreamOfFirings() throws IOException {
        writer = new FiringWriter(out);
        runner.addPropertyChangeListener(writer);
        runner.run();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(((ByteArrayOutputStream) out).toByteArray())));
        assertEquals("\"Round\",\"Transition\",\"P0\",\"P1\",\"P2\"", reader.readLine());
        assertEquals("0,\"\",1,0,0", reader.readLine());
        assertEquals("1,\"T0\",0,1,0", reader.readLine());
        assertEquals("2,\"T1\",0,0,1", reader.readLine());
    }

    @Test
    public void writesFile() throws IOException {
        writer = new FiringWriter(filename);
        runner.addPropertyChangeListener(writer);
        runner.run();
        assertTrue(file.exists());
    }

    @After
    public void tearDown() {
        //    	System.out.println(file.getAbsolutePath()); // uncomment to find file for viewing
        file.delete(); // comment to leave file for viewing
    }

    private PetriNet buildNet() throws PetriNetComponentException {
        PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P0").containing(1, "Default").token()).and(APlace.withId("P1"))
                .and(APlace.withId("P2")).and(AnImmediateTransition.withId("T0"))
                .and(AnImmediateTransition.withId("T1"))
                .and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token())
                .and(ANormalArc.withSource("T0").andTarget("P1").with("1", "Default").token())
                .and(ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token())
                .andFinally(ANormalArc.withSource("T1").andTarget("P2").with("1", "Default").token());
        return net;
    }

}
