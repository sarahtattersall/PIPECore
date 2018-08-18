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

import uk.ac.imperial.pipe.exceptions.IncludeException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.io.FileUtils;
import uk.ac.imperial.pipe.io.IncludeHierarchyIO;
import uk.ac.imperial.pipe.io.IncludeHierarchyIOImpl;
import uk.ac.imperial.pipe.io.PetriNetIOImpl;
import uk.ac.imperial.pipe.io.PetriNetReader;
import uk.ac.imperial.pipe.io.XmlFileEnum;
import uk.ac.imperial.pipe.models.petrinet.AbstractPetriNetPubSub;
import uk.ac.imperial.pipe.models.petrinet.Animator;
import uk.ac.imperial.pipe.models.petrinet.DiscreteExternalTransition;
import uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet;
import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.PetriNetAnimator;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.state.State;

public class PetriNetRunner extends AbstractPetriNetPubSub implements Runner, PropertyChangeListener {

    protected static final String PETRINET_CANNOT_BE_RUN_AFTER_IT_HAS_COMPLETED_EXECUTION = "Petrinet cannot be run after it has completed execution.  Create another runner and start from the beginning.";
    protected static Logger logger = LogManager.getLogger(PetriNetRunner.class);
    protected static final String PETRI_NET_RUNNER_DOT = "PetriNetRunner.";
    protected static final String PETRI_NET_RUNNER = "PetriNetRunner:  ";
    protected static final String PETRI_NET_TO_EXECUTE_IS_NULL = PETRI_NET_RUNNER + "PetriNet to execute is null: ";
    protected static final String PETRI_NET_XML_COULD_NOT_BE_PARSED_SUCCESSFULLY_ = PETRI_NET_RUNNER +
            "PetriNet XML could not be parsed successfully: ";
    protected static final String INCLUDE_HIERARCHY_EXCEPTION = PETRI_NET_RUNNER +
            "Error attempting to build include hierarchy: ";
    public static final String EXECUTION_STARTED = "execution started";
    public static final String UPDATED_STATE = "state updated";
    public static final String EXECUTION_COMPLETED = "execution complete";
    private static final String MARK_PLACE = "markPlace";
    private static final String LISTEN_FOR_TOKEN_CHANGES = "listenForTokenChanges";
    private static PrintStream PRINTSTREAM;
    private Random random;
    private int firingLimit;
    //TODO: executablePN should be protected
    public ExecutablePetriNet executablePetriNet;
    protected int round;
    private State previousState;
    protected Animator animator;
    protected Firing previousFiring;
    protected Firing firing;
    private Map<String, List<PropertyChangeListener>> listenerMap;
    private List<TokenCount> pendingPlaceMarkings;
    private Map<String, Object> transitionContextMap;
    private boolean waitForExternalInput;
    private boolean started;
    private boolean ended;

    public PetriNetRunner(PetriNet petriNet) {
        if (petriNet == null)
            throw new IllegalArgumentException(PETRI_NET_TO_EXECUTE_IS_NULL + "null");
        executablePetriNet = petriNet.getExecutablePetriNet();
        executablePetriNet.addPropertyChangeListener(ExecutablePetriNet.PETRI_NET_REFRESHED_MESSAGE, this);
        round = 0;
        //transitionsToFire = true;
        previousState = executablePetriNet.getState();
        previousFiring = new Firing(round, "", previousState);
        ;
        animator = new PetriNetAnimator(executablePetriNet);
        listenerMap = new HashMap<>();
        pendingPlaceMarkings = new ArrayList<>();
        transitionContextMap = new HashMap<>();
        ended = false;
        started = false;
        logger.info("creating PetriNetRunner for PetriNet " + petriNet.getName().getName());
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
        if (ended)
            throw new IllegalStateException(PETRINET_CANNOT_BE_RUN_AFTER_IT_HAS_COMPLETED_EXECUTION);
        logger.info("run ExecutablePetriNet " + executablePetriNet.getName().getName());
        start();
        if (!runContinue()) {
            end();
        }
    }

    private boolean runContinue() {
        boolean transitionsToFire = true;
        while ((round < firingLimit) && transitionsToFire) {
            round++;
            transitionsToFire = fireOneTransition();
        }
        return waitForExternalInput;

    }

    @Override
    public void markPlace(String placeId, String token, int count) throws InterfaceException {
        validateToken(token);
        validatePlace(placeId, MARK_PLACE);
        pendingPlaceMarkings.add(new TokenCount(placeId, token, count));
        logger.debug("received request to mark place " + placeId + " with " + count + " " + token + " tokens.");
    }

