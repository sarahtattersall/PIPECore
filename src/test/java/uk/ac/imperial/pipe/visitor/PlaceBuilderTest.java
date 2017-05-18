package uk.ac.imperial.pipe.visitor;

import static org.junit.Assert.*;

import java.awt.Color;

import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.exceptions.IncludeException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.DiscretePlace;
import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatus;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatusAvailable;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatusAway;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.PlaceStatusInterface;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;

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
	}

	@Test
	public void buildsCloneAsLinkedConnectableWithPlaceStatus() throws Exception {
	}
	@Test
	public void buildsAvailablePlaceFromHomePlace() throws Exception {
		builder = new PlaceBuilder(includes);
		homePlace.accept(builder);
		Place availablePlace = builder.cloned;
		assertEquals(includes, availablePlace.getStatus().getIncludeHierarchy());
		assertTrue(availablePlace.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAvailable);  
		assertEquals("b.P0", availablePlace.getId());
		assertFalse("not a linked connectable",availablePlace.isOrClonedFrom(homePlace));
		//TODO test that listens for property changes
	}
	protected PetriNet buildNetNoArc(int i) throws PetriNetComponentException {
		PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
				APlace.withId("P1")).andFinally(AnImmediateTransition.withId("T0"));
		net.setName(new NormalPetriNetName("net"+i));
		return net;
	}
	
	
}
