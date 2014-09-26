package uk.ac.imperial.pipe.models.petrinet;


public class DiscreteInterfacePlace extends DiscretePlace implements InterfacePlace {

	private Place place;
	private String fullyQualifiedName;
	private InterfacePlaceStatus status;
	private String homeAlias;
	private String awayAlias;

	public DiscreteInterfacePlace(DiscretePlace place, String homeAlias,
			String awayAlias) {
		this(place, InterfacePlaceStatusEnum.HOME, homeAlias, awayAlias); 
//		super(buildId(place.getId(),homeAlias, awayAlias), buildId(place.getId(),homeAlias, awayAlias));
//		if (place instanceof DiscreteInterfacePlace) throw new IllegalArgumentException("InterfaceDiscretePlace:  an InterfacePlace cannot be constructed from another InterfacePlace, only from a DiscretePlace.");
//		this.place = place; 
//		this.homeAlias = homeAlias;
//		this.awayAlias = awayAlias; 
//		place.setInInterface(true);
//		listenForTokenCountChanges(); 
	}

	public DiscreteInterfacePlace(DiscretePlace place, String homeAlias) {
		this(place, homeAlias, null); 
	}

	public DiscreteInterfacePlace(DiscretePlace place, InterfacePlaceStatusEnum status, String homeAlias) {
		this(place, status, homeAlias, null); 
	}
	public DiscreteInterfacePlace(DiscretePlace place,
			InterfacePlaceStatusEnum status, String homeAlias, String awayAlias) {
		super(buildId(place.getId(),homeAlias, awayAlias), buildId(place.getId(),homeAlias, awayAlias));
		if (place instanceof DiscreteInterfacePlace) throw new IllegalArgumentException("InterfaceDiscretePlace:  an InterfacePlace cannot be constructed from another InterfacePlace, only from a DiscretePlace.");
		this.place = place; 
		this.homeAlias = homeAlias;
		this.awayAlias = awayAlias; 
		this.status = status.buildStatus();  
		place.setInInterface(true);
		listenForTokenCountChanges(); 
	}

	private static String buildId(String id, String homeAlias, String awayAlias) {
		return (awayAlias == null) ? homeAlias+"."+id : awayAlias+".."+homeAlias+"."+id;
	}

	private void listenForTokenCountChanges() {
		this.addPropertyChangeListener(place); 
		place.addPropertyChangeListener(this); 
	}


	@Override
	public Place getPlace() {
		return place;
	}


	@Override
	public void setHomeAlias(String homeAlias) {
		this.homeAlias = homeAlias; 
		setId(buildId(place.getId(), homeAlias, awayAlias)); 
		setName(buildId(place.getId(), homeAlias, awayAlias)); 
	}

	@Override
	public void setAwayAlias(String awayAlias) {
		this.awayAlias = awayAlias; 
		setId(buildId(place.getId(), homeAlias, awayAlias)); 
		setName(buildId(place.getId(), homeAlias, awayAlias)); 
	}
	//TODO should probably be protected; status should be set at construction and modified by use(boolean)
	@Override
	public void setStatus(InterfacePlaceStatus status) {
		this.status = status; 
	}

	@Override
	public InterfacePlaceStatus getStatus() {
		return status;
	}

	@Override
	public boolean use() {
		boolean useResult = status.canUse(); 
		if (useResult) {
			status = status.use(); 
		}
		return useResult; 
	}
	@Override
	public boolean canUse() {
		return status.canUse();
	}

	@Override
	public boolean canRemove() {
		return status.canRemove();
	}

	@Override
	public boolean remove() {
		boolean removeResult = status.canRemove(); 
		if (removeResult) {
			status = status.remove(); 
		}
		return removeResult; 
	}
	
}
