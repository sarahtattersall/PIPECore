package uk.ac.imperial.pipe.parsers;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;

import java.awt.Color;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PetriNetWeightParserTest {

    private static final PetriNet EMPTY_PETRI_NET = new PetriNet();
    private EvalVisitor evalVisitor;

    @Before
    public void setUp() {
        executablePetriNet = EMPTY_PETRI_NET.getExecutablePetriNet();
        evalVisitor = new EvalVisitor(executablePetriNet);
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private ExecutablePetriNet executablePetriNet;

    @Test
    public void correctlyIdentifiesErrors() {
        FunctionalWeightParser<Double> parser = new PetriNetWeightParser(evalVisitor, executablePetriNet);
        FunctionalResults<Double> result = parser.evaluateExpression("2 +");
        assertTrue(result.hasErrors());
    }

    @Test
    public void producesCorrectErrorMessage() {
        FunctionalWeightParser<Double> parser = new PetriNetWeightParser(evalVisitor, executablePetriNet);
        FunctionalResults<Double> result = parser.evaluateExpression("2 *");
        assertThat(result.getErrors()).containsExactly("line 1:3 no viable alternative at input '<EOF>'");
    }

    @Test
    public void expressionIsNegativeIfContainsErrors() throws UnparsableException {
        FunctionalWeightParser<Double> parser = new PetriNetWeightParser(evalVisitor, executablePetriNet);
        FunctionalResults<Double> result = parser.evaluateExpression("2 *");
        assertEquals(new Double(-1.), result.getResult());
    }

    @Test
    public void returnsErrorIfResultIsLessThanZero() throws UnparsableException {
        FunctionalWeightParser<Double> parser = new PetriNetWeightParser(evalVisitor, executablePetriNet);
        FunctionalResults<Double> result = parser.evaluateExpression("2 - 6");
        assertThat(result.getErrors()).containsExactly("Expression result cannot be less than zero!");
    }

    @Test
    public void willNotEvaluateExpressionIfPetriNetDoesNotContainComponent()
            throws UnparsableException, PetriNetComponentException {
        PetriNet petriNet = APetriNet.withOnly(APlace.withId("P1"));
        executablePetriNet = petriNet.getExecutablePetriNet();
        FunctionalWeightParser<Double> parser = new PetriNetWeightParser(evalVisitor, executablePetriNet);
        FunctionalResults<Double> result = parser.evaluateExpression("#(P0)");
        assertThat(result.getErrors()).contains("Not all referenced components exist in the Petri net!");
    }

    @Test
    public void evaluatesIfPlaceIsInPetriNet() throws UnparsableException, PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .andFinally(APlace.withId("P0").containing(10, "Default").tokens());

        ExecutablePetriNet executablePetriNet = petriNet.getExecutablePetriNet();
        EvalVisitor evalVisitor = new EvalVisitor(executablePetriNet);
        FunctionalWeightParser<Double> parser = new PetriNetWeightParser(evalVisitor, executablePetriNet);
        FunctionalResults<Double> result = parser.evaluateExpression("#(P0)");
        assertEquals(new Double(10), result.getResult());
    }

    @Test
    public void returnsCorrectComponentsForTotalTokens() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .andFinally(APlace.withId("P0").containing(10, "Default").tokens());
        ExecutablePetriNet executablePetriNet = petriNet.getExecutablePetriNet();
        EvalVisitor evalVisitor = new EvalVisitor(executablePetriNet);
        FunctionalWeightParser<Double> parser = new PetriNetWeightParser(evalVisitor, executablePetriNet);
        FunctionalResults<Double> result = parser.evaluateExpression("#(P0)");
        assertTrue(result.getComponents().contains("P0"));
    }

    @Test
    public void returnsCorrectComponentsForSpecificTokens() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .andFinally(APlace.withId("P0").containing(10, "Default").tokens());
        ExecutablePetriNet executablePetriNet = petriNet.getExecutablePetriNet();
        FunctionalWeightParser<Double> parser = new PetriNetWeightParser(evalVisitor, executablePetriNet);
        FunctionalResults<Double> result = parser.evaluateExpression("#(P0, Default)");
        assertTrue(result.getComponents().contains("P0"));
        assertTrue(result.getComponents().contains("Default"));
    }

}
