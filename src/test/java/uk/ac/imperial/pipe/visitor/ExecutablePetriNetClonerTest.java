package uk.ac.imperial.pipe.visitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.ATimedTransition;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.exceptions.IncludeException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.AbstractPetriNet;
import uk.ac.imperial.pipe.models.petrinet.ArcPoint;
import uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet;
import uk.ac.imperial.pipe.models.petrinet.InboundArc;
import uk.ac.imperial.pipe.models.petrinet.InboundNormalArc;
import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatusAvailable;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatusAway;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatusHome;
import uk.ac.imperial.pipe.models.petrinet.NoOpInterfaceStatus;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.PetriNetComponent;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.PlaceStatus;
import uk.ac.imperial.pipe.models.petrinet.PlaceStatusNormal;
import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;

@RunWith(MockitoJUnitRunner.class)
public class ExecutablePetriNetClonerTest {
    PetriNet oldPetriNet;
    PetriNet clonedPetriNet;
    private PetriNet net2;
    private IncludeHierarchy includes;
    private ExecutablePetriNet executablePetriNet;

    @Mock
    Place mockSource;

    @Mock
    Transition mockTarget;

    @Mock
    private PropertyChangeListener mockListener;

    InboundNormalArc arc;

    @Before
    public void setUp() throws PetriNetComponentException {
        buildSimpleNet();
    }

