package uk.ac.imperial.pipe.runner;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.pipe.animation.Animator;
import uk.ac.imperial.pipe.animation.PetriNetAnimator;
import uk.ac.imperial.pipe.exceptions.IncludeException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.io.FileUtils;
import uk.ac.imperial.pipe.io.IncludeHierarchyIO;
import uk.ac.imperial.pipe.io.IncludeHierarchyIOImpl;
import uk.ac.imperial.pipe.io.PetriNetIOImpl;
import uk.ac.imperial.pipe.io.PetriNetReader;
import uk.ac.imperial.pipe.models.petrinet.AbstractPetriNetPubSub;
import uk.ac.imperial.pipe.models.petrinet.DiscreteExternalTransition;
import uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet;
import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.state.State;

public class PetriNetRunner extends AbstractPetriNetPubSub implements Runner, PropertyChangeListener {

	private static Logger logger = LogManager.getLogger(PetriNetRunner.class);  
	private static final String PETRI_NET_RUNNER = "PetriNetRunner.";
	private static final String PETRI_NET_TO_EXECUTE_IS_NULL_OR_NOT_FOUND = "PetriNetRunner:  PetriNet to execute is null or not found: ";
	private static final String PETRI_NET_XML_COULD_NOT_BE_PARSED_SUCCESSFULLY_ = "PetriNetRunner:  PetriNet XML could not be parsed successfully: ";
	private static final String INCLUDE_HIERARCHY_EXCEPTION = "PetriNetRunner:  Error attempting to build include hierarchy: ";
	public static final String EXECUTION_STARTED = "execution started";
	public static final String UPDATED_STATE = "state updated";
	public static final String EXECUTION_COMPLETED = "execution complete";
	private static final String MARK_PLACE = "markPlace";
	private static final String LISTEN_FOR_TOKEN_CHANGES = "listenForTokenChanges";
	private static PrintStream PRINTSTREAM;
	private Random random;
	private int firingLimit;
	private ExecutablePetriNet executablePetriNet;
	private int round;
	private boolean transitionsToFire;
	private State previousState;
	private Animator animator;
	private Firing previousFiring;
	private Firing firing;
	private Map<String, List<PropertyChangeListener>> listenerMap;
	private List<TokenCount> pendingPlaceMarkings;
	private Map<String, Object> transitionContextMap;

	public PetriNetRunner(PetriNet petriNet) {
		if (petriNet == null) throw new IllegalArgumentException(PETRI_NET_TO_EXECUTE_IS_NULL_OR_NOT_FOUND+"null");
		executablePetriNet = petriNet.getExecutablePetriNet(); 
		executablePetriNet.addPropertyChangeListener(ExecutablePetriNet.PETRI_NET_REFRESHED_MESSAGE, this); 
		round = 0; 
		transitionsToFire = true; 
		previousState = executablePetriNet.getState(); 
		previousFiring = new Firing(round, "", previousState);; 
		animator = new PetriNetAnimator(executablePetriNet);
		listenerMap = new HashMap<>();
		pendingPlaceMarkings = new ArrayList<>();
		transitionContextMap = new HashMap<>(); 
		logger.info("creating PetriNetRunner for PetriNet "+petriNet.getName().getName());
	}
	public PetriNetRunner() {
	}
	public PetriNetRunner(String path) {
		this(getPetriNet(path)); 
	}
	@Override
	public void setSeed(long seed) {
		this.random = new Random(seed); 
		animator.setRandom(random); 
	}

	@Override
	public void setFiringLimit(int firingLimit) {
		this.firingLimit = firingLimit; 
	}

