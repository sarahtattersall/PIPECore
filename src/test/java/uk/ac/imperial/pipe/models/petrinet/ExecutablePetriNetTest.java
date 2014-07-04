package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.*;

import java.beans.PropertyChangeListener;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;

public class ExecutablePetriNetTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private PetriNet net;
    private ExecutablePetriNet epn;

    @Mock
    private PropertyChangeListener mockListener;



    @Before
    public void setUp() {
        net = new PetriNet();
        epn = new ExecutablePetriNet(net); 
    }
    @Test
    public void importedNetsAddedSubordinateToTop() {
        PetriNet net2 = new PetriNet();
        PetriNet net3 = new PetriNet();
//        hierarchy.importNet(net2, "some-function"); 
//        hierarchy.importNet(net3, "another-function"); 
//        assertEquals(3, hierarchy.size());
//        assertEquals(net2, hierarchy.getImportedPetriNet("some-function"));
    }
//    @Test
	public void verifyTopLevelPetriNet() throws Exception
	{
    	expectedException.expect(IllegalArgumentException.class);
    	expectedException.expectMessage("Top level petri net may not be null.");
//    	hierarchy = new PetriNetHierarchy(null);
	}
    //TODO verifyImportsAreNotRecursive or verifyNumberOfCascadedImportsIsLessThanSomeConstant
    //TODO verifyNamingOfEachImportedNetIsDoneSeparately
    //TODO verifyDuplicateAliasIsSuffixedToEnsureUniqueness
    //TODO verifyDefaultAssignedWhenAliasIsBlank
    //TODO verifyAliasesAreStackedAsImportsAreAdded

}
