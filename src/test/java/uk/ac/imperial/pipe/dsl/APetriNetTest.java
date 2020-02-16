package uk.ac.imperial.pipe.dsl;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import uk.ac.imperial.pipe.exceptions.InvalidRateException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.*;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class APetriNetTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void createsPetriNetWithOnePlace() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.withOnly(APlace.withId("P0"));

        PetriNet expected = new PetriNet();
        Place place = new DiscretePlace("P0", "P0");
        expected.addPlace(place);

        assertEquals(expected, petriNet);
    }

    @Test
    public void createsNamedPetriNet() throws Exception {
        PetriNet petriNet = APetriNet.named("net1").andFinally(APlace.withId("P0"));

        PetriNet expected = new PetriNet(new NormalPetriNetName("net1"));
        Place place = new DiscretePlace("P0", "P0");
        expected.addPlace(place);

        assertEquals(expected, petriNet);

    }

    @Test
    public void createsPetriNetWithMultipleItems() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.RED))
                .and(APlace.withId("P0"))
                .and(AnImmediateTransition.withId("T0"))
                .andFinally(ANormalArc.withSource("P0").andTarget("T0").with("5", "Default").tokens());

        PetriNet expected = new PetriNet();
        Token token = new ColoredToken("Default", Color.RED);
        expected.addToken(token);
        Place place = new DiscretePlace("P0", "P0");
        expected.addPlace(place);
        Transition transition = new DiscreteTransition("T0", "T0");
        expected.addTransition(transition);
        Map<String, String> arcWeights = new HashMap<>();
        arcWeights.put(token.getId(), "5");
        InboundArc arc = new InboundNormalArc(place, transition, arcWeights);
        expected.addArc(arc);

        assertEquals(expected, petriNet);
    }

    /**
     * This is an example of creating everything.
     * It shows how much space DSL saves
     * @throws PetriNetComponentException 
     */
    @Test
    public void createColoredPetriNet() throws InvalidRateException, PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Red").withColor(Color.RED))
                .and(AToken.called("Blue").withColor(Color.BLUE))
                .and(ARateParameter.withId("Foo").andExpression("10"))
                .and(APlace.withId("P0").andCapacity(10).containing(5, "Blue").tokens().and(2, "Red").tokens())
                .and(APlace.withId("P1"))
                .and(ATimedTransition.withId("T0").withRateParameter("Foo"))
                .and(AnInhibitorArc.withSource("P1").andTarget("T0"))
                .andFinally(ANormalArc.withSource("P0").andTarget("T0").with("5", "Red").tokens().and("1", "Blue")
                        .token());

        PetriNet expected = new PetriNet();
        Token red = new ColoredToken("Red", Color.RED);
        expected.addToken(red);

        Token blue = new ColoredToken("Blue", Color.BLUE);
        expected.addToken(blue);

        FunctionalRateParameter rateParameter = new FunctionalRateParameter("10", "Foo", "Foo");
        expected.addRateParameter(rateParameter);

        Place p0 = new DiscretePlace("P0", "P0");
        p0.setCapacity(10);
        Map<String, Integer> p0Tokens = new HashMap<>();
        p0Tokens.put(blue.getId(), 5);
        p0Tokens.put(red.getId(), 2);
        p0.setTokenCounts(p0Tokens);
        expected.addPlace(p0);

        Place p1 = new DiscretePlace("P1", "P1");
        expected.addPlace(p1);

        Transition t0 = new DiscreteTransition("T0", "T0");
        t0.setTimed(true);
        t0.setRate(rateParameter);
        expected.addTransition(t0);

        Map<String, String> arcWeights = new HashMap<>();
        arcWeights.put(red.getId(), "5");
        arcWeights.put(blue.getId(), "1");
        InboundArc normalArc = new InboundNormalArc(p0, t0, arcWeights);
        expected.addArc(normalArc);

        InboundArc inhibitorArc = new InboundInhibitorArc(p1, t0);
        expected.addArc(inhibitorArc);

        assertEquals(expected, petriNet);
    }

    @Test
    public void createsPetriNetWithRateParameterReferencingPlace() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(APlace.withId("P0").and(1, "Default").token())
                .andFinally(ARateParameter.withId("rate1").andExpression("#(P0)"));
        RateParameter rate = petriNet.getComponent("rate1", RateParameter.class);
        assertEquals("#(P0)", rate.getExpression());
    }

    /**
     * Show that order of building the petrinet matters.  For details, see {@link DSLCreator}
     * @throws PetriNetComponentException
     */
    @Test
    public void throwsIfComponentCreatedBeforeComponentItDependsOn() throws PetriNetComponentException {
        expectedException.expect(PetriNetComponentException.class);
        expectedException
                .expectMessage("uk.ac.imperial.pipe.exceptions.InvalidRateException: Rate of #(P0) is invalid");
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(ARateParameter.withId("rate1").andExpression("#(P0)"))
                .andFinally(APlace.withId("P0").and(1, "Default").token());
    }

}
