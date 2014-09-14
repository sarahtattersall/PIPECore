package uk.ac.imperial.pipe.models.petrinet;

public class RenameIncludeAliasCommand<T> extends AbstractIncludeHierarchyCommand<T>  {

	public static final String INCLUDE_HIERARCHY_ATTEMPTED_RENAME_AT_LEVEL = "IncludeHierarchy attempted rename at level ";
	public static final String WOULD_CAUSE_DUPLICATE = " would cause duplicate: ";
	private String oldname;
	private String newname;

	public RenameIncludeAliasCommand(String oldname, String newname) {
		this.oldname = oldname; 
		this.newname = newname; 
	}

	@Override
	public Result<T> execute(IncludeHierarchy includeHierarchy) {
		boolean renamed = includeHierarchy.renameChildAlias(oldname, newname); 
		if (!renamed) {
			result.addMessage(INCLUDE_HIERARCHY_ATTEMPTED_RENAME_AT_LEVEL + 
					includeHierarchy.getName() + WOULD_CAUSE_DUPLICATE + newname); 
		}
		else {
			includeHierarchy.buildFullyQualifiedName(); 
		}
		return result;
	}

}
//buildFullyQualifiedName(); 
//rebuildMinimallyUniqueName();
//if (parent != null) { 
//	parent.renameChildAlias(oldName, newName);
//}
//notifyChildren(newName, oldName);
