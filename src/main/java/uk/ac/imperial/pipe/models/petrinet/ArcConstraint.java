package uk.ac.imperial.pipe.models.petrinet;

public interface ArcConstraint {

    public boolean acceptInboundArc();

    public boolean acceptOutboundArc();

}
