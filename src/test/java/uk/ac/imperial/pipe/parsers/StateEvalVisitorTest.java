package uk.ac.imperial.pipe.parsers;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.junit.Test;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.state.HashedState;
import uk.ac.imperial.state.HashedStateBuilder;
import uk.ac.imperial.state.State;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class StateEvalVisitorTest {

    private static final PetriNet EMPTY_PETRI_NET = new PetriNet();
    private static final ExecutablePetriNet EMPTY_EXECUTABLE_PETRI_NET = EMPTY_PETRI_NET.getExecutablePetriNet();
    private static final State EMPTY_STATE = new HashedState(new HashMap<String, Map<String, Integer>>());
    private ExecutablePetriNet executablePetriNet;

    public ParseTree parseTreeForExpr(String expr) {
        CharStream input = new ANTLRInputStream(expr);
        RateGrammarLexer lexer = new RateGrammarLexer(input);
        TokenStream tokens = new CommonTokenStream(lexer);
        RateGrammarParser parser = new RateGrammarParser(tokens);
        return parser.program();
    }

    @Test
    public void parsesBasicInt() {
        ParseTree tree = parseTreeForExpr("2");
        ParseTreeVisitor<Double> evalVisitor = new StateEvalVisitor(EMPTY_EXECUTABLE_PETRI_NET, EMPTY_STATE);
        Double result = evalVisitor.visit(tree);

        assertEquals(new Double(2.0), result);
    }

    @Test
    public void parsesBasicIntegerAddition() {
        ParseTree tree = parseTreeForExpr("2 + 8");
        ParseTreeVisitor<Double> evalVisitor = new StateEvalVisitor(EMPTY_EXECUTABLE_PETRI_NET, EMPTY_STATE);
        Double result = evalVisitor.visit(tree);
        assertEquals(new Double(10.0), result);
    }

    @Test
    public void parsesBasicDoubleAddition() {
        ParseTree tree = parseTreeForExpr("2.5 + 8.3");
        ParseTreeVisitor<Double> evalVisitor = new StateEvalVisitor(EMPTY_EXECUTABLE_PETRI_NET, EMPTY_STATE);
        Double result = evalVisitor.visit(tree);
        assertEquals(new Double(10.8), result);
    }

    @Test
    public void parsesBasicIntegerSubtraction() {
        ParseTree tree = parseTreeForExpr("5 - 1");
        ParseTreeVisitor<Double> evalVisitor = new StateEvalVisitor(EMPTY_EXECUTABLE_PETRI_NET, EMPTY_STATE);
        Double result = evalVisitor.visit(tree);
        assertEquals(new Double(4.0), result);
    }

    @Test
    public void parsesBasicDoubleSubtraction() {
        ParseTree tree = parseTreeForExpr("2.5 - 0.2");
        ParseTreeVisitor<Double> evalVisitor = new StateEvalVisitor(EMPTY_EXECUTABLE_PETRI_NET, EMPTY_STATE);
        Double result = evalVisitor.visit(tree);
        assertEquals(new Double(2.3), result);
    }

    @Test
    public void parsesBasicIntegerMultiplication() {
        ParseTree tree = parseTreeForExpr("5 * 2");
        ParseTreeVisitor<Double> evalVisitor = new StateEvalVisitor(EMPTY_EXECUTABLE_PETRI_NET, EMPTY_STATE);
        Double result = evalVisitor.visit(tree);
        assertEquals(new Double(10.0), result);
    }

    @Test
    public void parsesBasicDoubleMultiplication() {
        ParseTree tree = parseTreeForExpr("2.5 * 4");
        ParseTreeVisitor<Double> evalVisitor = new StateEvalVisitor(EMPTY_EXECUTABLE_PETRI_NET, EMPTY_STATE);
        Double result = evalVisitor.visit(tree);
        assertEquals(new Double(10.0), result);
    }

    @Test
    public void parsesBasicIntegerDivision() {
        ParseTree tree = parseTreeForExpr("5 / 2");
        ParseTreeVisitor<Double> evalVisitor = new StateEvalVisitor(EMPTY_EXECUTABLE_PETRI_NET, EMPTY_STATE);
        Double result = evalVisitor.visit(tree);
        assertEquals(new Double(2.5), result);
    }

    @Test
    public void parsesBasicDoubleDivision() {
        ParseTree tree = parseTreeForExpr("5 / 2.5");
        ParseTreeVisitor<Double> evalVisitor = new StateEvalVisitor(EMPTY_EXECUTABLE_PETRI_NET, EMPTY_STATE);
        Double result = evalVisitor.visit(tree);
        assertEquals(new Double(2), result);
    }

    @Test
    public void parsesBasicParentheses() {
        ParseTree tree = parseTreeForExpr("(2)");
        ParseTreeVisitor<Double> evalVisitor = new StateEvalVisitor(EMPTY_EXECUTABLE_PETRI_NET, EMPTY_STATE);
        Double result = evalVisitor.visit(tree);
        assertEquals(new Double(2), result);
    }

    @Test
    public void parsesAdditionParentheses() {
        ParseTree tree = parseTreeForExpr("(2 + 3) * 5");
        ParseTreeVisitor<Double> evalVisitor = new StateEvalVisitor(EMPTY_EXECUTABLE_PETRI_NET, EMPTY_STATE);
        Double result = evalVisitor.visit(tree);
        assertEquals(new Double(25), result);
    }

    @Test
    public void parsesFloor() {
        ParseTree tree = parseTreeForExpr("floor(2.1)");
        ParseTreeVisitor<Double> evalVisitor = new StateEvalVisitor(EMPTY_EXECUTABLE_PETRI_NET, EMPTY_STATE);
        Double result = evalVisitor.visit(tree);
        assertEquals(new Double(2.0), result);
    }

    @Test
    public void parsesCeil() {
        ParseTree tree = parseTreeForExpr("ceil(2.1)");
        ParseTreeVisitor<Double> evalVisitor = new StateEvalVisitor(EMPTY_EXECUTABLE_PETRI_NET, EMPTY_STATE);
        Double result = evalVisitor.visit(tree);
        assertEquals(new Double(3.0), result);
    }

    @Test
    public void parsesPlaceTokenNumber() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .andFinally(APlace.withId("P0"));
        executablePetriNet = petriNet.getExecutablePetriNet();
        HashedStateBuilder builder = new HashedStateBuilder();
        builder.placeWithToken("P0", "Default", 4);
        State state = builder.build();
        ParseTreeVisitor<Double> evalVisitor = new StateEvalVisitor(executablePetriNet, state);
        ParseTree parseTree = parseTreeForExpr("#(P0)");
        Double result = evalVisitor.visit(parseTree);
        assertEquals(new Double(4.0), result);
    }

    @Test
    public void parsesPlaceColorTokenNumber() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .and(AToken.called("Red").withColor(Color.RED)).andFinally(APlace.withId("P0"));
        executablePetriNet = petriNet.getExecutablePetriNet();
        HashedStateBuilder builder = new HashedStateBuilder();
        builder.placeWithToken("P0", "Default", 4);
        builder.placeWithToken("P0", "Red", 6);
        State state = builder.build();
        ParseTreeVisitor<Double> evalVisitor = new StateEvalVisitor(executablePetriNet, state);
        ParseTree parseTree = parseTreeForExpr("#(P0, Red)");
        Double result = evalVisitor.visit(parseTree);
        assertEquals(new Double(6.0), result);
    }

    @Test
    public void parsesPlaceTokenNumberAsZeroIfDoesNotExist() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .andFinally(APlace.withId("P0"));
        executablePetriNet = petriNet.getExecutablePetriNet();
        HashedStateBuilder builder = new HashedStateBuilder();
        builder.placeWithToken("P0", "Default", 4);
        State state = builder.build();
        ParseTreeVisitor<Double> evalVisitor = new StateEvalVisitor(executablePetriNet, state);
        ParseTree parseTree = parseTreeForExpr("#(P1)");
        Double result = evalVisitor.visit(parseTree);
        assertEquals(new Double(0.0), result);
    }

    @Test
    public void parsesPlaceCapacity() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .andFinally(APlace.withId("P0").andCapacity(10));
        executablePetriNet = petriNet.getExecutablePetriNet();
        HashedStateBuilder builder = new HashedStateBuilder();
        builder.placeWithToken("P0", "Default", 4);
        State state = builder.build();
        ParseTreeVisitor<Double> evalVisitor = new StateEvalVisitor(executablePetriNet, state);
        ParseTree parseTree = parseTreeForExpr("cap(P0)");
        Double result = evalVisitor.visit(parseTree);
        assertEquals(new Double(10.0), result);
    }

    @Test
    public void parsesPlaceCapacityAsZeroIfDoesNotExist() throws PetriNetComponentException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK))
                .andFinally(APlace.withId("P0").andCapacity(10));
        executablePetriNet = petriNet.getExecutablePetriNet();
        HashedStateBuilder builder = new HashedStateBuilder();
        builder.placeWithToken("P0", "Default", 4);
        State state = builder.build();
        ParseTreeVisitor<Double> evalVisitor = new StateEvalVisitor(executablePetriNet, state);
        ParseTree parseTree = parseTreeForExpr("cap(P1)");
        Double result = evalVisitor.visit(parseTree);
        assertEquals(new Double(0.0), result);
    }
}