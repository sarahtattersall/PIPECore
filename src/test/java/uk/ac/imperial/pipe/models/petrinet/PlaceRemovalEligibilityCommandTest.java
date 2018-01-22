package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.exceptions.IncludeException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;

public class PlaceRemovalEligibilityCommandTest {

    private IncludeHierarchy includes;
    private PlaceRemovalEligibilityCommand command;
    private PetriNet net;
    private PetriNet net2;
    private Place placeA;
    private Place topPlace;
    private Result<InterfacePlaceAction> result;
    private IncludeHierarchy include2;

    @Before
    public void setUp() throws Exception {
        net = createSimpleNet(1);
        net2 = createSimpleNet(2);
        buildHierarchyWithInterfacePlaces();
    }

    private void buildHierarchyWithInterfacePlaces()
            throws PetriNetComponentNotFoundException, IncludeException {
        includes = new IncludeHierarchy(net, "top");
        includes.include(net2, "a");
        include2 = includes.getInclude("a");
        placeA = net2.getComponent("P0", Place.class);
        include2.addToInterface(placeA, true, false, false, false);
        assertTrue(placeA.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusHome);
        assertEquals("a.P0", placeA.getStatus().getMergeInterfaceStatus().getAwayId());
        topPlace = includes.getInterfacePlace("a.P0");
        includes.addAvailablePlaceToPetriNet(topPlace);
        assertTrue(topPlace.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAway);
        assertEquals("place added to Petri net", 3, net.getPlaces().size());
        assertNotNull(includes.getPetriNet().getComponent("a.P0", Place.class));
    }

    @Test
    public void interfacePlaceWithNoDependenciesIsEligibleToBeRemovedFromHomeInclude() throws Exception {
        net = createSimpleNetOneArc(1);
        net2 = createSimpleNetOneArc(2);
        buildHierarchyWithInterfacePlaces();
        command = new PlaceRemovalEligibilityCommand(placeA);
        result = include2.parent(command);
        result = include2.self(command);
        //		command = new PlaceRemovalEligibilityCommand(topPlace);  // won't work; must be home place  
        //		result = command.execute(includes);
        assertFalse(result.hasResult());
    }

    @Test
    public void nonexistentplaceCantBeRemoved() throws Exception {
        net = createSimpleNetOneArc(1);
        net2 = createSimpleNet(2);
        buildHierarchyWithInterfacePlaces();
        command = new PlaceRemovalEligibilityCommand(new DiscretePlace("P99"));
        result = include2.self(command);
        assertTrue(result.hasResult());
        assertEquals(1, result.getEntries().size());
        Iterator<ResultEntry<InterfacePlaceAction>> iterator = result.getEntries().iterator();
        ResultEntry<InterfacePlaceAction> entry = iterator.next();
        assertEquals("Place P99 cannot be removed from IncludeHierarchy a" +
                " because no MergeInterfaceStatus was found for it in the IncludeHierarchy; probable logic error", entry.message);
    }

    @Test
    public void placeWithArcsIsNotEligibleToBeRemoved() throws Exception {
        net = createSimpleNetOneArc(1);
        net2 = createSimpleNet(2);
        buildHierarchyWithInterfacePlaces();
        Transition t0 = net.getComponent("T0", Transition.class);
        InboundArc arcIn = new InboundNormalArc(topPlace, t0, new HashMap<String, String>());
        OutboundArc arcOut = new OutboundNormalArc(t0, topPlace, new HashMap<String, String>());
        assertEquals(1, net.getArcs().size());
        net.add(arcIn);
        net.add(arcOut);
        assertEquals(3, net.getArcs().size());
        command = new PlaceRemovalEligibilityCommand(placeA);
        result = include2.parent(command);
        assertTrue(result.hasResult());
        assertEquals(2, result.getEntries().size());
        Iterator<ResultEntry<InterfacePlaceAction>> iterator = result.getEntries().iterator();
        ResultEntry<InterfacePlaceAction> entry = iterator.next();
        assertEquals("Place a.P0 cannot be removed from IncludeHierarchy top" +
                " because it is referenced in arc T0 TO a.P0", entry.message);
        assertEquals("T0 TO a.P0", entry.value.getComponentId());
        assertEquals(includes, entry.value.getIncludeHierarchy());
        assertEquals(topPlace, entry.value.getInterfacePlace());
        entry = iterator.next();
        assertEquals("Place a.P0 cannot be removed from IncludeHierarchy top" +
                " because it is referenced in arc a.P0 TO T0", entry.message);
        assertEquals("a.P0 TO T0", entry.value.getComponentId());
        assertEquals(includes, entry.value.getIncludeHierarchy());
        assertEquals(topPlace, entry.value.getInterfacePlace());
        //        OutboundArc exArcIn = executablePetriNet.getComponent("top.T1 TO top.a.P2", OutboundArc.class);
    }

