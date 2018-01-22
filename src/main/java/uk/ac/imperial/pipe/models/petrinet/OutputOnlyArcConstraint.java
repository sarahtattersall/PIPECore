package uk.ac.imperial.pipe.models.petrinet;

public class OutputOnlyArcConstraint implements ArcConstraint {

    @Override
    public boolean acceptInboundArc() {
        return false;
    }

    @Override
    public boolean acceptOutboundArc() {
        return true;
    }

}
