package uk.ac.imperial.pipe.io;

import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.pipe.exceptions.IncludeException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.io.adapters.model.AdaptedIncludeHierarchyBuilder.Includes;
import uk.ac.imperial.pipe.models.petrinet.*;
import utils.FileUtils;

import javax.xml.bind.JAXBException;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IncludeHierarchyReaderTest {

    IncludeHierarchyReader reader;

    @Before
    public void setUp() throws JAXBException {
        reader = new IncludeHierarchyIOImpl();
    }

    @Test
    public void createsSingleIncludeHierarchy()
    		throws Exception {
    	IncludeHierarchy include = reader.read(FileUtils.fileLocation(XMLUtils.getSingleIncludeHierarchyFile()));
    	assertEquals("a", include.getName()); 
    	PetriNet net = include.getPetriNet(); 
    	assertEquals(2, net.getPlaces().size()); 
    	assertEquals(1, net.getTransitions().size()); 
    	assertEquals(2, net.getArcs().size()); 
    }

    //TODO test multiple nesting levels 
    
    @Test
    public void createsIncludeHierarchyWithMultipleLevels()
    		throws PetriNetComponentNotFoundException, JAXBException, FileNotFoundException, IncludeException {
    	IncludeHierarchy include = reader.read(FileUtils.fileLocation(XMLUtils.getMultipleIncludeHierarchyFile()));
    	assertEquals("a", include.getName()); 
    	PetriNet net = include.getPetriNet(); 
    	assertEquals(5, net.getPlaces().size()); 
    	assertEquals(5, net.getTransitions().size()); 
    	assertEquals(12, net.getArcs().size()); 
    	IncludeHierarchy includeb = include.getInclude("b"); 
    	PetriNet netb = includeb.getPetriNet(); 
    	assertEquals(2, netb.getPlaces().size());
    	assertEquals(1, netb.getTransitions().size()); 
    	assertEquals(2, netb.getArcs().size()); 
    	IncludeHierarchy includec = include.getInclude("c"); 
    	PetriNet netc = includec.getPetriNet(); 
    	assertEquals(2, netc.getPlaces().size()); 
    	assertEquals(1, netc.getTransitions().size()); 
    	assertEquals(2, netc.getArcs().size()); 
    	Place p0 = netc.getComponent("P0", Place.class); 
    	assertEquals(225, p0.getX()); 
    }


}
