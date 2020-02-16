package uk.ac.imperial.pipe.visitor;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.File;

import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.ARateParameter;
import uk.ac.imperial.pipe.dsl.ATimedTransition;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.exceptions.IncludeException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet;
import uk.ac.imperial.pipe.models.petrinet.InboundArc;
import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatusAvailable;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatusAway;
import uk.ac.imperial.pipe.models.petrinet.MergeInterfaceStatusHome;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.RateParameter;
import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;
import uk.ac.imperial.pipe.models.petrinet.name.PetriNetFileName;

public class PetriNetClonerTest {
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
        oldPetriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P0").and(1, "Default").token()).and(APlace.withId("P1"))
                .and(ATimedTransition.withId("T0")).and(ATimedTransition.withId("T1"))
                .and(ANormalArc.withSource("P0").andTarget("T0").with("1", "Default").token())
                .and(ANormalArc.withSource("T0").andTarget("P1").with("1", "Default").token())
                .and(ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token())
                .andFinally(ANormalArc.withSource("T1").andTarget("P0").with("1", "Default").token());
    }

    @Test
    public void cloneEquality() {
        oldPetriNet.setName(new NormalPetriNetName("Petri net 0"));
        clonedPetriNet = PetriNetCloner.clone(oldPetriNet);
        assertEquals(oldPetriNet, clonedPetriNet);
    }

    @Test
    public void clonePetriNetNoName() {
        clonedPetriNet = PetriNetCloner.clone(oldPetriNet);
        assertEquals(oldPetriNet, clonedPetriNet);
    }

    @Test
    public void cloneEqualityWithFileName() {
        oldPetriNet.setName(new PetriNetFileName(new File("Petri net 0")));
        clonedPetriNet = PetriNetCloner.clone(oldPetriNet);
        assertEquals(oldPetriNet, clonedPetriNet);
    }

    @Test
    public void clonesArcWithNewSourceAndTarget() throws PetriNetComponentNotFoundException {
        oldPetriNet.setName(new NormalPetriNetName("Petri net 0"));
        clonedPetriNet = PetriNetCloner.clone(oldPetriNet);
        InboundArc arc = clonedPetriNet.getComponent("P0 TO T0", InboundArc.class);
        Place clonedP0 = clonedPetriNet.getComponent("P0", Place.class);
        Transition clonedT0 = clonedPetriNet.getComponent("T0", Transition.class);
        assertTrue(arc.getSource() == clonedP0);
        assertTrue(arc.getTarget() == clonedT0);
    }

    @Test
    public void clonesRateParameter() throws PetriNetComponentException {
        checkRateParameter("3");
    }

    @Test
    public void clonesRateParameterReferencingPlace() throws PetriNetComponentException {
        checkRateParameter("#(P0)");
    }

    protected void checkRateParameter(String rateExpression)
            throws PetriNetComponentException {
        oldPetriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P0").and(1, "Default").token())
                .andFinally(ARateParameter.withId("rate1").andExpression(rateExpression));
        clonedPetriNet = PetriNetCloner.clone(oldPetriNet);
        RateParameter rate = clonedPetriNet.getComponent("rate1", RateParameter.class);
        assertEquals(rateExpression, rate.getExpression());
    }

    protected PetriNet buildTestNet() throws PetriNetComponentException {
        PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0"))
                .and(APlace.withId("P1")).and(AnImmediateTransition.withId("T0"))
                .and(AnImmediateTransition.withId("T1")).and(ANormalArc.withSource("P1").andTarget("T1"))
                .andFinally(ANormalArc.withSource("T0").andTarget("P0").with("#(P0)", "Default").token());
        return net;
    }

}
