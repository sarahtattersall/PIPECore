package uk.ac.imperial.pipe.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.extractProperty;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.*;

import java.beans.PropertyChangeListener;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.imperial.pipe.models.petrinet.PetriNetHierarchy;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;

@RunWith(MockitoJUnitRunner.class)
public class PetriNetHierarchyTest
{
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private PetriNet net;
    private PetriNetHierarchy hierarchy;

    @Mock
    private PropertyChangeListener mockListener;


    @Before
    public void setUp() {
        net = new PetriNet();
        hierarchy = new PetriNetHierarchy(net);
    }
    @Test
    public void importedNetsAddedSubordinateToTop() {
        PetriNet net2 = new PetriNet();
        PetriNet net3 = new PetriNet();
        hierarchy.importNet(net2, "some-function"); 
        hierarchy.importNet(net3, "another-function"); 
        assertEquals(3, hierarchy.size());
        assertEquals(net2, hierarchy.getImportedPetriNet("some-function"));
    }
    @Test
	public void verifyTopLevelPetriNet() throws Exception
	{
    	expectedException.expect(IllegalArgumentException.class);
    	expectedException.expectMessage("Top level petri net may not be null.");
    	hierarchy = new PetriNetHierarchy(null);
	}
    //TODO verifyImportsAreNotRecursive or verifyNumberOfCascadedImportsIsLessThanSomeConstant
    //TODO verifyNamingOfEachImportedNetIsDoneSeparately
    //TODO verifyDuplicateAliasIsSuffixedToEnsureUniqueness
    //TODO verifyDefaultAssignedWhenAliasIsBlank
    //TODO verifyAliasesAreStackedAsImportsAreAdded
//    assertThat(petriNet.getPlaces()).hasSize(1);
//    assertThat(extractProperty("name").from(petriNet.getPlaces())).containsExactly("P0");
//    assertThat(petriNet.getPlaces()).extracting("x", "y").containsExactly(tuple(255, 240));
//    assertThat(petriNet.getPlaces()).extracting("markingXOffset", "markingYOffset").containsExactly(
//            tuple(0.0, 0.0));
//    assertThat(petriNet.getPlaces()).extracting("nameXOffset", "nameYOffset").containsExactly(tuple(5.0, 26.0));
//    assertThat(extractProperty("capacity").from(petriNet.getPlaces())).containsExactly(0);
//    assertThat(extractProperty("name").from(petriNet.getPlaces())).containsExactly("P0");

}
