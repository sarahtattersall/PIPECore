package uk.ac.imperial.pipe.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.extractProperty;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.*;

import java.beans.PropertyChangeListener;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.imperial.pipe.models.petrinet.DiscretePlace;
import uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet;
import uk.ac.imperial.pipe.models.petrinet.PetriNetHierarchy;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.Place;

@RunWith(MockitoJUnitRunner.class)
public class PetriNetHierarchyTest
{
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private PetriNet net;
    private PetriNet net2;
    private PetriNet net3;
    private PetriNetHierarchy hierarchy;
    private ExecutablePetriNet executablePetriNet;

    @Mock
    private PropertyChangeListener mockListener;

    @Before
    public void setUp() {
        net = new PetriNet();
        net2 = new PetriNet();
        net3 = new PetriNet();
        hierarchy = new PetriNetHierarchy(net);
        executablePetriNet = net.makeExecutablePetriNet(); 
    }
    @Test
    public void includedNetsAddedSubordinateToTop() {
    	assertEquals("hierarchy always includes the root",1, hierarchy.size()); 
        hierarchy.includeNet(net2, "some-function"); 
        hierarchy.includeNet(net3, "another-function"); 
        assertEquals(3, hierarchy.size());
        assertEquals(net2, hierarchy.getIncludedPetriNet("some-function"));
    }
    @Test
	public void verifyTopLevelPetriNet() throws Exception
	{
    	expectedException.expect(IllegalArgumentException.class);
    	expectedException.expectMessage("Root level petri net may not be null.");
    	hierarchy = new PetriNetHierarchy(null);
	}
    @Test
	public void aNetCanBeIncludedMultipleTimesUnderDifferentAliases() throws Exception {
    	net2.addPlace(new DiscretePlace("P0", "P0")); 
    	hierarchy.includeNet(net2, "left-function"); 
    	hierarchy.includeNet(net2, "right-function"); 
    	assertEquals(hierarchy.getIncludedPetriNet("left-function"), hierarchy.getIncludedPetriNet("right-function")); 
	}
    @Test
	public void returnsAllComponentsWithIdPrefixedWithAlias() throws Exception {
	  	net.addPlace(new DiscretePlace("P0", "P0root")); 
	  	net2.addPlace(new DiscretePlace("P0", "P0include")); 
	  	hierarchy.includeNet(net2, "a-function");
	  	Map<String, Place> places = hierarchy.getPlaces();
	  	assertEquals("P0root", places.get("P0").getName()); 
	  	assertEquals("P0include", places.get("a-function.P0").getName()); 
	}
    //TODO AliasInclude class
    //TODO generic getMap function.  use Id change 
    //TODO verifyImportsAreNotRecursive or verifyNumberOfCascadedImportsIsLessThanSomeConstant
    //TODO verifyDuplicateAliasIsSuffixedToEnsureUniqueness
    //TODO verifyDefaultAssignedWhenAliasIsBlank
    //TODO verifyAliasesAreStackedAsImportsAreAdded
	//TODO verifyAliasIsOptionalForRoot
//    assertThat(petriNet.getPlaces()).hasSize(1);
//    assertThat(extractProperty("name").from(petriNet.getPlaces())).containsExactly("P0");
//    assertThat(petriNet.getPlaces()).extracting("x", "y").containsExactly(tuple(255, 240));
//    assertThat(petriNet.getPlaces()).extracting("markingXOffset", "markingYOffset").containsExactly(
//            tuple(0.0, 0.0));
//    assertThat(petriNet.getPlaces()).extracting("nameXOffset", "nameYOffset").containsExactly(tuple(5.0, 26.0));
//    assertThat(extractProperty("capacity").from(petriNet.getPlaces())).containsExactly(0);
//    assertThat(extractProperty("name").from(petriNet.getPlaces())).containsExactly("P0");

}
