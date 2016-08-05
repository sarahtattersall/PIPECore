package uk.ac.imperial.pipe.io;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import uk.ac.imperial.pipe.io.adapters.modelAdapter.ArcAdapter;
import uk.ac.imperial.pipe.io.adapters.modelAdapter.PlaceAdapter;
import uk.ac.imperial.pipe.io.adapters.modelAdapter.RateParameterAdapter;
import uk.ac.imperial.pipe.io.adapters.modelAdapter.TokenAdapter;
import uk.ac.imperial.pipe.io.adapters.modelAdapter.TokenSetIntegerAdapter;
import uk.ac.imperial.pipe.io.adapters.modelAdapter.TransitionAdapter;
import uk.ac.imperial.pipe.models.PetriNetHolder;
import uk.ac.imperial.pipe.models.petrinet.ColoredToken;
import uk.ac.imperial.pipe.models.petrinet.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.Transition;

/**
 * Petri net IO implementation that writes and reads a Petri net using JAXB
 */
public class PetriNetIOImpl implements PetriNetIO {

    /**
     * JAXB context initialised in constructor
     */
    private final JAXBContext context;

    /**
     * PetriNetValidationEventHandler used to process validation events 
     * Defaults to stopping processing in the event of a failure, but can be overridden to continue
     * (should only continue when testing)
     */
    protected PetriNetValidationEventHandler petriNetValidationEventHandler;

	private Unmarshaller unmarshaller;

    /**
     * Constructor that sets the context to the {@link uk.ac.imperial.pipe.models.PetriNetHolder}
     * Provides explicit control over validation processing:  
     * continueProcessing flag enables processing to continue in the event of validation failure
     * suppressUnexpectedElementMessages suppresses "unexpected element" messages from tags in the pnml
     *   that are not explicitly supported by this version of PIPE. Behavior is modified depending on value of 
     *   continueProcessing flag.  Only relevant during read operations
     *   
     * Cases: 
     * continue=true, suppress=true:  processing continues, all messages suppressed (suitable only for testing)
     * continue=true, suppress=false:  processing continues, all messages printed (suitable only for testing)
     * continue=false, suppress=true:  processing stops at first message other than "unexpected element"; 
     *     all "unexpected element" messages suppressed (default)
     * continue=false, suppress=false:  processing stops at first message other than "unexpected element"; 
     *     all messages printed
     * @param continueProcessing processing to continue in the event of validation failure
     * @param suppressUnexpectedElementMessages suppresses "unexpected element" messages
     * @throws JAXBException if JAXBContext cannot be created for PetriNetHolder
     */
    public PetriNetIOImpl(boolean continueProcessing, boolean suppressUnexpectedElementMessages) throws JAXBException {
    	context = JAXBContext.newInstance(PetriNetHolder.class);
    	petriNetValidationEventHandler = new PetriNetValidationEventHandler(continueProcessing, suppressUnexpectedElementMessages);
    	// setting log level to FINEST to force continued reporting of errors; otherwise, suppressed 
    	// after 10 errors in static field, generating unpredictable test side effects, under Java 1.8
    	// https://java.net/projects/jaxb/lists/commits/archive/2013-08/message/4
    	// https://java.net/projects/jaxb/lists/users/archive/2015-11/message/6
    	Logger.getLogger("com.sun.xml.internal.bind").setLevel(Level.FINEST);
    }

    /**
     * Constructor that sets the context to the {@link uk.ac.imperial.pipe.models.PetriNetHolder}
     * Default settings of the continueProcessing and suppressUnexpectedElementMessage flags:
     * processing stops at first message other than "unexpected element"; all "unexpected element" messages suppressed
     * 
     * @throws JAXBException if JAXBContext cannot be created for PetriNetHolder
     */
    public PetriNetIOImpl() throws JAXBException {
    	this(false, true); 
    }

    

	/**
     * Writes the specified petri net to the given path
     *
     * @param path where Petri net will be written
     * @param petriNet to write
	 * @throws IOException if path is not found or other IO error
	 * @throws JAXBException if Petri net cannot be marshalled
     */
    @Override
    public void writeTo(String path, PetriNet petriNet) throws JAXBException, IOException {
		writeTo(new FileWriter(new File(path)), petriNet);
    }

    /**
     * Writes the Petri net to the given stream
     *
     * @param stream where Petri net will be written
     * @param petriNet to write 
     * @throws JAXBException if Petri net cannot be marshalled
     */
    @Override
    public void writeTo(Writer stream, PetriNet petriNet) throws JAXBException {
        Marshaller m = context.createMarshaller();
        m.setEventHandler(getEventHandler()); 
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        PetriNetHolder holder = getPetriNetHolder();
        holder.addNet(petriNet);
        try {
        	m.marshal(holder, stream);
        	getEventHandler().printMessages(); 
		} catch (JAXBException e) {
			getEventHandler().printMessages(); 
			throw e; 
		}
    }

	protected PetriNetHolder getPetriNetHolder() {
		return new PetriNetHolder();
	}

    /**
     * Reads a Petri net from the given path
     *
     * @param path xml path containing a PNML representation of a Petri net
     * @return read Petri net
     */
    @Override
    public PetriNet read(String path) throws JAXBException, FileNotFoundException {
        initialiseUnmarshaller();
        getUnmarshaller().setEventHandler(getEventHandler()); 
        PetriNetHolder holder = null; 
        try {
        	holder = (PetriNetHolder) getUnmarshaller().unmarshal(new FileReader(path));
        	getEventHandler().printMessages(); 
		} catch (JAXBException e) {
			getEventHandler().printMessages(); 
			throw e;  
		} 
        PetriNet petriNet = holder.getNet(0);
        if (petriNet.getTokens().isEmpty()) {
            Token token = createDefaultToken();
            petriNet.addToken(token);
        }
        return petriNet;
    }

    /**
     * initialize unmarshaller with the correct adapters needed
     * @throws JAXBException if Petri net cannot be unmarshalled 
     */
    protected void initialiseUnmarshaller() throws JAXBException {

        unmarshaller = context.createUnmarshaller();
        Map<String, Place> places = new HashMap<>();
        Map<String, Transition> transitions = new HashMap<>();
        Map<String, Token> tokens = new HashMap<>();
        Map<String, FunctionalRateParameter> rateParameters = new HashMap<>();

        unmarshaller.setAdapter(new RateParameterAdapter(rateParameters));
        unmarshaller.setAdapter(new ArcAdapter(places, transitions));
        unmarshaller.setAdapter(new PlaceAdapter(places));
        unmarshaller.setAdapter(new TransitionAdapter(transitions, rateParameters));
        unmarshaller.setAdapter(new TokenAdapter(tokens));
        unmarshaller.setAdapter(new TokenSetIntegerAdapter(tokens));
    }

	protected PetriNetValidationEventHandler getEventHandler() {
		return petriNetValidationEventHandler;
	}

    /**
     * @return a new default token
     */
    private Token createDefaultToken() {
        return new ColoredToken("Default", new Color(0, 0, 0));
    }

	protected final Unmarshaller getUnmarshaller() {
		return unmarshaller;
	}

}
