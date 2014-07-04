package uk.ac.imperial.pipe.models.petrinet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.awt.Color;
import java.beans.PropertyChangeListener;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;

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
        epn = net.makeExecutablePetriNet();  
    }
    @Test
    public void collectionsMatchOriginalPetriNet() {
        net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
                        APlace.withId("P1")).and(AnImmediateTransition.withId("T0")).and(
                        AnImmediateTransition.withId("T1")).and(
                        ANormalArc.withSource("T1").andTarget("P1")).andFinally(
                        ANormalArc.withSource("T0").andTarget("P0").with("#(P0)", "Default").token());
        epn = net.makeExecutablePetriNet();  
        assertThat(epn.getAnnotations()).hasSize(0); 
        assertThat(epn.getTokens()).hasSize(1); 
        assertThat(epn.getTransitions()).hasSize(2); 
        assertThat(epn.getInboundArcs()).hasSize(0); 
        assertThat(epn.getOutboundArcs()).hasSize(2); 
        assertThat(epn.getArcs()).hasSize(2); 
        assertThat(epn.getPlaces()).hasSize(2); 
        assertThat(epn.getRateParameters()).hasSize(0); 
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
