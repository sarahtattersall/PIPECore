package uk.ac.imperial.pipe.models.petrinet;

public class NoArcConstraint implements ArcConstraint {

    @Override
    public boolean acceptInboundArc() {
        return true;
    }

    @Override
    public boolean acceptOutboundArc() {
        return true;
    }

}
