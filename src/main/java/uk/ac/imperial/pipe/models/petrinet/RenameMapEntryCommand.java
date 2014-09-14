package uk.ac.imperial.pipe.models.petrinet;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class RenameMapEntryCommand<T> extends AbstractIncludeHierarchyCommand<T> {

	private IncludeHierarchyMapEnum includeEnum;
	private String oldname;
	private String newname;
	private IncludeHierarchy include;

	public RenameMapEntryCommand(IncludeHierarchyMapEnum includeEnum,
			String oldname, String newname, IncludeHierarchy include) {
		this.includeEnum = includeEnum; 
		this.oldname = oldname;
		this.newname = newname; 
		this.include = include; 
	}

	@Override
	public Result<T> execute(IncludeHierarchy includeHierarchy) {
		Map<String, IncludeHierarchy> map = includeEnum.getMap(includeHierarchy);
		if (map.containsKey(newname)) {
			result.addMessage("RenameMapEntryCommand:  map entry in "+includeEnum.getName()+" in IncludeHierarchy "+includeHierarchy.getName()+
					" for IncludeHierarchy with key "+oldname+" not renamed to "+newname+"; another entry by that name already exists.");
		}
		else {
			IncludeHierarchy child = map.get(oldname);
		    if (child != null) { 
				map.remove(oldname); 
				map.put(newname, include); 
		    }
		    else {
		    	if (map.containsValue(include)) {
		    		String key = getKeyForValue(map, include);
		    		result.addMessage("RenameMapEntryCommand:  no map entry found in "+includeEnum.getName()+" in IncludeHierarchy "+
		    		includeHierarchy.getName()+" for IncludeHierarchy with key "+oldname+ 
				    ". TargetHierarchy exists under different key: "+key+".  Not renamed.  Probable logic error.");
		    	}
		    	else {
		    		result.addMessage("RenameMapEntryCommand:  no map entry found in "+includeEnum.getName()+" in IncludeHierarchy "+
		    	    includeHierarchy.getName()+" for IncludeHierarchy with key "+oldname+
		    				". Target IncludeHierarchy does not exist under any name.  Not renamed.  Probable logic error.");
		    	}
		    }
		}

		return result;
	}

	private String getKeyForValue(Map<String, IncludeHierarchy> map,
			IncludeHierarchy include) {
		String key = null; 
		Set<Entry<String, IncludeHierarchy>> entries = map.entrySet(); 
		for (Entry<String, IncludeHierarchy> entry : entries) {
			if (entry.getValue().equals(include)) key = entry.getKey(); 
		}
		return key;
	}

}
