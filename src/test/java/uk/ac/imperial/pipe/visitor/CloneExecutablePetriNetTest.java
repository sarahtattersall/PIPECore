package uk.ac.imperial.pipe.visitor;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.File;

import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.ARateParameter;
import uk.ac.imperial.pipe.dsl.ATimedTransition;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.exceptions.IncludeException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet;
import uk.ac.imperial.pipe.models.petrinet.InboundArc;
import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatusAvailable;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatusAway;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatusHome;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.RateParameter;
import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;
import uk.ac.imperial.pipe.models.petrinet.name.PetriNetFileName;

public class CloneExecutablePetriNetTest {
    PetriNet oldPetriNet;
    PetriNet clonedPetriNet;
	private PetriNet net2;
	private IncludeHierarchy includes;
	private ExecutablePetriNet executablePetriNet;

    @Before
    public void setUp() throws PetriNetComponentException {
        buildSimpleNet();
    }
    //TODO clones InterfacePlaces (?)
	private void buildSimpleNet() throws PetriNetComponentException {
		oldPetriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
                APlace.withId("P0").and(1, "Default").token()).and(APlace.withId("P1")).and(
                ATimedTransition.withId("T0")).and(ATimedTransition.withId("T1"))
                .and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token())
                .and(ANormalArc.withSource("T0").andTarget("P1").with("1", "Default").token())
                .and(ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token())
                .andFinally(ANormalArc.withSource("T1").andTarget("P0").with("1", "Default").token());
	}

//    @Test
    public void cloneEquality() {
        oldPetriNet.setName(new NormalPetriNetName("Petri net 0"));
        clonedPetriNet = ClonePetriNet.clone(oldPetriNet);
        assertEquals(oldPetriNet, clonedPetriNet);
    }

//    @Test
    public void clonePetriNetNoName() {
        clonedPetriNet = ClonePetriNet.clone(oldPetriNet);
        assertEquals(oldPetriNet, clonedPetriNet);
    }

//    @Test
    public void cloneEqualityWithFileName() {
        oldPetriNet.setName(new PetriNetFileName(new File("Petri net 0")));
        clonedPetriNet = ClonePetriNet.clone(oldPetriNet);
        assertEquals(oldPetriNet, clonedPetriNet);
    }


//    @Test
    public void clonesArcWithNewSourceAndTarget() throws PetriNetComponentNotFoundException {
        oldPetriNet.setName(new NormalPetriNetName("Petri net 0"));
        clonedPetriNet = ClonePetriNet.clone(oldPetriNet);
        InboundArc arc = clonedPetriNet.getComponent("P0 TO T0", InboundArc.class);
        Place clonedP0 = clonedPetriNet.getComponent("P0", Place.class);
        Transition clonedT0 = clonedPetriNet.getComponent("T0", Transition.class);
        assertTrue(arc.getSource() == clonedP0);
        assertTrue(arc.getTarget() == clonedT0);
    }
//    @Test
	public void clonedPlaceAndItsSourcePlaceBothMirrorTokenCountChanges() throws Exception {
    	clonedPetriNet = ClonePetriNet.clone(oldPetriNet);
    	assertEquals(oldPetriNet, clonedPetriNet);
    	Place oldP = oldPetriNet.getComponent("P0", Place.class); 
    	Place cloneP = clonedPetriNet.getComponent("P0", Place.class); 
    	oldP.setTokenCount("Default", 2); 
    	assertEquals(2, cloneP.getTokenCount("Default")); 
    	cloneP.setTokenCount("Default", 3); 
    	assertEquals(3, oldP.getTokenCount("Default")); 
	}
