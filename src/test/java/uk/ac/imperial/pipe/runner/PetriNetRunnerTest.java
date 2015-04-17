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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;

import org.junit.After;
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
import uk.ac.imperial.pipe.dsl.AnExternalTransition;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.io.FileUtils;
import uk.ac.imperial.pipe.io.IncludeHierarchyBuilder;
import uk.ac.imperial.pipe.io.IncludeHierarchyIO;
import uk.ac.imperial.pipe.io.IncludeHierarchyIOImpl;
import uk.ac.imperial.pipe.io.PetriNetIO;
import uk.ac.imperial.pipe.io.PetriNetIOImpl;
import uk.ac.imperial.pipe.io.XMLUtils;
import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.OutboundArc;
import uk.ac.imperial.pipe.models.petrinet.OutboundNormalArc;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.TestingContext;
import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;

@RunWith(MockitoJUnitRunner.class)
public class PetriNetRunnerTest implements PropertyChangeListener {
	
    @Mock
    private PropertyChangeListener mockListener;
    private PropertyChangeListener mockListener2;
	
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private PetriNet net;
	private Runner runner;
	private int events;
	private StateReport report;
	private int checkCase;
	private ByteArrayOutputStream out;
	private PrintStream print;
	private BufferedReader reader;
	private File file;
	private int tokenFired;
	private boolean tokenEvent;
	private String targetPlaceId;
	private String includePath;

