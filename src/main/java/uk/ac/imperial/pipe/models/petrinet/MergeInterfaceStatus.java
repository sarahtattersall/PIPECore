package uk.ac.imperial.pipe.models.petrinet;

public interface MergeInterfaceStatus extends InterfaceStatus {

	public Place getHomePlace();

	public String getAwayId();

	public Result<InterfacePlaceAction> add(PetriNet petriNet);


}
