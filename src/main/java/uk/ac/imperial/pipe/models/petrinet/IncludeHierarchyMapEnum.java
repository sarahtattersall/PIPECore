package uk.ac.imperial.pipe.models.petrinet;

import java.util.Map;

public enum IncludeHierarchyMapEnum {
	INCLUDE { public Map<String, IncludeHierarchy> getMap(IncludeHierarchy includes) {
			return includes.getIncludeMap(); }
			public String getName() { return INCLUDE_MAP; } },
	INCLUDE_ALL { public Map<String, IncludeHierarchy> getMap(IncludeHierarchy includes) {
			return includes.getIncludeMapAll(); }
			public String getName() { return INCLUDE_MAP_ALL; } };

	private static final String INCLUDE_MAP = "IncludeMap";
	private static final String INCLUDE_MAP_ALL = "IncludeMapAll";

	public abstract Map<String, IncludeHierarchy> getMap(IncludeHierarchy includes);

	public abstract String getName();

}
