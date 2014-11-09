package uk.ac.imperial.pipe.models.petrinet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;
import uk.ac.imperial.state.HashedStateBuilder;
import uk.ac.imperial.state.State;

@RunWith(MockitoJUnitRunner.class)
public class ExecutablePetriNetTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private PetriNet net;
    private ExecutablePetriNet executablePetriNet;

    @Mock
    private PropertyChangeListener mockListener;

	private PetriNet net2;

    @Before
    public void setUp() {
        net = new PetriNet(new NormalPetriNetName("net"));
        executablePetriNet = net.getExecutablePetriNet();  
    }
    @Test
    public void equalsAndHashCodeLawsWhenEqual() {
    	net = buildTestNet();
    	executablePetriNet = net.getExecutablePetriNet(); 
    	PetriNet net2 = buildTestNet();
    	ExecutablePetriNet epn2 = net2.getExecutablePetriNet(); 
        assertTrue(executablePetriNet.equals(epn2));
        assertEquals(executablePetriNet.hashCode(), epn2.hashCode());
    }

    @Test
    public void equalsAndHashCodeLawsWhenNotEqual() throws PetriNetComponentException {
    	net = buildTestNet();
    	executablePetriNet = net.getExecutablePetriNet(); 
    	PetriNet net2 = buildTestNet();
    	net2.add(new DiscreteTransition("T99")); 
    	ExecutablePetriNet epn2 = net2.getExecutablePetriNet(); 
    	assertFalse(executablePetriNet.equals(epn2));
    	assertNotEquals(executablePetriNet.hashCode(), epn2.hashCode());
    }

    @Test
    public void collectionsMatchOriginalPetriNet() {
        net = buildTestNet();
        executablePetriNet = net.getExecutablePetriNet();  
        assertThat(executablePetriNet.getAnnotations()).hasSize(0); 
        assertThat(executablePetriNet.getTokens()).hasSize(1); 
        assertThat(executablePetriNet.getTransitions()).hasSize(2); 
        assertThat(executablePetriNet.getInboundArcs()).hasSize(1); 
        assertThat(executablePetriNet.getOutboundArcs()).hasSize(1); 
        assertThat(executablePetriNet.getArcs()).hasSize(2); 
        assertThat(executablePetriNet.getPlaces()).hasSize(2); 
        assertThat(executablePetriNet.getRateParameters()).hasSize(0); 
    }
    @Test
	public void componentsFound() throws Exception
	{
    	net = buildTestNet();
    	executablePetriNet = net.getExecutablePetriNet();
    	assertTrue(executablePetriNet.containsComponent("T0")); 
    	assertFalse(executablePetriNet.containsComponent("FRED")); 
    	
    	Transition t0 = executablePetriNet.getComponent("T0", Transition.class);
    	Transition t1 = executablePetriNet.getComponent("T1", Transition.class);
    	assertThat(executablePetriNet.inboundArcs(t1)).hasSize(1); 
    	assertThat(executablePetriNet.inboundArcs(t0)).hasSize(0); 
    	assertThat(executablePetriNet.outboundArcs(t0)).hasSize(1); 
    	InboundArc arc = executablePetriNet.getComponent("P1 TO T1", InboundArc.class);
    	assertTrue(executablePetriNet.inboundArcs(t1).contains(arc));
    	//TODO outboundArcs(Place place) s
	}
    @Test
	public void verifyPlaceCountUpdateIsMirroredToPlaceInOriginalPetriNet() throws Exception {
    	net = buildTestNet();
    	executablePetriNet = net.getExecutablePetriNet();
    	Place epnp1 = executablePetriNet.getComponent("P1", Place.class); 
    	Place netp1 = net.getComponent("P1", Place.class); 
    	assertEquals(0, epnp1.getTokenCount("Default")); 
    	epnp1.setTokenCount("Default", 2); 
    	assertEquals(2, epnp1.getTokenCount("Default")); 
    	assertEquals(2, netp1.getTokenCount("Default")); 
	}
    @Test
	public void evaluatesFunctionalExpressionAgainstCurrentState() throws Exception {
    	net = buildTestNet();
    	executablePetriNet = net.getExecutablePetriNet();
    	Place epnp1 = executablePetriNet.getComponent("P1", Place.class); 
    	epnp1.setTokenCount("Default", 2); 
    	assertEquals(new Double(2.0), executablePetriNet.evaluateExpressionAgainstCurrentState("#(P1)")); 
	}
    @Test
    public void evaluatesFunctionalExpressionGivenState() throws Exception {
    	net = buildTestNet();
    	executablePetriNet = net.getExecutablePetriNet();
        HashedStateBuilder builder = new HashedStateBuilder();
        builder.placeWithToken("P1", "Default", 4);
        State state = builder.build();
    	assertEquals(new Double(4.0), executablePetriNet.evaluateExpression(state, "#(P1)")); 
    }
    @Test
    public void returnsNegativeOneForInvalidFunctionalExpression() throws Exception {
    	net = buildTestNet();
    	executablePetriNet = net.getExecutablePetriNet();
    	Place epnp1 = executablePetriNet.getComponent("P1", Place.class); 
    	epnp1.setTokenCount("Default", 2); 
    	assertEquals(new Double(-1.0), executablePetriNet.evaluateExpressionAgainstCurrentState("Fred(P1)")); 
    }
    @Test
    public void stateCanBeExtractedAndThenReappliedResettingBothExecutableAndSourcePetriNets() throws Exception {
    	net = buildTestNet();
    	executablePetriNet = net.getExecutablePetriNet();
    	State beforeState = executablePetriNet.getState(); 
    	Place epnp1 = executablePetriNet.getComponent("P1", Place.class); 
    	Place netp1 = net.getComponent("P1", Place.class); 
    	assertEquals(0, epnp1.getTokenCount("Default")); 
    	assertEquals(0, netp1.getTokenCount("Default")); 
    	epnp1.setTokenCount("Default", 2); 
    	assertEquals(2, epnp1.getTokenCount("Default")); 
    	assertEquals(2, netp1.getTokenCount("Default")); 
    	assertNotEquals(beforeState, executablePetriNet.getState()); 
    	executablePetriNet.setState(beforeState); 
    	epnp1 = executablePetriNet.getComponent("P1", Place.class); 
    	netp1 = net.getComponent("P1", Place.class); 
    	assertEquals(0, epnp1.getTokenCount("Default")); 
    	assertEquals(0, netp1.getTokenCount("Default")); 
    	assertEquals(beforeState, executablePetriNet.getState()); 
    }
    @Test
	public void verifyExecutablePetriNetSeesAllIncludedComponentsWithAppropriatePrefixes() throws Exception {
	  	net.addPlace(new DiscretePlace("P0", "P0")); 
	  	net2 = new PetriNet();
	  	net2.addPlace(new DiscretePlace("P1", "P1")); 
	  	net2.addPlace(new DiscretePlace("P2", "P2")); 
	  	net.getIncludeHierarchy().include(net2, "some-function"); 
	  	assertEquals("source PN only sees root components",1, net.getPlaces().size()); 
	  	assertEquals("...but EPN sees all components",3, executablePetriNet.getPlaces().size()); 
	  	assertEquals("components from root net default to no prefix",
	  			"P0", executablePetriNet.getComponent("P0", Place.class).getId()); 
	  	assertEquals("components from included nets are prefixed in the executable PN",
	  			".some-function.P1", executablePetriNet.getComponent(".some-function.P1", Place.class).getId()); 
	  	assertEquals(".some-function.P2", executablePetriNet.getComponent(".some-function.P2", Place.class).getId()); 

	  	assertEquals("source PN component ids unaffected",
	  			"P0", net.getComponent("P0", Place.class).getId()); 
	  	assertEquals("P1", net2.getComponent("P1", Place.class).getId()); 
	}
    protected PetriNet buildNet1() {
    	PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).
    					and(AnImmediateTransition.withId("T0")).and(
    					AnImmediateTransition.withId("T1")).
    					andFinally(ANormalArc.withSource("T0").andTarget("P0").with("#(P0)", "Default").token());
    	return net; 
    }
    protected PetriNet buildNet2() {
    	PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
    					APlace.withId("P1")).and(APlace.withId("P2")).and(APlace.withId("P3")).and(
    					AnImmediateTransition.withId("T0")).and(
    					AnImmediateTransition.withId("T1")).and(
    					AnImmediateTransition.withId("T2")).and(
    					AnImmediateTransition.withId("T3")).and(
    					ANormalArc.withSource("P1").andTarget("T2")).and(								
    					ANormalArc.withSource("T2").andTarget("P2")).andFinally(								
    					ANormalArc.withSource("T3").andTarget("P3").with("#(P3)", "Default").token());
    	return net; 
    }
    @Test
	public void convertArcsForPlacesWithMergeStatusToArcsFromSourcePlace() throws Exception {
    	net = buildNet1();
    	net.setName(new NormalPetriNetName("net")); 
    	net2 = buildNet2();
    	IncludeHierarchy includes = new IncludeHierarchy(net, "top");
    	includes.include(net2, "a");  
    	net.setIncludeHierarchy(includes);
    	executablePetriNet = net.getExecutablePetriNet(); 
    	assertEquals(5,executablePetriNet.getPlaces().size()); 
    	assertEquals(6,executablePetriNet.getTransitions().size()); 
    	assertEquals(4,executablePetriNet.getArcs().size()); 
    	assertEquals(1, includes.getPetriNet().getPlaces().size()); 
    	assertEquals(1, includes.getPetriNet().getArcs().size()); 
    	assertEquals(4, includes.getInclude("a").getPetriNet().getPlaces().size()); 
    	assertEquals(3, includes.getInclude("a").getPetriNet().getArcs().size()); 
    	Place originP1 = net2.getComponent("P1", Place.class); 
    	Place originP2 = net2.getComponent("P2", Place.class); 
    	Place originP3 = net2.getComponent("P3", Place.class); 
    	includes.getInclude("a").addToInterface(originP1, true, false, false, false ); 
    	includes.getInclude("a").addToInterface(originP2, true, false, false, false ); 
    	includes.getInclude("a").addToInterface(originP3, true, false, false, false ); 
    	assertEquals("haven't added them to the net yet",1, includes.getPetriNet().getPlaces().size()); 
    	assertTrue(includes.getInterfacePlace("a.P1").getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAvailable); 
    	assertTrue(includes.getInterfacePlace("a.P2").getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAvailable); 
    	assertTrue(includes.getInterfacePlace("a.P3").getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAvailable); 
    	assertTrue(includes.getInclude("a").getInterfacePlace("P1").getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusHome); 
    	includes.addAvailablePlaceToPetriNet(includes.getInterfacePlace("a.P1")); 
    	includes.addAvailablePlaceToPetriNet(includes.getInterfacePlace("a.P2")); 
    	assertTrue(includes.getInterfacePlace("a.P1").getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAway); 
    	assertTrue(includes.getInterfacePlace("a.P2").getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAway); 
    	assertTrue("didn't use it, so still available",
    			includes.getInterfacePlace("a.P3").getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAvailable);
    	assertEquals("2 have been added",3, includes.getPetriNet().getPlaces().size()); 
    	assertEquals(5,executablePetriNet.getPlaces().size()); 
    	
    	checkNewHomePlace("a.P1");
    	checkNewHomePlace("a.P2");

    	Place topIP1 = includes.getInterfacePlace("a.P1"); 
    	Place topIP2 = includes.getInterfacePlace("a.P2"); 
    	Transition topT1 = net.getComponent("T1", Transition.class);
    	assertEquals(4,executablePetriNet.getArcs().size()); 
    	InboundArc arcIn = new InboundNormalArc(topIP1, topT1, new HashMap<String, String>());
    	OutboundArc arcOut = new OutboundNormalArc(topT1, topIP2, new HashMap<String, String>());
    	assertEquals(1, includes.getPetriNet().getArcs().size()); 
    	net.add(arcIn); 
    	net.add(arcOut); 
    	assertEquals(3, includes.getPetriNet().getArcs().size()); 
    	assertEquals(6,executablePetriNet.getArcs().size()); 
    	assertEquals(5,executablePetriNet.getPlaces().size()); 
    	checkPlaces("top.P0", "top.a.P0", "top.a.P1", "top.a.P2", "top.a.P3"); 
    	OutboundArc exArcIn = executablePetriNet.getComponent("top.T1 TO top.a.P2", OutboundArc.class);
    	InboundArc exArcOut = executablePetriNet.getComponent("top.a.P1 TO top.T1", InboundArc.class);
    	originP2.setId("top.a.P2"); 
    	originP1.setId("top.a.P1"); 
    	assertEquals(originP2, exArcIn.getTarget());
    	assertEquals(originP1, exArcOut.getSource());
    	expectInterfacePlaceArcNotFound("top.T1 TO a.P2", OutboundArc.class);  
    	expectInterfacePlaceArcNotFound("a.P1 TO top.T1", InboundArc.class);  
	}
	protected void checkNewHomePlace(String awayPlace)
			throws PetriNetComponentNotFoundException {
		String newId = "top."+awayPlace; 
		Place exPlace = executablePetriNet.getComponent(newId, Place.class); 
		assertTrue(exPlace.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusHome);
		assertEquals(exPlace, exPlace.getStatus().getMergeInterfaceStatus().getHomePlace());
		assertEquals(awayPlace, exPlace.getStatus().getMergeInterfaceStatus().getAwayId());
		int count = 0; 
		for (Place place : executablePetriNet.getPlaces()) {
			if ((place.getStatus() instanceof PlaceStatusInterface) && (place.getStatus().getMergeInterfaceStatus().getAwayId().equals(awayPlace))) {
				count++; 
			}
		}
		assertEquals(1, count); 	
	}
    //TODO functionalExpressionsOnAwayInterfacePlacesAreConvertedToReferenceHomePlace
    //TODO break this into multiple tests 