	@Override
	public void run() {
		logger.info("run ExecutablePetriNet "+executablePetriNet.getName().getName());
		start(); 
		while ((round < firingLimit) && transitionsToFire()) {
			round++; 
			fireOneTransition();
		}
		end(); 
	}
	@Override
	public void markPlace(String placeId, String token, int count) throws InterfaceException { 
		validateToken(token); 
		validatePlace(placeId, MARK_PLACE); 
		pendingPlaceMarkings.add(new TokenCount(placeId, token, count)); 
		logger.debug("received request to mark place "+placeId+" with "+count+" "+token+" tokens.");
	}
	private void validateToken(String requestedToken) throws InterfaceException {
		boolean found = false; 
		for (Token token : executablePetriNet.getTokens()) {
			if (token.getId().equalsIgnoreCase(requestedToken)) {
				found = true; 
			}
		}
		if (!found) {
			throw new InterfaceException(PETRI_NET_RUNNER+MARK_PLACE+": requested token does not exist in executable Petri net: "+requestedToken); 
		}
	}
	@Override
	public void listenForTokenChanges(PropertyChangeListener listener, String placeId) throws InterfaceException {
		validatePlace(placeId, LISTEN_FOR_TOKEN_CHANGES); 
		if (!(listenerMap.containsKey(placeId))) {
			listenerMap.put(placeId, new ArrayList<PropertyChangeListener>()); 
		}
		listenerMap.get(placeId).add(listener); 
		rebuildListeners();		
		logger.debug("received request from listener "+listener+" for token changes to place "+placeId);
	}
	private void validatePlace(String placeId, String location) throws InterfaceException {
		try {
			Place place = executablePetriNet.getComponent(placeId, Place.class);
			if (!place.getStatus().isExternal()) {
				throw new InterfaceException(PETRI_NET_RUNNER+location+": requested place is not externally accessible: "+placeId); 
			}
		} catch (PetriNetComponentNotFoundException e) {
			throw new InterfaceException(PETRI_NET_RUNNER+location+": requested place does not exist in executable Petri net: "+placeId); 
		} 
	}
	@Override
	public void setTransitionContext(String transitionId, Object object) {
		addContextAndPlaceMarkerToTransition(transitionId, object); 
		transitionContextMap.put(transitionId, object); 
	}
	protected void addContextAndPlaceMarkerToTransition(String transitionId, Object object) {
		if (object == null) {
			throw new IllegalArgumentException("PetriNetRunner:  set transition context requested but object provided was null");
		}
		try {
			Transition transition = executablePetriNet.getComponent(transitionId, Transition.class);
			if ((transition instanceof DiscreteExternalTransition)) { 
				((DiscreteExternalTransition) transition).setContextForClient(object); 
				((DiscreteExternalTransition) transition).setPlaceMarker(this); 
			}
			else {
				throw new IllegalArgumentException("PetriNetRunner:  set transition context may only be invoked for uk.ac.imperial.pipe.models.petrinet.DiscreteExternalTransition.  Requested component: "+transition.getClass().getName()); 
			}
		} catch (PetriNetComponentNotFoundException e) {
			throw new IllegalArgumentException("PetriNetRunner:  set transition context requested for a transition that does not exist in the executable petri net: "+transitionId); 
		}
		logger.debug("received request to add context "+object.toString()+" for external transition "+transitionId);
	}
	private void updateExternalTransitions() {
		Set<Entry<String, Object>> entries = transitionContextMap.entrySet(); 
		for (Entry<String, Object> entry : entries) {
			addContextAndPlaceMarkerToTransition(entry.getKey(), entry.getValue()); 
		}
	}

	private void rebuildListeners() {
		Set<Entry<String, List<PropertyChangeListener>>> entries = listenerMap.entrySet(); 
		for (Entry<String, List<PropertyChangeListener>> entry : entries) {
			Place place = null; 
			try {
				place = executablePetriNet.getComponent(entry.getKey(), Place.class);
				List<PropertyChangeListener> listeners = entry.getValue(); 
				for (PropertyChangeListener propertyChangeListener : listeners) {
					place.addPropertyChangeListener(propertyChangeListener); 
				}
			} catch (PetriNetComponentNotFoundException e) {
				e.printStackTrace(); // logic error, since we should guard against this at listen request
			} 
			
		}
	}

	private void markPendingPlaces() {
		Iterator<TokenCount> iterator = pendingPlaceMarkings.iterator(); 
		TokenCount tokenCount = null; 
		while (iterator.hasNext()) {
			tokenCount = iterator.next(); 
			try {
				Place place = executablePetriNet.getComponent(tokenCount.placeId, Place.class);
				place.setTokenCount(tokenCount.token, tokenCount.count);
			} catch (PetriNetComponentNotFoundException e) {
				e.printStackTrace();  // logic error, since we should guard against this at marking request
			}
			iterator.remove(); 
		}
	}

