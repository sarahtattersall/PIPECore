package uk.ac.imperial.pipe.io;

import org.junit.Before;
import org.junit.Test;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.*;
import utils.FileUtils;

import javax.xml.bind.JAXBException;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PetriNetReaderTest {

    private static final String DEFAULT_TOKEN = "Default";
    private static final String RED_TOKEN = "Red";
    PetriNetReader reader;

    @Before
    public void setUp() throws JAXBException {
        reader = new PetriNetIOImpl();
    }
    //TODO test PN name / id 
    @Test
    public void createGSPN() throws  JAXBException, FileNotFoundException {
        PetriNet petriNet = reader.read(FileUtils.fileLocation("/xml/gspn1.xml"));
        assertEquals(5, petriNet.getPlaces().size());
        assertEquals(5, petriNet.getTransitions().size());
        assertEquals(12, petriNet.getArcs().size());
        assertEquals(1, petriNet.getTokens().size());
        assertEquals("Net-One", petriNet.getNameValue()); 
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
    public void createsPlaceWithInterfaceStatus() throws JAXBException, FileNotFoundException, PetriNetComponentNotFoundException {
    	PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getSinglePlaceWithHomeInterfaceStatusPath()));
    	
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

        PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getSinglePlacePath()));
        assertThat(petriNet.getPlaces()).hasSize(1);
        Place place = petriNet.getPlaces().iterator().next();

        Map<String, Integer> counts = place.getTokenCounts();
        assertThat(counts).containsEntry(RED_TOKEN, 1);
    }

    @Test
    public void createsMarkingIfNoTokensSet() throws JAXBException, FileNotFoundException {

        PetriNet petriNet = reader.read(FileUtils.fileLocation(getNoPlaceTokenPath()));
        assertThat(petriNet.getPlaces()).isNotEmpty();
        Place place = petriNet.getPlaces().iterator().next();
        assertThat(place.getTokenCounts()).containsEntry(DEFAULT_TOKEN, 0);
        //SJD place will have a 0 count for each token instead of null token count
//        assertThat(place.getTokenCounts()).isEmpty();  
    }

    private String getNoPlaceTokenPath() {
        return "/xml/place/noTokenPlace.xml";
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
    public void createsExternalTransition() throws JAXBException, FileNotFoundException, PetriNetComponentNotFoundException {
    	
    	PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getExternalTransitionFile()));
    	
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

        PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getArcNoWeightFile()));
        Place expectedSource = new DiscretePlace("P0", "P0");
        expectedSource.setTokenCount(DEFAULT_TOKEN, 0);
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
    public void externalTransitionReferencesRateParameter()
    		throws PetriNetComponentNotFoundException, JAXBException, FileNotFoundException {
    	PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getExternalTransitionRateParameterFile()));
    	RateParameter rateParameter = petriNet.getComponent("foo", RateParameter.class);
    	Transition transition = petriNet.getComponent("T0", Transition.class);
    	assertEquals(rateParameter, transition.getRate());
    	assertTrue(transition instanceof DiscreteExternalTransition); 
    	assertTrue(((DiscreteExternalTransition) transition).getClient() instanceof TestingExternalTransition); 
    }

    @Test
    public void readsTokens() throws PetriNetComponentNotFoundException, JAXBException, FileNotFoundException {
        PetriNet petriNet = reader.read(FileUtils.fileLocation("/xml/token/two_token.xml"));
        Collection<Token> tokens = petriNet.getTokens();
        assertEquals(2,tokens.size());
    }


}