    @Before
    public void setUp() {
        events = 0; 
        checkCase = 0; 
        out = new ByteArrayOutputStream();
        print = new PrintStream(out); 
        file = new File("firingReport.csv"); 
        if (file.exists()) file.delete(); 
        tokenEvent = false; 
        tokenFired = 0;
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
	public void mapTracksListenersForPlaces() throws Exception {
    	net = buildTestNet();
    	PetriNetRunner runner = new PetriNetRunner(net); 
    	assertEquals(0, runner.getListenerMap().size()); 
    	runner.listenForTokenChanges(mockListener, "P1"); 
    	assertEquals(1, runner.getListenerMap().size()); 
    	assertEquals(1, runner.getListenerMap().get("P1").size()); 
    	runner.listenForTokenChanges(mockListener2, "P1"); 
    	assertEquals(1, runner.getListenerMap().size()); 
    	assertEquals(2, runner.getListenerMap().get("P1").size()); 
	}
    //TODO test listening for invalid place:  non-existent, not in interface, etc.
	@Test
	public void notifiesListenerOfTokenChanges() throws Exception {
    	checkCase = 1; 
    	net = buildTestNet();
    	runner = new PetriNetRunner(net); 
    	runner.setSeed(456327998101l);
    	runner.listenForTokenChanges(this, "P1"); 
    	targetPlaceId = "P1"; 
    	runner.addPropertyChangeListener(this); 
    	runner.setFiringLimit(10); 
    	runner.run();
    	assertTrue(tokenEvent); 
    	assertEquals(4, events); 
    	assertEquals(2, tokenFired);
	}
	//TODO test marking for invalid place:  non-existent, not in interface, etc.
	@Test
	public void clientRequestsPlaceBeMarked() throws Exception {
		checkCase = 3; 
		net = buildTestNet();
		runner = new PetriNetRunner(net); 
		runner.setSeed(456327998101l);
		runner.markPlace("P2","Default",2); 
		runner.addPropertyChangeListener(this); 
		runner.setFiringLimit(10); 
		runner.run();
		assertEquals(4, events); 
	}
	@Test
	public void clientMarksInputPlaceInResponseToOutputMarking() throws Exception {
			checkCase = 4; 
			net = buildHaltingNet(); 
			runner = new PetriNetRunner(net); 
			runner.setSeed(456327998101l);
			runner.listenForTokenChanges(this, "P1"); 
			targetPlaceId = "P1"; 
			runner.addPropertyChangeListener(this); 
//			runner.markPlace("P2","Default",1); //  done in listener.
			runner.setFiringLimit(5); 
			//check P1 gets set to 0 on second firing. 
			runner.run();
			assertEquals(5, events); 
	}
	@Test
	public void appliesPendingMarkingsBeforeEachTransitionFiring() throws Exception {
		net = buildTestNet();
		PetriNetRunner runner = new PetriNetRunner(net); 
		assertEquals(0, runner.getPendingPlaceMarkings().size()); 
		runner.markPlace("P2","Default",2); 
		assertEquals(1, runner.getPendingPlaceMarkings().size()); 
		runner.fireOneTransition();
		assertEquals(0, runner.getPendingPlaceMarkings().size()); 
	}
	@Test
	public void buildsNetWithExternalTransitionAndInvokesWithContext() throws Exception {
		net = buildExternalTransitionTestNet();
		Runner runner = new PetriNetRunner(net); 
		TestingContext test = new TestingContext(2);
		runner.setTransitionContext("T0", test); 
    	runner.setFiringLimit(10); 
		runner.run(); 
		assertEquals("net2", test.getUpdatedContext()); 
	} 
	@Test
	public void buildsNetWithExternalTransitionWhichMarksPlace() throws Exception {
		net = buildExternalTransitionTestNet();
		Runner runner = new PetriNetRunner(net); 
		TestingContext test = new TestingContext(2);
		runner.setTransitionContext("T0", test); 
		runner.setFiringLimit(10); 
		runner.run(); 
		assertEquals(2, net.getExecutablePetriNet().getComponent("P1", Place.class).getTokenCount("Default"));
		assertEquals("net2", test.getUpdatedContext()); 
	} 
	@Test
	public void throwsIfSetContextInvokedForNonExternalTransitionComponent() throws Exception {
    	expectedException.expect(IllegalArgumentException.class);
    	expectedException.expectMessage("PetriNetRunner:  set transition context may only be invoked for uk.ac.imperial.pipe.models.petrinet.DiscreteExternalTransition.  Requested component: uk.ac.imperial.pipe.models.petrinet.DiscreteTransition");
		net = buildTestNet();
		Runner runner = new PetriNetRunner(net); 
		TestingContext test = new TestingContext(2);
		runner.setTransitionContext("T0", test); 
	}
	@Test
	public void throwsIfSetContextInvokedForNonExistentTransition() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("PetriNetRunner:  set transition context requested for a transition that does not exist in the executable petri net: T99");
		net = buildTestNet();
		Runner runner = new PetriNetRunner(net); 
		TestingContext test = new TestingContext(2);
		runner.setTransitionContext("T99", test); 
	}
	@Test
    public  void stopsAtRunLimit() throws Exception {
    	checkCase = 2; 
    	
    	net = PetriNetRunner.readFileAsSinglePetriNet("src/test/resources/xml/loopingNet.xml"); 
//    	net = buildLoopingTestNet(); 
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
    	PetriNet net = null; 
    	runner = new PetriNetRunner(net); 
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
	@Test
	public void commandLineRuns() throws IOException {
		PetriNetRunner.setPrintStreamForTesting(print);
		String[] args = new String[]{"src/test/resources/xml/simpleNet2.xml","firingReport.csv","5","123456"}; 
		PetriNetRunner.main(args);
		reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(out.toByteArray())));
		assertEquals("PetriNetRunner:  executing src/test/resources/xml/simpleNet2.xml, for a maximum of 5 transitions, using random seed 123456, with results in firingReport.csv", reader.readLine());
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
	@Test
	public void runsFromIncludeXmlFile() throws Exception {
		PetriNetRunner.setPrintStreamForTesting(print);
		String[] args = new String[]{FileUtils.fileLocation(XMLUtils.getSingleIncludeHierarchyFileReadyToFire()),"firingReport.csv","5","123456"}; 
		PetriNetRunner.main(args);
		reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(out.toByteArray())));
