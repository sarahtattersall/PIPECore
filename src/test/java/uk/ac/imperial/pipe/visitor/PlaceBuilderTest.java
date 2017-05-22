package uk.ac.imperial.pipe.visitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatus;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatusAvailable;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatusHome;
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

	private ExecutablePetriNet executablePetriNet;
	private CloneExecutablePetriNet cloneInstance;

	
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
		cloneInstance = CloneExecutablePetriNet.cloneInstance;
	}

	@Test
	public void buildsCloneOfHomePlaceAsLinkedConnectableWithPlaceStatus() throws Exception {
		builder = new PlaceBuilder(cloneInstance);
		homePlace.accept(builder);
		Place cloned = builder.cloned;
		assertTrue(cloned.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusHome);  
		assertEquals("top.a.b.P0", cloned.getId());
		assertTrue(cloned.isOrClonedFrom(homePlace));
		homePlace.setTokenCount("red", 2);
		assertEquals("clone listens for token changes",2, cloned.getTokenCount("red"));
		Place clonedHome = cloned.getStatus().getMergeInterfaceStatus().getHomePlace(); 
		assertTrue(cloned == clonedHome);
		assertTrue(homePlace == clonedHome.getLinkedConnectable());
		assertTrue(cloned == cloneInstance.pendingNewHomePlaces.get(cloned.getStatus().getMergeInterfaceStatus().getAwayId()));
		assertFalse(cloneInstance.getPendingAwayPlacesForInterfacePlaceConversion().containsValue(cloned));
		assertTrue(cloneInstance.getPendingNewHomePlaces().containsValue(cloned));
	}
	@Test
	public void buildsCloneOfAwayPlaceAsLinkedConnectableWithPlaceStatus() throws Exception {
//		fail("refactor visit place, and then write me");
//		builder = new PlaceBuilder(executablePetriNet, cloneInstance);
//		homePlace.accept(builder);
//		Place cloned = builder.cloned;
//		assertTrue(cloned.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusHome);  
//		assertEquals("top.a.b.P0", cloned.getId());
//		assertTrue(cloned.isOrClonedFrom(homePlace));
//		homePlace.setTokenCount("red", 2);
//		assertEquals("clone listens for token changes",2, cloned.getTokenCount("red"));
//		Place clonedHome = cloned.getStatus().getMergeInterfaceStatus().getHomePlace(); 
//		assertTrue(cloned == clonedHome);
//		assertTrue(homePlace == clonedHome.getLinkedConnectable());
//		assertTrue(cloned == cloneInstance.pendingNewHomePlaces.get(cloned.getStatus().getMergeInterfaceStatus().getAwayId()));
//		assertFalse(cloneInstance.getPendingAwayPlacesForInterfacePlaceConversion().containsValue(cloned));
//		assertTrue(cloneInstance.getPendingNewHomePlaces().containsValue(cloned));
		
//		newPlace = cloner.cloned;
//		newPlace.getStatus().setIncludeHierarchy(includeHierarchy); 
//		MergeInterfaceStatus mergeStatus = 
//				new MergeInterfaceStatusAvailable(homePlace, newPlace.getStatus(),  newPlace.getStatus().getMergeInterfaceStatus().getAwayId());  
//		newPlace.getStatus().setMergeInterfaceStatus(mergeStatus); 
//		newPlace.getStatus().setExternal(false); 
//		if (homePlace.getStatus().isInputOnlyArcConstraint()) { 
//			newPlace.getStatus().setInputOnlyArcConstraint(true); 
//			((PlaceStatusInterface) newPlace.getStatus()).buildInputOnlyArcConstraint();  
//		}
//		else if (homePlace.getStatus().isOutputOnlyArcConstraint()) {
//			newPlace.getStatus().setOutputOnlyArcConstraint(true); 
//			((PlaceStatusInterface) newPlace.getStatus()).buildOutputOnlyArcConstraint();  
//		}
//		
//		newPlace.setId(mergeStatus.getAwayId()); 
//		listenForTokenCountChanges(newPlace);
//		return newPlace;

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
	}
	@Test
	public void buildsAvailablePlaceAndHomePlaceMirrorTokensT() throws Exception {
		builder = new PlaceBuilder(includes);
		homePlace.accept(builder);
		Place availablePlace = builder.cloned;
		homePlace.setTokenCount("blue", 3);
		assertEquals("available place listens to home", 3, availablePlace.getTokenCount("blue"));
		availablePlace.setTokenCount("blue", 4);
		assertEquals("...and vice versa", 4, homePlace.getTokenCount("blue"));
	}

    
    
	protected PetriNet buildNetNoArc(int i) throws PetriNetComponentException {
		PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
				APlace.withId("P1")).andFinally(AnImmediateTransition.withId("T0"));
		net.setName(new NormalPetriNetName("net"+i));
		return net;
	}
	
	
}
