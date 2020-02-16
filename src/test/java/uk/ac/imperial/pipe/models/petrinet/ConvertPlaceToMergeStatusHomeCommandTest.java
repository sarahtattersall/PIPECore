package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;

import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;

public class ConvertPlaceToMergeStatusHomeCommandTest {

    private IncludeHierarchy includes;
    private PetriNet net;
    private Place place;
    private PlaceStatus status;
    private ConvertPlaceToMergeStatusHomeCommand command;

    @Before
    public void setUp() throws Exception {
        net = new PetriNet();
        includes = new IncludeHierarchy(net, "top");
        place = new DiscretePlace("P10");
        status = new PlaceStatusInterface(place, includes);
        place.setStatus(status);
    }

    @Test
    public void placeAddedToInterfaceInHomeIncludHierarchy() throws Exception {
        assertEquals(0, includes.getInterfacePlaceMap().size());
        command = new ConvertPlaceToMergeStatusHomeCommand(place, includes);
        Result<InterfacePlaceAction> result = command.execute(includes);
        assertFalse(result.hasResult());
        assertEquals(1, includes.getInterfacePlaceMap().size());
        Place addedPlace = includes.getInterfacePlace("P10");
        assertEquals("P10", addedPlace.getId());
    }

    @Test
    public void placeAddedToAwayIncludeHierarchyWithAvailableStatus() throws Exception {
        PetriNet net2 = buildNet(2);
        IncludeHierarchy include2 = new IncludeHierarchy(net2, "a");
        assertEquals(0, includes.getInterfacePlaceMap().size());
        assertEquals(0, include2.getInterfacePlaceMap().size());
        MergeInterfaceStatusHome mergeStatus = new MergeInterfaceStatusHome(place, place.getStatus());
        mergeStatus.buildAwayId("a.");
        place.getStatus().setMergeInterfaceStatus(mergeStatus);
        command = new ConvertPlaceToMergeStatusHomeCommand(place, includes);
        Result<InterfacePlaceAction> result = command.execute(include2);
        assertFalse(result.hasResult());
        assertEquals("not added to home include hierarchy", 0, includes.getInterfacePlaceMap().size());
        assertEquals("...added to away IH", 1, include2.getInterfacePlaceMap().size());
        Place addedPlace = include2.getInterfacePlace("a.P10");
        assertEquals("prefixed with away Id", "a.P10", addedPlace.getId());
        assertTrue(addedPlace.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAvailable);
    }

    protected PetriNet buildNet(int i) throws PetriNetComponentException {
        PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0"))
                .and(APlace.withId("P1")).and(AnImmediateTransition.withId("T0"))
                .andFinally(ANormalArc.withSource("P0").andTarget("T0"));
        net.setName(new NormalPetriNetName("net" + i));
        return net;
    }

}
