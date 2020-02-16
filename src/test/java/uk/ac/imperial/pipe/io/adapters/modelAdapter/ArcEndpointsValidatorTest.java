package uk.ac.imperial.pipe.io.adapters.modelAdapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.Arc;
import uk.ac.imperial.pipe.models.petrinet.Connectable;
import uk.ac.imperial.pipe.models.petrinet.DiscretePlace;
import uk.ac.imperial.pipe.models.petrinet.DiscreteTransition;
import uk.ac.imperial.pipe.models.petrinet.InboundInhibitorArc;
import uk.ac.imperial.pipe.models.petrinet.InboundNormalArc;
import uk.ac.imperial.pipe.models.petrinet.OutboundNormalArc;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.Transition;

public class ArcEndpointsValidatorTest {

    private static final String AND = " and ";
    private static final String DOES_NOT_EXIST = " does not exist in file.";
    private static final String BUT = " but ";
    private static final String REFERENCES = "' references ";
    private static final String PLACE = "place ";
    private static final String TRANSITION = "transition ";
    private static final String IN_ARC_ADAPTER = " in uk.ac.imperial.pipe.io.adapters.modelAdapter.ArcAdapter: Arc '";
    private static final String DO_NOT_EXIST = " do not exist in file.";
    private static final String SPECIFIES = "' specifies that both ";
    private static final String ARE = " are ";
    private static final String BUT_NOT_POSSIBLE_CONFIGURATION = " but this is not a possible configuration.";
    private static final String PLACES = "places,";
    private static final String TRANSITIONS = "transitions,";
    private static final String INHIBITORY = "' specifies an inhibitory arc with ";
    private static final String AS_SOURCE = " as source, ";
    private Map<String, Place> places;
    private Map<String, Transition> transitions;
    private Map<String, String> weights;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        places = new HashMap<>();
        places.put("P0", new DiscretePlace("P0"));
        transitions = new HashMap<>();
        transitions.put("T0", new DiscreteTransition("T0"));
        weights = new HashMap<>();
        weights.put("Default", "1");
    }

    @Test
    public void verifyPlaceSourceAndTransitionTargetReturnsInboundNormalArc() throws PetriNetComponentException {
        ArcEndpointsValidator aev = new ArcEndpointsValidator("P0", "T0", "P0 TO T0", places, transitions, true,
                weights);
        Arc<? extends Connectable, ? extends Connectable> arc = aev.createArc();
        assertTrue(arc instanceof InboundNormalArc);
        assertEquals("P0 TO T0", arc.getId());
    }

    @Test
    public void verifyTransitionSourceAndPlaceTargetReturnsOutboundNormalArc() throws PetriNetComponentException {
        ArcEndpointsValidator aev = new ArcEndpointsValidator("T0", "P0", "T0 TO P0", places, transitions, true,
                weights);
        Arc<? extends Connectable, ? extends Connectable> arc = aev.createArc();
        assertTrue(arc instanceof OutboundNormalArc);
    }

    @Test
    public void verifyPlaceSourceAndTransitionTargetReturnsInboundInhibitorArc() throws PetriNetComponentException {
        ArcEndpointsValidator aev = new ArcEndpointsValidator("P0", "T0", "P0 TO T0", places, transitions, false, null);
        Arc<? extends Connectable, ? extends Connectable> arc = aev.createArc();
        assertTrue(arc instanceof InboundInhibitorArc);
    }

    @Test
    public void throwsWhenTargetTransitionExistsButSourcePlaceDoesNot() throws PetriNetComponentException {
        String arcId = "P1 TO T0";
        String source = "P1";
        String target = "T0";
        String type = PLACE;
        expectedException.expect(PetriNetComponentException.class);
        expectedException.expectMessage(IN_ARC_ADAPTER + arcId + REFERENCES +
                type + source + BUT + source + DOES_NOT_EXIST);
        ArcEndpointsValidator aev = new ArcEndpointsValidator(source, target, arcId, places, transitions, true, null);
        aev.createArc();
    }

    @Test
    public void throwsWhenSourceTransitionExistsButTargetPlaceDoesNot() throws PetriNetComponentException {
        String arcId = "T0 TO P1";
        String source = "T0";
        String target = "P1";
        String type = PLACE;
        expectedException.expect(PetriNetComponentException.class);
        expectedException.expectMessage(IN_ARC_ADAPTER + arcId + REFERENCES +
                type + target + BUT + target + DOES_NOT_EXIST);
        ArcEndpointsValidator aev = new ArcEndpointsValidator(source, target, arcId, places, transitions, true, null);
        aev.createArc();
    }

    @Test
    public void throwsWhenSourcePlaceExistsButTargetTransitionDoesNot() throws PetriNetComponentException {
        String arcId = "P0 TO T1";
        String source = "P0";
        String target = "T1";
        String type = TRANSITION;
        expectedException.expect(PetriNetComponentException.class);
        expectedException.expectMessage(IN_ARC_ADAPTER + arcId + REFERENCES +
                type + target + BUT + target + DOES_NOT_EXIST);
        ArcEndpointsValidator aev = new ArcEndpointsValidator(source, target, arcId, places, transitions, true, null);
        aev.createArc();
    }

    @Test
    public void throwsWhenTargetPlaceExistsButSourceTransitionDoesNot() throws PetriNetComponentException {
        String arcId = "T1 TO P0";
        String source = "T1";
        String target = "P0";
        String type = TRANSITION;
        expectedException.expect(PetriNetComponentException.class);
        expectedException.expectMessage(IN_ARC_ADAPTER + arcId + REFERENCES +
                type + source + BUT + source + DOES_NOT_EXIST);
        ArcEndpointsValidator aev = new ArcEndpointsValidator(source, target, arcId, places, transitions, true, null);
        aev.createArc();
    }

    @Test
    public void throwsWhenNeitherPlaceNorTransitionExists() throws PetriNetComponentException {
        String arcId = "P1 TO T1";
        String source = "P1";
        String target = "T1";
        expectedException.expect(PetriNetComponentException.class);
        expectedException.expectMessage(IN_ARC_ADAPTER + arcId + REFERENCES +
                source + AND + target + BUT + source + AND + target + DO_NOT_EXIST);
        ArcEndpointsValidator aev = new ArcEndpointsValidator(source, target, arcId, places, transitions, true, null);
        aev.createArc();
    }

    @Test
    public void throwsWhenBothEndPointsArePlaces() throws PetriNetComponentException {
        String arcId = "P0 TO P0";
        String source = "P0";
        String target = "P0";
        expectedException.expect(PetriNetComponentException.class);
        expectedException.expectMessage(IN_ARC_ADAPTER + arcId + SPECIFIES +
                source + AND + target + ARE + PLACES + BUT_NOT_POSSIBLE_CONFIGURATION);
        ArcEndpointsValidator aev = new ArcEndpointsValidator(source, target, arcId, places, transitions, true, null);
        aev.createArc();
    }

    @Test
    public void throwsWhenBothEndPointsAreTransitions() throws PetriNetComponentException {
        String arcId = "T0 TO T0";
        String source = "T0";
        String target = "T0";
        expectedException.expect(PetriNetComponentException.class);
        expectedException.expectMessage(IN_ARC_ADAPTER + arcId + SPECIFIES +
                source + AND + target + ARE + TRANSITIONS + BUT_NOT_POSSIBLE_CONFIGURATION);
        ArcEndpointsValidator aev = new ArcEndpointsValidator(source, target, arcId, places, transitions, true, null);
        aev.createArc();
    }

    @Test
    public void throwsWhenInhibitoryButSourceIsTransition() throws PetriNetComponentException {
        String arcId = "T0 TO P0";
        String source = "T0";
        String target = "P0";
        expectedException.expect(PetriNetComponentException.class);
        expectedException.expectMessage(IN_ARC_ADAPTER + arcId +
                INHIBITORY + source + TRANSITION + AS_SOURCE + BUT_NOT_POSSIBLE_CONFIGURATION);
        ArcEndpointsValidator aev = new ArcEndpointsValidator(source, target, arcId, places, transitions, false, null);
        aev.createArc();
    }
}
