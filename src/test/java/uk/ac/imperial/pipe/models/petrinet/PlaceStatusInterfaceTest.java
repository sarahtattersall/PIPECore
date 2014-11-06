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
	private Result<InterfacePlaceAction> result;
	@Before
	public void setUp() throws Exception {
		place = new DiscretePlace("P0"); 
		buildNet(); 
		includes = new IncludeHierarchy(net, "top");
		status = new PlaceStatusInterface(place, includes);  
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
		assertFalse(interfaceStatus.add(null).hasResult()); 
		assertFalse(interfaceStatus.remove(null).hasResult()); 
	}
	@Test
	public void mergeStatus() throws Exception {
		status.setMergeStatus(true); 
		result = status.update();
		assertFalse(result.hasResult()); 
		assertTrue(status.getMergeInterfaceStatus() instanceof MergeInterfaceStatus); 
		status.setMergeStatus(false); 
		assertFalse(status.update().hasResult()); 
		assertTrue(status.getMergeInterfaceStatus() instanceof NoOpInterfaceStatus);
	}
	@Test
	public void externalStatus() throws Exception {
		status.setExternalStatus(true); 
		assertFalse(status.update().hasResult()); 
		assertTrue(status.getExternalInterfaceStatus() instanceof ExternalInterfaceStatus); 
		status.setExternalStatus(false); 
		assertFalse(status.update().hasResult()); 
		assertTrue(status.getMergeInterfaceStatus() instanceof NoOpInterfaceStatus);
	}
	@Test
	public void inputOnlyStatus() throws Exception {
		status.setInputOnlyStatus(true); 
		assertFalse(status.update().hasResult()); 
		assertTrue(status.getInputOnlyInterfaceStatus() instanceof InputOnlyInterfaceStatus); 
		status.setExternalStatus(false); 
		assertFalse(status.update().hasResult()); 
		assertTrue(status.getMergeInterfaceStatus() instanceof NoOpInterfaceStatus);
	}
	@Test
	public void outputOnlyStatus() throws Exception {
		status.setOutputOnlyStatus(true); 
		assertFalse(status.update().hasResult()); 
		assertTrue(status.getOutputOnlyInterfaceStatus() instanceof OutputOnlyInterfaceStatus); 
		status.setExternalStatus(false); 
		assertFalse(status.update().hasResult()); 
		assertTrue(status.getMergeInterfaceStatus() instanceof NoOpInterfaceStatus);
	}
	@Test
	public void inputAndOutputCantCoexist() throws Exception {
		status.setInputOnlyStatus(true); 
		result = status.update(); 
		assertFalse(result.hasResult());
		status.setOutputOnlyStatus(true); 
		result = status.update(); 
		assertTrue(result.hasResult());
		assertEquals("PlaceStatus.setOutputOnlyStatus: status may not be both input only and output only.", result.getMessage()); 
		
		assertTrue(status.isInputOnlyStatus()); 
		assertFalse(status.isOutputOnlyStatus()); 
		status.setInputOnlyStatus(false); 
		result = status.update(); 
		assertFalse(result.hasResult());
		status.setOutputOnlyStatus(true); 
		result = status.update(); 
		assertFalse(result.hasResult());
		status.setInputOnlyStatus(true); 
		result = status.update(); 
		assertTrue(result.hasResult());
		assertEquals("PlaceStatus.setInputOnlyStatus: status may not be both input only and output only.", result.getMessage()); 
	}
	@Test
	public void copyConstructorForPlaceStatus() throws Exception {
		status.setMergeStatus(true); 
		status.setExternalStatus(true); 
		status.setInputOnlyStatus(true); 
		PlaceStatus newstatus = new PlaceStatusInterface(status, place);  
		assertTrue(newstatus.isMergeStatus());
		assertTrue(newstatus.isExternalStatus());
		assertTrue(newstatus.isInputOnlyStatus());
		assertFalse(newstatus.isOutputOnlyStatus());
		
	}
	protected void buildNet() throws PetriNetComponentNotFoundException {
		net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
    			APlace.withId("P1")).and(AnImmediateTransition.withId("T0")).andFinally(
    			ANormalArc.withSource("P0").andTarget("T0"));
	}

	//TODO PlaceStatusNormalTest
}
