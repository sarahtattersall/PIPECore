package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.*;

import java.awt.Color;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;

//TODO test changes to status for existing nets and components
// ....e.g., change to inputOnly for a place that already has outbound arcs
public class PlaceStatusInterfaceTest {

    private Place place;
    private PlaceStatus status;
    private PetriNet net;
    private IncludeHierarchy includes;
    private Result<InterfacePlaceAction> result;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        place = new DiscretePlace("P0");
        buildNet();
        includes = new IncludeHierarchy(net, "top");
        status = new PlaceStatusInterface(place, includes);
        place.setStatus(status);
    }

    @Test
    public void throwsIfPlaceOfPlaceStatusIsNotTheSameAsThePlaceForWhichThisIsStatus() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException
                .expectMessage("PlaceStatus can only be assigned with same Place (if not null) as this Place\n" +
                        "This place: P99.  Place from status: P0.");
        DiscretePlace place2 = new DiscretePlace("P99");
        status = new PlaceStatusInterface(place, includes);
        place2.setStatus(status);
    }

    @Test
    public void throwsWhenSettingPlaceIfPlaceDoesNotHaveThisPlaceStatus() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("PlaceStatus for Place P99 (if not null) must be same as this PlaceStatus");
        DiscretePlace place2 = new DiscretePlace("P99");
        status.setPlace(place2);
    }

    @Test
    public void throwsWhenCopyingPlaceStatusIfSettingPlaceIfPlaceDoesNotHaveThisPlaceStatus() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("PlaceStatus for Place P99 (if not null) must be same as this PlaceStatus");
        DiscretePlace place2 = new DiscretePlace("P99");
        status.setPlace(place2);
    }

    @Test
    public void defaultsToNoOpInterfaceStatusForMergeAndExternalAndInputAndOutput() throws Exception {
        assertTrue(status.getMergeInterfaceStatus() instanceof NoOpInterfaceStatus);
        assertTrue(status.getArcConstraint() instanceof NoArcConstraint);
    }

    @Test
    public void NoOpStatusReturnsEmptyResult() throws Exception {
        InterfaceStatus interfaceStatus = new NoOpInterfaceStatus(null);
        assertFalse(interfaceStatus.add(null).hasResult());
        assertFalse(interfaceStatus.remove(null).hasResult());
    }

    @Test
    public void mergeStatus() throws Exception {
        status.setMergeStatus(true);
        result = status.update();
        assertFalse(result.hasResult());
        assertTrue(status.getMergeInterfaceStatus() instanceof MergeInterfaceStatus);
        status.setMergeStatus(false);
        assertFalse(status.update().hasResult());
        assertTrue(status.getMergeInterfaceStatus() instanceof NoOpInterfaceStatus);
    }

    @Test
    public void externalStatus() throws Exception {
        status.setExternal(true);
        assertFalse(status.update().hasResult());
        assertTrue(status.isExternal());
        status.setExternal(false);
        assertFalse(status.update().hasResult());
        assertFalse(status.isExternal());
    }

    @Test
    public void inputOnlyStatus() throws Exception {
        status.setInputOnlyArcConstraint(true);
        assertFalse(status.update().hasResult());
        assertTrue(status.getArcConstraint() instanceof InputOnlyArcConstraint);
        status.setExternal(false);
        assertFalse(status.update().hasResult());
        assertTrue(status.getMergeInterfaceStatus() instanceof NoOpInterfaceStatus);
        assertTrue(status.getArcConstraint().acceptInboundArc());
        assertFalse(status.getArcConstraint().acceptOutboundArc());
    }

    @Test
    public void outputOnlyStatus() throws Exception {
        status.setOutputOnlyArcConstraint(true);
        assertFalse(status.update().hasResult());
        assertTrue(status.getArcConstraint() instanceof OutputOnlyArcConstraint);
        status.setExternal(false);
        assertFalse(status.update().hasResult());
        assertTrue(status.getMergeInterfaceStatus() instanceof NoOpInterfaceStatus);
        assertFalse(status.getArcConstraint().acceptInboundArc());
        assertTrue(status.getArcConstraint().acceptOutboundArc());
    }

    @Test
    public void inputAndOutputCantCoexist() throws Exception {
        status.setInputOnlyArcConstraint(true);
        result = status.update();
        assertFalse(result.hasResult());
        status.setOutputOnlyArcConstraint(true);
        result = status.update();
        assertTrue(result.hasResult());
        assertEquals("PlaceStatus.setOutputOnlyArcConstraint: arc constraint may not be both input only and output only.", result
                .getMessage());

        assertTrue(status.isInputOnlyArcConstraint());
        assertFalse(status.isOutputOnlyArcConstraint());
        status.setInputOnlyArcConstraint(false);
        result = status.update();
        assertFalse(result.hasResult());
        status.setOutputOnlyArcConstraint(true);
        result = status.update();
        assertFalse(result.hasResult());
        status.setInputOnlyArcConstraint(true);
        result = status.update();
        assertTrue(result.hasResult());
        assertEquals("PlaceStatus.setInputOnlyArcConstraint: arc constraint may not be both input only and output only.", result
                .getMessage());
    }

    @Test
    public void copyConstructorForPlaceStatus() throws Exception {
        status.setMergeStatus(true);
        status.setExternal(true);
        status.setInputOnlyArcConstraint(true);
        PlaceStatus newstatus = new PlaceStatusInterface(status, place);
        assertTrue(newstatus.isMergeStatus());
        assertTrue(newstatus.isExternal());
        assertTrue(newstatus.isInputOnlyArcConstraint());
        assertFalse(newstatus.isOutputOnlyArcConstraint());
        //TODO incorporate adjusting the include hierarchy into ClonePetriNet?  see MergeInterfaceStatusHome
        assertEquals("but this is probably not right -- usually we're copying to another include", status
                .getIncludeHierarchy(), newstatus.getIncludeHierarchy());
    }

    @Test
    public void inputOnlyAcceptsOnlyInboundArcsAndThrowsForOutboundArcs() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Place has an inputOnly ArcConstraint, and will only accept InboundArcs: P0");
        Place place = buildPlaceWithStatus();
        status.setInputOnlyArcConstraint(true);
        status.update();
        Transition transition = net.getComponent("T0", Transition.class);
        InboundArc inbound = new InboundNormalArc(place, transition, new HashMap<String, String>());
        assertEquals("P0", inbound.getSource().getId());

        @SuppressWarnings("unused")
        OutboundArc outbound = new OutboundNormalArc(transition, place, new HashMap<String, String>());
    }

    @Test
    public void outputOnlyAcceptsOnlyOutboundArcsAndThrowsForInboundArcs() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Place has an outputOnly ArcConstraint, and will only accept OutboundArcs: P0");
        Place place = buildPlaceWithStatus();
        status.setOutputOnlyArcConstraint(true);
        status.update();
        Transition transition = net.getComponent("T0", Transition.class);
        OutboundArc outbound = new OutboundNormalArc(transition, place, new HashMap<String, String>());
        assertEquals("P0", outbound.getTarget().getId());

        @SuppressWarnings("unused")
        InboundArc inbound = new InboundNormalArc(place, transition, new HashMap<String, String>());
    }

    private Place buildPlaceWithStatus() throws PetriNetComponentNotFoundException {
        Place place = net.getComponent("P0", Place.class);
        status = new PlaceStatusInterface(place, includes);
        place.setStatus(status);
        return place;
    }

    protected void buildNet() throws PetriNetComponentException {
        net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0"))
                .and(APlace.withId("P1")).andFinally(AnImmediateTransition.withId("T0"));
    }

    //TODO PlaceStatusNormalTest
}
