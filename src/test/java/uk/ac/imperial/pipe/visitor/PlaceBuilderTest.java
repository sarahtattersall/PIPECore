package uk.ac.imperial.pipe.visitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Color;
import java.beans.PropertyChangeListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.DiscretePlace;
import uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet;
import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.InterfacePlaceAction;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatus;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatusAvailable;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatusAway;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatusHome;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.PlaceStatusInterface;
import uk.ac.imperial.pipe.models.petrinet.Result;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;
import uk.ac.imperial.pipe.tuple.Tuple;

public class PlaceBuilderTest {

    private PlaceBuilder builder;
    private PetriNet net;
    private IncludeHierarchy includes;
    private DiscretePlace place;
    private PlaceStatusInterface status;
    private IncludeHierarchy include2;
    private IncludeHierarchy include3;
    private PetriNet net2;
    private PetriNet net3;
    private Place homePlace;
    private MergeInterfaceStatus mergeStatus;
    private Place aPlace;
    private Place topPlace;

    private ExecutablePetriNet executablePetriNet;
    private ExecutablePetriNetCloner cloneInstance;

    @Before
    public void setup() throws Exception {
        net = new PetriNet();
        includes = new IncludeHierarchy(net, "top");
        place = new DiscretePlace("P10");
        status = new PlaceStatusInterface(place, includes);
        place.setStatus(status);
        net2 = buildNetNoArc(2);
        net3 = buildNetNoArc(3);
        includes.include(net2, "a");
        include2 = includes.getInclude("a");
        include2.include(net3, "b");
        include3 = includes.getInclude("b");
        net2.addPlace(place);
        homePlace = net3.getComponent("P0", Place.class);
        include3.addToInterface(homePlace, true, false, false, false);
        mergeStatus = homePlace.getStatus().getMergeInterfaceStatus();
        executablePetriNet = net.getExecutablePetriNet();
        cloneInstance = ExecutablePetriNetCloner.cloneInstance;
    }

    @Test
    public void buildsSimpleCopy() throws Exception {
        DiscretePlace place = new DiscretePlace("P10");
        builder = new PlaceBuilder(cloneInstance, true); // needed so place can be added 
        place.accept(builder);
        DiscretePlace builtPlace = (DiscretePlace) builder.built;
        assertEquals("P10", builtPlace.getId());
        assertEquals("built place not listening for token changes", 0, place.changeSupport
                .getPropertyChangeListeners().length);
        assertEquals("original place not listening for token changes", 0, builtPlace.changeSupport
                .getPropertyChangeListeners().length);
    }

    @Test
    public void buildsCloneOfAwayPlaceWithHomePlaceUnCloned() throws Exception {
        makeTopPlaceAnAwayPlace();
        Place cloned = checkCloneHasRightMergeClassAndIdAndRespondsToTokenChanges(topPlace, MergeInterfaceStatusAway.class, "b.P0");
        Place homeNotCLoned = cloned.getStatus().getMergeInterfaceStatus().getHomePlace();
        assertTrue("homeplace stays as is; not cloned", homePlace == homeNotCLoned);
        assertFalse(cloneInstance.getPendingNewHomePlaces().containsValue(cloned));
    }

    @Test
    public void buildsCloneOfHomePlaceAsLinkedConnectableWithPlaceStatus() throws Exception {
        builder = new PlaceBuilder(cloneInstance);
        Place cloned = checkCloneHasRightMergeClassAndIdAndRespondsToTokenChanges(homePlace, MergeInterfaceStatusHome.class, "top.a.b.P0");
        Place clonedHome = cloned.getStatus().getMergeInterfaceStatus().getHomePlace();
        assertTrue(cloneInstance.getPendingNewHomePlaces().containsValue(cloned));
        assertTrue("id is different, but otherwise same", homePlace.equalsPosition(clonedHome));
        assertTrue(cloned == clonedHome);
        assertTrue(cloned == cloneInstance.pendingNewHomePlaces
                .get(cloned.getStatus().getMergeInterfaceStatus().getAwayId()));
        assertFalse("false but misleading; pendingAwayPlaces only initially populated when refreshing EPN.", cloneInstance
                .getPendingAwayPlacesForInterfacePlaceConversion().containsValue(cloned));
    }

    public Place checkCloneHasRightMergeClassAndIdAndRespondsToTokenChanges(Place originalPlace,
            Class<? extends MergeInterfaceStatus> mergeClass,
            String placeId) throws Exception {
        builder = new PlaceBuilder(cloneInstance);
        originalPlace.accept(builder);
        Place cloned = builder.built;
        assertEquals(cloned.getStatus().getMergeInterfaceStatus().getClass(), mergeClass);
        assertEquals(placeId, cloned.getId());
        assertTrue(cloned.equalsPosition(originalPlace));
        originalPlace.setTokenCount("red", 2);
        assertEquals("clone listens for token changes", 2, cloned.getTokenCount("red"));
        return cloned;
    }

    private void makeTopPlaceAnAwayPlace() {
        topPlace = includes.getInterfacePlace("b.P0");
        MergeInterfaceStatus status = topPlace.getStatus().getMergeInterfaceStatus();
        Result<InterfacePlaceAction> result = status.add(net);
        assertTrue(topPlace.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAway);
        assertEquals("b.P0", topPlace.getId());
    }

    @Test
    public void buildsAvailablePlaceFromHomePlace() throws Exception {
        builder = new PlaceBuilder(includes);
        homePlace.accept(builder);
        Place availablePlace = builder.built;
        assertEquals(includes, availablePlace.getStatus().getIncludeHierarchy());
        assertTrue(availablePlace.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAvailable);
        assertEquals("b.P0", availablePlace.getId());
    }

    @Test
    public void buildsAvailablePlaceAndHomePlaceMirrorTokensT() throws Exception {
        builder = new PlaceBuilder(includes);
        homePlace.accept(builder);
        Place availablePlace = builder.built;
        homePlace.setTokenCount("blue", 3);
        assertEquals("available place listens to home", 3, availablePlace.getTokenCount("blue"));
        availablePlace.setTokenCount("blue", 4);
        assertEquals("...and vice versa", 4, homePlace.getTokenCount("blue"));
    }

    protected PetriNet buildNetNoArc(int i) throws PetriNetComponentException {
        PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0"))
                .and(APlace.withId("P1")).andFinally(AnImmediateTransition.withId("T0"));
        net.setName(new NormalPetriNetName("net" + i));
        return net;
    }

}