	protected void fireOneTransition() {
		markPendingPlaces();
		Transition transition = null; 
		try {
			transition = animator.getRandomEnabledTransition(); 
			logger.debug("about to fire transition "+transition.getId()); 
			animator.fireTransition(transition); 
			firing = new Firing(round, transition.getId(), executablePetriNet.getState()); 
			changeSupport.firePropertyChange(UPDATED_STATE, previousFiring, firing);
			previousFiring = firing; 
		} catch (RuntimeException e) { //TODO rework this:  no transitions shouldnt be an exception 
			if ((e.getMessage() != null) && (e.getMessage().equals(Animator.ERROR_NO_TRANSITIONS_TO_FIRE))) transitionsToFire = false;  
			else throw e; 
		}
	}
	
	private void end() {
		changeSupport.firePropertyChange(EXECUTION_COMPLETED, null, null); 
		logger.debug(EXECUTION_COMPLETED); 
	}

	private boolean transitionsToFire() {
		return transitionsToFire;
	}

	private void start() {
		// previousFiring is Round 0
		changeSupport.firePropertyChange(EXECUTION_STARTED, previousFiring, executablePetriNetPlaces()); 
		logger.debug(EXECUTION_STARTED); 
	}

	private Collection<String> executablePetriNetPlaces() {	
		SortedSet<String> sortedPlaces = new TreeSet<>(); 
		for (Place place : executablePetriNet.getPlaces()) {
			sortedPlaces.add(place.getId());
		}
		return sortedPlaces;
	}
	protected Map<String, List<PropertyChangeListener>> getListenerMap() {
		return listenerMap;
	}
	protected List<TokenCount> getPendingPlaceMarkings() {
		return pendingPlaceMarkings;
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

	public static PetriNet getPetriNet(String petriNetName) {
		PetriNet net = null;  
		try {
			net = readFileAsIncludeHierarchy(petriNetName); 
		} catch (JAXBException e) {
	        try {
	        	net = readFileAsSinglePetriNet(petriNetName);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				throw new IllegalArgumentException(PETRI_NET_TO_EXECUTE_IS_NULL_OR_NOT_FOUND+petriNetName); 
			} catch (JAXBException e1) {
				e1.printStackTrace();
				throw new IllegalArgumentException(PETRI_NET_XML_COULD_NOT_BE_PARSED_SUCCESSFULLY_+petriNetName); 
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(PETRI_NET_TO_EXECUTE_IS_NULL_OR_NOT_FOUND+petriNetName); 
		} catch (IncludeException e) {
			//TODO test 
			e.printStackTrace();
			throw new IllegalArgumentException(INCLUDE_HIERARCHY_EXCEPTION+petriNetName); 
		}
		return net;
	}
	protected static PetriNet readFileAsIncludeHierarchy(String petriNetName)
			throws JAXBException, FileNotFoundException, IncludeException {
		IncludeHierarchyIO reader = new IncludeHierarchyIOImpl();
		IncludeHierarchy include = reader.read(petriNetName);
		return include.getPetriNet();
	}
	protected static PetriNet readFileAsSinglePetriNet(String petriNetName)
			throws JAXBException, FileNotFoundException {
		PetriNetReader reader = new PetriNetIOImpl();
		return reader.read(FileUtils.fileLocation(petriNetName));
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
	protected static void setPrintStreamForTesting(PrintStream print) {
		PRINTSTREAM = print;
	}
	private class TokenCount {
		public String placeId; 
		public String token;
		public int count;
		public TokenCount(String placeId, String token, int count) {
			this.placeId = placeId;
			this.token = token; 
			this.count = count; 
		}
		@Override
		public String toString() {
			return "TokenCount: "+this.placeId+" "+this.token+" "+this.count;
		}
	}
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(ExecutablePetriNet.PETRI_NET_REFRESHED_MESSAGE)) {
			rebuildListeners();	
			updateExternalTransitions(); 
			logger.debug("received "+ExecutablePetriNet.PETRI_NET_REFRESHED_MESSAGE+"; rebuilt listeners and updated external transitions.");
		}
		else {
			throw new RuntimeException("PetriNetRunner received unexpected event: "+evt.getPropertyName());
		}
	}
}
