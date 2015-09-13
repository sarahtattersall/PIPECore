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
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.Arc;
import uk.ac.imperial.pipe.models.petrinet.ArcPoint;
import uk.ac.imperial.pipe.models.petrinet.ArcType;
import uk.ac.imperial.pipe.models.petrinet.ColoredToken;
import uk.ac.imperial.pipe.models.petrinet.Connectable;
import uk.ac.imperial.pipe.models.petrinet.DiscreteExternalTransition;
import uk.ac.imperial.pipe.models.petrinet.DiscretePlace;
import uk.ac.imperial.pipe.models.petrinet.DiscreteTransition;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.PlaceStatus;
import uk.ac.imperial.pipe.models.petrinet.PlaceStatusInterface;
import uk.ac.imperial.pipe.models.petrinet.RateParameter;
import uk.ac.imperial.pipe.models.petrinet.TestingExternalTransition;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.Transition;

public class PetriNetReaderTest {

    private static final String DEFAULT_TOKEN = "Default";
    private static final String RED_TOKEN = "Red";
    PetriNetReader reader;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
	private PrintStream err;

    
    @Before
    public void setUp() throws JAXBException {
        reader = new PetriNetIOImpl();  
        err = System.err; // see throwsIfFileNotValidXml
    }
    @After
    public void tearDown() {
    	System.setErr(err); 
    }
//    @Test
    public void readNetForConvenientDebugging() throws  JAXBException, FileNotFoundException {
    	PetriNet petriNet = null; 
        petriNet = reader.read("/some/path/somepnml.xml");
        assertNotNull(petriNet); 
    }
    //TODO test PN name / id 
    @Test
    public void createGSPN() throws  JAXBException, FileNotFoundException {
    	//XML has unparsed tags, and will exceed static error counter unless logging is forced
    	// see PetriNetIOImpl constructor
        reader = new PetriNetIOImpl(true, true);  // change to true, false to see error
        PetriNet petriNet = reader.read(FileUtils.resourceLocation("/xml/gspn1.xml"));
//SJDclean=======
//    	//XML has unparsed tags 
//        reader = new PetriNetIOImpl(true, true);  // change to true, false to see error
//        PetriNet petriNet = reader.read(FileUtils.fileLocation("/xml/gspn1.xml"));
//>>>>>>> fixes for issues #22,24.  Update POM to 1.1.0-SNAPSHOT
        assertEquals(5, petriNet.getPlaces().size());
        assertEquals(5, petriNet.getTransitions().size());
        assertEquals(12, petriNet.getArcs().size());
        assertEquals(1, petriNet.getTokens().size());
        assertEquals("Net-One", petriNet.getNameValue()); 
    }

    @Test
    public void createsDefaultTokenIfDoesNotExist() throws JAXBException, FileNotFoundException {
        PetriNet petriNet = reader.read(FileUtils.resourceLocation("/xml/noTokenPlace.xml"));
        assertTrue("Petri net has no tokens registered to it", petriNet.getTokens().size() > 0);
        Token expectedToken = new ColoredToken("Default", new Color(0, 0, 0));
        assertThat(petriNet.getTokens()).contains(expectedToken);
    }

    @Test
    public void createsDefaultTokenIfDoesNotExistAndPlaceMatchesThisToken()
            throws  JAXBException, FileNotFoundException {
        PetriNet petriNet = reader.read(FileUtils.resourceLocation("/xml/noTokenPlace.xml"));
        assertThat(petriNet.getTokens()).isNotEmpty();
        assertThat(petriNet.getPlaces()).isNotEmpty();

        Place place = petriNet.getPlaces().iterator().next();
        assertThat(place.getTokenCounts()).containsKey("Default");
    }

    @Test
    public void losesSourceAndTargetArcPath() throws JAXBException, FileNotFoundException {
        PetriNet petriNet = reader.read(FileUtils.resourceLocation(XMLUtils.getNormalArcWithWeight()));
        assertThat(petriNet.getArcs()).isNotEmpty();
        assertThat(petriNet.getArcs()).hasSize(1);
    }

