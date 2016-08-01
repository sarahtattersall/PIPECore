package uk.ac.imperial.pipe.io;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.extractProperty;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.annotation.XmlType;

import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.io.adapters.modelAdapter.PetriNetAdapter;
import uk.ac.imperial.pipe.io.adapters.modelAdapter.TestingThrowsPetriNetAdapter;
import uk.ac.imperial.pipe.models.petrinet.Arc;
import uk.ac.imperial.pipe.models.petrinet.ArcPoint;
import uk.ac.imperial.pipe.models.petrinet.ArcType;
import uk.ac.imperial.pipe.models.petrinet.ColoredToken;
import uk.ac.imperial.pipe.models.petrinet.Connectable;
import uk.ac.imperial.pipe.models.petrinet.DiscretePlace;
import uk.ac.imperial.pipe.models.petrinet.DiscreteTransition;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.RateParameter;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.Transition;
import utils.FileUtils;

public class PetriNetReaderTest {

 	
    private static final String DEFAULT_TOKEN = "Default";
    private static final String RED_TOKEN = "Red";
    PetriNetReader reader;

    @Before
    public void setUp() throws JAXBException {
        reader = new PetriNetIOImpl();  
//        reader = new PetriNetIOImpl(false, true); // equivalent to null constructor  
    }
//    @Test
    public void readNetForConvenientDebugging() throws  JAXBException, FileNotFoundException {
    	PetriNet petriNet = null; 
        petriNet = reader.read("/some/path/somepnml.xml");
        assertNotNull(petriNet); 
    }

    @Test
    public void createGSPN() throws  JAXBException, FileNotFoundException {
    	//XML has unparsed tags, and will exceed static error counter unless logging is forced
    	// see PetriNetIOImpl constructor
        reader = new PetriNetIOImpl(true, true);  // change to true, false to see error
        PetriNet petriNet = reader.read(FileUtils.fileLocation("/xml/gspn1.xml"));
        assertEquals(5, petriNet.getPlaces().size());
        assertEquals(5, petriNet.getTransitions().size());
        assertEquals(12, petriNet.getArcs().size());
        assertEquals(1, petriNet.getTokens().size());
    }

    @Test
    public void createsDefaultTokenIfDoesNotExist() throws JAXBException, FileNotFoundException {
        PetriNet petriNet = reader.read(FileUtils.fileLocation("/xml/noTokenPlace.xml"));
        assertTrue("Petri net has no tokens registered to it", petriNet.getTokens().size() > 0);
        Token expectedToken = new ColoredToken("Default", new Color(0, 0, 0));
        assertThat(petriNet.getTokens()).contains(expectedToken);
    }

    @Test
    public void createsDefaultTokenIfDoesNotExistAndPlaceMatchesThisToken()
            throws  JAXBException, FileNotFoundException {
        PetriNet petriNet = reader.read(FileUtils.fileLocation("/xml/noTokenPlace.xml"));
        assertThat(petriNet.getTokens()).isNotEmpty();
        assertThat(petriNet.getPlaces()).isNotEmpty();

        Place place = petriNet.getPlaces().iterator().next();
        assertThat(place.getTokenCounts()).containsKey("Default");
    }

