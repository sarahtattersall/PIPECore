package uk.ac.imperial.pipe.models.petrinet;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class UpdateMapEntryCommand<T> extends
        AbstractIncludeHierarchyCommand<UpdateResultEnum> {

    private static final String UPDATE_MAP_ENTRY_COMMAND = "UpdateMapEntryCommand:  ";
    private IncludeHierarchyMapEnum includeEnum;
    private String oldname;
    private String newname;
    private T value;
    private boolean force;

    public UpdateMapEntryCommand(IncludeHierarchyMapEnum includeEnum,
            String oldname, String newname, T value, boolean force) {
        this.includeEnum = includeEnum;
        this.oldname = oldname;
        this.newname = newname;
        this.value = value;
        this.force = force;
    }

    public UpdateMapEntryCommand(IncludeHierarchyMapEnum includeEnum,
            String oldname, String newname, T value) {
        this(includeEnum, oldname, newname, value, false);
    }

    public UpdateMapEntryCommand(IncludeHierarchyMapEnum includeEnum,
            String newname, T value, boolean force) {
        this(includeEnum, null, newname, value, force);
    }

    public UpdateMapEntryCommand(IncludeHierarchyMapEnum includeEnum,
            String newname, T value) {
        this(includeEnum, null, newname, value, false);
    }

    @Override
    public Result<UpdateResultEnum> execute(IncludeHierarchy includeHierarchy) {
        Map<String, T> map = includeHierarchy.getMap(includeEnum);
        //		Map<String, T> map = (Map<String, T>) includeEnum.getMap(includeHierarchy);
        String aliasForExistingInclude = getKeyForValue(map, value);
        T includeForNewAlias = map.get(newname);
        if ((includeForNewAlias == null) && (aliasForExistingInclude == null)) {
            map.put(newname, value);
        } else if (includeForNewAlias != null) {
            if (includeForNewAlias.equals(value)) {
                // no action; entry already exists
            } else if (!(includeForNewAlias.equals(value)) && (oldname == null)) { // (nameForExistingInclude == null)
                if (!force) {
                    result.addEntry(buildNotAddedMessage(includeHierarchy), UpdateResultEnum.NAME_ALREADY_EXISTS);
                } else {
                    map.remove(newname);
                    map.put(newname, value);
                }
            } else if (!(includeForNewAlias.equals(value)) && (oldname != null)) {
                if (!force) {
                    result.addEntry(buildNotRenamedMessage(includeHierarchy), UpdateResultEnum.NAME_ALREADY_EXISTS);
                } else {
                    map.remove(newname);
                    rename(map);
                }
            }
        } else if ((includeForNewAlias == null) && (aliasForExistingInclude != null)) {
            if (!aliasForExistingInclude.equals(oldname)) {
                if (!force) {
                    result.addEntry(buildNotRenamedDifferentOldNameMessage(includeHierarchy, aliasForExistingInclude), UpdateResultEnum.INCLUDE_EXISTS_UNDER_DIFFERENT_OLDNAME);
                } else {
                    map.remove(aliasForExistingInclude);
                    map.put(newname, value);
                }
            } else {
                rename(map);
            }
        }

        return result;
    }

    protected void rename(Map<String, T> map) {
        map.remove(oldname);
        map.put(newname, value);
    }

    private String buildNotRenamedDifferentOldNameMessage(
            IncludeHierarchy includeHierarchy, String aliasForExistingInclude) {
        return UPDATE_MAP_ENTRY_COMMAND + "no map entry found in " + includeEnum.getName() + " in IncludeHierarchy " +
                includeHierarchy.getName() + " for IncludeHierarchy with key " + oldname +
                ". TargetHierarchy exists under different key: " + aliasForExistingInclude +
                ".  Not renamed.  Probable logic error.";
    }

    private String buildNotRenamedMessage(IncludeHierarchy includeHierarchy) {
        return UPDATE_MAP_ENTRY_COMMAND + "map entry in " + includeEnum.getName() + " in IncludeHierarchy " +
                includeHierarchy.getName() +
                " for IncludeHierarchy with key " + oldname + " not renamed to " + newname +
                "; another entry by that name already exists.";
    }

    private String buildNotAddedMessage(IncludeHierarchy includeHierarchy) {
        return UPDATE_MAP_ENTRY_COMMAND + "map entry not added to " +
                includeEnum.getName() + " in IncludeHierarchy " + includeHierarchy.getName() +
                " because another entry already exists with key: " + newname;
    }

    private String getKeyForValue(Map<String, T> map,
            T value) {
        String key = null;
        Set<Entry<String, T>> entries = map.entrySet();
        for (Entry<String, T> entry : entries) {
            if (entry.getValue().equals(value))
                key = entry.getKey();
        }
        return key;
    }

}
