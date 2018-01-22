package uk.ac.imperial.pipe.dsl;

import uk.ac.imperial.pipe.models.petrinet.*;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * DSL for creating normal Petri net arcs, to be used in conjunction wiht {@link uk.ac.imperial.pipe.dsl.APetriNet}
 *
 * Usage:
 * ANormalArc.withSource("P0").andTarget("T0").with("5", "Red").tokens().and("2", "Blue").tokens()
 */
public final class ANormalArc implements DSLCreator<Arc<? extends Connectable, ? extends Connectable>> {
    /**
     * String source
     */
    private String source;

    /**
     * String target
     */
    private String target;

    /**
     * Map token id -> functional expression
     */
    private Map<String, String> weights = new HashMap<>();

    /**
     * intermediate arc points
     */
    private List<ArcPoint> intermediatePoints = new LinkedList<>();

    /**
     * Private constructor
     */
    private ANormalArc() {
    }

    /**
     * Factory constructor
     * @param source arc source id
     * @return builder for chaining
     */
    public static ANormalArc withSource(String source) {
        ANormalArc aNormalArc = new ANormalArc();
        aNormalArc.source = source;
        return aNormalArc;
    }

    /**
     *
     * @param target target id
     * @return builder for chaining
     */
    public ANormalArc andTarget(String target) {
        this.target = target;
        return this;
    }

    /**
     * Method for creating tokens
     * E.g. with("5", "Red").tokens()
     * @param tokenWeight for the token
     * @param tokenName for the token
     * @return a builder for chaining 
     */
    public ANormalArc with(String tokenWeight, String tokenName) {
        weights.put(tokenName, tokenWeight);
        return this;
    }

    /**
     * Added for readability, same as with method
     * @param tokenWeight for the token
     * @param tokenName for the token
     * @return a builder for chaining 
     */
    public ANormalArc and(String tokenWeight, String tokenName) {
        return with(tokenWeight, tokenName);
    }

    /**
     *
     * @param tokens map of created tokens with id of Token
     * @param places map of created places with id of Connectable
     * @param transitions map of created transitions with id of Transition
     * @param rateParameters map of created rateParameters with id of rateParameter
     * @return new inbound/outbound arc depending on if the soure was a place/transition
     */
    @Override
    public Arc<? extends Connectable, ? extends Connectable> create(Map<String, Token> tokens,
            Map<String, Place> places,
            Map<String, Transition> transitions, Map<String, FunctionalRateParameter> rateParameters) {
        Arc<? extends Connectable, ? extends Connectable> arc;
        if (places.containsKey(source)) {
            arc = new InboundNormalArc(places.get(source), transitions.get(target), weights);
        } else {
            arc = new OutboundNormalArc(transitions.get(source), places.get(target), weights);
        }
        arc.addIntermediatePoints(intermediatePoints);
        return arc;
    }

    /**
     *
     * Noop action to aid readability
     *
     * @return builder for chaining
     */
    public ANormalArc tokens() {
        return this;
    }

    /**
     *
     * Noop action to aid readability
     *
     * @return builder for chaining
     */
    public ANormalArc token() {
        return this;
    }

    /**
     *
     * Registers a straight intermediate point at (x,y)
     * @param x coordinate
     * @param y coordinate
     * @return builder for chaining
     */
    public ANormalArc andIntermediatePoint(int x, int y) {
        intermediatePoints.add(new ArcPoint(new Point2D.Double(x, y), false));
        return this;
    }

    /**
     * Registers a curved intermedaite point at (x,y)
     * @param x coordinate
     * @param y coordinate
     * @return builder for chaining
     */
    public ANormalArc andACurvedIntermediatePoint(int x, int y) {
        intermediatePoints.add(new ArcPoint(new Point2D.Double(x, y), true));
        return this;
    }
}
