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
import uk.ac.imperial.pipe.models.petrinet.InterfaceDiscretePlace;
import uk.ac.imperial.pipe.models.petrinet.PlaceVisitor;

public class InterfaceDiscretePlaceTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    DiscretePlace place;

	private InterfaceDiscretePlace interfaceDiscretePlace;

	private InterfaceDiscretePlace interfaceDiscretePlace2;

    @Before
    public void setUp() {
        place = new DiscretePlace("test", "test");
        interfaceDiscretePlace = new InterfaceDiscretePlace(place);
    }
    //TODO should look different in GUI
    @Test
    public void placeIdAndNameAreSuffixedToIndicateInterface() {
    	assertEquals("test-I", interfaceDiscretePlace.getId()); 
    	assertEquals("test-I", interfaceDiscretePlace.getName()); 
    }
    @Test
	public void mirrorsTokenCountOfSourcePlace() throws Exception {
    	place.setTokenCount("Default", 3); 
    	assertEquals(3, interfaceDiscretePlace.getTokenCount("Default")); 
	}
    @Test
    public void sourceMirrorsTokenCountOfInterfacePlace() throws Exception {
    	interfaceDiscretePlace.setTokenCount("Default", 2); 
    	assertEquals(2, place.getTokenCount("Default")); 
    }
    @Test
    public void multipleInterfacePlacesMirrorSource() throws Exception {
    	interfaceDiscretePlace2 = new InterfaceDiscretePlace(place);
    	place.setTokenCount("Default", 4); 
    	assertEquals(4, interfaceDiscretePlace.getTokenCount("Default")); 
    	assertEquals(4, interfaceDiscretePlace2.getTokenCount("Default")); 
    }
    @Test
    public void oneInterfacePlaceSendsCountsToSourceAndOtherInterfacePlaces() throws Exception {
    	interfaceDiscretePlace2 = new InterfaceDiscretePlace(place);
    	interfaceDiscretePlace2.setTokenCount("Default", 1); 
    	assertEquals(1, place.getTokenCount("Default")); 
    	assertEquals(1, interfaceDiscretePlace.getTokenCount("Default")); 
    }
    @Test
	public void interfacePlaceCantBeBuiltFromAnotherInterfacePlace() throws Exception {
    	exception.expect(IllegalArgumentException.class);
    	exception.expectMessage("InterfaceDiscretePlace:  an InterfacePlace cannot be constructed from another InterfacePlace, only from a DiscretePlace.");
    	interfaceDiscretePlace2 = new InterfaceDiscretePlace(interfaceDiscretePlace);
    	
	}

}