    private void validateToken(String requestedToken) throws InterfaceException {
        boolean found = false;
        for (Token token : executablePetriNet.getTokens()) {
            if (token.getId().equalsIgnoreCase(requestedToken)) {
                found = true;
            }
        }
        if (!found) {
            throw new InterfaceException(PETRI_NET_RUNNER_DOT + MARK_PLACE +
                    ": requested token does not exist in executable Petri net: " + requestedToken);
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
        logger.debug("received request from listener " + listener + " for token changes to place " + placeId);
    }

    private void validatePlace(String placeId, String location) throws InterfaceException {
        try {
            Place place = executablePetriNet.getComponent(placeId, Place.class);
            if (!place.getStatus().isExternal()) {
                throw new InterfaceException(
                        PETRI_NET_RUNNER_DOT + location + ": requested place is not externally accessible: " + placeId);
            }
        } catch (PetriNetComponentNotFoundException e) {
            throw new InterfaceException(PETRI_NET_RUNNER_DOT + location +
                    ": requested place does not exist in executable Petri net: " + placeId);
        }
    }

    @Override
    public void setTransitionContext(String transitionId, Object object) {
        addContextAndPlaceMarkerToTransition(transitionId, object);
        transitionContextMap.put(transitionId, object);
    }

    protected void addContextAndPlaceMarkerToTransition(String transitionId, Object object) {
        if (object == null) {
            throw new IllegalArgumentException(
                    "PetriNetRunner:  set transition context requested but object provided was null");
        }
        try {
            Transition transition = executablePetriNet.getComponent(transitionId, Transition.class);
            if ((transition instanceof DiscreteExternalTransition)) {
                ((DiscreteExternalTransition) transition).setContextForClient(object);
                ((DiscreteExternalTransition) transition).setPlaceMarker(this);
            } else {
                throw new IllegalArgumentException(
                        "PetriNetRunner:  set transition context may only be invoked for uk.ac.imperial.pipe.models.petrinet.DiscreteExternalTransition.  Requested component: " +
                                transition.getClass().getName());
            }
        } catch (PetriNetComponentNotFoundException e) {
            throw new IllegalArgumentException(
                    "PetriNetRunner:  set transition context requested for a transition that does not exist in the executable petri net: " +
                            transitionId);
        }
        logger.debug("received request to add context " + object.toString() + " for external transition " +
                transitionId);
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
                logger.debug("marking pending place: " + place.getId() + " with token " + tokenCount.token +
                        " and count " + tokenCount.count);
            } catch (PetriNetComponentNotFoundException e) {
                e.printStackTrace(); // logic error, since we should guard against this at marking request
            }
            iterator.remove();
        }
        if (tokenCount != null) {
            // TODO: Can token counts can be decreased?
            // This should be checked too.
            this.executablePetriNet.getTimingQueue()
                    .queueEnabledTimedTransitions(this.executablePetriNet.getEnabledTimedTransitions());
            //					this.executablePetriNet.getTimedState().getEnabledTimedTransitions() );
        }
    }

    protected boolean fireOneTransition() {
        markPendingPlaces();
        Transition transition = null;
        transition = animator.getRandomEnabledTransition();
        if (transition == null) {
            logger.debug("no enabled transitions to fire");
            return false;
        } else {
            logger.debug("about to fire transition " + transition.getId());
            animator.fireTransition(transition);
            firing = new Firing(round, transition.getId(), executablePetriNet.getState());
            changeSupport.firePropertyChange(UPDATED_STATE, previousFiring, firing);
            previousFiring = firing;
            return true;
        }
    }

    protected void end() {
        ended = true;
        changeSupport.firePropertyChange(EXECUTION_COMPLETED, null, null);
        logger.debug(EXECUTION_COMPLETED);
    }

    protected void start() {
        // previousFiring is Round 0
        if (!started) {
            changeSupport.firePropertyChange(EXECUTION_STARTED, previousFiring, executablePetriNetPlaces());
            logger.debug(EXECUTION_STARTED);
            started = true;
        }
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
        } else {
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
            PRINTSTREAM.println("PetriNetRunner:  executing " + petrinetName + ", for a maximum of " + firingLimit +
                    " transitions, using random seed " + seed +
                    ", with results in " + resultsFile);
            runner.run();
            PRINTSTREAM.println("PetriNetRunner:  complete.");

        }
    }

    private static long getSeed(String seed) {
        long longSeed = 0l;
        try {
            Long.parseLong(seed);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("PetriNetRunner:  seed parameter is not a long integer: " + seed);
        }
        return longSeed;
    }

    private static int getFiringLimit(String limit) {
        int firingLimit = 0;
        try {
            firingLimit = Integer.parseInt(limit);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("PetriNetRunner:  firing limit parameter is not an integer: " + limit);
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
        XmlFileEnum xmlFileEnum = determineFileType(petriNetName);
        try {
            switch (xmlFileEnum) {
            case PETRI_NET:
                net = readFileAsSinglePetriNet(petriNetName);
                break;
            case INCLUDE_HIERARCHY:
                net = readFileAsIncludeHierarchy(petriNetName);
                break;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(PETRI_NET_RUNNER + e.getMessage());
        }
        return net;
    }

    protected static XmlFileEnum determineFileType(String petriNetName) {
        PetriNetReader reader;
        XmlFileEnum xmlFileEnum = null;
        try {
            reader = new PetriNetIOImpl();
            xmlFileEnum = reader.determineFileType(petriNetName);
        } catch (Exception e) {
            throw new IllegalArgumentException(PETRI_NET_RUNNER + e.getMessage());
        }
        return xmlFileEnum;
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
        PRINTSTREAM
                .println("usage: PetriNetRunner [name of petri net to execute] [results filename] [maximum number of transitions to fire] [long integer seed for random transition selection]");
        PRINTSTREAM.println("number of transitions = 0:  no limit");
        PRINTSTREAM.println("seed = 0:  system creates new seed on each invocation ");

    }

    private static void setPrintStream() {
        if (PRINTSTREAM == null)
            PRINTSTREAM = System.out;
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
            return "TokenCount: " + this.placeId + " " + this.token + " " + this.count;
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(ExecutablePetriNet.PETRI_NET_REFRESHED_MESSAGE)) {
            rebuildListeners();
            updateExternalTransitions();
            logger.debug("received " + ExecutablePetriNet.PETRI_NET_REFRESHED_MESSAGE +
                    "; rebuilt listeners and updated external transitions.");
        } else {
            throw new RuntimeException("PetriNetRunner received unexpected event: " + evt.getPropertyName());
        }
    }

    @Override
    public void setWaitForExternalInput(boolean wait) {
        this.waitForExternalInput = wait;
    }
}
