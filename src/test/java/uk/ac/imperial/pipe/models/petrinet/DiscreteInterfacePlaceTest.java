package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.stringtemplate.v4.compiler.STParser.expr_return;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.DiscretePlace;
import uk.ac.imperial.pipe.models.petrinet.DiscretePlaceVisitor;
import uk.ac.imperial.pipe.models.petrinet.DiscreteInterfacePlace;
import uk.ac.imperial.pipe.models.petrinet.PlaceVisitor;

public class DiscreteInterfacePlaceTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    DiscretePlace place;

	private InterfacePlace discreteInterfacePlace;

	private InterfacePlace discreteInterfacePlace2;

    @Before
    public void setUp() {
        place = new DiscretePlace("test", "test");
        discreteInterfacePlace = new DiscreteInterfacePlace(place,"home", "away");
    }
    //TODO should look different in GUI
    @Test
	public void mirrorsTokenCountOfSourcePlace() throws Exception {
    	place.setTokenCount("Default", 3); 
    	assertEquals(3, discreteInterfacePlace.getTokenCount("Default")); 
	}
    @Test
    public void sourceMirrorsTokenCountOfInterfacePlace() throws Exception {
    	discreteInterfacePlace.setTokenCount("Default", 2); 
    	assertEquals(2, place.getTokenCount("Default")); 
    }
    @Test
    public void multipleInterfacePlacesMirrorSource() throws Exception {
    	discreteInterfacePlace2 = new DiscreteInterfacePlace(place,"","");
    	place.setTokenCount("Default", 4); 
    	assertEquals(4, discreteInterfacePlace.getTokenCount("Default")); 
    	assertEquals(4, discreteInterfacePlace2.getTokenCount("Default")); 
    }
    @Test
    public void oneInterfacePlaceSendsCountsToSourceAndOtherInterfacePlaces() throws Exception {
    	discreteInterfacePlace2 = new DiscreteInterfacePlace(place, "","");
    	discreteInterfacePlace2.setTokenCount("Default", 1); 
    	assertEquals(1, place.getTokenCount("Default")); 
    	assertEquals(1, discreteInterfacePlace.getTokenCount("Default")); 
    }
//    @Test
//	public void interfacePlaceCantBeBuiltFromAnotherInterfacePlace() throws Exception {
//    	exception.expect(IllegalArgumentException.class);
//    	exception.expectMessage("InterfaceDiscretePlace:  an InterfacePlace cannot be constructed from another InterfacePlace, only from a DiscretePlace.");
//    	discreteInterfacePlace2 = new DiscreteInterfacePlace(discreteInterfacePlace,"","");
//	}
    //TODO consider whether use(false) is same as remove()
    @Test
	public void useInterfacePlaceTogglesItsStatus() throws Exception {
    	discreteInterfacePlace = new DiscreteInterfacePlace(place, InterfacePlaceStatusEnum.AVAILABLE, "home", "away");
    	assertTrue(discreteInterfacePlace.getStatus() instanceof InterfacePlaceStatusAvailable); 
    	assertTrue(discreteInterfacePlace.canUse()); 
    	discreteInterfacePlace.use(); 
    	assertTrue(discreteInterfacePlace.getStatus() instanceof InterfacePlaceStatusInUse); 
    	assertTrue(discreteInterfacePlace.canRemove()); 
    	discreteInterfacePlace.remove(); 
    	assertTrue(discreteInterfacePlace.getStatus() instanceof InterfacePlaceStatusAvailable); 
	}
    @Test
	public void homeInterfacePlaceCantBeUsed() throws Exception {
    	//TODO throw or just return false? 
//    	exception.expect(IllegalStateException.class); 
//    	exception.expectMessage("InterfacePlaceStatusHome: interface place cannot be used in the petri net that is the home of its underlying place.");
    	discreteInterfacePlace = new DiscreteInterfacePlace(place, InterfacePlaceStatusEnum.HOME, "home");
    	assertFalse("can't use home IP",discreteInterfacePlace.canUse()); 
    	assertFalse(discreteInterfacePlace.use()); 
	}
//    @Test
	public void homeStatusImpliesNoAwayAlias() throws Exception {
    	//TODO homeStatusImpliesNoAwayAlias
	}
//    @Test
    public void awayStatusImpliesNotNullAwayAlias() throws Exception {
    	//TODO awayStatusImpliesNotNullAwayAlias
    }
    @Test
	public void eitherHomeOrAwayIncludeNameCanBeChanged() throws Exception {
    	discreteInterfacePlace = new DiscreteInterfacePlace(place, "home", "away");
    	assertEquals("away..home.test", discreteInterfacePlace.getId()); 
    	assertEquals("away..home.test", discreteInterfacePlace.getName()); 
    	discreteInterfacePlace.setHomeAlias("x"); 
    	assertEquals("away..x.test", discreteInterfacePlace.getId()); 
    	discreteInterfacePlace.setAwayAlias("y"); 
    	assertEquals("y..x.test", discreteInterfacePlace.getId()); 
    	assertEquals("y..x.test", discreteInterfacePlace.getName()); 
	}
	// interfaceplacetest:  added to a PN; marking follows other IP; participates in EPN 
//	@Test
//	public void listensForChangesToSourceAndTargetIncludesAndRenamesAccordingly() throws Exception {
//
//	}

}
