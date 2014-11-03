package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.*;

import java.awt.Color;

import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;

public class PlaceStatusInterfaceTest {

	private Place place;
	private PlaceStatus status;
	private PetriNet net;
	private IncludeHierarchy includes;
	@Before
	public void setUp() throws Exception {
		place = new DiscretePlace("P0"); 
		buildNet(); 
		includes = new IncludeHierarchy(net, "top");
		status = new PlaceStatusInterface(place, includes);  
	}
	@Test
	public void statusKnowsItsPlace() {
		assertEquals(place, status.getPlace());  
		assertEquals(includes, status.getIncludeHierarchy());  
	}
	@Test
	public void defaultsToNoOpInterfaceStatusForMergeAndExternalAndInputAndOutput() throws Exception {
		assertTrue(status.getMergeInterfaceStatus() instanceof NoOpInterfaceStatus);
		assertTrue(status.getExternalInterfaceStatus() instanceof NoOpInterfaceStatus);
		assertTrue(status.getInputOnlyInterfaceStatus() instanceof NoOpInterfaceStatus);
		assertTrue(status.getOutputOnlyInterfaceStatus() instanceof NoOpInterfaceStatus);
	}
	@Test
	public void NoOpStatusReturnsEmptyResult() throws Exception {
		InterfaceStatus interfaceStatus = new NoOpInterfaceStatus(); 
		assertFalse(interfaceStatus.add().hasResult()); 
		assertFalse(interfaceStatus.remove().hasResult()); 
	}
	@Test
	public void mergeStatus() throws Exception {
		status.setMergeStatus(true); 
		assertTrue(status.getMergeInterfaceStatus() instanceof MergeInterfaceStatus); // s/b ...Home
		status.setMergeStatus(false); 
		assertTrue(status.getMergeInterfaceStatus() instanceof NoOpInterfaceStatus);
	}
	@Test
	public void externalStatus() throws Exception {
		status.setExternalStatus(true); 
		assertTrue(status.getExternalInterfaceStatus() instanceof ExternalInterfaceStatus); //s/b Home
		status.setExternalStatus(false); 
		assertTrue(status.getMergeInterfaceStatus() instanceof NoOpInterfaceStatus);
	}
	@Test
	public void inputOnlyStatus() throws Exception {
		status.setInputOnlyStatus(true); 
		assertTrue(status.getInputOnlyInterfaceStatus() instanceof InputOnlyInterfaceStatus); 
		status.setExternalStatus(false); 
		assertTrue(status.getMergeInterfaceStatus() instanceof NoOpInterfaceStatus);
	}
	@Test
	public void outputOnlyStatus() throws Exception {
		status.setOutputOnlyStatus(true); 
		assertTrue(status.getOutputOnlyInterfaceStatus() instanceof OutputOnlyInterfaceStatus); 
		status.setExternalStatus(false); 
		assertTrue(status.getMergeInterfaceStatus() instanceof NoOpInterfaceStatus);
	}
//	@Test
	public void inputAndOutputCantCoexist() throws Exception {
		//TODO
	}
	@Test
	public void copyConstructorForPlaceStatus() throws Exception {
		status.setMergeStatus(true); 
		status.setExternalStatus(true); 
		status.setInputOnlyStatus(true); 
		PlaceStatus newstatus = new PlaceStatusInterface(status, place);  
		assertEquals(place, newstatus.getPlace()); 
		assertEquals(includes, newstatus.getIncludeHierarchy()); 
		assertTrue(newstatus.isMergeStatus());
		assertTrue(newstatus.isExternalStatus());
		assertTrue(newstatus.isInputOnlyStatus());
		assertFalse(newstatus.isOutputOnlyStatus());
		
	}
	//TODO MergeInterfaceStatus is interface...add with defualt Home  
//	protected void buildNetWithOldAndNewPlaces(String source, String target) throws PetriNetComponentNotFoundException {
	protected void buildNet() throws PetriNetComponentNotFoundException {
		net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
    			APlace.withId("P1")).and(AnImmediateTransition.withId("T0")).andFinally(
    			ANormalArc.withSource("P0").andTarget("T0"));
	}

	//TODO PlaceStatusNormalTest
}