    //TODO clones InterfacePlaces (?)
    private void buildSimpleNet() throws PetriNetComponentException {
        oldPetriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P0").and(1, "Default").token()).and(APlace.withId("P1"))
                .and(ATimedTransition.withId("T0")).and(ATimedTransition.withId("T1"))
                .and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token())
                .and(ANormalArc.withSource("T0").andTarget("P1").with("1", "Default").token())
                .and(ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token())
                .andFinally(ANormalArc.withSource("T1").andTarget("P0").with("1", "Default").token());
    }

    @Test
    public void clonePetriNetToExecutablePetriNetReplacingExistingState() throws Exception {
        buildSimpleNet();
        oldPetriNet.setIncludeHierarchy(new IncludeHierarchy(oldPetriNet, "root"));
        ExecutablePetriNet executablePetriNet = new ExecutablePetriNet(oldPetriNet);
        ExecutablePetriNetCloner.refreshFromIncludeHierarchy(executablePetriNet);
        assertEquals("root.P0", executablePetriNet.getComponent("root.P0", Place.class).getId());
        assertEquals("root.T0", executablePetriNet.getComponent("root.T0", Transition.class).getId());
        assertEquals("root.P0 TO root.T0", executablePetriNet.getComponent("root.P0 TO root.T0", InboundArc.class)
                .getId());
    }

    //TODO test for transitionOut/InboundArcs
    //TODO test for arcweights
    @Test
    public void convertsArcsFromInterfacePlaceToNewOriginPlace() throws Exception {
        buildIncludeHierarchyAndRefreshExecutablePetriNet();
        checkExecutableHasSumOfOldPNAndNet2Components(net2, executablePetriNet);
        Place originPlace = net2.getComponent("P0", Place.class);
        includes.getInclude("a").addToInterface(originPlace, true, false, false, false);

        Place topPlace = includes.getInterfacePlace("a.P0");
        assertTrue(includes.getInterfacePlace("a.P0").getStatus()
                .getMergeInterfaceStatus() instanceof MergeInterfaceStatusAvailable);
        assertTrue(includes.getInclude("a").getInterfacePlace("P0").getStatus()
                .getMergeInterfaceStatus() instanceof MergeInterfaceStatusHome);
        includes.addAvailablePlaceToPetriNet(topPlace);
        assertTrue(includes.getInterfacePlace("a.P0").getStatus()
                .getMergeInterfaceStatus() instanceof MergeInterfaceStatusAway);
        assertEquals("now has another place", 3, oldPetriNet.getPlaces().size());
        ExecutablePetriNetCloner.refreshFromIncludeHierarchy(executablePetriNet);
        assertEquals("...but interface places not copied to executable PN", 4, executablePetriNet.getPlaces().size());
        Place newPlace = ExecutablePetriNetCloner.cloneInstance.getPendingAwayPlacesForInterfacePlaceConversion()
                .get("a.P0");
        originPlace.setId("a.P0");
        assertTrue("s/b same once origin id is forced, except for isOriginal()", (newPlace
                .equalsPosition(originPlace) && (newPlace.equalsStructure(originPlace))));
        assertEquals(1, ExecutablePetriNetCloner.cloneInstance.getPendingAwayPlacesForInterfacePlaceConversion()
                .size());
        assertEquals("a.P0", ExecutablePetriNetCloner.cloneInstance.getPendingAwayPlacesForInterfacePlaceConversion()
                .values().iterator().next().getId());
    }

    @Test
    public void cloningArcsRemovesNewIntermediatePointChangeListenerBeforeAddingPointToNewArc() throws Exception {
        when(mockSource.getId()).thenReturn("source");
        when(mockTarget.getId()).thenReturn("target");
        when(mockSource.getArcEdgePoint(anyDouble())).thenReturn(new Point2D.Double(0, 0));
        when(mockTarget.getArcEdgePoint(anyDouble())).thenReturn(new Point2D.Double(0, 0));
        when(mockSource.getStatus()).thenReturn(new PlaceStatusNormal(mockSource));
        Point2D.Double center = mock(Point2D.Double.class);
        when(mockSource.getCentre()).thenReturn(center);
        when(mockTarget.getCentre()).thenReturn(new Point2D.Double(15, 15));
        arc = new InboundNormalArc(mockSource, mockTarget, new HashMap<String, String>());

        ArcPoint point = new ArcPoint(center, false);
        ArcPoint intermediate = new ArcPoint(new Point2D.Double(1, 5), false);
        ArcPoint intermediate2 = new ArcPoint(new Point2D.Double(1, 6), false);
        arc.addIntermediatePoint(intermediate);
        arc.addIntermediatePoint(intermediate2);
        assertEquals(1, intermediate.changeSupport.getPropertyChangeListeners().length);
        assertEquals(1, intermediate2.changeSupport.getPropertyChangeListeners().length);
        assertEquals(4, arc.getArcPoints().size());
        assertEquals(0, arc.getArcPoints().get(0).changeSupport.getPropertyChangeListeners().length);
        assertEquals("intermediate point automatically has listener", 1, arc.getArcPoints().get(1).changeSupport
                .getPropertyChangeListeners().length);
        assertEquals(1, arc.getArcPoints().get(2).changeSupport.getPropertyChangeListeners().length);
        assertEquals(0, arc.getArcPoints().get(3).changeSupport.getPropertyChangeListeners().length);

        TestingCloner cloner = new TestingCloner();
        InboundArc newArc = cloner.buildInboundArc(arc, mockSource, mockTarget);
        assertEquals(4, newArc.getArcPoints().size());
        assertEquals(0, newArc.getArcPoints().get(0).changeSupport.getPropertyChangeListeners().length);
        assertEquals("when intermediate point is copied to the new arc, it should remove the listener " +
                "just created (not the original arc's listener)", 1, newArc
                        .getArcPoints().get(1).changeSupport.getPropertyChangeListeners().length);
        assertEquals(1, newArc.getArcPoints().get(2).changeSupport.getPropertyChangeListeners().length);
        assertEquals(0, newArc.getArcPoints().get(3).changeSupport.getPropertyChangeListeners().length);
        InboundArc newArc2 = cloner.buildInboundArc(arc, mockSource, mockTarget);
        assertEquals(4, newArc2.getArcPoints().size());
        assertEquals(0, newArc2.getArcPoints().get(0).changeSupport.getPropertyChangeListeners().length);
        assertEquals("when we clone the same source arc over and over, we preserve its listener, " +
                "not the new listener", 1, newArc2.getArcPoints().get(1).changeSupport
                        .getPropertyChangeListeners().length);
        assertEquals(1, newArc2.getArcPoints().get(2).changeSupport.getPropertyChangeListeners().length);
        assertEquals(0, newArc2.getArcPoints().get(3).changeSupport.getPropertyChangeListeners().length);

    }

    private class TestingCloner extends AbstractPetriNetCloner {

        @Override
        protected AbstractPetriNet getNewPetriNet() {
            return null;
        }

        @Override
        protected AbstractPetriNetCloner getInstance() {
            return null;
        }

        @Override
        protected void prefixIdWithQualifiedName(PetriNetComponent component) {
        }

        @Override
        protected void prepareExecutablePetriNetPlaceProcessing(Place place, Place newPlace) {
        }

    }

    @Test
    public void throwsIfAwayPlaceHasNoCorrespondingHomePlaceWhenConvertingArcs() throws Exception {
        buildIncludeHierarchyAndRefreshExecutablePetriNet();
        // refresh builds pending home places
        Place originPlace = net2.getComponent("P0", Place.class);
        includes.getInclude("a").addToInterface(originPlace, true, false, false, false);

        Place topPlace = includes.getInterfacePlace("a.P0");
        assertTrue(includes.getInterfacePlace("a.P0").getStatus()
                .getMergeInterfaceStatus() instanceof MergeInterfaceStatusAvailable);
        assertTrue(includes.getInclude("a").getInterfacePlace("P0").getStatus()
                .getMergeInterfaceStatus() instanceof MergeInterfaceStatusHome);
        includes.addAvailablePlaceToPetriNet(topPlace);
        assertTrue(includes.getInterfacePlace("a.P0").getStatus()
                .getMergeInterfaceStatus() instanceof MergeInterfaceStatusAway);
        HashMap<String, String> weights = new HashMap<>();
        // need an arc that uses the away place
        weights.put("default", "1");
        executablePetriNet.addArc(new InboundNormalArc(topPlace, net2.getComponent("T0", Transition.class), weights));
        PlaceStatus placeStatus = includes.getInclude("a").getInterfacePlace("P0").getStatus();
        // remove home status from original place
        placeStatus.setMergeInterfaceStatus(new NoOpInterfaceStatus(placeStatus));
        ExecutablePetriNetCloner.cloneInstance.pendingNewHomePlaces = new HashMap<>();
        // NPE when creating arc if not caught.
        try {
            ExecutablePetriNetCloner.refreshFromIncludeHierarchy(executablePetriNet);
            fail("should throw");
        } catch (IncludeException e) {
            assertEquals("Away place a.P0 does not have a corresponding Home place.  " +
                    "Possible causes:\n" +
                    "  petri net opened standalone, outside of its include hierarchy\n" +
                    "  missing 'merge type=\"home\"' entry in the PNML for the place in its home petri net", e
                            .getMessage());
        }
    }

    @Test
    public void buildsMapOfOldAndClonedPlacesInExecutablePetriNet() throws Exception {
        buildSimpleNet();
        executablePetriNet = oldPetriNet.getExecutablePetriNet();
        ExecutablePetriNetCloner.refreshFromIncludeHierarchy(executablePetriNet);
        assertEquals(2, executablePetriNet.getPlaceCloneMap().size());
        Place P0 = oldPetriNet.getComponent("P0", Place.class);
        Place P1 = oldPetriNet.getComponent("P1", Place.class);
        Place clonedP0 = executablePetriNet.getComponent("P0", Place.class);
        Place clonedP1 = executablePetriNet.getComponent("P1", Place.class);
        assertEquals(clonedP0, executablePetriNet.getPlaceCloneMap().get(P0));
        assertEquals(clonedP1, executablePetriNet.getPlaceCloneMap().get(P1));
    }

    @Test
    public void homePlacesInMergeInterfaceStatusAreNotCloned() throws Exception {
        buildIncludeHierarchyAndRefreshExecutablePetriNet();
        Place originPlace = net2.getComponent("P0", Place.class);
        includes.getInclude("a").addToInterface(originPlace, true, false, false, false);
        assertTrue(includes.getInterfacePlace("a.P0").getStatus()
                .getMergeInterfaceStatus() instanceof MergeInterfaceStatusAvailable);
        assertTrue(includes.getInclude("a").getInterfacePlace("P0").getStatus()
                .getMergeInterfaceStatus() instanceof MergeInterfaceStatusHome);
        assertTrue(originPlace == includes.getInclude("a").getInterfacePlace("P0").getStatus().getMergeInterfaceStatus()
                .getHomePlace());
        assertTrue(originPlace == includes.getInterfacePlace("a.P0").getStatus().getMergeInterfaceStatus()
                .getHomePlace());
        ExecutablePetriNetCloner.refreshFromIncludeHierarchy(executablePetriNet);
        assertTrue(originPlace == includes.getInclude("a").getInterfacePlace("P0").getStatus().getMergeInterfaceStatus()
                .getHomePlace());
        assertTrue(originPlace == includes.getInterfacePlace("a.P0").getStatus().getMergeInterfaceStatus()
                .getHomePlace());
    }

    private void buildIncludeHierarchyAndRefreshExecutablePetriNet()
            throws PetriNetComponentException, IncludeException {
        buildIncludeHierarchy();
        executablePetriNet = new ExecutablePetriNet(oldPetriNet);
        ExecutablePetriNetCloner.refreshFromIncludeHierarchy(executablePetriNet);
    }

    private void buildIncludeHierarchy() throws PetriNetComponentException, IncludeException {
        buildSimpleNet();
        oldPetriNet.setName(new NormalPetriNetName("net"));
        net2 = buildTestNet();
        includes = new IncludeHierarchy(oldPetriNet, "top");
        includes.include(net2, "a");
        oldPetriNet.setIncludeHierarchy(includes);
    }

    protected void checkExecutableHasSumOfOldPNAndNet2Components(PetriNet net2,
            ExecutablePetriNet executablePetriNet) {
        assertEquals(2, oldPetriNet.getPlaces().size());
        assertEquals(4, oldPetriNet.getArcs().size());
        assertEquals(2, oldPetriNet.getTransitions().size());
        assertEquals(2, net2.getPlaces().size());
        assertEquals(2, net2.getArcs().size());
        assertEquals(2, net2.getTransitions().size());
        assertEquals("oldPN + net2 places = 4", 4, executablePetriNet.getPlaces().size());
        assertEquals("oldPN + net2 transitions = 4", 4, executablePetriNet.getTransitions().size());
        assertEquals("oldPN + net2 arcs = 6", 6, executablePetriNet.getArcs().size());
    }

    protected PetriNet buildTestNet() throws PetriNetComponentException {
        PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0"))
                .and(APlace.withId("P1")).and(AnImmediateTransition.withId("T0"))
                .and(AnImmediateTransition.withId("T1")).and(ANormalArc.withSource("P1").andTarget("T1"))
                .andFinally(ANormalArc.withSource("T0").andTarget("P0").with("#(P0)", "Default").token());
        return net;
    }

}
