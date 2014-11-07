package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import java.awt.Color;

import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;

public class MergeInterfaceStatusAwayTest {

	private IncludeHierarchy includes;
	private PetriNet net;
	private Place place;
	private PlaceStatus status;
	
	@Before
	public void setUp() throws Exception {
		net = buildNet(1); 
		includes = new IncludeHierarchy(net, "top"); 
		place = new DiscretePlace("P10");
		status = new PlaceStatusInterface(place, includes); 
		place.setStatus(status); 
	}
	@Test
	public void cantRemoveAwayStatusFromInterfacePlaces() throws Exception {
		MergeInterfaceStatus mergeStatus = new MergeInterfaceStatusAway(place, status, "a.P0"); 
		assertFalse(mergeStatus.canRemove()); 
		Result<InterfacePlaceAction> result = mergeStatus.remove(includes); 
		assertTrue(result.hasResult()); 
		assertEquals("MergeInterfaceStatusAway.remove: not supported for Away status.  " +
				"Must be issued by MergeInterfaceStatusHome against the home include hierarchy.", result.getMessage()); 
	}
	protected PetriNet buildNet(int i) throws PetriNetComponentNotFoundException {
		PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
    			APlace.withId("P1")).and(AnImmediateTransition.withId("T0")).andFinally(
    			ANormalArc.withSource("P0").andTarget("T0"));
		net.setName(new NormalPetriNetName("net"+i));
		return net;
	}

}
