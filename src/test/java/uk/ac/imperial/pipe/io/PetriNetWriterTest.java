package uk.ac.imperial.pipe.io;

import java.awt.Color;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Collection;

import javax.xml.bind.JAXBException;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.SAXException;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.models.petrinet.AnnotationImpl;
import uk.ac.imperial.pipe.models.petrinet.ColoredToken;
import uk.ac.imperial.pipe.models.petrinet.DiscretePlace;
import uk.ac.imperial.pipe.models.petrinet.DiscreteTransition;
import uk.ac.imperial.pipe.models.petrinet.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.petrinet.NormalRate;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.Transition;

public class PetriNetWriterTest extends XMLTestCase {
    PetriNetWriter writer;

    @Override
    public void setUp() throws JAXBException {
        XMLUnit.setIgnoreWhitespace(true);
        writer = new PetriNetIOImpl();
    }

    public void testInvalidNetGeneratesUnderstandableMessage() throws Exception {
    	writer = new PetriNetIOImpl(true, true);
    	PetriNet petriNet = new InvalidPetriNet();
    	StringWriter stringWriter = new StringWriter();
    	writer.writeTo(stringWriter, petriNet);
    	assertTrue(((PetriNetIOImpl) writer).getEventHandler().getFormattedEvents().get(0).formattedEvent.startsWith(
    		"PetriNetValidationEventHandler received a ValidationEvent, probably during processing by PetriNetIOImpl.  Details: \n" +
    	    "Message: TestingInvalidPetriNet threw an exception for testing"));
    }
    
    public void testMarshalsPlace() throws Exception {
        PetriNet petriNet = new PetriNet();
        Token token = new ColoredToken("Red", new Color(255, 0, 0));
        Place place = new DiscretePlace("P0", "P0");
        place.setX(255);
        place.setY(240);
        place.setNameXOffset(5);
        place.setNameYOffset(26);
        place.setTokenCount(token.getId(), 1);
        petriNet.addToken(token);
        petriNet.addPlace(place);
        assertResultsEqual(XMLUtils.getSinglePlacePath(), petriNet);
    }
    

    private void assertResultsEqual(String expectedPath, PetriNet petriNet)
            throws IOException, SAXException, JAXBException, URISyntaxException {
        StringWriter stringWriter = new StringWriter();
        writer.writeTo(stringWriter, petriNet);

        String expected = XMLUtils.readFile(expectedPath, Charset.defaultCharset());

        String actual = stringWriter.toString();
        assertXMLEqual(expected, actual);
    }

    public void testMarshalsTransition() throws Exception {
        PetriNet petriNet = new PetriNet();
        Transition transition = new DiscreteTransition("T0", "T0");
        transition.setX(375);
        transition.setY(225);
        transition.setNameXOffset(-5.0);
        transition.setNameYOffset(35.0);
        transition.setRate(new NormalRate("1.0"));
        transition.setTimed(false);
        transition.setInfiniteServer(false);
        transition.setPriority(1);
        petriNet.addTransition(transition);
        assertResultsEqual(XMLUtils.getTransitionFile(), petriNet);
    }

    public void testMarshalsTransitionWithRateParameter()
            throws Exception {
        PetriNet petriNet = new PetriNet();
        FunctionalRateParameter rateParameter = new FunctionalRateParameter("6.0", "foo", "foo");

        Transition transition = new DiscreteTransition("T0", "T0");
        transition.setX(435);
        transition.setY(180);
        transition.setNameXOffset(-5.0);
        transition.setNameYOffset(35.0);
        transition.setRate(rateParameter);
        transition.setTimed(true);
        transition.setInfiniteServer(false);
        transition.setPriority(1);

        petriNet.addTransition(transition);
        petriNet.addRateParameter(rateParameter);

        assertResultsEqual(XMLUtils.getTransitionRateParameterFile(), petriNet);
    }

    public void testMarshalsArc() throws Exception {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
                APlace.withId("P0").locatedAt(0, 0)).and(AnImmediateTransition.withId("T0").locatedAt(0, 0)).andFinally(
                ANormalArc.withSource("P0").andTarget("T0").and("4", "Default").tokens());


        assertResultsEqual(XMLUtils.getNormalArcWithWeight(), petriNet);
    }


    public void testMarshalsToken() throws Exception {
        PetriNet petriNet = new PetriNet();
        Token token = new ColoredToken("red", new Color(255, 0, 0));
        petriNet.add(token);
        assertResultsEqual(XMLUtils.getTokenFile(), petriNet);
    }

    public void testMarshalsAnnotation() throws Exception {
        PetriNet petriNet = new PetriNet();
        AnnotationImpl annotation = new AnnotationImpl(93, 145, "#P12s", 48, 20, false);
        petriNet.addAnnotation(annotation);
        assertResultsEqual(XMLUtils.getAnnotationFile(), petriNet);
    }
    private class InvalidPetriNet extends PetriNet {
    	@Override
    	public Collection<Token> getTokens() {
    		throw new RuntimeException("TestingInvalidPetriNet threw an exception for testing.");
    	}
    }
}


