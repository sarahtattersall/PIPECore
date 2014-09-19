package uk.ac.imperial.pipe.models.petrinet;

public class BuildUniqueNameCommand<T> extends AbstractIncludeHierarchyCommand<T> {

	public static final String BUILD_UNIQUE_NAME = "BuildUniqueNameCommand: "; 

	private String name;
	private IncludeHierarchy conflictingInclude;
	private String currentUniqueName;
	private IncludeHierarchy includeHierarchy;
	private int nameType;
	private boolean controller;
	private boolean finished;
	private static final int shortName = 1;
	private static final int minimalName = 2;
	private static final int fullyQualifiedName = 3;

	public BuildUniqueNameCommand() {
		this(true); 
		finished = false; 
	}
	private BuildUniqueNameCommand(boolean controller) {
		this.controller = controller; 
	}
	@SuppressWarnings("unchecked")
	@Override
	public Result<T> execute(IncludeHierarchy includeHierarchy) {
		if (controller) {
			BuildUniqueNameCommand<IncludeHierarchy> command = null;  
			while (!finished) {
				command = new BuildUniqueNameCommand<>(false); 
				Result<IncludeHierarchy> buildResult = includeHierarchy.getRoot().all(command); 
				if (!buildResult.hasResult()) {
					finished = true; 
				}
			}
		}
		else {
			init(includeHierarchy); 
			String newUniqueName = buildUniqueName(); 
			if (newUniqueName != null) {
				updateUniqueNameInSelfAndParentsMaps(includeHierarchy, newUniqueName);
				result.addEntry(newUniqueName, (T) includeHierarchy); 
			}
		}
		return result;
	}
	private String buildUniqueName() {
		String uniqueName = null;
		String targetName = buildTargetName();
		uniqueName = (targetName.equals(currentUniqueName)) ? null : targetName ;
		return uniqueName;
	}
	protected String buildTargetName() {
		conflictingInclude = includeHierarchy.getRoot().getIncludeMapAll().get(name); 
		String targetName = null;
		boolean done = false;  
		nameType = shortName; 
		while (!done) {
			switch (nameType) {
			case shortName: 
				targetName = includeHierarchy.getName(); 
				if ((noConflict() || self() || parent() || aunt())) {
					done = true; 
				}
				else {
					nameType = minimalName; 
				}
				break; 
			case minimalName:  
				if (child()) {
					targetName = buildMinimalName(); 
					done = true; 
				}
				else {
					nameType = fullyQualifiedName; 
				}
				break; 
			case fullyQualifiedName:
				if (niece()) {
					targetName = includeHierarchy.getFullyQualifiedName(); 
					done = true; 
				}
				else {
					throw new RuntimeException(BUILD_UNIQUE_NAME+"logic error in building unique name; no relation found.");
				}
				break; 
			}
		}
		return targetName; 
	}
	private String buildMinimalName() {
		IncludeHierarchy parent = includeHierarchy.getParent(); 
		String minimalName = null; 
		String tempName = name; 
		while (parent != null) {
			tempName = parent.getName()+"."+tempName; 
			if (parent.equals(conflictingInclude)) {
				minimalName = tempName; 
			}
			parent = parent.getParent(); 
		}
		if (minimalName == null) throw new RuntimeException(BUILD_UNIQUE_NAME+"buildMinimalName logic error; minimal name should not be null.  Temp name: "+tempName);
		return minimalName;
	}
	private boolean noConflict() {
		return (conflictingInclude == null);
	}
	private boolean self() {
		return (includeHierarchy.equals(conflictingInclude));
	}
	private boolean parent() {
		return (conflictingInclude.hasParent(includeHierarchy));
	}
	private boolean child() {
		return (includeHierarchy.hasParent(conflictingInclude));
	}
	private boolean aunt() {
		return (includeHierarchy.higherLevelInHierarchyThanOther(conflictingInclude));
	}
	private boolean niece() {
		return (conflictingInclude.higherLevelInHierarchyThanOther(includeHierarchy));
	}
	private void init(
			IncludeHierarchy includeHierarchy) {
		super.validate(includeHierarchy); 
		this.includeHierarchy = includeHierarchy; 
		this.name = includeHierarchy.getName(); 
		this.currentUniqueName = includeHierarchy.getUniqueName(); 
	}
	private void updateUniqueNameInSelfAndParentsMaps(IncludeHierarchy includeHierarchy, String uniqueName) {
		includeHierarchy.setUniqueName(uniqueName); 
		includeHierarchy.buildUniqueNameAsPrefix();
		IncludeHierarchyCommand<Object> updateCommand = 
			new UpdateMapEntryCommand<>(IncludeHierarchyMapEnum.INCLUDE_ALL, currentUniqueName, uniqueName, includeHierarchy, true);
		Result<Object> updateResult = includeHierarchy.parents(updateCommand);
		updateResult = includeHierarchy.self(updateCommand);
		if (updateResult.hasResult()) throw new RuntimeException(updateResult.getMessage());
	}
}
		
