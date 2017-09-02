package uk.ac.imperial.pipe.visitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;

import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.ATimedTransition;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.exceptions.IncludeException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet;
import uk.ac.imperial.pipe.models.petrinet.InboundArc;
import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatusAvailable;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatusAway;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatusHome;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;

public class ExecutablePetriNetClonerTest {
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

    @Test
	public void clonePetriNetToExecutablePetriNetReplacingExistingState() throws Exception {
    	buildSimpleNet(); 
    	oldPetriNet.setIncludeHierarchy(new IncludeHierarchy(oldPetriNet, "root"));
    	ExecutablePetriNet executablePetriNet = new ExecutablePetriNet(oldPetriNet); 
    	ExecutablePetriNetCloner.refreshFromIncludeHierarchy(executablePetriNet); 
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
    	ExecutablePetriNetCloner.refreshFromIncludeHierarchy(executablePetriNet); 
    	assertEquals("...but interface places not copied to executable PN",
    			4,executablePetriNet.getPlaces().size()); 
    	Place newPlace = ExecutablePetriNetCloner.cloneInstance.getPendingAwayPlacesForInterfacePlaceConversion().get("a.P0"); 
    	originPlace.setId("a.P0");  
    	assertTrue("s/b same once origin id is forced, except for isOriginal()",
    			(newPlace.equalsPosition(originPlace) && (newPlace.equalsStructure(originPlace)))); 
    	assertEquals(1, ExecutablePetriNetCloner.cloneInstance.getPendingAwayPlacesForInterfacePlaceConversion().size());
    	assertEquals("a.P0", ExecutablePetriNetCloner.cloneInstance.getPendingAwayPlacesForInterfacePlaceConversion().values().iterator().next().getId());
    }
    
    @Test
    public void buildsMapOfOldAndClonedPlacesInExecutablePetriNet() throws Exception {
    	buildSimpleNet();
    	executablePetriNet = oldPetriNet.getExecutablePetriNet();
    	ExecutablePetriNetCloner.refreshFromIncludeHierarchy(executablePetriNet);
    	assertEquals(2, executablePetriNet.getPlaceCloneMap().size());
    	Place P0 = oldPetriNet.getComponent("P0", Place.class);
    	Place P1 = oldPetriNet.getComponent("P1", Place.class);
    	Place clonedP0 = executablePetriNet.getComponent("P0", Place.class);
    	Place clonedP1 = executablePetriNet.getComponent("P1", Place.class);
    	assertEquals(clonedP0, executablePetriNet.getPlaceCloneMap().get(P0)); 
    	assertEquals(clonedP1, executablePetriNet.getPlaceCloneMap().get(P1)); 
    }
    @Test
    public void homePlacesInMergeInterfaceStatusAreNotCloned() throws Exception {
    	buildIncludeHierarchyAndRefreshExecutablePetriNet(); 
    	Place originPlace = net2.getComponent("P0", Place.class); 
    	includes.getInclude("a").addToInterface(originPlace, true, false, false, false); 
    	assertTrue(includes.getInterfacePlace("a.P0").getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAvailable); 
    	assertTrue(includes.getInclude("a").getInterfacePlace("P0").getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusHome);
    	assertTrue(originPlace == includes.getInclude("a").getInterfacePlace("P0").getStatus().getMergeInterfaceStatus().getHomePlace());
    	assertTrue(originPlace == includes.getInterfacePlace("a.P0").getStatus().getMergeInterfaceStatus().getHomePlace());
    	ExecutablePetriNetCloner.refreshFromIncludeHierarchy(executablePetriNet); 
    	assertTrue(originPlace == includes.getInclude("a").getInterfacePlace("P0").getStatus().getMergeInterfaceStatus().getHomePlace());
    	assertTrue(originPlace == includes.getInterfacePlace("a.P0").getStatus().getMergeInterfaceStatus().getHomePlace());
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
    	ExecutablePetriNetCloner.refreshFromIncludeHierarchy(executablePetriNet);
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

	protected PetriNet buildTestNet() throws PetriNetComponentException {
		PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
                        APlace.withId("P1")).and(AnImmediateTransition.withId("T0")).and(
                        AnImmediateTransition.withId("T1")).and(
                        ANormalArc.withSource("P1").andTarget("T1")).andFinally(
                        ANormalArc.withSource("T0").andTarget("P0").with("#(P0)", "Default").token());
		return net; 
	}

}
