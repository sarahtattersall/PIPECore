package uk.ac.imperial.pipe.runner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;

import org.junit.After;
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
import uk.ac.imperial.pipe.models.petrinet.PetriNet;

@RunWith(MockitoJUnitRunner.class)
public class PetriNetRunnerTest implements PropertyChangeListener {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private PetriNet net;
	private PetriNetRunner runner;
	private int events;
	private StateReport report;
	private int checkCase;

	private ByteArrayOutputStream out;

	private PrintStream print;

	private BufferedReader reader;

	private File file;

    @Before
    public void setUp() {
        events = 0; 
        checkCase = 0; 
        out = new ByteArrayOutputStream();
        print = new PrintStream(out); 
        file = new File("firingReport.csv"); 
        if (file.exists()) file.delete(); 
    }
    @Test
    public void simpleNetNotifiesOfStartAndFinishAndEachStateChange() throws InterruptedException {
    	checkCase = 1; 
    	net = buildTestNet();
    	runner = new PetriNetRunner(net); 
    	runner.setSeed(456327998101l);
    	runner.addPropertyChangeListener(this); 
    	runner.setFiringLimit(10); 
    	runner.run();
    	assertEquals(4, events); 
    }
    @Test
    public  void stopsAtRunLimit() throws InterruptedException {
    	checkCase = 2; 
    	net = buildLoopingTestNet(); 
    	runner = new PetriNetRunner(net); 
    	runner.setSeed(456327998101l);
    	runner.addPropertyChangeListener(this); 
    	runner.setFiringLimit(5); 
    	runner.run();
    	assertEquals(7, events); 
    }
    @Test
    public void throwsIfNullPetriNetOrPetriNetNotFound() throws Exception {
    	expectedException.expect(IllegalArgumentException.class);
    	expectedException.expectMessage("PetriNetRunner:  PetriNet to execute is null or not found: null");
    	runner = new PetriNetRunner(null); 
    	expectedException.expectMessage("PetriNetRunner:  PetriNet to execute is null or not found: nonexistentNet");
    	String[] args = new String[]{"nonexistentNet","","",""}; 
    	PetriNetRunner.main(args);
    }
    @Test
	public void commandLinePrintsUsageIfGivenInsufficientArguments() throws Exception {
    	PetriNetRunner.setPrintStreamForTesting(print);
    	String[] args = new String[]{}; 
    	PetriNetRunner.main(args);
    	reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(out.toByteArray())));
    	assertEquals("usage: PetriNetRunner [name of petri net to execute] [results filename] [maximum number of transitions to fire] [long integer seed for random transition selection]", reader.readLine());
    	assertEquals("number of transitions = 0:  no limit", reader.readLine());
    	assertEquals("seed = 0:  system creates new seed on each invocation ", reader.readLine());
    	PetriNetRunner.setPrintStreamForTesting(null);
	}
