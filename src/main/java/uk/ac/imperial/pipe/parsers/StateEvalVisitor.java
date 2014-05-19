package uk.ac.imperial.pipe.parsers;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.component.place.Place;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.state.State;

import java.util.Map;

/**
 * This class evaluates an expression based on a State
 */
public class StateEvalVisitor extends RateGrammarBaseVisitor<Double> {
    private final PetriNet petriNet;
    private final State state;

    public StateEvalVisitor(PetriNet petriNet, State state) {
        this.petriNet = petriNet;
        this.state = state;
    }

    @Override
    public Double visitMultOrDiv(RateGrammarParser.MultOrDivContext ctx) {
        Double left = visit(ctx.expression(0));
        Double right = visit(ctx.expression(1));
        return (ctx.op.getType() == RateGrammarParser.MUL) ? left * right : left / right;
    }

    @Override
    public Double visitAddOrSubtract(RateGrammarParser.AddOrSubtractContext ctx) {
        Double left = visit(ctx.expression(0));
        Double right = visit(ctx.expression(1));
        return (ctx.op.getType() == RateGrammarParser.ADD) ? left + right : left - right;
    }

    @Override
    public Double visitParenExpression(RateGrammarParser.ParenExpressionContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public Double visitToken_number(RateGrammarParser.Token_numberContext ctx) {
        String name = ctx.ID().getText();
        double count = 0;
        if (!state.containsTokens(name)) {
            return 0.0;
        }
        for (Integer value : state.getTokens(name).values()) {
            count += value;
        }
        return count;
    }

    @Override
    public Double visitToken_color_number(RateGrammarParser.Token_color_numberContext ctx) {
        String name = ctx.ID().get(0).getText();
        String color = ctx.ID().get(1).getText();
        Map<String, Integer> tokens = state.getTokens(name);
        return (double) tokens.get(color);
    }

    @Override
    public Double visitCapacity(RateGrammarParser.CapacityContext ctx) {
        try {
            Place place = getPlace(ctx.ID().getText());
            return (double) place.getCapacity();
        } catch (PetriNetComponentNotFoundException ignored) {
            return 0.0;
        }
    }

    @Override
    public Double visitInteger(RateGrammarParser.IntegerContext ctx) {
        return Double.valueOf(ctx.INT().getText());
    }

    @Override
    public Double visitDouble(RateGrammarParser.DoubleContext ctx) {
        return Double.valueOf(ctx.DOUBLE().getText());
    }

    @Override
    public Double visitFloor(RateGrammarParser.FloorContext ctx) {
        Double value = visit(ctx.expression());
        return Math.floor(value);
    }

    @Override
    public Double visitCeil(RateGrammarParser.CeilContext ctx) {
        Double value = visit(ctx.expression());
        return Math.ceil(value);
    }


    public Place getPlace(String name) throws PetriNetComponentNotFoundException {
        return petriNet.getComponent(name, Place.class);
    }

}
