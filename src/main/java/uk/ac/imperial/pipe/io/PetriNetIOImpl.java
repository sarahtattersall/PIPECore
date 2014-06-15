package uk.ac.imperial.pipe.io;

import uk.ac.imperial.pipe.io.adapters.modelAdapter.*;
import uk.ac.imperial.pipe.models.PetriNetHolder;
import uk.ac.imperial.pipe.models.petrinet.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * Petri net IO implementation that writes and reads a Petri net using JAXB
 */
public final class PetriNetIOImpl implements PetriNetIO {

    /**
     * JAXB context initialised in constructor
     */
    private final JAXBContext context;

    /**
     * Constructor that sets the context to the {@link uk.ac.imperial.pipe.models.PetriNetHolder}
     *
     * @throws JAXBException
     */
    public PetriNetIOImpl() throws JAXBException {
        context = JAXBContext.newInstance(PetriNetHolder.class);
    }

    /**
     * Writes the specified petri net to the given path
     *
     * @param path
     * @param petriNet
     */
    @Override
    public void writeTo(String path, PetriNet petriNet) throws JAXBException {
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        PetriNetHolder holder = new PetriNetHolder();
        holder.addNet(petriNet);
        m.marshal(holder, new File(path));
    }

    /**
     * Writes the Petri net to the given stream
     *
     * @param stream
     * @param petriNet
     */
    @Override
    public void writeTo(Writer stream, PetriNet petriNet) throws JAXBException {
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        PetriNetHolder holder = new PetriNetHolder();
        holder.addNet(petriNet);
        m.marshal(holder, stream);
    }

    /**
     * Reads a Petri net from the given path
     *
     * @param path xml path containing a PNML representation of a Petri net
     * @return read Petri net
     */
    @Override
    public PetriNet read(String path) throws JAXBException, FileNotFoundException {
        Unmarshaller um = initialiseUnmarshaller();
        PetriNetHolder holder = (PetriNetHolder) um.unmarshal(new FileReader(path));
        PetriNet petriNet = holder.getNet(0);
        if (petriNet.getTokens().isEmpty()) {
            Token token = createDefaultToken();
            petriNet.addToken(token);
        }
        return petriNet;
    }

    /**
     * @return initialised unmarshaller with the correct adapters needed
     * @throws JAXBException
     */
    private Unmarshaller initialiseUnmarshaller() throws JAXBException {

        Unmarshaller um = context.createUnmarshaller();

        Map<String, Place> places = new HashMap<>();
        Map<String, Transition> transitions = new HashMap<>();
        Map<String, Token> tokens = new HashMap<>();
        Map<String, FunctionalRateParameter> rateParameters = new HashMap<>();

        um.setAdapter(new RateParameterAdapter(rateParameters));
        um.setAdapter(new ArcAdapter(places, transitions));
        um.setAdapter(new PlaceAdapter(places));
        um.setAdapter(new TransitionAdapter(transitions, rateParameters));
        um.setAdapter(new TokenAdapter(tokens));
        um.setAdapter(new TokenSetIntegerAdapter(tokens));
        return um;
    }

    /**
     * @return a new default token
     */
    private Token createDefaultToken() {
        return new ColoredToken("Default", new Color(0, 0, 0));
    }

}