    @Test
    public void losesSourceAndTargetArcPath() throws JAXBException, FileNotFoundException {
        PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getNormalArcWithWeight()));
        assertThat(petriNet.getArcs()).isNotEmpty();
        assertThat(petriNet.getArcs()).hasSize(1);
    }

    @Test
    public void keepsIntermediatePoints() throws JAXBException, FileNotFoundException {
        PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getNormalArcWithWeight()));
        ArcPoint expected = new ArcPoint(new Point2D.Double(87, 36), true);
        Arc<? extends Connectable, ? extends Connectable> arc = petriNet.getArcs().iterator().next();
        assertThat(arc.getArcPoints().contains(expected));
    }

    @Test
    public void createsPlace() throws JAXBException, FileNotFoundException {
        PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getSinglePlacePath()));

        assertThat(petriNet.getPlaces()).hasSize(1);
        assertThat(extractProperty("name").from(petriNet.getPlaces())).containsExactly("P0");
        assertThat(petriNet.getPlaces()).extracting("x", "y").containsExactly(tuple(255, 240));
        assertThat(petriNet.getPlaces()).extracting("markingXOffset", "markingYOffset").containsExactly(
                tuple(0.0, 0.0));
        assertThat(petriNet.getPlaces()).extracting("nameXOffset", "nameYOffset").containsExactly(tuple(5.0, 26.0));
        assertThat(extractProperty("capacity").from(petriNet.getPlaces())).containsExactly(0);
        assertThat(extractProperty("name").from(petriNet.getPlaces())).containsExactly("P0");
        assertThat(extractProperty("tokenCounts").from(petriNet.getPlaces())).hasSize(1);
    }

    @Test
    public void createsMarkingCorrectlyWithTokenMap() throws JAXBException, FileNotFoundException {

        PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getSinglePlacePath()));
        assertThat(petriNet.getPlaces()).hasSize(1);
        Place place = petriNet.getPlaces().iterator().next();

        Map<String, Integer> counts = place.getTokenCounts();
        assertThat(counts).containsEntry(RED_TOKEN, 1);
    }

    @Test
    public void createsMarkingIfNoTokensSet() throws JAXBException, FileNotFoundException {
        PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getNoPlaceTokenPath()));
        assertThat(petriNet.getPlaces()).isNotEmpty();
        Place place = petriNet.getPlaces().iterator().next();
        assertThat(place.getTokenCounts()).isEmpty();
    }

    @Test
    public void createsTransition() throws JAXBException, FileNotFoundException {

        PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getTransitionFile()));

        assertThat(petriNet.getTransitions()).extracting("x", "y").containsExactly(tuple(375, 225));
        assertThat(petriNet.getTransitions()).extracting("id", "name").containsExactly(tuple("T0", "T0"));
        assertThat(petriNet.getTransitions()).extracting("rate.expression").containsExactly("1.0");
        assertThat(petriNet.getTransitions()).extractingResultOf("isTimed").containsExactly(false);
        assertThat(petriNet.getTransitions()).extractingResultOf("isInfiniteServer").containsExactly(false);
        assertThat(petriNet.getTransitions()).extracting("nameXOffset", "nameYOffset").containsExactly(
                tuple(-5.0, 35.0));
        assertThat(petriNet.getTransitions()).extracting("priority").containsExactly(1);
    }

    @Test
    public void createsArc() throws JAXBException, FileNotFoundException {

        PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getArcNoWeightFile()));
        Place expectedSource = new DiscretePlace("P0", "P0");
        Transition expectedTarget = new DiscreteTransition("T0", "T0");
        assertThat(petriNet.getArcs()).extracting("type", "source", "target", "id").contains(
                tuple(ArcType.NORMAL, expectedSource, expectedTarget, "P0 TO T0"));
    }

    @Test
    public void createsCorrectMarkingIfWeightSpecified()
            throws JAXBException, FileNotFoundException {
        PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getNormalArcWithWeight()));
        Arc<? extends Connectable, ? extends Connectable> arc = petriNet.getArcs().iterator().next();

        Map<String, String> weights = arc.getTokenWeights();
        assertEquals(1, weights.size());

        assertTrue(weights.containsKey(DEFAULT_TOKEN));
        String weight = weights.get(DEFAULT_TOKEN);
        assertEquals("4", weight);
    }

    @Test
    public void createsMarkingWithCorrectToken() throws JAXBException, FileNotFoundException {
        PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getNormalArcWithWeight()));
        Arc<? extends Connectable, ? extends Connectable> arc = petriNet.getArcs().iterator().next();
        Map<String, String> weights = arc.getTokenWeights();
        assertEquals(1, weights.size());
        assertTrue(weights.containsKey(DEFAULT_TOKEN));
    }

    @Test
    public void createsInhibitoryArc() throws JAXBException, FileNotFoundException {
    	//XML has unparsed tags 
        reader = new PetriNetIOImpl(true, true);  // change to true, false to see error
        PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getInhibitorArcFile()));
        assertThat(petriNet.getArcs()).extracting("type").containsExactly(ArcType.INHIBITOR);
    }

    @Test
    public void createsRedToken() throws JAXBException, FileNotFoundException {
        PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getTokenFile()));
        Token redToken = new ColoredToken("red", new Color(255, 0, 0));
        assertThat(petriNet.getTokens()).containsExactly(redToken);
    }

    @Test
    public void createsAnnotation() throws JAXBException, FileNotFoundException {
        PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getAnnotationFile()));
        assertThat(petriNet.getAnnotations()).extracting("text", "x", "y", "height", "width").containsExactly(
                tuple("#P12s", 93, 145, 20, 48));
    }

    @Test
    public void createsRateParameter() throws JAXBException, FileNotFoundException {
        PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getRateParameterFile()));
        assertThat(petriNet.getRateParameters()).extracting("id","expression").containsExactly(
                tuple("rate0",  "5.0"));
    }

    @Test
    public void transitionReferencesRateParameter()
            throws PetriNetComponentNotFoundException, JAXBException, FileNotFoundException {
        PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getTransitionRateParameterFile()));
        RateParameter rateParameter = petriNet.getComponent("foo", RateParameter.class);
        Transition transition = petriNet.getComponent("T0", Transition.class);
        assertEquals(rateParameter, transition.getRate());
    }

    @Test
    public void rateParameterReferencesPlace()
    		throws PetriNetComponentNotFoundException, JAXBException, FileNotFoundException {
    	PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getRateParameterReferencesPlaceFile()));
        assertThat(petriNet.getRateParameters()).extracting("id","expression").containsExactly(
                tuple("rate1",  "#(P0)"));
    }
    @Test
    public void messagePrintedAndThrowsForFirstNonUnexpectedElementError() throws PetriNetComponentNotFoundException, JAXBException, FileNotFoundException {
    	reader = new TestingThrowsPetriNetIOImpl(false, true);  // true false 
    	try {
    		@SuppressWarnings("unused")
    		PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getInvalidPetriNetFile()));
    	} catch (UnmarshalException e) {
    		assertEquals("java.lang.RuntimeException: TestingThrowsPetriNetAdapter exception.", e.getMessage()); 
    	}
    	PetriNetValidationEventHandler handler = ((PetriNetIOImpl) reader).getEventHandler();  
    	assertEquals(2, handler.getFormattedEvents().size());
    	assertEquals("unexpected element message saved, but doesn't throw",true, handler.getFormattedEvents().get(0).unexpected); 
    	assertFalse(handler.printMessage(handler.getFormattedEvents().get(0)));
    	checkValidationMessage(handler);
    	assertEquals("second error not unexpected element, so throws",false, handler.getFormattedEvents().get(1).unexpected); 
    	assertEquals("PetriNetValidationEventHandler received a ValidationEvent, probably during processing by PetriNetIOImpl.  Details: \n" +
    			"Message: java.lang.RuntimeException: TestingThrowsPetriNetAdapter exception.\n" +
    			"Object: null\n" +
    			"URL: null\n" +
    			"Node: null\n" +
    			"Line: 6\n" +
    			"Column: 11\n" +
    			"Linked exception: java.lang.RuntimeException: TestingThrowsPetriNetAdapter exception.", handler.getFormattedEvents().get(1).formattedEvent);
    	assertTrue(handler.printMessage(handler.getFormattedEvents().get(1)));
    }
    @Test
    public void readsTokens() throws PetriNetComponentNotFoundException, JAXBException, FileNotFoundException {
    	PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getTwoTokenFile()));
    	Collection<Token> tokens = petriNet.getTokens();
    	assertEquals(2,tokens.size());
    }
    @Test
    // fails
    public void noMessagePrintedAndDoesntThrowWhenUnexpectedElement() throws PetriNetComponentNotFoundException, JAXBException, FileNotFoundException {
    	reader = new TestingPetriNetIOImpl(true, true);   
//    	reader = new PetriNetIOImpl(true, true);   
    	@SuppressWarnings("unused")
    	PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getInvalidPetriNetFile()));
    	PetriNetValidationEventHandler handler = ((PetriNetIOImpl) reader).getEventHandler();  
    	assertEquals(true, handler.getFormattedEvents().get(0).unexpected); 
    	assertFalse(handler.printMessage(handler.getFormattedEvents().get(0)));
    }
    
    @Test
    // fails
    public void messagePrintedButDoesntThrowWhenUnexpectedElement() throws PetriNetComponentNotFoundException, JAXBException, FileNotFoundException {
    	reader = new TestingPetriNetIOImpl(false, false); 
    	checkPrintedAndDoesntThrowWhenUnexpectedElement();
    }
    @Test
    public void messagePrintedWithoutThrowingWhenUnexpectedElement() throws PetriNetComponentNotFoundException, JAXBException, FileNotFoundException {
        reader = new TestingPetriNetIOImpl(true, false); 
        checkPrintedAndDoesntThrowWhenUnexpectedElement();
    }

	protected void checkPrintedAndDoesntThrowWhenUnexpectedElement()
			throws JAXBException, FileNotFoundException {
		@SuppressWarnings("unused")
		PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getInvalidPetriNetFile()));
        PetriNetValidationEventHandler handler = ((PetriNetIOImpl) reader).getEventHandler();  
    	checkValidationMessage(handler);
    	assertTrue(handler.printMessage(handler.getFormattedEvents().get(0)));
	}
	private void checkValidationMessage(PetriNetValidationEventHandler handler) {
		String message = handler.getFormattedEvents().get(0).formattedEvent;  
		assertTrue(message.startsWith("PetriNetValidationEventHandler received a ValidationEvent, probably during processing by PetriNetIOImpl.  Details: \n" +
    			"Message: unexpected element (uri:\"\", local:\"blah\"). Expected elements are "));
		assertTrue(message.endsWith(				
				"Object: null\n" +
				"URL: null\n" +
				"Node: null\n" +
				"Line: 4\n" +
				"Column: 16\n" +
				"Linked exception: null"));
		// the order of tags appears arbitrary between Java 7 & 8, 
		// and neither order follows @XmlType(propOrder ...) as specified in AdaptedPetriNet
		String[] tags = new String[]{"arc","definition","place","transition","token","labels" };
		for (int i = 0; i < tags.length; i++) {
			assertFalse("tag not found: "+tags[i],(message.indexOf(tags[i]) == -1));
		}
		assertEquals("tags may have been added or removed",334,message.length());
	}
    private class TestingPetriNetIOImpl extends PetriNetIOImpl {

		public TestingPetriNetIOImpl(boolean continueProcessing,
				boolean suppressUnexpectedElementMessages) throws JAXBException {
			super(continueProcessing, suppressUnexpectedElementMessages);
	    	petriNetValidationEventHandler = new TestingPetriNetValidationEventHandler(continueProcessing, suppressUnexpectedElementMessages); 
		}
		@Override
		protected void initialiseUnmarshaller() throws JAXBException {
			super.initialiseUnmarshaller();
		}
    }
    private class TestingThrowsPetriNetIOImpl extends TestingPetriNetIOImpl {
    	
    	public TestingThrowsPetriNetIOImpl(boolean continueProcessing,
    			boolean suppressUnexpectedElementMessages) throws JAXBException {
    		super(continueProcessing, suppressUnexpectedElementMessages);
    		petriNetValidationEventHandler = new TestingPetriNetValidationEventHandler(continueProcessing, suppressUnexpectedElementMessages); 
    	}
    	@Override
    	protected void initialiseUnmarshaller() throws JAXBException {
    		super.initialiseUnmarshaller();
    		getUnmarshaller().setAdapter(PetriNetAdapter.class, new TestingThrowsPetriNetAdapter()); 
    	}
    }
    private class TestingPetriNetValidationEventHandler extends PetriNetValidationEventHandler {

		public TestingPetriNetValidationEventHandler(
				boolean continueProcessing,
				boolean suppressUnexpectedElementMessages) {
			super(continueProcessing, suppressUnexpectedElementMessages);
		}
    	@Override
    	public void printMessages() {
    	}
    }
}