//		assertEquals("PetriNetRunner:  executing /Users/stevedoubleday/git/PIPECore/target/test-classes/xml/include/singleIncludeReadyToFire.xml, for a maximum of 5 transitions, using random seed 123456, with results in firingReport.csv", reader.readLine());
		reader.readLine();  // skip first line as it is local to the machine on which test is run.
		assertEquals("PetriNetRunner:  complete.", reader.readLine());
		PetriNetRunner.setPrintStreamForTesting(null);
		BufferedReader fileReader = new BufferedReader(new FileReader(file)); 
		List<String> lines = new ArrayList<String>();
		String line = fileReader.readLine();
		while (line != null) {
//			System.out.println(line);
			lines.add(line); 
			line = fileReader.readLine();
		}
		fileReader.close(); 
		assertEquals(6, lines.size()); 
		assertEquals("\"Round\",\"Transition\",\"a.P0\",\"a.P1\"", lines.get(0)); 
		assertEquals("4,\"a.T0\",0,4", lines.get(5)); 
		fileReader.close(); 
	}


	@Test
	public void commandLineRunsForInterfacePlaces() throws IOException {
		PetriNetRunner.setPrintStreamForTesting(print);
		String[] args = new String[]{"src/test/resources/xml/include/twoNetsOneInterfaceStatus.xml","firingReport.csv","5","123456"}; 
		PetriNetRunner.main(args);
		reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(out.toByteArray())));
		assertEquals("PetriNetRunner:  executing src/test/resources/xml/include/twoNetsOneInterfaceStatus.xml, for a maximum of 5 transitions, using random seed 123456, with results in firingReport.csv", reader.readLine());
		assertEquals("PetriNetRunner:  complete.", reader.readLine());
		PetriNetRunner.setPrintStreamForTesting(null);
		BufferedReader fileReader = new BufferedReader(new FileReader(file)); 
		List<String> lines = new ArrayList<String>();
		String line = fileReader.readLine();
		while (line != null) {
			lines.add(line); 
			line = fileReader.readLine();
		}
		fileReader.close(); 
		assertEquals(4, lines.size()); 
		assertEquals("\"Round\",\"Transition\",\"top.P0\",\"top.a.P0\",\"top.a.P1\",\"top.a.P2\"", lines.get(0)); 
		assertEquals("0,\"\",1,1,0,0", lines.get(1)); 
		assertEquals("1,\"top.T0\",0,1,1,0", lines.get(2)); 
		assertEquals("2,\"top.a.T0\",0,0,0,1", lines.get(3)); 
	}	
	@Test
	public void roundTripThroughPersistenceForInterfacePlacesAndExternalTransitions() throws Exception {
		checkCase = 5; 
		buildNetWithInterfacePlacesAndExternalTransition(); 
		runner = new PetriNetRunner(includePath); 
		runner.setSeed(456327998101l);
		runner.addPropertyChangeListener(this); 
		runner.listenForTokenChanges(this, "a.P1");
		targetPlaceId = "a.P1"; 
		TestingContext test = new TestingContext(7, "", false);
		runner.setTransitionContext("a.b.T0", test); 
    	runner.setFiringLimit(10); 
		runner.run(); 
		assertEquals("testnet7", test.getUpdatedContext()); 
	}	
	@Test
	public void externalTransitionMarksAnotherPlace() throws Exception {
		checkCase = 6; 
		net = buildNet5(); 
		runner = new PetriNetRunner(net); 
		runner.setSeed(456327998101l);
		runner.addPropertyChangeListener(this); 
		TestingContext test = new TestingContext(7, "P2", true);
		runner.setTransitionContext("T0", test); 
		runner.setFiringLimit(10); 
		runner.run(); 
		assertEquals("testnet7", test.getUpdatedContext()); 
	}	
	@Test
	public void throwsIfListenRequestReceivedForPlaceNotExternallyAccessible() throws Exception {
		expectedException.expect(InterfaceException.class); 
		expectedException.expectMessage("PetriNetRunner.listenForTokenChanges: requested place is not externally accessible: a.P0"); 
		net = buildNetWithInterfacePlacesAndExternalTransition(); 
		runner = new PetriNetRunner(net); 
		runner.listenForTokenChanges(this, "a.P0");
	}
	@Test
	public void throwsIfListenRequestReceivedForNonexistentPlace() throws Exception {
		expectedException.expect(InterfaceException.class); 
		expectedException.expectMessage("PetriNetRunner.listenForTokenChanges: requested place does not exist in executable Petri net: a.P99"); 
		net = buildNetWithInterfacePlacesAndExternalTransition(); 
		runner = new PetriNetRunner(net); 
		runner.listenForTokenChanges(this, "a.P99");
	}
	@Test
	public void throwsIfMarkPlaceRequestReceivedForPlaceNotExternallyAccessible() throws Exception {
		expectedException.expect(InterfaceException.class); 
		expectedException.expectMessage("PetriNetRunner.markPlace: requested place is not externally accessible: a.P0"); 
		net = buildNetWithInterfacePlacesAndExternalTransition(); 
		runner = new PetriNetRunner(net); 
		runner.markPlace("a.P0", "Default", 1); 
	}
	@Test
	public void throwsIfMarkPlaceRequestReceivedForNonexistentPlace() throws Exception {
		expectedException.expect(InterfaceException.class); 
		expectedException.expectMessage("PetriNetRunner.markPlace: requested place does not exist in executable Petri net: a.P99"); 
		net = buildNetWithInterfacePlacesAndExternalTransition(); 
		runner = new PetriNetRunner(net); 
		runner.markPlace("a.P99", "Default", 1); 
	}
	@Test
	public void throwsIfMarkPlaceRequestReceivedForNonexistentToken() throws Exception {
		expectedException.expect(InterfaceException.class); 
		expectedException.expectMessage("PetriNetRunner.markPlace: requested token does not exist in executable Petri net: FredToken"); 
		net = buildNetWithInterfacePlacesAndExternalTransition(); 
		runner = new PetriNetRunner(net); 
		runner.markPlace("a.P1", "FredToken", 1); 
	}

	@After
	public void tearDown() {
        deleteFile("net3.xml");
        deleteFile("net4.xml");
        deleteFile("include.xml");
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
                        and(APlace.withId("P1").externallyAccessible()).and(APlace.withId("P2").externallyAccessible()).and(AnImmediateTransition.withId("T0")).and(
                        AnImmediateTransition.withId("T1")).and(
                        ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token()).and(
                        ANormalArc.withSource("T0").andTarget("P1").with("1", "Default").token()).and(
                        ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token()).andFinally(
                        ANormalArc.withSource("T1").andTarget(place).with("1", "Default").token()); 
		return net;
	}
	private PetriNet buildHaltingNet() {
		PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0").containing(1, "Default").token()).
				and(APlace.withId("P1").externallyAccessible()).and(APlace.withId("P2").externallyAccessible()).and(AnImmediateTransition.withId("T0")).and(
				AnImmediateTransition.withId("T1")).and(
				ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token()).and(
				ANormalArc.withSource("T0").andTarget("P1").with("1", "Default").token()).and(
				ANormalArc.withSource("P2").andTarget("T1").with("1", "Default").token()).and(
				ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token()).andFinally(
				ANormalArc.withSource("T1").andTarget("P0").with("1", "Default").token()); 
		return net;
	}
    private PetriNet buildExternalTransitionTestNet() {
    	PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0").containing(1, "Default").token()).
    			and(APlace.withId("P1").externallyAccessible()).and(
    			AnExternalTransition.withId("T0").andExternalClass("uk.ac.imperial.pipe.models.petrinet.TestingExternalTransition")).and(
    			ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token()).andFinally(
    			ANormalArc.withSource("T0").andTarget("P1").with("1", "Default").token());
    			net.setName(new NormalPetriNetName("net"));
    	return net;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
//		System.out.println(evt);
		if (!(evt.getPropertyName().equals(Place.TOKEN_CHANGE_MESSAGE))) events++; 
		switch (checkCase) {
		case 1: checkNormalEvents(evt); break; 
		case 2: checkNormalEventsLooping(evt); break; 
		case 3: checkNormalEventsWithMarking(evt); break; 
		case 4: checkNormalEventsWithMarkingAndHalt(evt); break; 
		case 5: checkNormalEventsWithMarkingAndExernalTransition(evt); break; 
		case 6: checkNormalEventsWithExternalTransitionMarkingAnotherPlace(evt); break; 
		default: assertTrue(true); break; 
		}
	}
	private void checkNormalEventsWithExternalTransitionMarkingAnotherPlace(
			PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(PetriNetRunner.EXECUTION_STARTED)) {
			Firing round0firing = (Firing) evt.getOldValue(); 
			checkFiring(round0firing, 0,  "", 1,0,0,0);
//			checkHasListOfPlaceIds(evt); 
		}
		else if (evt.getPropertyName().equals(PetriNetRunner.UPDATED_STATE)) {
			Firing firing = (Firing) evt.getNewValue(); 
			Firing prevfiring = (Firing) evt.getOldValue(); 
			if (events == 2) checkFiring(firing, 1,  "T0", 0,1,0,0); 
			if (events == 3) checkFiring(firing, 2, "T1", 0,1,0,2); 
		}
		else if (evt.getPropertyName().equals(PetriNetRunner.EXECUTION_COMPLETED)) {
			assertEquals(4, events); 
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
		else if (evt.getPropertyName().equals(Place.TOKEN_CHANGE_MESSAGE)) {
			tokenEvent = true; 
			Place place = (Place) evt.getSource(); 
			assertEquals(targetPlaceId, place.getId()); 
			Map<String, Integer> token = (Map<String, Integer>) evt.getNewValue(); 
			assertEquals(1, token.size());
			Entry<String, Integer> entry = token.entrySet().iterator().next(); 
			if (tokenFired == 0) { checkToken("Default", entry.getKey(), 1, entry.getValue()); }
			if (tokenFired == 1) { checkToken("Default", entry.getKey(), 0, entry.getValue()); }
			tokenFired++;
		}
	}
	private void checkNormalEventsWithMarking(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(PetriNetRunner.EXECUTION_STARTED)) {
			Firing round0firing = (Firing) evt.getOldValue(); 
			checkFiring(round0firing, 0,  "", 1,0,0);
			checkHasListOfPlaceIds(evt); 
		}
		else if (evt.getPropertyName().equals(PetriNetRunner.UPDATED_STATE)) {
			Firing firing = (Firing) evt.getNewValue(); 
			Firing prevfiring = (Firing) evt.getOldValue(); 
			if (events == 2) checkFiring(prevfiring, 0,  "", 1,0,0); 
			if (events == 2) checkFiring(firing, 1,  "T0", 0,1,2); 
			if (events == 3) checkFiring(prevfiring, 1, "T0", 0,1,2); 
			if (events == 3) checkFiring(firing, 2, "T1", 0,0,3); 
		}
		else if (evt.getPropertyName().equals(PetriNetRunner.EXECUTION_COMPLETED)) {
			assertEquals(4, events); 
		}
		
	}
	protected void checkToken(String expectedToken, String token, int expectedCount, int count ) {
//		System.out.println(tokenFired+" "+token+" count: "+count);
		assertEquals(expectedToken, token); 
		assertEquals(expectedCount, count);
	}
	private void checkNormalEventsLooping(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(PetriNetRunner.UPDATED_STATE)) {
			Firing firing = (Firing) evt.getNewValue(); 
			if (events == 2) checkFiring(firing, 1,  "T0", 0,1,0); 
			if (events == 3) checkFiring(firing, 2, "T1", 1,0,0); 
			if (events == 4) checkFiring(firing, 3,  "T0", 0,1,0); 
			if (events == 5) checkFiring(firing, 4, "T1", 1,0,0); 
			if (events == 6) checkFiring(firing, 5,  "T0", 0,1,0); 
		}
		else if (evt.getPropertyName().equals(PetriNetRunner.EXECUTION_COMPLETED)) {
			assertEquals(7, events); 
		}
	}
	private void checkNormalEventsWithMarkingAndHalt(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(PetriNetRunner.UPDATED_STATE)) {
			Firing firing = (Firing) evt.getNewValue(); 
			if (events == 2) checkFiring(firing, 1,  "T0", 0,1,0); 
			if (events == 3) checkFiring(firing, 2, "T1", 1,0,0); 
			if (events == 4) checkFiring(firing, 3,  "T0", 0,1,0); 
		}
		else if (evt.getPropertyName().equals(PetriNetRunner.EXECUTION_COMPLETED)) {
			assertEquals(5, events); 
		}
		else if (evt.getPropertyName().equals(Place.TOKEN_CHANGE_MESSAGE)) {
			tokenEvent = true; 
			Place place = (Place) evt.getSource(); 
			assertEquals(targetPlaceId, place.getId()); 
			Map<String, Integer> token = (Map<String, Integer>) evt.getNewValue(); 
			assertEquals(1, token.size());
			Entry<String, Integer> entry = token.entrySet().iterator().next(); 
			if (tokenFired == 0) { 
				checkToken("Default", entry.getKey(), 1, entry.getValue()); 
				try {
					runner.markPlace("P2","Default",1);
				} catch (InterfaceException e) {
					e.printStackTrace();
				} 
			}
			if (tokenFired == 1) { checkToken("Default", entry.getKey(), 0, entry.getValue()); }
			tokenFired++;
		}
	}
	private void checkNormalEventsWithMarkingAndExernalTransition(
			PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(PetriNetRunner.EXECUTION_STARTED)) {
			Firing round0firing = (Firing) evt.getOldValue(); 
			checkFiring(round0firing, 0,  "", 1,0,0,0,0);
			checkHasListOfPlaceIdsExternal(evt); 
		}

		else if (evt.getPropertyName().equals(PetriNetRunner.UPDATED_STATE)) {
			Firing firing = (Firing) evt.getNewValue(); 
			if (events == 2) checkFiring(firing, 1,  "a.T0", 0,1,1,0,0); 
			if (events == 3) checkFiring(firing, 2, "a.b.T0", 0,1,0,0,1); 

		}
		else if (evt.getPropertyName().equals(PetriNetRunner.EXECUTION_COMPLETED)) {
			assertEquals(4, events); 
		}
		else if (evt.getPropertyName().equals(Place.TOKEN_CHANGE_MESSAGE)) {
			tokenEvent = true; 
			Place place = (Place) evt.getSource(); 
			assertEquals(targetPlaceId, place.getId()); 
			Map<String, Integer> token = (Map<String, Integer>) evt.getNewValue(); 
			assertEquals(1, token.size());
			Entry<String, Integer> entry = token.entrySet().iterator().next(); 
			if (tokenFired == 0) { 
				checkToken("Default", entry.getKey(), 1, entry.getValue()); 
				try {
					runner.markPlace("a.b.P1","Default",1);
				} catch (InterfaceException e) {
					e.printStackTrace();
				} 
			}
			if (tokenFired == 1) { checkToken("Default", entry.getKey(), 0, entry.getValue()); }
			tokenFired++;
		}
	}
	private void checkFiring(Firing firing, int round, String transition, int... placeCols) {
//		StringBuffer sb = new StringBuffer(); 
//		for (int i = 0; i < placeCols.length; i++) {
//			sb.append(placeCols[i]);
//			sb.append(" "); 
//		}
//		System.out.println("round: "+firing.round+" transition: "+transition+" "+sb.toString());
		assertEquals(round, firing.round); 
		assertEquals(transition, firing.transition); 
		report = new StateReport(firing.state); 
//		StringBuffer sb = new StringBuffer(); 
//		sb = new StringBuffer(); 
//		for (String place : report.getPlaces()) {
//			sb.append(place);
//			sb.append(" ");
//		}
//		System.out.println(sb.toString());
		checkRecord(placeCols); 
    }
	private void checkRecord(int... placeCols ) {
		assertEquals(1, report.getTokenFiringRecords().size()); 
		assertEquals(placeCols.length, report.getTokenFiringRecords().get(0).getCounts().size()); 
		assertEquals("Default", report.getTokenFiringRecords().get(0).token); 
//		System.out.println("report: ");
		for (int i = 0; i < placeCols.length; i++) {
//			System.out.println((int) report.getTokenFiringRecords().get(0).getCounts().get(i)+" ");
			assertEquals("column: "+placeCols[i],placeCols[i], (int) report.getTokenFiringRecords().get(0).getCounts().get(i)); 
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
	private void checkHasListOfPlaceIdsExternal(PropertyChangeEvent evt) {
		Collection<String> ids = (Collection<String>)evt.getNewValue(); 
		assertEquals(5, ids.size());
		Iterator<String> it = ids.iterator();
		assertEquals("a.P0", it.next());
		assertEquals("a.P1", it.next());
		assertEquals("a.b.P0", it.next());
		assertEquals("a.b.P1", it.next());
		assertEquals("a.b.P2", it.next());
	}
	
	private PetriNet buildNetWithInterfacePlacesAndExternalTransition() throws Exception{
		PetriNet net3 = buildNet3();
		PetriNet net4 = buildNet4();
		IncludeHierarchy includes = new IncludeHierarchy(net3, "a");
		IncludeHierarchy includeb = includes.include(net4, "b");  
		net3.setIncludeHierarchy(includes);
		Place originP0 = net4.getComponent("P0", Place.class); 
		includeb.addToInterface(originP0, true, false, false, false ); 
		includes.addAvailablePlaceToPetriNet(includes.getInterfacePlace("b.P0")); 
		Place aIP0 = includes.getInterfacePlace("b.P0"); 
		Transition aT0 = net3.getComponent("T0", Transition.class);
		Map<String,String> tokenweights = new HashMap<String, String>(); 
		tokenweights.put("Default", "1"); 
		OutboundArc arcOut = new OutboundNormalArc(aT0, aIP0, tokenweights);
		net3.add(arcOut); 
		writeFiles(net3, net4, includes, includeb); 
		return net3;
	}
	protected void writeFiles(PetriNet net3, PetriNet net4,
			IncludeHierarchy includes, IncludeHierarchy includeb)
			throws JAXBException, IOException {
		PetriNetIO netIO = new PetriNetIOImpl(); 
		netIO.writeTo("net3.xml", net3); 
		netIO.writeTo("net4.xml", net4);
		includes.setPetriNetLocation("net3.xml");
		includeb.setPetriNetLocation("net4.xml");
		IncludeHierarchyIO includeIO = new IncludeHierarchyIOImpl(); 
		includePath = "include.xml"; 
		includeIO.writeTo(includePath, new IncludeHierarchyBuilder(includes));
	}

    private PetriNet buildNet4() {
		PetriNet net = APetriNet.named("includednet").and(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0").containing(1, "Default").token()).
			and(APlace.withId("P1").externallyAccessible()).and(APlace.withId("P2")).
			and(AnExternalTransition.withId("T0").andExternalClass("uk.ac.imperial.pipe.models.petrinet.TestingExternalTransition")).
			and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token()).
			and(ANormalArc.withSource("P1").andTarget("T0").with("1", "Default").token()).
			andFinally(ANormalArc.withSource("T0").andTarget("P2").with("1", "Default").token());
		return net; 
    }
    private PetriNet buildNet3() {
    	PetriNet net = APetriNet.named("testnet").and(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0").containing(1, "Default").token()).
    			and(APlace.withId("P1").externallyAccessible()).and(AnImmediateTransition.withId("T0")).
    			and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token()).
    			andFinally(ANormalArc.withSource("T0").andTarget("P1").with("1", "Default").token());
    	return net; 
    }
	private PetriNet buildNet5() {
    	PetriNet net = APetriNet.named("testnet").and(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0").containing(1, "Default").token()).
			and(APlace.withId("P1").externallyAccessible()).and(APlace.withId("P2").externallyAccessible()).and(APlace.withId("P3")).
			and(AnExternalTransition.withId("T0").andExternalClass("uk.ac.imperial.pipe.models.petrinet.TestingExternalTransition")).
			and(AnImmediateTransition.withId("T1")).
			and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token()).
			and(ANormalArc.withSource("T0").andTarget("P1").with("1", "Default").token()).
			and(ANormalArc.withSource("P2").andTarget("T1").with("2", "Default").token()).
			andFinally(ANormalArc.withSource("T1").andTarget("P3").with("2", "Default").token());
    	return net; 
	}

    private void deleteFile(String filename) {
    	File file = new File(filename);
    	if (file.exists()) file.delete(); 
	}

}
