package uk.ac.imperial.pipe.dsl;

import uk.ac.imperial.pipe.models.component.Connectable;
import uk.ac.imperial.pipe.models.component.arc.*;
import uk.ac.imperial.pipe.models.component.place.Place;
import uk.ac.imperial.pipe.models.component.rate.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.component.token.Token;
import uk.ac.imperial.pipe.models.component.transition.Transition;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ANormalArc implements DSLCreator<Arc<? extends Connectable, ? extends Connectable>> {
    private String source;

    private String target;

    private Map<String, String> weights = new HashMap<>();

    private List<ArcPoint> intermediatePoints = new LinkedList<>();


    private ANormalArc() {
    }

    public static ANormalArc withSource(String source) {
        ANormalArc aNormalArc = new ANormalArc();
        aNormalArc.source = source;
        return aNormalArc;
    }

    public ANormalArc andTarget(String target) {
        this.target = target;
        return this;
    }

    /**
     * Method for creating tokens
     * E.g. with("5", "Red").tokens()
     * @param tokenWeight
     * @param tokenName
     * @return
     */
    public ANormalArc with(String tokenWeight, String tokenName) {
        weights.put(tokenName, tokenWeight);
        return this;
    }

    /**
     * Added for readability, same as with method
     * @param tokenWeight
     * @param tokenName
     * @return
     */
    public ANormalArc and(String tokenWeight, String tokenName) {
        return with(tokenWeight, tokenName);
    }

    @Override
    public Arc<? extends Connectable, ? extends Connectable> create(Map<String, Token> tokens,
                                                                    Map<String, Place> places,
                                                                    Map<String, Transition> transitions, Map<String, FunctionalRateParameter> rateParameters) {
        Arc<? extends Connectable, ? extends Connectable> arc;
        if(places.containsKey(source)){
            arc = new InboundNormalArc(places.get(source), transitions.get(target), weights);
        } else {
            arc = new OutboundNormalArc(transitions.get(source), places.get(target), weights);
        }
        arc.addIntermediatePoints(intermediatePoints);
        return arc;
    }

    public ANormalArc tokens() {
        return this;
    }

    public ANormalArc token() {
        return this;
    }

    public ANormalArc andIntermediatePoint(int x, int y) {
        intermediatePoints.add(new ArcPoint(new Point2D.Double(x, y), false));
        return this;
    }

    public ANormalArc andACurvedIntermediatePoint(int x, int y) {
        intermediatePoints.add(new ArcPoint(new Point2D.Double(x, y), true));
        return this;
    }
}


