package uk.ac.imperial.pipe.visitor;

import org.junit.Before;
import org.junit.Test;
import uk.ac.imperial.pipe.dsl.*;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.AbstractPetriNet;
import uk.ac.imperial.pipe.models.petrinet.Arc;
import uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet;
import uk.ac.imperial.pipe.models.petrinet.InboundArc;
import uk.ac.imperial.pipe.models.petrinet.InboundNormalArc;
import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.InterfacePlace;
import uk.ac.imperial.pipe.models.petrinet.InterfacePlaceStatusAvailable;
import uk.ac.imperial.pipe.models.petrinet.InterfacePlaceStatusHome;
import uk.ac.imperial.pipe.models.petrinet.InterfacePlaceStatusInUse;
import uk.ac.imperial.pipe.models.petrinet.OutboundNormalArc;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;
import uk.ac.imperial.pipe.models.petrinet.name.PetriNetFileName;

import java.awt.Color;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
    @Test
	public void clonedExecutablePetriNetDoesNotHaveInterfacePlaces() throws Exception {
    	buildSimpleNet(); 
    	oldPetriNet.setName(new NormalPetriNetName("net")); 
    	PetriNet net2 = buildTestNet(); 
    	IncludeHierarchy includes = new IncludeHierarchy(oldPetriNet, "top");
    	includes.include(net2, "a");  
    	oldPetriNet.setIncludeHierarchy(includes);
    	ExecutablePetriNet executablePetriNet = new ExecutablePetriNet(oldPetriNet); 
    	ClonePetriNet.refreshFromIncludeHierarchy(executablePetriNet); 
    	assertEquals(2, oldPetriNet.getPlaces().size()); 
		assertEquals(4,executablePetriNet.getPlaces().size()); 
		Place originPlace = net2.getComponent("P0", Place.class); 
		includes.getInclude("a").addToInterfaceOld(originPlace); 
		includes.useInterfacePlace("top..a.P0"); 
		assertEquals("now has an interface place",3, oldPetriNet.getPlaces().size()); 
		ClonePetriNet.refreshFromIncludeHierarchy(executablePetriNet); 
		assertEquals("...but interface places not copied to executable PN",
				4,executablePetriNet.getPlaces().size()); 
    }
    @Test
    public void clonedExecutablePetriNetTracksNewPlacesForInterfacePlaceReplacement() throws Exception {
    	buildSimpleNet(); 
    	oldPetriNet.setName(new NormalPetriNetName("net")); 
    	PetriNet net2 = buildTestNet(); 
    	IncludeHierarchy includes = new IncludeHierarchy(oldPetriNet, "top");
    	includes.include(net2, "a");  
    	oldPetriNet.setIncludeHierarchy(includes);
    	Place originPlace = net2.getComponent("P0", Place.class); 
    	includes.getInclude("a").addToInterfaceOld(originPlace); 
    	includes.useInterfacePlace("top..a.P0"); 
    	ExecutablePetriNet executablePetriNet = new ExecutablePetriNet(oldPetriNet); 
    	ClonePetriNet.refreshFromIncludeHierarchy(executablePetriNet); 
    	Place newPlace = ClonePetriNet.getInstanceForTesting().getPendingPlacesForInterfacePlaceConversion().get("top..a.P0"); 
    	originPlace.setId("top.a.P0");  
    	assertEquals("s/b same once origin id is forced",newPlace, originPlace); 
    	assertEquals(1, ClonePetriNet.getInstanceForTesting().getPendingPlacesForInterfacePlaceConversion().size()); 
    }
    //TODO test for transitionOut/InboundArcs
    //TODO test for arcweights 
//    @Test
    public void convertsArcsFromInterfacePlaceToNewOriginPlace() throws Exception {
    	buildSimpleNet(); 
    	oldPetriNet.setName(new NormalPetriNetName("net")); 
    	PetriNet net2 = buildTestNet(); 
    	IncludeHierarchy includes = new IncludeHierarchy(oldPetriNet, "top");
    	includes.include(net2, "a");  
    	oldPetriNet.setIncludeHierarchy(includes);
    	ExecutablePetriNet executablePetriNet = new ExecutablePetriNet(oldPetriNet); 
    	ClonePetriNet.refreshFromIncludeHierarchy(executablePetriNet); 
    	//new IncludeHierarchy(net, "top"); 
//		executablePetriNet = oldPetriNet.getExecutablePetriNet(); 
    	assertEquals(4,executablePetriNet.getPlaces().size()); 
    	assertEquals(4,executablePetriNet.getTransitions().size()); 
    	assertEquals(6,executablePetriNet.getArcs().size()); 
    	assertEquals(2, oldPetriNet.getPlaces().size()); 
    	assertEquals(4, oldPetriNet.getArcs().size()); 
    	Place originPlace = net2.getComponent("P0", Place.class); 
    	includes.getInclude("a").addToInterfaceOld(originPlace); 
    	assertTrue(includes.getInterfacePlaceOld("top..a.P0").getInterfacePlaceStatus() instanceof InterfacePlaceStatusAvailable); 
    	assertTrue(includes.getInclude("a").getInterfacePlaceOld("a.P0").getInterfacePlaceStatus() instanceof InterfacePlaceStatusHome); 
    	includes.useInterfacePlace("top..a.P0"); 
    	assertTrue(includes.getInterfacePlaceOld("top..a.P0").getInterfacePlaceStatus() instanceof InterfacePlaceStatusInUse); 
    	assertEquals("now has an interface place",3, oldPetriNet.getPlaces().size()); 
    	ClonePetriNet.refreshFromIncludeHierarchy(executablePetriNet); 
    	assertEquals("...but interface places not copied to executable PN",
    			4,executablePetriNet.getPlaces().size()); 
    	Place newPlace = ClonePetriNet.getInstanceForTesting().getPendingPlacesForInterfacePlaceConversion().get("top..a.P0"); 
    	originPlace.setId("top.a.P0");  
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
