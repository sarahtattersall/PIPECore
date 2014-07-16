package uk.ac.imperial.pipe.runner;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import uk.ac.imperial.pipe.animation.Animator;
import uk.ac.imperial.pipe.animation.PetriNetAnimator;
import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.models.petrinet.AbstractPetriNetPubSub;
import uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.state.State;

public class PetriNetRunner extends AbstractPetriNetPubSub {


	private static final String PETRI_NET_TO_EXECUTE_IS_NULL_OR_NOT_FOUND = "PetriNetRunner:  PetriNet to execute is null or not found: ";
	public static final String EXECUTION_STARTED = "execution started";
	public static final String UPDATED_STATE = "state updated";
	public static final String EXECUTION_COMPLETED = "execution complete";
	private static PrintStream PRINTSTREAM;
	private static Map<String, PetriNet> TEST_NETS;
	private Random random;
	private int firingLimit;
	private ExecutablePetriNet executablePetriNet;
	private int round;
	private boolean transitionsToFire;
	private State previousState;
	private Animator animator;
	private Firing previousFiring;
	private Firing firing;

	/**
	 * temporary map to store some sample Petri nets for testing, until support added for marshalling hierarchical Petri nets from XML
	 */
	static {
		TEST_NETS = new HashMap<String, PetriNet>(); 
		TEST_NETS.put("testSimple", buildTestNet()); 
		TEST_NETS.put("testLooping", buildLoopingTestNet()); 
		TEST_NETS.put("testHierarchy", buildNetWithHierarchy()); 
		TEST_NETS.put("testLoopingHierarchy", buildNetWithLoopingHierarchy()); 
	}
    private static PetriNet buildLoopingTestNet() {
    	return buildNet("P0"); 
    }
	private static PetriNet buildNetWithLoopingHierarchy() {
		PetriNet petriNet = buildLoopingTestNet(); 
		petriNet.getIncludeHierarchy().include(buildLoopingTestNet(), "left"); 
		petriNet.getIncludeHierarchy().include(buildLoopingTestNet(), "right"); 
		return petriNet;
	}
	private static PetriNet buildNetWithHierarchy() {
		PetriNet petriNet = buildTestNet(); 
		petriNet.getIncludeHierarchy().include(buildTestNet(), "left"); 
		petriNet.getIncludeHierarchy().include(buildTestNet(), "right"); 
		return petriNet;
	}
	private static PetriNet buildTestNet() {
		return buildNet("P2"); 
	}
	private static PetriNet buildNet(String place) {
		PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0").containing(1, "Default").token()).
                        and(APlace.withId("P1")).and(APlace.withId("P2")).and(AnImmediateTransition.withId("T0")).and(
                        AnImmediateTransition.withId("T1")).and(
                        ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token()).and(
                        ANormalArc.withSource("T0").andTarget("P1").with("1", "Default").token()).and(
                        ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token()).andFinally(
                        ANormalArc.withSource("T1").andTarget(place).with("1", "Default").token()); 
		return net;
	}

	
	public PetriNetRunner(PetriNet petriNet) {
		if (petriNet == null) throw new IllegalArgumentException(PETRI_NET_TO_EXECUTE_IS_NULL_OR_NOT_FOUND+"null");
		executablePetriNet = petriNet.getExecutablePetriNet(); 
		round = 0; 
		transitionsToFire = true; 
		previousState = executablePetriNet.getState(); 
		previousFiring = new Firing(round, "", previousState);; 
		animator = new PetriNetAnimator(executablePetriNet); 
	}

	public void setSeed(long seed) {
		this.random = new Random(seed); 
		animator.setRandom(random); 
	}

	public void setFiringLimit(int firingLimit) {
		this.firingLimit = firingLimit; 
	}

	public void run() {
		start(); 
		while ((round < firingLimit) && transitionsToFire()) {
			round++; 
			fireOneTransition();
		}
		end(); 
	}

	private void fireOneTransition() {
		Transition transition = null; 
		try {
			transition = animator.getRandomEnabledTransition(); 
			animator.fireTransition(transition); 
			firing = new Firing(round, transition.getId(), executablePetriNet.getState()); 
			changeSupport.firePropertyChange(UPDATED_STATE, previousFiring, firing); 
			previousFiring = firing; 
		} catch (RuntimeException e) {
			if (e.getMessage().equals(Animator.ERROR_NO_TRANSITIONS_TO_FIRE)) transitionsToFire = false;  
			else throw e; 
		}
	}
	
	private void end() {
		changeSupport.firePropertyChange(EXECUTION_COMPLETED, null, null); 
	}

	private boolean transitionsToFire() {
		return transitionsToFire;
	}

	private void start() {
		// previousFiring is Round 0
		changeSupport.firePropertyChange(EXECUTION_STARTED, previousFiring, executablePetriNetPlaces()); 
	}

	private Collection<String> executablePetriNetPlaces() {	
		SortedSet<String> sortedPlaces = new TreeSet<>(); 
		for (Place place : executablePetriNet.getPlaces()) {
			sortedPlaces.add(place.getId());
		}
		return sortedPlaces;
	}

	public static void setPrintStreamForTesting(PrintStream print) {
		PRINTSTREAM = print;
	}
    public static void main(String[] args) {
    	if (args.length != 4) {
    		printUsage(); 
//    		System.exit(1); 
    	}
    	else {
    		String petrinetName = args[0];  
    		String resultsFile = args[1]; 
    		String firingLimit = args[2];
    		String seed = args[3]; 

    		PetriNet petriNet = getPetriNet(petrinetName);  
    		PetriNetRunner runner = new PetriNetRunner(petriNet); 
    		runner.addPropertyChangeListener(getFiringWriter(resultsFile));
    		runner.setFiringLimit(getFiringLimit(firingLimit)); 
    		runner.setSeed(getSeed(seed)); 
    		setPrintStream(); 
    		PRINTSTREAM.println("PetriNetRunner:  executing "+petrinetName+", for a maximum of "+firingLimit+" transitions, using random seed "+seed+
    				", with results in "+resultsFile); 
    		runner.run(); 
    		PRINTSTREAM.println("PetriNetRunner:  complete.");
    		
    	}
    }
	private static long getSeed(String seed) {
		long longSeed = 0l; 
		try {
			Long.parseLong(seed); 
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException("PetriNetRunner:  seed parameter is not a long integer: "+seed); 
		}
		return longSeed;
	}
	private static int getFiringLimit(String limit) {
		int firingLimit = 0; 
		try {
			firingLimit = Integer.parseInt(limit); 
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException("PetriNetRunner:  firing limit parameter is not an integer: "+limit); 
		}
		return firingLimit;
	}
	private static FiringWriter getFiringWriter(String filename) {
		FiringWriter writer = null; 
		try {
			writer = new FiringWriter(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return writer; 
	}

	private static PetriNet getPetriNet(String petriNetName) {
		PetriNet net = TEST_NETS.get(petriNetName); 
		if (net == null) throw new IllegalArgumentException(PETRI_NET_TO_EXECUTE_IS_NULL_OR_NOT_FOUND+petriNetName); 
		else return net;
	}

	private static void printUsage() {
		setPrintStream(); 
		PRINTSTREAM.println("usage: PetriNetRunner [name of petri net to execute] [results filename] [maximum number of transitions to fire] [long integer seed for random transition selection]");
		PRINTSTREAM.println("number of transitions = 0:  no limit");
		PRINTSTREAM.println("seed = 0:  system creates new seed on each invocation ");

	}
	private static void setPrintStream() {
		if (PRINTSTREAM == null) PRINTSTREAM = System.out;
	}
}
