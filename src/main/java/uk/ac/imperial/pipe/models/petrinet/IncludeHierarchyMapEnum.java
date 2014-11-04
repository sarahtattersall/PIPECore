package uk.ac.imperial.pipe.models.petrinet;


public enum IncludeHierarchyMapEnum {
	INCLUDE { public String getName() { return "IncludeMap"; } },
	INCLUDE_ALL { public String getName() { return "IncludeMapAll"; } }, 
	PLACES_IN_INTERFACE { public String getName() { return "PlacesInInterface"; } };

	public abstract String getName();

}