//		includeHierarchy.initIncludeMapAll(); 
//		if (conflictName == null) {
//			updateUniqueNameInSelfAndParentsMaps(includeHierarchy, name);
//		} 
//		// build unique name.  if conflict with parent, build minimal. if conflict with uncle, build fqn  
//		else {
//			if (this.equals(conflictName)) {
//				if (!name.equals(includeHierarchy.getUniqueName())) {
//					System.out.println("here !=");
//					updateUniqueNameInSelfAndParentsMaps(includeHierarchy, name);
//				}
//			}
//			else if (includeHierarchy.hasParent(conflictName)) {
//				System.out.println("base");
//				IncludeHierarchy parent = includeHierarchy.getParent(); 
//				String tempName = name; 
//				while (parent != null) {
//					tempName = parent.getName()+"."+tempName; 
//					if (parent.equals(conflictName)) {
//						includeHierarchy.setUniqueName(tempName); 
//					}
//					parent = parent.getParent(); 
//				}
//				updateUniqueNameInSelfAndParentsMaps(includeHierarchy, includeHierarchy.getUniqueName());
//			}
//			else if (conflictName.hasParent(includeHierarchy)) {
//				System.out.println("here");
//				// no action required; handled by rename. 
//			}
//			else if (includeHierarchy.lowerLevelInHierarchyThanOther(conflictName)) {
//				System.out.println("there");
//				updateUniqueNameInSelfAndParentsMaps(includeHierarchy, includeHierarchy.getFullyQualifiedName());
//			}
//			else if (includeHierarchy.higherLevelInHierarchyThanOther(conflictName)) {
//				System.out.println("or there");
//				includeHierarchy.getRoot().all(this); // start over and the other (lower) guy will use fully qualified name
//			}
//		}
//		includeHierarchy.buildUniqueNameAsPrefix(); 
//		return result;
//	}


