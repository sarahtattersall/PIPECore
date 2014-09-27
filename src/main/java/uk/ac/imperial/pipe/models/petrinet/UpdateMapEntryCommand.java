package uk.ac.imperial.pipe.models.petrinet;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class UpdateMapEntryCommand extends
		AbstractIncludeHierarchyCommand<UpdateResultEnum>  {

	private static final String UPDATE_MAP_ENTRY_COMMAND = "UpdateMapEntryCommand:  ";
	private IncludeHierarchyMapEnum includeEnum;
	private String oldname;
	private String newname;
	private IncludeHierarchy include;
	private boolean force;

	public UpdateMapEntryCommand(IncludeHierarchyMapEnum includeEnum,
			String oldname, String newname, IncludeHierarchy include, boolean force) {
		this.includeEnum = includeEnum; 
		this.oldname = oldname;
		this.newname = newname; 
		this.include = include; 
		this.force = force; 
	}
	public UpdateMapEntryCommand(IncludeHierarchyMapEnum includeEnum,
			String oldname, String newname, IncludeHierarchy include) {
		this(includeEnum, oldname, newname, include, false); 
	}
	public UpdateMapEntryCommand(IncludeHierarchyMapEnum includeEnum,
			String newname, IncludeHierarchy include, boolean force) {
		this(includeEnum, null, newname, include, force); 
	}
	public UpdateMapEntryCommand(IncludeHierarchyMapEnum includeEnum,
			String newname, IncludeHierarchy include) {
		this(includeEnum, null, newname, include, false); 
	}
	
	@Override
	public Result<UpdateResultEnum> execute(IncludeHierarchy includeHierarchy) {
		Map<String, IncludeHierarchy> map = includeEnum.getMap(includeHierarchy);
		String aliasForExistingInclude = getKeyForValue(map, include); 
		IncludeHierarchy includeForNewAlias = map.get(newname);
		if ((includeForNewAlias == null) && (aliasForExistingInclude == null)) {
			map.put(newname, include);
		}
		else if (includeForNewAlias != null) {
			if (includeForNewAlias.equals(include)) {
				// no action; entry already exists 
			}
			else if (!(includeForNewAlias.equals(include)) && (oldname == null)) {  // (nameForExistingInclude == null)
				if (!force) {
					result.addEntry(buildNotAddedMessage(includeHierarchy), UpdateResultEnum.NAME_ALREADY_EXISTS); 
				} 
				else {
					map.remove(newname);
					map.put(newname, include);
				}
			}
			else if (!(includeForNewAlias.equals(include)) && (oldname != null)) {
				if (!force) {
					result.addEntry(buildNotRenamedMessage(includeHierarchy), UpdateResultEnum.NAME_ALREADY_EXISTS); 
				}
				else {
					map.remove(newname);
					rename(map); 
				}
			}
		}
		else if ((includeForNewAlias == null) && (aliasForExistingInclude != null)) {
			if (!aliasForExistingInclude.equals(oldname)) {
				if (!force) {
					result.addEntry(buildNotRenamedDifferentOldNameMessage(includeHierarchy,aliasForExistingInclude),
							UpdateResultEnum.INCLUDE_EXISTS_UNDER_DIFFERENT_OLDNAME);
				} 
				else {
					map.remove(aliasForExistingInclude);
					map.put(newname, include);
				}
			}
			else {
				rename(map); 
			}
		}

		return result;
	}
	protected void rename(Map<String, IncludeHierarchy> map) {
		map.remove(oldname); 
		map.put(newname, include);
	}
	private String buildNotRenamedDifferentOldNameMessage(
			IncludeHierarchy includeHierarchy, String aliasForExistingInclude) {
		return UPDATE_MAP_ENTRY_COMMAND+"no map entry found in "+includeEnum.getName()+" in IncludeHierarchy "+
				includeHierarchy.getName()+" for IncludeHierarchy with key "+oldname+ 
				". TargetHierarchy exists under different key: "+aliasForExistingInclude+".  Not renamed.  Probable logic error.";
	}
	private String buildNotRenamedMessage(IncludeHierarchy includeHierarchy) {
		return UPDATE_MAP_ENTRY_COMMAND+"map entry in "+includeEnum.getName()+" in IncludeHierarchy "+includeHierarchy.getName()+
				" for IncludeHierarchy with key "+oldname+" not renamed to "+newname+"; another entry by that name already exists.";
	}
	private String buildNotAddedMessage(IncludeHierarchy includeHierarchy) {
		return UPDATE_MAP_ENTRY_COMMAND+"map entry for IncludeHierarchy "+include.getName()+" not added to "+
				includeEnum.getName()+" in IncludeHierarchy "+includeHierarchy.getName()+" because another entry already exists with key: "+newname;
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