//    runner = new PetriNetRunner(net); 
//    runner.setSeed(456327998101l);
//    runner.setFiringLimit(10);
//    filename = "firing.csv"; 
//    file = new File(filename); 
//    if (file.exists()) file.delete(); 
//    out = new ByteArrayOutputStream(); 
//}
	@Test
	public void commandLineRuns() throws IOException {
		PetriNetRunner.setPrintStreamForTesting(print);
		String[] args = new String[]{"testSimple","firingReport.csv","5","123456"}; 
		PetriNetRunner.main(args);
		reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(out.toByteArray())));
		assertEquals("PetriNetRunner:  executing testSimple, for a maximum of 5 transitions, using random seed 123456, with results in firingReport.csv", reader.readLine());
		assertEquals("PetriNetRunner:  complete.", reader.readLine());
		PetriNetRunner.setPrintStreamForTesting(null);
		BufferedReader fileReader = new BufferedReader(new FileReader(file)); 
		int lines = 0; 
		while (fileReader.readLine() != null) {
			lines++; 
		}
		fileReader.close(); 
		assertEquals(4, lines); 
	}	
	@After
	public void tearDown() {
//		System.out.println(file.getAbsolutePath()); // uncomment to find file
		if (file.exists()) file.delete(); // comment to view file
	}
	//TODO execution message changes appropriately for firing limit and seed values
    //TODO runsNetsWithMultipleColors
    //TODO runsNetsWithTimedTransitions
    //TODO runsNetsWithFunctionalExpressions
    //TODO generatesSameFiringSequenceIfMultipleTransitionsEnabledWhenSeedIsSpecified
    protected PetriNet buildLoopingTestNet() {
    	return buildNet("P0"); 
    }
	protected PetriNet buildTestNet() {
		return buildNet("P2"); 
	}
	private PetriNet buildNet(String place) {
		PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0").containing(1, "Default").token()).
                        and(APlace.withId("P1")).and(APlace.withId("P2")).and(AnImmediateTransition.withId("T0")).and(
                        AnImmediateTransition.withId("T1")).and(
                        ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token()).and(
                        ANormalArc.withSource("T0").andTarget("P1").with("1", "Default").token()).and(
                        ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token()).andFinally(
                        ANormalArc.withSource("T1").andTarget(place).with("1", "Default").token()); 
		return net;
	}
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		events++; 
		switch (checkCase) {
		case 1: checkNormalEvents(evt); break; 
		case 2: checkNormalEventsLooping(evt); break; 
		default: assertTrue(true); break; 
		}
		
		
	}
	private void checkNormalEvents(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(PetriNetRunner.EXECUTION_STARTED)) {
			Firing round0firing = (Firing) evt.getOldValue(); 
			checkFiring(round0firing, 0,  "", 1,0,0);
			checkHasListOfPlaceIds(evt); 
		}
		else if (evt.getPropertyName().equals(PetriNetRunner.UPDATED_STATE)) {
			Firing firing = (Firing) evt.getNewValue(); 
			Firing prevfiring = (Firing) evt.getOldValue(); 
			if (events == 2) checkFiring(prevfiring, 0,  "", 1,0,0); 
			if (events == 2) checkFiring(firing, 1,  "T0", 0,1,0); 
			if (events == 3) checkFiring(prevfiring, 1, "T0", 0,1,0); 
			if (events == 3) checkFiring(firing, 2, "T1", 0,0,1); 
		}
		else if (evt.getPropertyName().equals(PetriNetRunner.EXECUTION_COMPLETED)) {
			assertEquals(4, events); 
		}
	}
	private void checkNormalEventsLooping(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(PetriNetRunner.UPDATED_STATE)) {
			Firing firing = (Firing) evt.getNewValue(); 
			if (events == 2) checkFiring(firing, 1,  "T0", 0,1); 
			if (events == 3) checkFiring(firing, 2, "T1", 1,0); 
			if (events == 4) checkFiring(firing, 3,  "T0", 0,1); 
			if (events == 5) checkFiring(firing, 4, "T1", 1,0); 
			if (events == 6) checkFiring(firing, 5,  "T0", 0,1); 
		}
		else if (evt.getPropertyName().equals(PetriNetRunner.EXECUTION_COMPLETED)) {
			assertEquals(7, events); 
		}
	}
	private void checkFiring(Firing firing, int round, String transition, int... placeCols) {
		assertEquals(round, firing.round); 
		assertEquals(transition, firing.transition); 
		report = new StateReport(firing.state); 
		checkRecord(placeCols); 
    }
	private void checkRecord(int... placeCols ) {
		assertEquals(1, report.getTokenFiringRecords().size()); 
		assertEquals(3, report.getTokenFiringRecords().get(0).getCounts().size()); 
		assertEquals("Default", report.getTokenFiringRecords().get(0).token); 
		for (int i = 0; i < placeCols.length; i++) {
			assertEquals(placeCols[i], (int) report.getTokenFiringRecords().get(0).getCounts().get(i)); 
		}
	}
	@SuppressWarnings("unchecked")
	private void checkHasListOfPlaceIds(PropertyChangeEvent evt) {
		Collection<String> ids = (Collection<String>)evt.getNewValue(); 
		assertEquals(3, ids.size());
		Iterator<String> it = ids.iterator();
		assertEquals("P0", it.next());
		assertEquals("P1", it.next());
		assertEquals("P2", it.next());
	}

}
