package uk.ac.imperial.pipe.models.petrinet;


public interface InterfacePlace extends Place {

	public static final String interfaceSuffix = "-I";

	public String getFullyQualifiedPrefix();

	public void setFullyQualifiedName(String fullyQualifiedName);
	/**
	 * 
	 * @return the {@link uk.ac.imperial.pipe.models.Place} corresponding to this InterfacePlace
	 */
	public Place getPlace();

}