//    @Test
	public void convertsInterfacePlaceArcsToArcsToFromSourcePlace() throws Exception {
    	net = buildNet1();
    	net.setName(new NormalPetriNetName("net")); 
    	net2 = buildNet2();
    	IncludeHierarchy includes = new IncludeHierarchy(net, "top");
    	includes.include(net2, "a");  
    	net.setIncludeHierarchy(includes);
		executablePetriNet = net.getExecutablePetriNet(); 
		assertEquals(5,executablePetriNet.getPlaces().size()); 
		assertEquals(6,executablePetriNet.getTransitions().size()); 
		assertEquals(4,executablePetriNet.getArcs().size()); 
		assertEquals(1, includes.getPetriNet().getPlaces().size()); 
		assertEquals(1, includes.getPetriNet().getArcs().size()); 
		assertEquals(4, includes.getInclude("a").getPetriNet().getPlaces().size()); 
		assertEquals(3, includes.getInclude("a").getPetriNet().getArcs().size()); 
		Place originP1 = net2.getComponent("P1", Place.class); 
		Place originP2 = net2.getComponent("P2", Place.class); 
		Place originP3 = net2.getComponent("P3", Place.class); 
		includes.getInclude("a").addToInterfaceOld(originP1); 
		includes.getInclude("a").addToInterfaceOld(originP2); 
		includes.getInclude("a").addToInterfaceOld(originP3); 
		assertTrue(includes.getInterfacePlaceOld("top..a.P1").getInterfacePlaceStatus() instanceof InterfacePlaceStatusAvailable); 
		assertTrue(includes.getInterfacePlaceOld("top..a.P2").getInterfacePlaceStatus() instanceof InterfacePlaceStatusAvailable); 
		assertTrue(includes.getInterfacePlaceOld("top..a.P3").getInterfacePlaceStatus() instanceof InterfacePlaceStatusAvailable); 
		assertTrue(includes.getInclude("a").getInterfacePlaceOld("a.P1").getInterfacePlaceStatus() instanceof InterfacePlaceStatusHome); 
		includes.useInterfacePlace("top..a.P1"); 
		includes.useInterfacePlace("top..a.P2"); 
		assertTrue(includes.getInterfacePlaceOld("top..a.P1").getInterfacePlaceStatus() instanceof InterfacePlaceStatusInUse); 
		assertTrue(includes.getInterfacePlaceOld("top..a.P2").getInterfacePlaceStatus() instanceof InterfacePlaceStatusInUse); 
		assertFalse("didn't use it, so still available",
				includes.getInterfacePlaceOld("top..a.P3").getInterfacePlaceStatus() instanceof InterfacePlaceStatusInUse); 
		assertEquals(5,executablePetriNet.getPlaces().size()); 
		InterfacePlace topIP1 = includes.getInterfacePlaceOld("top..a.P1"); 
		InterfacePlace topIP2 = includes.getInterfacePlaceOld("top..a.P2"); 
		Transition topT1 = net.getComponent("T1", Transition.class);
		assertEquals(4,executablePetriNet.getArcs().size()); 
        InboundArc arcIn = new InboundNormalArc(topIP1, topT1, new HashMap<String, String>());
        OutboundArc arcOut = new OutboundNormalArc(topT1, topIP2, new HashMap<String, String>());
        assertEquals(1, includes.getPetriNet().getArcs().size()); 
        net.add(arcIn); 
        net.add(arcOut); 
        assertEquals(3, includes.getPetriNet().getArcs().size()); 
        assertEquals(6,executablePetriNet.getArcs().size()); 
        assertEquals(5,executablePetriNet.getPlaces().size()); 
        checkPlaces("top.P0", "top.a.P0", "top.a.P1", "top.a.P2", "top.a.P3"); 
        OutboundArc exArcIn = executablePetriNet.getComponent("top.T1 TO top.a.P2", OutboundArc.class);
        InboundArc exArcOut = executablePetriNet.getComponent("top.a.P1 TO top.T1", InboundArc.class);
        originP2.setId("top.a.P2"); 
        originP1.setId("top.a.P1"); 
        assertEquals(originP2, exArcIn.getTarget());
        assertEquals(originP1, exArcOut.getSource());
        expectInterfacePlaceArcNotFound("top.T1 TO top..a.P2", OutboundArc.class);  
        expectInterfacePlaceArcNotFound("top..a.P1 TO top.T1", InboundArc.class);  
	}
	private void checkPlaces(String... places) {
		for (int i = 0; i < places.length; i++) {
			try {
				assertEquals(places[i], executablePetriNet.getComponent(places[i], Place.class).getId());
			} catch (PetriNetComponentNotFoundException e) {
				e.printStackTrace();
			} 
		}
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void expectInterfacePlaceArcNotFound(String id, Class clazz) {
		try {
			executablePetriNet.getComponent(id, clazz);
			fail("should throw"); 
		} 
		catch (PetriNetComponentNotFoundException e) {
		}
	}
	protected PetriNet buildTestNet() {
		PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
                        APlace.withId("P1")).and(AnImmediateTransition.withId("T0")).and(
                        AnImmediateTransition.withId("T1")).and(
                        ANormalArc.withSource("P1").andTarget("T1")).andFinally(
                        ANormalArc.withSource("T0").andTarget("P0").with("#(P0)", "Default").token());
		return net; 
	}
}