//@Override
//public Result<T> execute(IncludeHierarchy includeHierarchy) {
//	init(includeHierarchy); 
//	conflict = includeHierarchy.getRoot().getIncludeMapAll().get(name); 
//	String msg = (conflict == null) ? "null conflict " : "conflict UN: "+conflict.getUniqueName()+" conflict name: "+conflict.getName(); 
//	System.out.println(msg);
//	includeHierarchy.initIncludeMapAll(); 
//	if (conflict == null) {
//		updateUniqueNameInSelfAndParentsMaps(includeHierarchy, name);
//	} 
//	// build unique name.  if conflict with parent, build minimal. if conflict with uncle, build fqn  
//	else {
//		if (this.equals(conflict)) {
//			if (!name.equals(includeHierarchy.getUniqueName())) {
//				System.out.println("here !=");
//				updateUniqueNameInSelfAndParentsMaps(includeHierarchy, name);
//			}
//		}
//		else if (includeHierarchy.hasParent(conflict)) {
//			System.out.println("base");
//			IncludeHierarchy parent = includeHierarchy.getParent(); 
//			String tempName = name; 
//			while (parent != null) {
//				tempName = parent.getName()+"."+tempName; 
//				if (parent.equals(conflict)) {
//					includeHierarchy.setUniqueName(tempName); 
//				}
//				parent = parent.getParent(); 
//			}
//			updateUniqueNameInSelfAndParentsMaps(includeHierarchy, includeHierarchy.getUniqueName());
//		}
//		else if (conflict.hasParent(includeHierarchy)) {
//			System.out.println("here");
//			// no action required; handled by rename. 
//		}
//		else if (includeHierarchy.lowerLevelInHierarchyThanOther(conflict)) {
//			System.out.println("there");
//			updateUniqueNameInSelfAndParentsMaps(includeHierarchy, includeHierarchy.getFullyQualifiedName());
//		}
//		else if (includeHierarchy.higherLevelInHierarchyThanOther(conflict)) {
//			System.out.println("or there");
//			includeHierarchy.getRoot().all(this); // start over and the other (lower) guy will use fully qualified name
//		}
//	}
//	includeHierarchy.buildUniqueNameAsPrefix(); 
//	return result;
//}
////	IncludeHierarchy bInclude = addIncludeAndBuildUniqueName(includes, net2, "b"); 
////	IncludeHierarchy aInclude = addIncludeAndBuildUniqueName(includes, net2, "a"); 
////	IncludeHierarchy abInclude = addIncludeAndBuildUniqueName(aInclude, net3, "b"); 
////	assertEquals("minimially unique name for lower level include that is not child will just be fully qualified name",
////			includes.getChildInclude("a").getChildInclude("b"), includes.getInclude("top.a.b")); 
////	System.out.println("first");
////	checkIncludeMapAllEntries(includes, new ME("top", includes), new ME("a", aInclude), new ME("b", bInclude), new ME("top.a.b", abInclude) );
//////	checkIncludeMapAllEntries(bInclude, new ME("b", bInclude) );
//////	checkIncludeMapAllEntries(aInclude, new ME("a", aInclude), new ME("top.a.b", abInclude) );
//////	checkIncludeMapAllEntries(abInclude, new ME("top.a.b", abInclude) );
////	IncludeHierarchy cInclude = addIncludeAndBuildUniqueName(includes, net2, "c"); 
////	IncludeHierarchy cdInclude = addIncludeAndBuildUniqueName(cInclude, net3, "d"); 
////	System.out.println("second");
////	checkIncludeMapAllEntries(includes, new ME("top", includes), new ME("a", aInclude), new ME("b", bInclude), new ME("top.a.b", abInclude),
////			new ME("c", cInclude), new ME("d", cdInclude));
////	IncludeHierarchy dInclude = addIncludeAndBuildUniqueName(includes, net2, "d"); 
////	assertEquals("same result if nets added in the opposite order",
////			includes.getChildInclude("c").getChildInclude("d"), includes.getInclude("top.c.d")); 
////	System.out.println("third");
////	checkIncludeMapAllEntries(includes, new ME("top", includes), new ME("a", aInclude), new ME("b", bInclude), new ME("top.a.b", abInclude),
////			new ME("c", cInclude), new ME("top.c.d", cdInclude), new ME("d", dInclude) );
////	there
////	first
////	expecting: 
////	top name top unique name top
////	a name a unique name a
////	b name b unique name b
////	top.a.b name b unique name top.a.b
////	actual map: 
////	map: b name b unique name b
////	map: top.a.b name b unique name top.a.b
////	map: a name a unique name a
////	map: top name top unique name top
////	second
////	expecting: 
////	top name top unique name top
////	a name a unique name a
////	b name b unique name b
////	top.a.b name b unique name top.a.b
////	c name c unique name c
////	d name d unique name d
////	actual map: 
////	map: d name d unique name d
////	map: b name b unique name b
////	map: c name c unique name c
////	map: top.a.b name b unique name top.a.b
////	map: a name a unique name a
////	map: top name top unique name top
////	or there
////  d is higher than c.d, forces all
////	there
////	there
////	third
////	expecting: 
////	top name top unique name top
////	a name a unique name a
////	b name b unique name b
////	top.a.b name b unique name top.a.b
////	c name c unique name c
////	top.c.d name d unique name top.c.d
////	d name d unique name d
////	actual map: 
////	map: d name d unique name d
////	map: b name b unique name b
////	map: c name c unique name c
////	map: top.a.b name b unique name top.a.b
////	map: a name a unique name a
////	map: top.c.d name d unique name top.c.d