    @Test
    public void placeWithFunctionalExpressionDependenciesIsNotEligibleToBeRemoved() throws Exception {
        FunctionalRateParameter rateParameter = new FunctionalRateParameter("#(a.P0)", "frp1", "frp1");
        net.addRateParameter(rateParameter);
        Transition t2 = new DiscreteTransition("T2", "T2");
        t2.setTimed(true);
        t2.setRate(rateParameter);
        net.addTransition(t2);
        Map<String, String> tokenWeights = new HashMap<>();
        tokenWeights.put("Default", "#(a.P0)");
        Transition t0 = net.getComponent("T0", Transition.class);
        OutboundArc newArc = new OutboundNormalArc(t0, topPlace, tokenWeights);
        net.addArc(newArc);

        command = new PlaceRemovalEligibilityCommand(placeA);
        result = include2.parent(command);
        assertTrue(result.hasResult());
        assertEquals(4, result.getEntries().size());
        Iterator<ResultEntry<InterfacePlaceAction>> iterator = result.getEntries().iterator();
        ResultEntry<InterfacePlaceAction> entry = iterator.next();
        assertEquals("Place a.P0 cannot be removed from IncludeHierarchy top" +
                " because it is referenced in arc T0 TO a.P0", entry.message);
        assertEquals("T0 TO a.P0", entry.value.getComponentId());
        assertEquals(includes, entry.value.getIncludeHierarchy());
        assertEquals(topPlace, entry.value.getInterfacePlace());
        entry = iterator.next();
        assertEquals("Place a.P0 cannot be removed from IncludeHierarchy top" +
                " because it is referenced in a functional expression in component T0 TO a.P0", entry.message);
        assertEquals("T0 TO a.P0", entry.value.getComponentId());
        assertEquals(includes, entry.value.getIncludeHierarchy());
        assertEquals(topPlace, entry.value.getInterfacePlace());
        entry = iterator.next();
        assertEquals("Place a.P0 cannot be removed from IncludeHierarchy top" +
                " because it is referenced in a functional expression in component frp1", entry.message);
        assertEquals("frp1", entry.value.getComponentId());
        entry = iterator.next();
        assertEquals("Place a.P0 cannot be removed from IncludeHierarchy top" +
                " because it is referenced in a functional expression in component T2", entry.message);
        assertEquals("T2", entry.value.getComponentId());

    }

    private PetriNet createSimpleNet(int i) throws PetriNetComponentException {
        PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0"))
                .and(APlace.withId("P1")).and(AnImmediateTransition.withId("T0"))
                .and(AnImmediateTransition.withId("T1")).and(ANormalArc.withSource("T1").andTarget("P1"))
                .andFinally(ANormalArc.withSource("T0").andTarget("P0").with("#(P0)", "Default").token());
        net.setName(new NormalPetriNetName("net" + i));
        return net;
    }

    private PetriNet createSimpleNetOneArc(int i) throws PetriNetComponentException {
        PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0"))
                .and(APlace.withId("P1")).and(AnImmediateTransition.withId("T0"))
                .and(AnImmediateTransition.withId("T1")).andFinally(ANormalArc.withSource("T1").andTarget("P1"));
        net.setName(new NormalPetriNetName("net" + i));
        return net;
    }

}
