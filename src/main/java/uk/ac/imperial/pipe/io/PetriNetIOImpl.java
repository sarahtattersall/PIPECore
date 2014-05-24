package uk.ac.imperial.pipe.io;

import uk.ac.imperial.pipe.io.adapters.modelAdapter.*;
import uk.ac.imperial.pipe.models.PetriNetHolder;
import uk.ac.imperial.pipe.models.component.place.Place;
import uk.ac.imperial.pipe.models.component.rate.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.component.token.ColoredToken;
import uk.ac.imperial.pipe.models.component.token.Token;
import uk.ac.imperial.pipe.models.component.transition.Transition;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.parsers.UnparsableException;

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

public class PetriNetIOImpl implements PetriNetIO {

    private final JAXBContext context;

    public PetriNetIOImpl() throws JAXBException {
        context = JAXBContext.newInstance(PetriNetHolder.class);
    }

    @Override
    public void writeTo(String path, PetriNet petriNet) {
        try {
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            PetriNetHolder holder = new PetriNetHolder();
            holder.addNet(petriNet);
            m.marshal(holder, new File(path));
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeTo(Writer stream, PetriNet petriNet) {
        try {
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            PetriNetHolder holder = new PetriNetHolder();
            holder.addNet(petriNet);
            m.marshal(holder, stream);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public PetriNet read(String path) throws UnparsableException {

        try {
            Unmarshaller um = initialiseUnmarshaller();
            PetriNetHolder holder = (PetriNetHolder) um.unmarshal(new FileReader(path));
            PetriNet petriNet = holder.getNet(0);

            if (petriNet.getTokens().size() == 0) {
                Token token = createDefaultToken();
                petriNet.addToken(token);
            }


            return petriNet;

        } catch (JAXBException | FileNotFoundException e) {
            e.printStackTrace();
            throw new UnparsableException("Could not read PetriNet file properly!");
        }
    }

    private Token createDefaultToken() {
        return new ColoredToken("Default", new Color(0, 0, 0));
    }

    private Unmarshaller initialiseUnmarshaller() throws JAXBException {

        Unmarshaller um = context.createUnmarshaller();

        Map<String, Place> places = new HashMap<>();
        Map<String, Transition> transitions = new HashMap<>();
        Map<String, Token> tokens = new HashMap<>();
        Map<String, FunctionalRateParameter> rateParameters = new HashMap<>();

        um.setAdapter(new RateParameterAdapter(rateParameters));
        um.setAdapter(new ArcAdapter(places, transitions, tokens));
        um.setAdapter(new PlaceAdapter(places, tokens));
        um.setAdapter(new TransitionAdapter(transitions, rateParameters));
        um.setAdapter(new TokenAdapter(tokens));
        um.setAdapter(new TokenSetIntegerAdapter(tokens));
        return um;
    }

}
