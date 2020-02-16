package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.*;

import java.awt.Color;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.exceptions.IncludeException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;
import uk.ac.imperial.pipe.visitor.ExecutablePetriNetCloner;

@RunWith(MockitoJUnitRunner.class)
public class MergeInterfaceStatusHomeTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private IncludeHierarchy includes;
    private PetriNet net;
    private Place place;
    private PlaceStatus status;
    private PetriNet net2;
    private PetriNet net3;
    private IncludeHierarchy include2;
    private IncludeHierarchy include3;
    private Place homePlace;
    private MergeInterfaceStatus mergeStatus;
    private Place aPlace;
    private Place topPlace;

    @Before
    public void setUp() throws Exception {
        setup(true);
    }

    protected void setup(boolean arcs) throws IncludeException, PetriNetComponentException {
        net = new PetriNet();
        includes = new IncludeHierarchy(net, "top");
        place = new DiscretePlace("P10");
        status = new PlaceStatusInterface(place, includes);
        place.setStatus(status);
        if (arcs) {
            net2 = buildNet(2);
            net3 = buildNet(3);
        } else {
            net2 = buildNetNoArc(2);
            net3 = buildNetNoArc(3);
        }
        includes.include(net2, "a");
        include2 = includes.getInclude("a");
        include2.include(net3, "b");
        include3 = includes.getInclude("b");
        net2.addPlace(place);
    }

    @Test
    public void removingHomePlaceFromInterfaceReturnsPlaceToNormalStatus() throws Exception {
        net.addPlace(place);
        assertEquals(1, net.getPlaces().size());
        assertEquals(0, includes.getInterfacePlaceMap().size());
        mergeStatus = new MergeInterfaceStatusHome(place, status);
        status.setMergeInterfaceStatus(mergeStatus);
        Result<InterfacePlaceAction> result = mergeStatus.add(includes);
        assertFalse(result.hasResult());
        assertEquals(1, net.getPlaces().size());
        assertEquals(1, includes.getInterfacePlaceMap().size());
        Place addedPlace = includes.getInterfacePlace("P10");
        assertEquals("P10", addedPlace.getId());
        assertTrue(addedPlace.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusHome);
        result = mergeStatus.remove(includes);
        assertFalse(result.hasResult());
        assertTrue(place.getStatus() instanceof PlaceStatusNormal);
        assertEquals(1, net.getPlaces().size());
        assertEquals(0, includes.getInterfacePlaceMap().size());
    }

    @Test
    public void deletingAwayPlaceReturnsItToAvailableStatus() throws Exception {
        setup(false);
        Transition t0 = new DiscreteTransition("T0");
        net.addTransition(t0);
        buildAvailableAndAwayPlaces(true);
        InboundArc arcIn = new InboundNormalArc(topPlace, t0, new HashMap<String, String>());
        net.add(arcIn);
        assertEquals(1, net.getArcs().size());
        assertEquals(1, net.getPlaces().size());
        topPlace = includes.getInterfacePlace("b.P0");
        assertTrue(topPlace.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAway);
        net.removePlace(topPlace);
        assertEquals(0, net.getArcs().size());
        assertEquals(0, net.getPlaces().size());
        assertEquals(1, includes.getInterfacePlaceMap().size());
        assertTrue(topPlace.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAvailable);
    }

    @Test
    public void cantDeleteHomePlaceIfAwayPlaceIsInUse() throws Exception {
        setup(false);
        Transition t0 = new DiscreteTransition("T0");
        net.addTransition(t0);
        buildAvailableAndAwayPlaces(true);
        InboundArc arcIn = new InboundNormalArc(topPlace, t0, new HashMap<String, String>());
        net.add(arcIn);
        assertEquals(1, net.getArcs().size());
        try {
            net3.removePlace(homePlace);
            fail("should throw");
        } catch (PetriNetComponentException e) {
            assertEquals("Cannot delete P0:\n" +
                    "Place b.P0 cannot be removed from IncludeHierarchy top because it is referenced in arc b.P0 TO T0\n", e
                            .getMessage());
        }
        assertEquals(1, includes.getInterfacePlaceMap().size());
        assertEquals(1, include2.getInterfacePlaceMap().size());
        assertEquals(1, include3.getInterfacePlaceMap().size());
        assertEquals(1, net.getPlaces().size());
        assertEquals(3, net2.getPlaces().size());
        assertEquals(2, net3.getPlaces().size());
        assertTrue(homePlace.getStatus() instanceof PlaceStatusInterface);
    }

    @Test
    public void homePlaceInUseCanStillBeDeletedIfNoAwayPlacesAreInUse() throws Exception {
        setup(false);
        buildAvailableAndAwayPlaces(true);
        net3.removePlace(homePlace);
        assertEquals(0, includes.getInterfacePlaceMap().size());
        assertEquals(0, include2.getInterfacePlaceMap().size());
        assertEquals(0, include3.getInterfacePlaceMap().size());
        assertEquals(0, net.getPlaces().size());
        assertEquals(3, net2.getPlaces().size());
        assertEquals("P0 deleted", 1, net3.getPlaces().size());
        assertTrue(homePlace.getStatus() instanceof PlaceStatusNormal);
        expectedException.expect(PetriNetComponentNotFoundException.class);
        net3.getComponent("P0", Place.class);
    }

    @Test
    public void availableAndAwayInterfacePlacesRemovedIfNotInUse() throws Exception {
        checkAvailableAndAwayInterfacePlacesRemovedIfNotInUse(false);
    }

    @Test
    public void availableAndAwayInterfacePlacesRemovedIfNotInUseWithRefresh() throws Exception {
        checkAvailableAndAwayInterfacePlacesRemovedIfNotInUse(true);
    }

    public void checkAvailableAndAwayInterfacePlacesRemovedIfNotInUse(boolean refresh)
            throws IncludeException, PetriNetComponentException, PetriNetComponentNotFoundException {
        setup(false);
        buildAvailableAndAwayPlaces(refresh);
        assertTrue(homePlace.getStatus() instanceof PlaceStatusInterface);
        //		assertTrue(((Place) homePlace.getLinkedConnectable()).getStatus() instanceof PlaceStatusInterface); 
        Result<InterfacePlaceAction> result = mergeStatus.remove(include3);
        assertFalse(result.hasResult());
        assertEquals(0, includes.getInterfacePlaceMap().size());
        assertEquals(0, include2.getInterfacePlaceMap().size());
        assertEquals(0, include3.getInterfacePlaceMap().size());
        assertEquals(0, net.getPlaces().size());
        assertEquals(3, net2.getPlaces().size());
        assertEquals(2, net3.getPlaces().size());
        assertTrue(homePlace.getStatus() instanceof PlaceStatusNormal);
        //TODO consider not relying on linkedConnectable (this isn't the same one as is being referenced in Merge..Home.resetStatusToNormal()
        //     perhaps homePlace is never updated, as it is not needed once the EPN is built; homePlace only needed by IncludeHierarchy
        //     and PetriNet.  
        //		assertTrue(((Place) homePlace.getLinkedConnectable()).getStatus() instanceof PlaceStatusNormal); 
    }

    @Test
    public void noInterfacePlacesRemovedIfInUseEitherHomeOrAway() throws Exception {
        checkNoInterfacePlacesRemovedIfInUseEitherHomeOrAway(false);
    }

    @Test
    public void noInterfacePlacesRemovedIfInUseEitherHomeOrAwayWithRefresh() throws Exception {
        checkNoInterfacePlacesRemovedIfInUseEitherHomeOrAway(true);
    }

    public void checkNoInterfacePlacesRemovedIfInUseEitherHomeOrAway(boolean refresh)
            throws PetriNetComponentNotFoundException, IncludeException, PetriNetComponentException {
        Transition t0 = new DiscreteTransition("T0");
        net.addTransition(t0);
        buildAvailableAndAwayPlaces(refresh);
        InboundArc arcIn = new InboundNormalArc(topPlace, t0, new HashMap<String, String>());
        net.add(arcIn);
        assertEquals(1, net.getArcs().size());
        Result<InterfacePlaceAction> result = mergeStatus.remove(include3);
        assertTrue(result.hasResult());
        assertEquals(2, result.getMessages().size());
        assertEquals("Place b.P0 cannot be removed from IncludeHierarchy top because it is referenced in arc b.P0 TO T0", result
                .getMessages().get(0));
        assertEquals("Place P0 cannot be removed from IncludeHierarchy b because it is referenced in arc P0 TO T0", result
                .getMessages().get(1));
        assertEquals(1, includes.getInterfacePlaceMap().size());
        assertEquals(1, include2.getInterfacePlaceMap().size());
        assertEquals(1, include3.getInterfacePlaceMap().size());
        assertEquals(1, net.getPlaces().size());
        assertEquals(3, net2.getPlaces().size());
        assertEquals(2, net3.getPlaces().size());
        assertTrue(homePlace.getStatus() instanceof PlaceStatusInterface);
    }

    protected void buildAvailableAndAwayPlaces(boolean refresh)
            throws PetriNetComponentNotFoundException, IncludeException {
        homePlace = net3.getComponent("P0", Place.class);
        assertEquals(0, net.getPlaces().size());
        assertEquals(0, includes.getInterfacePlaceMap().size());
        assertEquals(3, net2.getPlaces().size());
        assertEquals(0, include2.getInterfacePlaceMap().size());
        assertEquals(2, net3.getPlaces().size());
        assertEquals(0, include2.getInterfacePlaceMap().size());
        include3.addToInterface(homePlace, true, false, false, false);
        assertEquals(1, includes.getInterfacePlaceMap().size());
        assertEquals(1, include2.getInterfacePlaceMap().size());
        assertEquals(1, include3.getInterfacePlaceMap().size());
        mergeStatus = homePlace.getStatus().getMergeInterfaceStatus();
        aPlace = include2.getInterfacePlace("b.P0");
        assertTrue(aPlace.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAvailable);
        topPlace = includes.getInterfacePlace("b.P0");
        includes.addAvailablePlaceToPetriNet(topPlace);
        if (refresh) {
            ExecutablePetriNetCloner.refreshFromIncludeHierarchy(net.getExecutablePetriNet());
        }
        assertEquals(1, net.getPlaces().size());
        assertTrue(topPlace.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAway);
    }

    //	assertTrue(status.canRemove()); 
    //TODO test that place must be part of the PN before it can be added to the interface
    @Test
    public void interfacePlaceAddedToInitialHierarchy() throws Exception {
        net.addPlace(place);
        MergeInterfaceStatusHome mergeStatus = new MergeInterfaceStatusHome(place, status);
        assertEquals(1, net.getPlaces().size());
        assertEquals(0, includes.getInterfacePlaceMap().size());
        Result<InterfacePlaceAction> result = mergeStatus.add(includes);
        assertFalse(result.hasResult());
        assertEquals("no change to places", 1, net.getPlaces().size());
        assertEquals(1, includes.getInterfacePlaceMap().size());
        Place addedPlace = includes.getInterfacePlace("P10");
        assertEquals("P10", addedPlace.getId());
    }

    @Test
    public void interfacePlaceAddedToIncludeHierarchiesInAccessScopeOnlyWithAvailableMergeStatus() throws Exception {
        MergeInterfaceStatusHome mergeStatus = new MergeInterfaceStatusHome(place, status);
        status.setMergeInterfaceStatus(mergeStatus); // normally automatic
        assertEquals(3, net2.getPlaces().size());
        assertEquals(0, includes.getInterfacePlaceMap().size());
        assertEquals(0, include2.getInterfacePlaceMap().size());
        assertEquals(0, include3.getInterfacePlaceMap().size());
        Result<InterfacePlaceAction> result = mergeStatus.add(include2);
        assertFalse(result.hasResult());
        assertEquals(1, includes.getInterfacePlaceMap().size());
        assertEquals(1, include2.getInterfacePlaceMap().size());
        assertEquals("child not updated", 0, include3.getInterfacePlaceMap().size());
        Place homePlace = include2.getInterfacePlace("P10");
        Place availablePlace = includes.getInterfacePlace("a.P10");
        assertTrue(homePlace.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusHome);
        assertTrue(availablePlace.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAvailable);
    }

    @Test
    public void newAvailableMergeStatusKnowsItsIncludeHierarchyAndPlaceListensForTokenChanges() throws Exception {
        status = new PlaceStatusInterface(place, include2); // automatic under IH.addToInterface
        place.setStatus(status);
        MergeInterfaceStatusHome mergeStatus = new MergeInterfaceStatusHome(place, status);
        status.setMergeInterfaceStatus(mergeStatus); // automatic under IH.addToInterface
        Result<InterfacePlaceAction> result = mergeStatus.add(include2);
        Place homePlace = include2.getInterfacePlace("P10");
        Place availablePlace = includes.getInterfacePlace("a.P10");
        assertTrue(homePlace.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusHome);
        assertTrue(availablePlace.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAvailable);
        homePlace.setTokenCount("Default", 3);
        assertEquals(3, availablePlace.getTokenCount("Default"));
        assertEquals(homePlace.getStatus().getIncludeHierarchy(), include2);
        assertEquals("new place knows its include hierarchy", availablePlace.getStatus()
                .getIncludeHierarchy(), includes);
    }

    protected PetriNet buildNet(int i) throws PetriNetComponentException {
        PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0"))
                .and(APlace.withId("P1")).and(AnImmediateTransition.withId("T0"))
                .andFinally(ANormalArc.withSource("P0").andTarget("T0"));
        net.setName(new NormalPetriNetName("net" + i));
        return net;
    }

    protected PetriNet buildNetNoArc(int i) throws PetriNetComponentException {
        PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0"))
                .and(APlace.withId("P1")).andFinally(AnImmediateTransition.withId("T0"));
        net.setName(new NormalPetriNetName("net" + i));
        return net;
    }

}
