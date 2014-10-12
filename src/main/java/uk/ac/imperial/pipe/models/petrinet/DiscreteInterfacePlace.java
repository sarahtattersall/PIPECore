package uk.ac.imperial.pipe.models.petrinet;


public class DiscreteInterfacePlace extends DiscretePlace implements InterfacePlace {

	private Place place;
	private InterfacePlaceStatus status;
	private String homeName;
	private String awayName;
	//TODO consider whether homeName is really needed in the ID
	public DiscreteInterfacePlace(DiscretePlace place, InterfacePlaceStatus status, String homeName, String awayName) {
		super(status.buildId(place.getId(),homeName, awayName), status.buildId(place.getId(),homeName, awayName));
		if (place instanceof DiscreteInterfacePlace) throw new IllegalArgumentException("InterfaceDiscretePlace:  an InterfacePlace cannot be constructed from another InterfacePlace, only from a DiscretePlace.");
		this.place = place; 
		this.homeName = homeName;
		this.awayName = awayName; 
		this.status = status;  
		status.setInterfacePlace(this); 
		setInInterface(true); 
		setInterfacePlace(this); 
		listenForTokenCountChanges(); 
	}

	public DiscreteInterfacePlace(DiscretePlace place, InterfacePlaceStatus status, String homeName) {
		this(place, status, homeName, null); 
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
	public void setHomeName(String homeName) {
		this.homeName = homeName; 
		setId(status.buildId(place.getId(), homeName, awayName)); 
		setName(status.buildId(place.getId(), homeName, awayName)); 
	}

	@Override
	public void setAwayName(String awayName) {
		this.awayName = awayName; 
		setId(status.buildId(place.getId(), homeName, awayName)); 
		setName(status.buildId(place.getId(), homeName, awayName)); 
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
			Result<InterfacePlaceAction> result = status.use(); 
			if (result.hasResult()) {
				useResult = false;
			}
			else {
				useResult = true;
				status = status.nextStatus(); 
			}
		}
		return useResult; 
	}
	@Override
	public boolean canUse() {
		return status.canUse();
	}

	@Override
	public boolean remove() {
		boolean removeResult = status.isInUse(); 
		if (removeResult) {
			Result<InterfacePlaceAction> result = status.remove(); 
			if (result.hasResult()) {
				removeResult = false;
			}
			else {
				removeResult = true;
				status = status.nextStatus(); 
			}
		}
		return removeResult; 
	}

	@Override
	public boolean isInUse() {
		return status.isInUse();
	}

	@Override
	public boolean isHome() {
		return status.isHome();
	}
	@Override
	public void setInInterface(boolean inInterface) {
		super.setInInterface(inInterface);
		place.setInInterface(inInterface); 
	}
	@Override
	public void setInterfacePlace(InterfacePlace interfacePlace) {
		status.setInterfacePlace(interfacePlace); 
		super.setInterfacePlace(interfacePlace);
//		place.setInterfacePlace(interfacePlace); 
	}
}
