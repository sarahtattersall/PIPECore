package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import java.awt.Color;
import java.util.Collection;
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

public class RemoveInterfacePlaceCommandTest {

	private IncludeHierarchy includes;
	private InterfacePlaceRemovalEligibilityCommand command;
	private PetriNet net;
	private PetriNet net2;
	private Place placeA;
	private InterfacePlace placeTopIP;
	private Result<InterfacePlaceAction> result;
	
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
    	placeA = net2.getComponent("P0", Place.class); 
    	includes.getChildInclude("a").addToInterfaceOld(placeA);
    	placeTopIP = includes.getInterfacePlace("top..a.P0"); 
    	placeTopIP.getInterfacePlace().getInterfacePlaceStatus().use(); 
	}

	@Test
	public void interfacePlaceWithNoDependenciesIsEligibleToBeRemoved() throws Exception {
		command = new InterfacePlaceRemovalEligibilityCommand(placeTopIP); 
		result = command.execute(includes);
		assertFalse(result.hasResult()); 
	}
	@Test
	public void interfacePlaceWithDependenciesIsNotEligibleToBeRemoved() throws Exception {
		FunctionalRateParameter rateParameter = new FunctionalRateParameter("#(top..a.P0)", "frp1", "frp1");
        net.addRateParameter(rateParameter);
        Transition t2 = new DiscreteTransition("T2", "T2");
        t2.setTimed(true);
        t2.setRate(rateParameter);
        net.addTransition(t2);
        Map<String, String> tokenWeights = new HashMap<>(); 
        tokenWeights.put("Default", "#(top..a.P0)");
        Transition t0 = net.getComponent("T0", Transition.class); 
        OutboundArc newArc = new OutboundNormalArc(t0, placeTopIP, tokenWeights);
        net.addArc(newArc); 

        command = new InterfacePlaceRemovalEligibilityCommand(placeTopIP); 
        result = command.execute(includes);
		assertTrue(result.hasResult()); 
		assertEquals(3, result.getEntries().size()); 
		Iterator<ResultEntry<InterfacePlaceAction>> iterator = result.getEntries().iterator();
		ResultEntry<InterfacePlaceAction> entry = iterator.next(); 
		assertEquals("InterfacePlace top..a.P0 cannot be removed from IncludeHierarchy top" +
				" because it is referenced in a functional expression in component T0 TO top..a.P0", entry.message);
		assertEquals("T0 TO top..a.P0",entry.value.getComponentId()); 
		assertEquals(includes,entry.value.getIncludeHierarchy()); 
		assertEquals(placeTopIP,entry.value.getInterfacePlace()); 
		entry = iterator.next(); 
		assertEquals("InterfacePlace top..a.P0 cannot be removed from IncludeHierarchy top" +
				" because it is referenced in a functional expression in component frp1", entry.message);
		assertEquals("frp1",entry.value.getComponentId()); 
		entry = iterator.next(); 
		assertEquals("InterfacePlace top..a.P0 cannot be removed from IncludeHierarchy top" +
				" because it is referenced in a functional expression in component T2", entry.message);
		assertEquals("T2",entry.value.getComponentId()); 

	}

 	private PetriNet createSimpleNet(int i) {
		PetriNet net = 
				APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
				APlace.withId("P1")).and(AnImmediateTransition.withId("T0")).and(
				AnImmediateTransition.withId("T1")).and(
				ANormalArc.withSource("T1").andTarget("P1")).andFinally(
				ANormalArc.withSource("T0").andTarget("P0").with("#(P0)", "Default").token());
		net.setName(new NormalPetriNetName("net"+i));
		return net; 
	}

}
