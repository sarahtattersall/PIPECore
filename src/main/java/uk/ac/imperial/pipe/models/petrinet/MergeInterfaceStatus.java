package uk.ac.imperial.pipe.models.petrinet;

public interface MergeInterfaceStatus extends InterfaceStatus {

	public Place getHomePlace();
	public void setHomePlace(Place homePlace);

	public String getAwayId();

	public Result<InterfacePlaceAction> add(PetriNet petriNet);

	public boolean canRemove();


}
