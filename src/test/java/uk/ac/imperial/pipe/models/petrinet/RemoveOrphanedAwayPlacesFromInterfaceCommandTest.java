package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;

public class RemoveOrphanedAwayPlacesFromInterfaceCommandTest {

    private static final String AWAY_PLACE_WITHOUT_A_CORRESPONDING_HOME_PLACE = "Place P0 in PetriNet net1 is an Away place without a corresponding Home place, possibly due to errors in the pnml file or opening an incomplete interface hierarchy.  Place is no longer a merge place; impact to PetriNet execution is undefined.";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private IncludeHierarchy includes;
    private PetriNet net;
    private Place place;
    private RemoveOrphanedAwayPlacesFromInterfaceCommand command;
    private IncludeHierarchy include2;
    private PetriNet net2;

    //TODO distinguish "remove from interface" from placeStatus.setMergeStatus(false)
    @Before
    public void setUp() throws Exception {
        net = buildNet(1);
        includes = new IncludeHierarchy(net, "top");
        //        net2 = buildNet(2);
        assertEquals(2, net.getPlaces().size());
        //        includes.include(net2, "a");
        //        include2 = includes.getInclude("a");
        assertEquals(0, includes.getInterfacePlaceMap().size());
        //        assertEquals(0, include2.getInterfacePlaceMap().size());
        place = net.getComponent("P0", Place.class);
        includes.addToInterface(place, true, false, false, false);
        place.getStatus().setMergeInterfaceStatus(new MergeInterfaceStatusAway(null, place.getStatus(), "someAway."));
        assertEquals(1, includes.getInterfacePlaceMap().size());
        assertEquals(2, net.getPlaces().size());
    }

    @Test
    public void awayPlaceRemovedFromInterfaceButStaysInPetrinet() throws Exception {
        command = new RemoveOrphanedAwayPlacesFromInterfaceCommand();
        Result<InterfacePlaceAction> result = command.execute(includes);
        assertTrue(result.hasResult());
        assertEquals(AWAY_PLACE_WITHOUT_A_CORRESPONDING_HOME_PLACE, result
                .getMessage());
        assertEquals("removed from interface", 0, includes.getInterfacePlaceMap().size());
        assertNull(includes.getInterfacePlace("P0"));
        assertEquals("we just removed it from interface; place stays in home net", 2, net.getPlaces().size());
        place = net.getComponent("P0", Place.class);
        assertFalse(place.getStatus().isMergeStatus());
        assertTrue(place.getStatus().getMergeInterfaceStatus() instanceof NoOpInterfaceStatus);
        assertTrue(place.getStatus() instanceof PlaceStatusNormal);
    }

    @Test
    public void awayPlaceMergeStatusChangedToNoOpButStaysInInterface() throws Exception {
        place.getStatus().setExternal(true);
        command = new RemoveOrphanedAwayPlacesFromInterfaceCommand();
        Result<InterfacePlaceAction> result = command.execute(includes);
        assertTrue(result.hasResult());
        assertEquals(AWAY_PLACE_WITHOUT_A_CORRESPONDING_HOME_PLACE, result
                .getMessage());
        assertEquals("stays in interface", 1, includes.getInterfacePlaceMap().size());
        assertEquals("P0", includes.getInterfacePlace("P0").getId());
        assertEquals(2, net.getPlaces().size());
        place = net.getComponent("P0", Place.class);
        assertFalse(place.getStatus().isMergeStatus());
        assertTrue(place.getStatus().getMergeInterfaceStatus() instanceof NoOpInterfaceStatus);
        assertTrue(place.getStatus() instanceof PlaceStatusInterface);
    }
    //    @Test
    //    public void homePlaceRemovedFromInterfaceInHomeIncludeHierarchyButNotPetriNet() throws Exception {
    //    }
    //
    //    @Test
    //    public void availablePlaceRemovedFromInterfaceInAwayIncludeHierarchyWithNoEffectOnPetriNet() throws Exception {
    //        command = new RemovePlaceFromInterfaceCommand(place);
    //        Result<InterfacePlaceAction> result = command.execute(includes);
    //        assertFalse(result.hasResult());
    //        assertEquals("home unaffected", 1, include2.getInterfacePlaceMap().size());
    //        assertEquals("removed from away", 0, includes.getInterfacePlaceMap().size());
    //        assertNull(includes.getInterfacePlace("a.P0"));
    //        assertEquals("no effect on net's places as was only available", 2, net.getPlaces().size());
    //    }
    //
    //    @Test
    //    public void awayPlaceRemovedFromInterfaceInAwayIncludeHierarchyAsWellAsPetriNet() throws Exception {
    //        Place topPlace = includes.getInterfacePlace("a.P0");
    //        includes.addAvailablePlaceToPetriNet(topPlace);
    //        assertTrue(topPlace.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAway);
    //        assertEquals("place added to Petri net", 3, net.getPlaces().size());
    //        assertNotNull(includes.getPetriNet().getComponent("a.P0", Place.class));
    //
    //        command = new RemovePlaceFromInterfaceCommand(place);
    //        Result<InterfacePlaceAction> result = command.execute(includes);
    //        assertFalse(result.hasResult());
    //        assertEquals("home unaffected", 1, include2.getInterfacePlaceMap().size());
    //        assertEquals("removed from away", 0, includes.getInterfacePlaceMap().size());
    //        assertNull(includes.getInterfacePlace("a.P0"));
    //        expectedException.expect(PetriNetComponentNotFoundException.class);
    //        includes.getPetriNet().getComponent("a.P0", Place.class);
    //        assertEquals("place also removed from Petri net", 2, net.getPlaces().size());
    //    }

    protected PetriNet buildNet(int i) throws PetriNetComponentException {
        PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0"))
                .and(APlace.withId("P1")).and(AnImmediateTransition.withId("T0"))
                .andFinally(ANormalArc.withSource("P0").andTarget("T0"));
        net.setName(new NormalPetriNetName("net" + i));
        return net;
    }
}