//    @Test
    public void clonedTransitionMirrorsEnabledStatusOfSourceTransition() throws Exception {
    	clonedPetriNet = ClonePetriNet.clone(oldPetriNet);
    	Transition sourceT = oldPetriNet.getComponent("T0", Transition.class); 
    	Transition cloneT = clonedPetriNet.getComponent("T0", Transition.class); 
    	assertFalse(sourceT.isEnabled()); 
    	cloneT.enable();  
    	assertTrue("clone enable status mirrored to source transition",sourceT.isEnabled()); 
    	cloneT.disable();  
    	assertFalse("clone disable status mirrored to source transition",sourceT.isEnabled()); 
    }
    @Test
	public void clonePetriNetToExecutablePetriNetReplacingExistingState() throws Exception {
    	buildSimpleNet(); 
    	oldPetriNet.setIncludeHierarchy(new IncludeHierarchy(oldPetriNet, "root"));
    	ExecutablePetriNet executablePetriNet = new ExecutablePetriNet(oldPetriNet); 
    	CloneExecutablePetriNet.refreshFromIncludeHierarchy(executablePetriNet); 
    	assertEquals("root.P0", executablePetriNet.getComponent("root.P0", Place.class).getId()); 
    	assertEquals("root.T0", executablePetriNet.getComponent("root.T0", Transition.class).getId()); 
    	assertEquals("root.P0 TO root.T0", executablePetriNet.getComponent("root.P0 TO root.T0", InboundArc.class).getId()); 
	}
    //TODO test for transitionOut/InboundArcs
    //TODO test for arcweights 
    @Test
    public void convertsArcsFromInterfacePlaceToNewOriginPlace() throws Exception {
    	buildIncludeHierarchyAndRefreshExecutablePetriNet(); 
    	checkExecutableHasSumOfOldPNAndNet2Components(net2, executablePetriNet); 
    	Place originPlace = net2.getComponent("P0", Place.class); 
    	includes.getInclude("a").addToInterface(originPlace, true, false, false, false); 
    	
		Place topPlace = includes.getInterfacePlace("a.P0"); 
    	assertTrue(includes.getInterfacePlace("a.P0").getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAvailable); 
    	assertTrue(includes.getInclude("a").getInterfacePlace("P0").getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusHome); 
    	includes.addAvailablePlaceToPetriNet(topPlace); 
    	assertTrue(includes.getInterfacePlace("a.P0").getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAway); 
    	assertEquals("now has another place",3, oldPetriNet.getPlaces().size()); 
    	CloneExecutablePetriNet.refreshFromIncludeHierarchy(executablePetriNet); 
    	assertEquals("...but interface places not copied to executable PN",
    			4,executablePetriNet.getPlaces().size()); 
    	Place newPlace = CloneExecutablePetriNet.getInstanceForTesting().getPendingPlacesForInterfacePlaceConversion().get("a.P0"); 
    	originPlace.setId("a.P0");  
    	assertEquals("s/b same once origin id is forced",newPlace, originPlace); 
    	assertEquals(1, CloneExecutablePetriNet.getInstanceForTesting().getPendingPlacesForInterfacePlaceConversion().size());
    	assertEquals("a.P0", CloneExecutablePetriNet.getInstanceForTesting().getPendingPlacesForInterfacePlaceConversion().values().iterator().next().getId());
    }
    @Test
    public void buildsMapOfOldAndClonedPlacesInExecutablePetriNet() throws Exception {
    	buildSimpleNet();
    	executablePetriNet = oldPetriNet.getExecutablePetriNet();
    	CloneExecutablePetriNet.refreshFromIncludeHierarchy(executablePetriNet);
    	assertEquals(2, executablePetriNet.getPlaceCloneMap().size());
    	Place P0 = oldPetriNet.getComponent("P0", Place.class);
    	Place P1 = oldPetriNet.getComponent("P1", Place.class);
    	Place clonedP0 = executablePetriNet.getComponent("P0", Place.class);
    	Place clonedP1 = executablePetriNet.getComponent("P1", Place.class);
    	assertEquals(clonedP0, executablePetriNet.getPlaceCloneMap().get(P0)); 
    	assertEquals(clonedP1, executablePetriNet.getPlaceCloneMap().get(P1)); 
    }
	private void buildIncludeHierarchyAndRefreshExecutablePetriNet()
			throws PetriNetComponentException, IncludeException {
		buildSimpleNet(); 
    	oldPetriNet.setName(new NormalPetriNetName("net")); 
    	net2 = buildTestNet(); 
    	includes = new IncludeHierarchy(oldPetriNet, "top");
    	includes.include(net2, "a");  
    	oldPetriNet.setIncludeHierarchy(includes);
    	executablePetriNet = new ExecutablePetriNet(oldPetriNet); 
    	CloneExecutablePetriNet.refreshFromIncludeHierarchy(executablePetriNet);
	}
	protected void checkExecutableHasSumOfOldPNAndNet2Components(PetriNet net2,
			ExecutablePetriNet executablePetriNet) {
		assertEquals(2, oldPetriNet.getPlaces().size()); 
    	assertEquals(4, oldPetriNet.getArcs().size()); 
    	assertEquals(2, oldPetriNet.getTransitions().size()); 
    	assertEquals(2, net2.getPlaces().size()); 
    	assertEquals(2, net2.getArcs().size()); 
    	assertEquals(2, net2.getTransitions().size()); 
    	assertEquals("oldPN + net2 places = 4",4,executablePetriNet.getPlaces().size()); 
    	assertEquals("oldPN + net2 transitions = 4",4,executablePetriNet.getTransitions().size()); 
    	assertEquals("oldPN + net2 arcs = 6",6,executablePetriNet.getArcs().size());
	}
//    @Test
//    public void clonesRateParameter() throws PetriNetComponentException {
//	    checkRateParameter("3");
//    }
//    @Test
//    public void clonesRateParameterReferencingPlace() throws PetriNetComponentException {
//    	checkRateParameter("#(P0)");
//    }
//	protected void checkRateParameter(String rateExpression)
//			throws PetriNetComponentException {
//		oldPetriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
//				APlace.withId("P0").and(1, "Default").token()).andFinally(
//				ARateParameter.withId("rate1").andExpression(rateExpression)); 
//    	clonedPetriNet = CloneExecutablePetriNet.clone(oldPetriNet);
//    	RateParameter rate = clonedPetriNet.getComponent("rate1", RateParameter.class);
//    	assertEquals(rateExpression, rate.getExpression());
//	}

	protected PetriNet buildTestNet() throws PetriNetComponentException {
		PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
                        APlace.withId("P1")).and(AnImmediateTransition.withId("T0")).and(
                        AnImmediateTransition.withId("T1")).and(
                        ANormalArc.withSource("P1").andTarget("T1")).andFinally(
                        ANormalArc.withSource("T0").andTarget("P0").with("#(P0)", "Default").token());
		return net; 
	}

}
