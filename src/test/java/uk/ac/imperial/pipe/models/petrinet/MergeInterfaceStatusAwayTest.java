package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;

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
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;

@RunWith(MockitoJUnitRunner.class)
public class MergeInterfaceStatusAwayTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private IncludeHierarchy includes;
    private PetriNet net;
    private Place place;
    private PlaceStatus status;
    private MergeInterfaceStatus mergeStatus;

    @Before
    public void setUp() throws Exception {
        net = buildNet(1);
        includes = new IncludeHierarchy(net, "top");
        place = net.getComponent("P0", Place.class);
        includes.getInterfacePlaceMap().put("P0", place);
        status = place.getStatus();
    }

    @Test
    public void deleteAwayPlaceReturnsToMergeStatusAvailable() throws Exception {
        assertEquals(2, net.getPlaces().size());
        assertEquals(1, includes.getInterfacePlaceMap().size());
        mergeStatus = new MergeInterfaceStatusAway(place, status, "P0");
        status.setMergeInterfaceStatus(mergeStatus);
        assertFalse(mergeStatus.canRemove());
        Result<InterfacePlaceAction> result = mergeStatus.remove(includes);
        assertFalse(result.hasResult());
        assertEquals(1, net.getPlaces().size());
        assertEquals(1, includes.getInterfacePlaceMap().size());
        assertTrue(place.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAvailable);
        expectedException.expect(PetriNetComponentNotFoundException.class);
        net.getComponent("P0", Place.class);
        //		assertEquals("MergeInterfaceStatusAway.remove: not supported for Away status.  " +
        //				"Must be issued by MergeInterfaceStatusHome against the home include hierarchy.", result.getMessage()); 
    }

    protected PetriNet buildNet(int i) throws PetriNetComponentException {
        PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0"))
                .and(APlace.withId("P1")).and(AnImmediateTransition.withId("T0"))
                .andFinally(ANormalArc.withSource("P0").andTarget("T0"));
        net.setName(new NormalPetriNetName("net" + i));
        return net;
    }

}
