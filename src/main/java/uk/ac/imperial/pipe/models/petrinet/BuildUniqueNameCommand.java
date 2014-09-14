package uk.ac.imperial.pipe.models.petrinet;

public class BuildUniqueNameCommand<T> extends AbstractIncludeHierarchyCommand<T> {

	public static final String BUILD_UNIQUE_NAME = "BuildUniqueNameCommand: "; 
	public static final String ONLY_INVOKED_FOR_SAME_INCLUDE_HIERARCHY = BUILD_UNIQUE_NAME+"Build unique name should only be invoked once, for the include hierarchy whose unique name is to be built.  " +
			"Expected: ";
	private String name;

	public BuildUniqueNameCommand() {
	}
	@Override
	public Result<T> execute(IncludeHierarchy includeHierarchy) {
		init(includeHierarchy); 
		IncludeHierarchy conflict = includeHierarchy.getRoot().getIncludeMapAll().get(name);  
		if (conflict == null) {
			addUniqueNameToSelfAndParentsMaps(includeHierarchy, name);
		} else {
			if (this.equals(conflict)) {
				if (!name.equals(includeHierarchy.getUniqueName())) {
					System.out.println("here");
					addUniqueNameToSelfAndParentsMaps(includeHierarchy, name);
				}
			}
			else if (includeHierarchy.hasParent(conflict)) {
				IncludeHierarchy parent = includeHierarchy.getParent(); 
				String tempName = name; 
				while (parent != null) {
					tempName = parent.getName()+"."+tempName; 
					if (parent.equals(conflict)) {
						includeHierarchy.setUniqueName(tempName); 
					}
					parent = parent.getParent(); 
				}
				addUniqueNameToSelfAndParentsMaps(includeHierarchy, includeHierarchy.getUniqueName());
			}
			else if (conflict.hasParent(includeHierarchy)) {
				// no action required; handled by rename. 
			}
			else if (includeHierarchy.lowerLevelInHierarchyThanOther(conflict)) {
				addUniqueNameToSelfAndParentsMaps(includeHierarchy, includeHierarchy.getFullyQualifiedName());
			}
			else if (includeHierarchy.higherLevelInHierarchyThanOther(conflict)) {
				includeHierarchy.getRoot().all(this); // start over and the other guy will use fully qualified name
			}
		}
		includeHierarchy.buildUniqueNameAsPrefix(); 
		return result;
	}
	protected void addUniqueNameToSelfAndParentsMaps(IncludeHierarchy includeHierarchy, String uniqueName) {
		includeHierarchy.setUniqueName(uniqueName); 
		IncludeHierarchyCommand<Object> addEntryCommand = new AddMapEntryCommand<>(IncludeHierarchyMapEnum.INCLUDE_ALL, uniqueName, includeHierarchy);
		Result<Object> addResult = includeHierarchy.parents(addEntryCommand);
		addResult = includeHierarchy.self(addEntryCommand); 
		if (addResult.hasResult()) throw new RuntimeException(addResult.getMessage());
	}
	private void init(
			IncludeHierarchy includeHierarchy) {
		super.validate(includeHierarchy); 
		this.name = includeHierarchy.getName(); 
		includeHierarchy.initIncludeMapAll(); 
	}
}
