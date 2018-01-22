package uk.ac.imperial.pipe.parsers;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.AbstractPetriNet;
import uk.ac.imperial.pipe.models.petrinet.Place;

/**
 * Evaluates a functional expression
 */
public final class EvalVisitor extends RateGrammarBaseVisitor<Double> {
    /**
     * Underlying Petri net
     */
    private AbstractPetriNet abstractPetriNet;

    /**
     * Constructor for evaluating expressions that contain petri net 
     * components, i.e places
     * @param abstractPetriNet for this visitor 
     */
    public EvalVisitor(AbstractPetriNet abstractPetriNet) {
        this.abstractPetriNet = abstractPetriNet;
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
        try {
            Place place = getPlace(ctx.ID().getText());
            return (double) place.getNumberOfTokensStored();
        } catch (PetriNetComponentNotFoundException ignored) {
            return 0.0;
        }
    }

    @Override
    public Double visitToken_color_number(RateGrammarParser.Token_color_numberContext ctx) {
        String name = ctx.ID().get(0).getText();
        String color = ctx.ID().get(1).getText();
        try {
            Place place = getPlace(name);
            return (double) place.getTokenCount(color);
        } catch (PetriNetComponentNotFoundException ignored) {
            return 0.0;
        }
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

    /**
     *
     * @param id of the place
     * @return place for id
     * @throws PetriNetComponentNotFoundException if place is not found in the Petri net
     */
    public Place getPlace(String id) throws PetriNetComponentNotFoundException {
        return abstractPetriNet.getComponent(id, Place.class);
    }

}
