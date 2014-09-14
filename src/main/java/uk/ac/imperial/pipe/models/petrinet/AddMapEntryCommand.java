package uk.ac.imperial.pipe.models.petrinet;

import java.util.Map;

public class AddMapEntryCommand<T> extends AbstractIncludeHierarchyCommand<T> {

	private IncludeHierarchyMapEnum includeEnum;
	private String alias;
	private IncludeHierarchy include;

	public AddMapEntryCommand(IncludeHierarchyMapEnum includeEnum, String alias, IncludeHierarchy include) {
		this.includeEnum = includeEnum; 
		this.alias = alias; 
		this.include = include; 
	}

	@Override
	public Result<T> execute(IncludeHierarchy includeHierarchy) {
		Map<String, IncludeHierarchy> map = includeEnum.getMap(includeHierarchy);
		if (map.containsKey(alias)) {
			result.addMessage("AddMapEntryCommand:  map entry for IncludeHierarchy "+include.getName()+" not added to "+
			includeEnum.getName()+" in IncludeHierarchy "+includeHierarchy.getName()+" because another entry already exists with key: "+alias); 
		}
		else {
			map.put(alias, include); 
		}
		return result;
	}

}
