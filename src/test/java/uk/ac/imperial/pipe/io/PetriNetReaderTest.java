package uk.ac.imperial.pipe.io;

import org.junit.Before;
import org.junit.Test;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.component.Connectable;
import uk.ac.imperial.pipe.models.component.arc.Arc;
import uk.ac.imperial.pipe.models.component.arc.ArcPoint;
import uk.ac.imperial.pipe.models.component.arc.ArcType;
import uk.ac.imperial.pipe.models.component.place.DiscretePlace;
import uk.ac.imperial.pipe.models.component.place.Place;
import uk.ac.imperial.pipe.models.component.rate.RateParameter;
import uk.ac.imperial.pipe.models.component.token.ColoredToken;
import uk.ac.imperial.pipe.models.component.token.Token;
import uk.ac.imperial.pipe.models.component.transition.DiscreteTransition;
import uk.ac.imperial.pipe.models.component.transition.Transition;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.parsers.UnparsableException;
import utils.FileUtils;

import javax.xml.bind.JAXBException;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PetriNetReaderTest {

    private static final String DEFAULT_TOKEN = "Default";
    private static final String RED_TOKEN = "Red";
    PetriNetReader reader;

    @Before
    public void setUp() throws JAXBException {
        reader = new PetriNetIOImpl();
    }

    @Test
    public void createGSPN() throws UnparsableException {
        PetriNet petriNet = reader.read(FileUtils.fileLocation("/xml/gspn1.xml"));
        assertEquals(5, petriNet.getPlaces().size());
        assertEquals(5, petriNet.getTransitions().size());
        assertEquals(12, petriNet.getArcs().size());
        assertEquals(1, petriNet.getTokens().size());
    }

    @Test
    public void createsDefaultTokenIfDoesNotExist() throws UnparsableException {
        PetriNet petriNet = reader.read(FileUtils.fileLocation("/xml/noTokenPlace.xml"));
        assertTrue("Petri net has no tokens registered to it", petriNet.getTokens().size() > 0);
        Token expectedToken = new ColoredToken("Default", new Color(0, 0, 0));
        assertThat(petriNet.getTokens()).contains(expectedToken);
    }

    @Test
    public void createsDefaultTokenIfDoesNotExistAndPlaceMatchesThisToken() throws UnparsableException {
        PetriNet petriNet = reader.read(FileUtils.fileLocation("/xml/noTokenPlace.xml"));
        assertThat(petriNet.getTokens()).isNotEmpty();
        assertThat(petriNet.getPlaces()).isNotEmpty();

        Place place = petriNet.getPlaces().iterator().next();
        assertThat(place.getTokenCounts()).containsKey("Default");
    }

    @Test
    public void losesSourceAndTargetArcPath() throws UnparsableException {
        PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getNormalArcWithWeight()));
        assertThat(petriNet.getArcs()).isNotEmpty();
        assertThat(petriNet.getArcs()).hasSize(1);
    }

    @Test
    public void keepsIntermediatePoints() throws UnparsableException {
        PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getNormalArcWithWeight()));
        ArcPoint expected = new ArcPoint(new Point2D.Double(87, 36), true);
        Arc<? extends Connectable, ? extends Connectable> arc = petriNet.getArcs().iterator().next();
        assertThat(arc.getArcPoints().contains(expected));
    }

    @Test
    public void createsPlace() throws UnparsableException {
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
    public void createsMarkingCorrectlyWithTokenMap() throws UnparsableException {

        PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getSinglePlacePath()));
        assertThat(petriNet.getPlaces()).hasSize(1);
        Place place = petriNet.getPlaces().iterator().next();

        Map<String, Integer> counts = place.getTokenCounts();
        assertThat(counts).containsEntry(RED_TOKEN, 1);
    }

    @Test
    public void createsMarkingIfNoTokensSet() throws UnparsableException {

        PetriNet petriNet = reader.read(FileUtils.fileLocation(getNoPlaceTokenPath()));
        assertThat(petriNet.getPlaces()).isNotEmpty();
        Place place = petriNet.getPlaces().iterator().next();
        assertThat(place.getTokenCounts()).isEmpty();
    }

    private String getNoPlaceTokenPath() {
        return "/xml/place/noTokenPlace.xml";
    }

    @Test
    public void createsTransition() throws UnparsableException {

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
    public void createsArc() throws UnparsableException {

        PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getArcNoWeightFile()));
        Place expectedSource = new DiscretePlace("P0", "P0");
        Transition expectedTarget = new DiscreteTransition("T0", "T0");
        assertThat(petriNet.getArcs()).extracting("type", "source", "target", "id").contains(
                tuple(ArcType.NORMAL, expectedSource, expectedTarget, "P0 TO T0"));
    }

    @Test
    public void createsCorrectMarkingIfWeightSpecified() throws UnparsableException {
        PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getNormalArcWithWeight()));
        Arc<? extends Connectable, ? extends Connectable> arc = petriNet.getArcs().iterator().next();

        Map<String, String> weights = arc.getTokenWeights();
        assertEquals(1, weights.size());

        assertTrue(weights.containsKey(DEFAULT_TOKEN));
        String weight = weights.get(DEFAULT_TOKEN);
        assertEquals("4", weight);
    }

    @Test
    public void createsMarkingWithCorrectToken() throws UnparsableException {
        PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getNormalArcWithWeight()));
        Arc<? extends Connectable, ? extends Connectable> arc = petriNet.getArcs().iterator().next();
        Map<String, String> weights = arc.getTokenWeights();
        assertEquals(1, weights.size());
        assertTrue(weights.containsKey(DEFAULT_TOKEN));
    }

    @Test
    public void createsInhibitoryArc() throws UnparsableException {
        PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getInhibitorArcFile()));
        assertThat(petriNet.getArcs()).extracting("type").containsExactly(ArcType.INHIBITOR);
    }

    @Test
    public void createsRedToken() throws UnparsableException {
        PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getTokenFile()));
        Token redToken = new ColoredToken("red", new Color(255, 0, 0));
        assertThat(petriNet.getTokens()).containsExactly(redToken);
    }

    @Test
    public void createsAnnotation() throws UnparsableException {
        PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getAnnotationFile()));
        assertThat(petriNet.getAnnotations()).extracting("text", "x", "y", "height", "width").containsExactly(
                tuple("#P12s", 93, 145, 20, 48));
    }

    @Test
    public void createsRateParameter() throws UnparsableException {
        PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getRateParameterFile()));
        assertThat(petriNet.getRateParameters()).extracting("id","expression").containsExactly(
                tuple("rate0",  "5.0"));
    }

    @Test
    public void transitionReferencesRateParameter() throws UnparsableException, PetriNetComponentNotFoundException {
        PetriNet petriNet = reader.read(FileUtils.fileLocation(XMLUtils.getTransitionRateParameterFile()));
        RateParameter rateParameter = petriNet.getComponent("foo", RateParameter.class);
        Transition transition = petriNet.getComponent("T0", Transition.class);
        assertEquals(rateParameter, transition.getRate());
    }


}
