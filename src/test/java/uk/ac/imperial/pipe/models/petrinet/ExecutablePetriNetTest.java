package uk.ac.imperial.pipe.models.petrinet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.beans.PropertyChangeListener;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.state.HashedStateBuilder;
import uk.ac.imperial.state.State;

public class ExecutablePetriNetTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private PetriNet net;
    private ExecutablePetriNet executablePetriNet;

    @Mock
    private PropertyChangeListener mockListener;

	private PetriNet net2;

	private IncludeHierarchy hierarchy;



    @Before
    public void setUp() {
        net = new PetriNet();
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
	protected PetriNet buildTestNet() {
		PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
                        APlace.withId("P1")).and(AnImmediateTransition.withId("T0")).and(
                        AnImmediateTransition.withId("T1")).and(
                        ANormalArc.withSource("P1").andTarget("T1")).andFinally(
                        ANormalArc.withSource("T0").andTarget("P0").with("#(P0)", "Default").token());
		return net; 
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
}
