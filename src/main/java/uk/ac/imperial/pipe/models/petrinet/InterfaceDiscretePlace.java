package uk.ac.imperial.pipe.models.petrinet;


public class InterfaceDiscretePlace extends DiscretePlace implements InterfacePlace {

	private Place place;
	private String fullyQualifiedName;

	public InterfaceDiscretePlace(Place place) {
		super(addInterfaceSuffix(place.getId()), addInterfaceSuffix(place.getName()));
		if (place instanceof InterfaceDiscretePlace) throw new IllegalArgumentException("InterfaceDiscretePlace:  an InterfacePlace cannot be constructed from another InterfacePlace, only from a DiscretePlace.");
		this.place = place; 
		listenForTokenCountChanges(); 
	}

	private void listenForTokenCountChanges() {
		this.addPropertyChangeListener(place); 
		place.addPropertyChangeListener(this); 
	}

	private static String addInterfaceSuffix(String place) {
		return place+interfaceSuffix;
	}

	/* (non-Javadoc)
	 * @see uk.ac.imperial.pipe.models.InterfacePlace#getFullyQualifiedPrefix()
	 */
	@Override
	public String getFullyQualifiedPrefix() {
		return fullyQualifiedName;
	}

	/* (non-Javadoc)
	 * @see uk.ac.imperial.pipe.models.InterfacePlace#setFullyQualifiedName(java.lang.String)
	 */
	@Override
	public void setFullyQualifiedName(String fullyQualifiedName) {
		this.fullyQualifiedName = fullyQualifiedName;
	}

	@Override
	public Place getPlace() {
		return place;
	}
	
}
