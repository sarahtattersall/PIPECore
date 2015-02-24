package uk.ac.imperial.pipe.visitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.File;

import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.ATimedTransition;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
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
import uk.ac.imperial.pipe.models.petrinet.name.PetriNetFileName;

public class ClonePetriNetTest {
    PetriNet oldPetriNet;
    PetriNet clonedPetriNet;

    @Before
    public void setUp() {
        buildSimpleNet();
    }
    //TODO clones InterfacePlaces (?)
	private void buildSimpleNet() {
		oldPetriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
                APlace.withId("P0").and(1, "Default").token()).and(APlace.withId("P1")).and(
                ATimedTransition.withId("T0")).and(ATimedTransition.withId("T1"))
                .and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token())
                .and(ANormalArc.withSource("T0").andTarget("P1").with("1", "Default").token())
                .and(ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token())
                .andFinally(ANormalArc.withSource("T1").andTarget("P0").with("1", "Default").token());
	}

    @Test
    public void cloneEquality() {
        oldPetriNet.setName(new NormalPetriNetName("Petri net 0"));
        clonedPetriNet = ClonePetriNet.clone(oldPetriNet);
        assertEquals(oldPetriNet, clonedPetriNet);
    }

    @Test
    public void clonePetriNetNoName() {
        clonedPetriNet = ClonePetriNet.clone(oldPetriNet);
        assertEquals(oldPetriNet, clonedPetriNet);
    }

    @Test
    public void cloneEqualityWithFileName() {
        oldPetriNet.setName(new PetriNetFileName(new File("Petri net 0")));
        clonedPetriNet = ClonePetriNet.clone(oldPetriNet);
        assertEquals(oldPetriNet, clonedPetriNet);
    }


    @Test
    public void clonesArcWithNewSourceAndTarget() throws PetriNetComponentNotFoundException {
        oldPetriNet.setName(new NormalPetriNetName("Petri net 0"));
        clonedPetriNet = ClonePetriNet.clone(oldPetriNet);
        InboundArc arc = clonedPetriNet.getComponent("P0 TO T0", InboundArc.class);
        Place clonedP0 = clonedPetriNet.getComponent("P0", Place.class);
        Transition clonedT0 = clonedPetriNet.getComponent("T0", Transition.class);
        assertTrue(arc.getSource() == clonedP0);
        assertTrue(arc.getTarget() == clonedT0);
    }
    @Test
	public void clonePetriNetToExecutablePetriNetReplacingExistingState() throws Exception {
    	buildSimpleNet(); 
    	oldPetriNet.setIncludeHierarchy(new IncludeHierarchy(oldPetriNet, "root"));
    	ExecutablePetriNet executablePetriNet = new ExecutablePetriNet(oldPetriNet); 
    	ClonePetriNet.refreshFromIncludeHierarchy(executablePetriNet); 
    	assertEquals("root.P0", executablePetriNet.getComponent("root.P0", Place.class).getId()); 
    	assertEquals("root.T0", executablePetriNet.getComponent("root.T0", Transition.class).getId()); 
    	assertEquals("root.P0 TO root.T0", executablePetriNet.getComponent("root.P0 TO root.T0", InboundArc.class).getId()); 
	}
    //TODO test for transitionOut/InboundArcs
    //TODO test for arcweights 
    public void convertsArcsFromInterfacePlaceToNewOriginPlace() throws Exception {
    	buildSimpleNet(); 
    	oldPetriNet.setName(new NormalPetriNetName("net")); 
    	PetriNet net2 = buildTestNet(); 
    	IncludeHierarchy includes = new IncludeHierarchy(oldPetriNet, "top");
    	includes.include(net2, "a");  
    	oldPetriNet.setIncludeHierarchy(includes);
    	ExecutablePetriNet executablePetriNet = new ExecutablePetriNet(oldPetriNet); 
    	ClonePetriNet.refreshFromIncludeHierarchy(executablePetriNet); 
    	assertEquals(4,executablePetriNet.getPlaces().size()); 
    	assertEquals(4,executablePetriNet.getTransitions().size()); 
    	assertEquals(6,executablePetriNet.getArcs().size()); 
    	assertEquals(2, oldPetriNet.getPlaces().size()); 
    	assertEquals(4, oldPetriNet.getArcs().size()); 
    	Place originPlace = net2.getComponent("P0", Place.class); 
    	includes.getInclude("a").addToInterface(originPlace, true, false, false, false); 
    	Place topPlace = oldPetriNet.getComponent("a.P0", Place.class);
    	assertTrue(includes.getInterfacePlace("a.P0").getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAvailable); 
    	assertTrue(includes.getInclude("a").getInterfacePlace("P0").getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusHome); 
    	includes.addAvailablePlaceToPetriNet(topPlace); 
    	assertTrue(includes.getInterfacePlace("a.P0").getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAway); 
    	assertEquals("now has another place",3, oldPetriNet.getPlaces().size()); 
    	ClonePetriNet.refreshFromIncludeHierarchy(executablePetriNet); 
    	assertEquals("...but interface places not copied to executable PN",
    			4,executablePetriNet.getPlaces().size()); 
    	Place newPlace = ClonePetriNet.getInstanceForTesting().getPendingPlacesForInterfacePlaceConversion().get("a.P0"); 
    	originPlace.setId("a.P0");  
    	assertEquals("s/b same once origin id is forced",newPlace, originPlace); 
    	assertEquals(1, ClonePetriNet.getInstanceForTesting().getPendingPlacesForInterfacePlaceConversion().size()); 
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