    @Test
    public void keepsIntermediatePoints() throws JAXBException, FileNotFoundException {
        PetriNet petriNet = reader.read(FileUtils.resourceLocation(XMLUtils.getNormalArcWithWeight()));
        ArcPoint expected = new ArcPoint(new Point2D.Double(87, 36), true);
        Arc<? extends Connectable, ? extends Connectable> arc = petriNet.getArcs().iterator().next();
        assertThat(arc.getArcPoints().contains(expected));
    }

    @Test
    public void createsPlace() throws JAXBException, FileNotFoundException {
        PetriNet petriNet = reader.read(FileUtils.resourceLocation(XMLUtils.getSinglePlacePath()));

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
    public void createsPlaceWithInterfaceStatus() throws JAXBException, FileNotFoundException, PetriNetComponentNotFoundException {
    	PetriNet petriNet = reader.read(FileUtils.resourceLocation(XMLUtils.getSinglePlaceWithHomeInterfaceStatusPath()));
    	
    	assertThat(petriNet.getPlaces()).hasSize(1);
    	assertThat(extractProperty("name").from(petriNet.getPlaces())).containsExactly("P0");
    	assertThat(petriNet.getPlaces()).extracting("x", "y").containsExactly(tuple(255, 240));
    	assertThat(petriNet.getPlaces()).extracting("markingXOffset", "markingYOffset").containsExactly(
    			tuple(0.0, 0.0));
    	assertThat(petriNet.getPlaces()).extracting("nameXOffset", "nameYOffset").containsExactly(tuple(5.0, 26.0));
    	assertThat(extractProperty("capacity").from(petriNet.getPlaces())).containsExactly(0);
    	assertThat(extractProperty("name").from(petriNet.getPlaces())).containsExactly("P0");
    	assertThat(extractProperty("tokenCounts").from(petriNet.getPlaces())).hasSize(1);
    	Place place = petriNet.getComponent("P0", Place.class); 
    	PlaceStatus status = place.getStatus(); 
    	assertTrue(status instanceof PlaceStatusInterface); 
    	assertTrue(status.isMergeStatus());
    	assertTrue(status.isExternal());
    	assertTrue(status.isInputOnlyArcConstraint());
    	assertFalse(status.isOutputOnlyArcConstraint());
    	assertEquals(place, status.getPlace()); 
    }

    @Test
    public void createsMarkingCorrectlyWithTokenMap() throws JAXBException, FileNotFoundException {

        PetriNet petriNet = reader.read(FileUtils.resourceLocation(XMLUtils.getSinglePlacePath()));
        assertThat(petriNet.getPlaces()).hasSize(1);
        Place place = petriNet.getPlaces().iterator().next();

        Map<String, Integer> counts = place.getTokenCounts();
        assertThat(counts).containsEntry(RED_TOKEN, 1);
    }

    @Test
    public void createsMarkingIfNoTokensSet() throws JAXBException, FileNotFoundException {
        PetriNet petriNet = reader.read(FileUtils.resourceLocation(XMLUtils.getNoPlaceTokenPath()));
//=======
//        PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getNoPlaceTokenPath()));
//>>>>>>> fixes for issues #22,24.  Update POM to 1.1.0-SNAPSHOT
        assertThat(petriNet.getPlaces()).isNotEmpty();
        Place place = petriNet.getPlaces().iterator().next();
        assertThat(place.getTokenCounts()).containsEntry(DEFAULT_TOKEN, 0);
        //SJD place will have a 0 count for each token instead of null token count
//        assertThat(place.getTokenCounts()).isEmpty();  
    }

    @Test
    public void createsTransition() throws JAXBException, FileNotFoundException {

        PetriNet petriNet = reader.read(FileUtils.resourceLocation(XMLUtils.getTransitionFile()));

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
    public void createsExternalTransition() throws JAXBException, FileNotFoundException, PetriNetComponentNotFoundException {
    	
    	PetriNet petriNet = reader.read(FileUtils.resourceLocation(XMLUtils.getExternalTransitionFile()));
    	
    	assertThat(petriNet.getTransitions()).extracting("x", "y").containsExactly(tuple(375, 225));
    	assertThat(petriNet.getTransitions()).extracting("id", "name").containsExactly(tuple("T0", "T0"));
    	assertThat(petriNet.getTransitions()).extracting("rate.expression").containsExactly("1.0");
    	assertThat(petriNet.getTransitions()).extractingResultOf("isTimed").containsExactly(false);
    	assertThat(petriNet.getTransitions()).extractingResultOf("isInfiniteServer").containsExactly(false);
    	assertThat(petriNet.getTransitions()).extracting("nameXOffset", "nameYOffset").containsExactly(
    			tuple(-5.0, 35.0));
    	assertThat(petriNet.getTransitions()).extracting("priority").containsExactly(1);
    	Transition transition = petriNet.getComponent("T0", Transition.class);
    	assertTrue(transition instanceof DiscreteExternalTransition); 
    	assertTrue(((DiscreteExternalTransition) transition).getClient() instanceof TestingExternalTransition); 
    }

    
    @Test
    public void createsArc() throws JAXBException, FileNotFoundException {

        PetriNet petriNet = reader.read(FileUtils.resourceLocation(XMLUtils.getArcNoWeightFile()));
        Place expectedSource = new DiscretePlace("P0", "P0");
        expectedSource.setTokenCount(DEFAULT_TOKEN, 0);
        Transition expectedTarget = new DiscreteTransition("T0", "T0");
        assertThat(petriNet.getArcs()).extracting("type", "source", "target", "id").contains(
                tuple(ArcType.NORMAL, expectedSource, expectedTarget, "P0 TO T0"));
    }

    @Test
    public void createsCorrectMarkingIfWeightSpecified()
            throws JAXBException, FileNotFoundException {
        PetriNet petriNet = reader.read(FileUtils.resourceLocation(XMLUtils.getNormalArcWithWeight()));
        Arc<? extends Connectable, ? extends Connectable> arc = petriNet.getArcs().iterator().next();

        Map<String, String> weights = arc.getTokenWeights();
        assertEquals(1, weights.size());

        assertTrue(weights.containsKey(DEFAULT_TOKEN));
        String weight = weights.get(DEFAULT_TOKEN);
        assertEquals("4", weight);
    }

    @Test
    public void createsMarkingWithCorrectToken() throws JAXBException, FileNotFoundException {
        PetriNet petriNet = reader.read(FileUtils.resourceLocation(XMLUtils.getNormalArcWithWeight()));
        Arc<? extends Connectable, ? extends Connectable> arc = petriNet.getArcs().iterator().next();
        Map<String, String> weights = arc.getTokenWeights();
        assertEquals(1, weights.size());
        assertTrue(weights.containsKey(DEFAULT_TOKEN));
    }

    @Test
    public void createsInhibitoryArc() throws JAXBException, FileNotFoundException {
    	//XML has unparsed tags 
        reader = new PetriNetIOImpl(true, true);  // change to true, false to see error
        PetriNet petriNet = reader.read(FileUtils.resourceLocation(XMLUtils.getInhibitorArcFile()));
//=======
//        PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getInhibitorArcFile()));
//>>>>>>> fixes for issues #22,24.  Update POM to 1.1.0-SNAPSHOT
        assertThat(petriNet.getArcs()).extracting("type").containsExactly(ArcType.INHIBITOR);
    }

    @Test
    public void createsRedToken() throws JAXBException, FileNotFoundException {
        PetriNet petriNet = reader.read(FileUtils.resourceLocation(XMLUtils.getTokenFile()));
        Token redToken = new ColoredToken("red", new Color(255, 0, 0));
        assertThat(petriNet.getTokens()).containsExactly(redToken);
    }

    @Test
    public void createsAnnotation() throws JAXBException, FileNotFoundException {
        PetriNet petriNet = reader.read(FileUtils.resourceLocation(XMLUtils.getAnnotationFile()));
        assertThat(petriNet.getAnnotations()).extracting("text", "x", "y", "height", "width").containsExactly(
                tuple("#P12s", 93, 145, 20, 48));
    }

    @Test
    public void createsRateParameter() throws JAXBException, FileNotFoundException {
        PetriNet petriNet = reader.read(FileUtils.resourceLocation(XMLUtils.getRateParameterFile()));
        assertThat(petriNet.getRateParameters()).extracting("id","expression").containsExactly(
                tuple("rate0",  "5.0"));
    }

    @Test
    public void transitionReferencesRateParameter()
            throws PetriNetComponentNotFoundException, JAXBException, FileNotFoundException {
        PetriNet petriNet = reader.read(FileUtils.resourceLocation(XMLUtils.getTransitionRateParameterFile()));
        RateParameter rateParameter = petriNet.getComponent("foo", RateParameter.class);
        Transition transition = petriNet.getComponent("T0", Transition.class);
        assertEquals(rateParameter, transition.getRate());
    }
    
    @Test
    public void externalTransitionReferencesRateParameter()
    		throws PetriNetComponentNotFoundException, JAXBException, FileNotFoundException {
    	PetriNet petriNet = reader.read(FileUtils.resourceLocation(XMLUtils.getExternalTransitionRateParameterFile()));
    	RateParameter rateParameter = petriNet.getComponent("foo", RateParameter.class);
    	Transition transition = petriNet.getComponent("T0", Transition.class);
    	assertEquals(rateParameter, transition.getRate());
    	assertTrue(transition instanceof DiscreteExternalTransition); 
    	assertTrue(((DiscreteExternalTransition) transition).getClient() instanceof TestingExternalTransition); 
    }

    @Test
    public void rateParameterReferencesPlace()
    		throws PetriNetComponentNotFoundException, JAXBException, FileNotFoundException {
//<<<<<<< b3c5fc1ed238e32bdc5d64e94dcbc86ac8fd9c84
    	PetriNet petriNet = reader.read(FileUtils.resourceLocation(XMLUtils.getRateParameterReferencesPlaceFile()));
        assertThat(petriNet.getRateParameters()).extracting("id","expression").containsExactly(
                tuple("rate1",  "#(P0)"));
    }
    @Test
    public void readsTokens() throws PetriNetComponentNotFoundException, JAXBException, FileNotFoundException {
    	PetriNet petriNet = reader.read(FileUtils.resourceLocation(XMLUtils.getTwoTokenFile()));
    	Collection<Token> tokens = petriNet.getTokens();
    	assertEquals(2,tokens.size());
    }
    @Test
    public void throwsWhenArcReferencesNonexistentPlace() throws PetriNetComponentNotFoundException, JAXBException, FileNotFoundException {
    	String path = FileUtils.resourceLocation(XMLUtils.getArcWithoutPlaceFile());  
    	expectedException.expect(JAXBException.class);
    	expectedException.expectMessage("PetriNetValidationEventHandler error attempting to build Petri net from file "+path+
    			": uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException:  in uk.ac.imperial.pipe.io.adapters.modelAdapter.ArcAdapter: " +
    			"Arc 'P1 TO T0' references place P1 but P1 does not exist in file.");  
    	reader.read(path);
    }
    @Test
	public void identifiesPetriNetXmlFile() throws Exception {
    	assertEquals(XmlFileEnum.PETRI_NET, 
    			reader.determineFileType(FileUtils.resourceLocation(XMLUtils.getSimplePetriNet())));  
	}
    @Test
    public void identifiesIncludeXmlFile() throws Exception {
    	assertEquals(XmlFileEnum.INCLUDE_HIERARCHY, 
    			reader.determineFileType(FileUtils.resourceLocation(XMLUtils.getSingleIncludeHierarchyFile())));  
    }
    @Test
    public void throwsIfFileHasUnknownXmlTagsAtHighestLevel() throws Exception {
    	expectedException.expect(PetriNetFileException.class);    	
    	expectedException.expectMessage(PetriNetIOImpl.PETRI_NET_IO_IMPL_DETERMINE_FILE_TYPE+
    			PetriNetIOImpl.XML_NOT_PNML_NOR_INCLUDE_FORMAT); 
    	reader.determineFileType(FileUtils.resourceLocation(XMLUtils.getUnknownXml()));  
    }
    @Test
    public void throwsIfFileNotValidXml() throws Exception {
    	//TODO find a different way to suppress:  [Fatal Error] :1:1: Content is not allowed in prolog.
    	System.setErr(new PrintStream(new OutputStream() {
    	    @Override public void write(int b) throws IOException {}
    	}));
    	expectedException.expect(PetriNetFileException.class);
    	expectedException.expectMessage(PetriNetIOImpl.PETRI_NET_IO_IMPL_DETERMINE_FILE_TYPE+
    			PetriNetIOImpl.FILE_IS_NOT_IN_XML_FORMAT); 
    	reader.determineFileType(FileUtils.resourceLocation(XMLUtils.getInvalidXml())); 
    }
    @Test
    public void throwsIfFileNotFound() throws Exception {
    	expectedException.expect(PetriNetFileException.class);
    	expectedException.expectMessage(PetriNetIOImpl.PETRI_NET_IO_IMPL_DETERMINE_FILE_TYPE+
    			PetriNetIOImpl.FILE_NOT_FOUND); 
    	reader.determineFileType(XMLUtils.getNonExistentFile());  
    }
    
//SJDclean =======
//    	PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getRateParameterReferencesPlaceFile()));
//        assertThat(petriNet.getRateParameters()).extracting("id","expression").containsExactly(
//                tuple("rate1",  "#(P0)"));
//    }
//    
//    @Test
//    public void readsTokens() throws PetriNetComponentNotFoundException, JAXBException, FileNotFoundException {
//        PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getTwoTokenFile()));
//        Collection<Token> tokens = petriNet.getTokens();
//        assertEquals(2,tokens.size());
//    }
//    @Test
//    public void messagePrintedWithoutThrowingWhenUnexpectedElement() throws PetriNetComponentNotFoundException, JAXBException, FileNotFoundException {
//        reader = new TestingPetriNetIOImpl(true, false); 
//        checkPrintedAndDoesntThrowWhenUnexpectedElement();
//    }
//    @Test
//    public void messagePrintedButDoesntThrowWhenUnexpectedElement() throws PetriNetComponentNotFoundException, JAXBException, FileNotFoundException {
//    	reader = new TestingPetriNetIOImpl(false, false); 
//    	checkPrintedAndDoesntThrowWhenUnexpectedElement();
//    }
//
//	protected void checkPrintedAndDoesntThrowWhenUnexpectedElement()
//			throws JAXBException, FileNotFoundException {
//		@SuppressWarnings("unused")
//		PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getInvalidPetriNetFile()));
//        PetriNetValidationEventHandler handler = ((PetriNetIOImpl) reader).getEventHandler();  
//    	assertEquals("PetriNetValidationEventHandler received a ValidationEvent, probably during processing by PetriNetIOImpl.  Details: \n" +
//    			"Message: unexpected element (uri:\"\", local:\"blah\"). Expected elements are <{}definition>,<{}arc>,<{}token>,<{}labels>,<{}transition>,<{}place>\n" +
//    			"Object: null\n" +
//    			"URL: null\n" +
//    			"Node: null\n" +
//    			"Line: 4\n" +
//    			"Column: 16\n" +
//    			"Linked exception: null", handler.getFormattedEvents().get(0).formattedEvent);
//    	assertTrue(handler.printMessage(handler.getFormattedEvents().get(0)));
//	}
//    @Test
//    public void noMessagePrintedAndDoesntThrowWhenUnexpectedElement() throws PetriNetComponentNotFoundException, JAXBException, FileNotFoundException {
//    	reader = new PetriNetIOImpl(true, true);   
//    	@SuppressWarnings("unused")
//    	PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getInvalidPetriNetFile()));
//    	PetriNetValidationEventHandler handler = ((PetriNetIOImpl) reader).getEventHandler();  
//    	assertEquals(true, handler.getFormattedEvents().get(0).unexpected); 
//    	assertFalse(handler.printMessage(handler.getFormattedEvents().get(0)));
//    }
//    @Test
//    public void messagePrintedAndThrowsForFirstNonUnexpectedElementError() throws PetriNetComponentNotFoundException, JAXBException, FileNotFoundException {
//    	reader = new TestingThrowsPetriNetIOImpl(false, true);  // true false 
//    	try {
//    		@SuppressWarnings("unused")
//    		PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getInvalidPetriNetFile()));
//    	} catch (UnmarshalException e) {
//    		assertEquals("java.lang.RuntimeException: TestingThrowsPetriNetAdapter exception.", e.getMessage()); 
//    	}
//    	PetriNetValidationEventHandler handler = ((PetriNetIOImpl) reader).getEventHandler();  
//    	assertEquals(2, handler.getFormattedEvents().size());
//    	assertEquals("unexpected element message saved, but doesn't throw",true, handler.getFormattedEvents().get(0).unexpected); 
//    	assertFalse(handler.printMessage(handler.getFormattedEvents().get(0)));
//    	assertEquals("PetriNetValidationEventHandler received a ValidationEvent, probably during processing by PetriNetIOImpl.  Details: \n" +
//    			"Message: unexpected element (uri:\"\", local:\"blah\"). Expected elements are <{}definition>,<{}arc>,<{}token>,<{}labels>,<{}transition>,<{}place>\n" +
//    			"Object: null\n" +
//    			"URL: null\n" +
//    			"Node: null\n" +
//    			"Line: 4\n" +
//    			"Column: 16\n" +
//    			"Linked exception: null", handler.getFormattedEvents().get(0).formattedEvent);
//    	assertEquals("second error not unexpected element, so throws",false, handler.getFormattedEvents().get(1).unexpected); 
//    	assertEquals("PetriNetValidationEventHandler received a ValidationEvent, probably during processing by PetriNetIOImpl.  Details: \n" +
//    			"Message: java.lang.RuntimeException: TestingThrowsPetriNetAdapter exception.\n" +
//    			"Object: null\n" +
//    			"URL: null\n" +
//    			"Node: null\n" +
//    			"Line: 6\n" +
//    			"Column: 11\n" +
//    			"Linked exception: java.lang.RuntimeException: TestingThrowsPetriNetAdapter exception.", handler.getFormattedEvents().get(1).formattedEvent);
//    	assertTrue(handler.printMessage(handler.getFormattedEvents().get(1)));
//    }
//    private class TestingPetriNetIOImpl extends PetriNetIOImpl {
//
//		public TestingPetriNetIOImpl(boolean continueProcessing,
//				boolean suppressUnexpectedElementMessages) throws JAXBException {
//			super(continueProcessing, suppressUnexpectedElementMessages);
//	    	petriNetValidationEventHandler = new TestingPetriNetValidationEventHandler(continueProcessing, suppressUnexpectedElementMessages); 
//		}
//		@Override
//		protected void initialiseUnmarshaller() throws JAXBException {
//			super.initialiseUnmarshaller();
//		}
//    }
//    private class TestingThrowsPetriNetIOImpl extends TestingPetriNetIOImpl {
//    	
//    	public TestingThrowsPetriNetIOImpl(boolean continueProcessing,
//    			boolean suppressUnexpectedElementMessages) throws JAXBException {
//    		super(continueProcessing, suppressUnexpectedElementMessages);
//    		petriNetValidationEventHandler = new TestingPetriNetValidationEventHandler(continueProcessing, suppressUnexpectedElementMessages); 
//    	}
//    	@Override
//    	protected void initialiseUnmarshaller() throws JAXBException {
//    		super.initialiseUnmarshaller();
//    		getUnmarshaller().setAdapter(PetriNetAdapter.class, new TestingThrowsPetriNetAdapter()); 
//    	}
//    }
//    private class TestingPetriNetValidationEventHandler extends PetriNetValidationEventHandler {
//
//		public TestingPetriNetValidationEventHandler(
//				boolean continueProcessing,
//				boolean suppressUnexpectedElementMessages) {
//			super(continueProcessing, suppressUnexpectedElementMessages);
//		}
//    	@Override
//    	public void printMessages() {
//    	}
//    }
//>>>>>>> fixes for issues #22,24.  Update POM to 1.1.0-SNAPSHOT
}
