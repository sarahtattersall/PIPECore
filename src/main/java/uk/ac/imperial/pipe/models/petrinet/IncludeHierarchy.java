package uk.ac.imperial.pipe.models.petrinet;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.pipe.exceptions.IncludeException;

/**
 * A composite Petri net is one that include other Petri nets.  A Petri net that does not 
 * include other Petri nets is a "single Petri net", or just a "Petri net".    
 * The composite Petri net is a tree, with a single root, the Petri net that imports other Petri nets.
 * Included Petri nets may be either single Petri nets or composite Petri nets.  
 * An included composite Petri net forms the root of its own sub-tree.
 * A composite Petri net may not be imported recursively, either directly or indirectly.     
 * <p>
 * A composite Petri net behaves differently depending on its status:
 * <ul>
 * <li>editable:  only the components that have been added or modified in this Petri net are accessible.  
 * Components of included Petri nets are not accessible.  
 * A persisted Petri net contains the XML for the components at the root level only, 
 * as well as XML for the statements that include other Petri nets.  
 * In this status, components may be added, modified or removed, and markings may be manually modified.  
 * <li>executable:  all the components for the root level and for all included Petri nets are accessible, 
 * as part of an executable Petri net (@see uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet).  
 * An executable Petri net is one where all include statements have been replaced with the components 
 * that comprise the included Petri net, resulting in a single Petri net.  
 * In this status:  the Petri net may be animated or analyzed by a module, 
 * and the markings that result from firing enabled transitions will be populated in the affected places.  
 * When the affected places are components in an included Petri net, the markings in the updated places 
 * in the expanded Petri net are mirrored to the corresponding included Petri net.
 * </ul>
 * As of PIPE 5.0, single Petri nets behave identically regardless of their status. 
 * <p>
 * In the PIPE 5.0 gui, each included Petri net is displayed in its own tab, 
 * and may be edited and persisted separately.  
 * Expanded Petri nets are not visible in the gui; their updated markings are visible 
 * in the tabs of the corresponding included Petri net. 
 */

public class IncludeHierarchy  {

	public static final String INCLUDE_NAME_NOT_FOUND_AT_LEVEL = "Include name not found at level ";
	public static final String INCLUDE_NAME_MAY_NOT_BE_BLANK_OR_NULL = "Include name may not be blank or null";
	public static final String INCLUDE_HIERARCHY_PETRI_NET_MAY_NOT_BE_NULL = "IncludeHierarchy:  PetriNet may not be null";
	public static final String INCLUDED_NET_MAY_NOT_EXIST_AS_PARENT_IN_HIERARCHY = "Included Petri net name may not exist as a parent Petri net in this include hierarchy.";
	public static final String INCLUDE_NAME_NOT_FOUND_AT_ANY_LEVEL = "Include name not found at any level: ";
	private String name;
	private PetriNet petriNet;
	private IncludeHierarchy parent;
	private String fullyQualifiedName;
	private String fullyQualifiedNameAsPrefix;
	private String uniqueName;
	private String uniqueNameAsPrefix;
	private IncludeIterator iterator;
	private boolean isRoot;
	private int level;
	private IncludeHierarchy root;
	private Map<String, IncludeHierarchy> includeMap  = new HashMap<>();
	private Map<String, IncludeHierarchy> includeMapAll  = new HashMap<>(); 
	private Map<String, InterfacePlace> interfacePlaces = new HashMap<>();
	private IncludeHierarchyCommandScope interfacePlaceAccessScope;
	private IncludeHierarchyCommandScopeEnum interfacePlaceAccessScopeEnum;
	public IncludeHierarchy(PetriNet net, String name) {
		this(net, null, name); 
	}
	
	public IncludeHierarchy(PetriNet petriNet, IncludeHierarchy parent, String name) {
		if (petriNet == null) throw new IllegalArgumentException(INCLUDE_HIERARCHY_PETRI_NET_MAY_NOT_BE_NULL);
		this.petriNet = petriNet; 
		this.parent = parent;
		buildRootAndLevelRelativeToRoot(parent); 
		buildNames(name);
		setInterfacePlaceAccessScope(IncludeHierarchyCommandScopeEnum.PARENTS);  
	}

	private void buildNames(String name) {
		if (!isValid(name, isRoot)) name = "";
		this.name = name.trim(); 
		buildUniqueName();
		buildFullyQualifiedName();
	}
	public IncludeHierarchy include(PetriNet petriNet, String name) throws  IncludeException {
		validateInclude(petriNet, name);
		IncludeHierarchy childHierarchy = new IncludeHierarchy(petriNet, this, name);
		addIncludeToIncludeMap(name, childHierarchy); 
		childHierarchy.buildUniqueName(); 
		childHierarchy.setInterfacePlaceAccessScope(interfacePlaceAccessScopeEnum); 
		return childHierarchy; 
	}
	
	private void buildRootAndLevelRelativeToRoot(IncludeHierarchy parent) {
		isRoot = (parent == null) ? true : false;
		if (isRoot) {
			root = this; 
			level = 0; 
		}
		else {
			root = parent.getRoot(); 
			level = parent.getLevelRelativeToRoot()+1; 
		}
	}

	private boolean isValid(String name, boolean root) {
		if (name == null) return false;
		if (name.trim().isEmpty()) {
			if (root) return true; 
			else return false;
		}
		return true;
	}

	protected void buildUniqueName()  {
    	self(new BuildUniqueNameCommand()); 
	}

	protected boolean higherLevelInHierarchyThanOther(IncludeHierarchy conflict) {
		return getLevelRelativeToRoot() < conflict.getLevelRelativeToRoot();
	}
	protected boolean lowerLevelInHierarchyThanOther(IncludeHierarchy conflict) {
		return getLevelRelativeToRoot() > conflict.getLevelRelativeToRoot();
	}

	protected void addIncludeToIncludeMap(String name, IncludeHierarchy childHierarchy) throws IncludeException {
		Result<UpdateResultEnum> result = self(new UpdateMapEntryCommand(IncludeHierarchyMapEnum.INCLUDE, name, childHierarchy)); 
		if (result.hasResult()) throw new IncludeException(result.getMessage());
	}

	protected void validateInclude(PetriNet petriNet, String name) throws IncludeException {
		if (petriNet == null) { 
			throw new IncludeException(INCLUDE_HIERARCHY_PETRI_NET_MAY_NOT_BE_NULL);
		}
		validateName(name, false);
		checkForDuplicatePetriNetNameInSelfAndParentIncludes(petriNet);
	}
	protected void validateName(String name, boolean root) throws IncludeException {
		if (!isValid(name, root)) { 
			throw new IncludeException(INCLUDE_NAME_MAY_NOT_BE_BLANK_OR_NULL);
		}
	}
	/**
	 * Returns the IncludeHierarchy, if it exists, with the specified name that is the immediate child of this include hierarchy
	 * @param includeName
	 * @return
	 * @throws IncludeException 
	 */
	public IncludeHierarchy getChildInclude(String includeName) throws IncludeException {
		IncludeHierarchy child = includeMap.get(includeName); 
		if (child == null) {			
			throw new IncludeException(INCLUDE_NAME_NOT_FOUND_AT_LEVEL + name + ": " + includeName);
		}
		return child;
	}
	/**
	 * Returns the IncludeHierarchy whose uniqueName matches the parameter, from the set that includes this IncludeHierarchy and all of its children.  
	 * If executed against the root IncludeHierarchy, this searches all includes in the IncludeHierarchy.  
	 * <p>Throws RuntimeException if no includes are found with a matching unique name.       
	 * @param uniqueName
	 * @return
	 * @throws IncludeException 
	 */
	public IncludeHierarchy getInclude(String uniqueName) throws IncludeException {
		IncludeHierarchy include = includeMapAll.get(uniqueName); 
		if (include == null) {			
			throw new IncludeException(INCLUDE_NAME_NOT_FOUND_AT_ANY_LEVEL + uniqueName);
		}
		return include;
	}

	protected void checkForDuplicatePetriNetNameInSelfAndParentIncludes(PetriNet petriNet) throws IncludeException {
		DuplicatePetriNetNameCheckCommand<String> duplicateCheck = new DuplicatePetriNetNameCheckCommand<String>(petriNet.getName()); 
		Result<String> result = self(duplicateCheck); 
		result = parents(duplicateCheck); 
		if (result.hasResult()) {
			throw new IncludeException(IncludeHierarchy.INCLUDED_NET_MAY_NOT_EXIST_AS_PARENT_IN_HIERARCHY+"\n"+result.getEntry().message);
		}
	}
	public Result<UpdateResultEnum> rename(String newname) throws IncludeException {
		validateName(newname, isRoot);
		Result<UpdateResultEnum> renameResult = renameBare(newname);
		if (renameResult.hasResult()) {
			throw new IncludeException(renameResult.getMessage()); 
		}
		buildFullyQualifiedName();   
		buildUniqueName();
		return renameResult; 
 	}

	protected Result<UpdateResultEnum> renameBare(String newname) throws IncludeException {
		Result<UpdateResultEnum> result = parent(new UpdateMapEntryCommand(IncludeHierarchyMapEnum.INCLUDE,getName(), newname, this));  
		if (!result.hasResult()) {
			setName(newname); 
		}
		return result;
	}
	
	public boolean hasParent(IncludeHierarchy include) {
		return parents(new IsParentCommand(include)).hasResult();
	}

	protected void buildFullyQualifiedName() {
		BuildFullyQualifiedNameCommand buildFullyQualifiedNameCommand = new BuildFullyQualifiedNameCommand();  
		self(buildFullyQualifiedNameCommand);
		children(buildFullyQualifiedNameCommand); 
	}

	protected void buildFullyQualifiedNameAsPrefix() {
		fullyQualifiedNameAsPrefix = buildNameAsPrefix(fullyQualifiedName);
	}
	protected void buildUniqueNameAsPrefix() {
		uniqueNameAsPrefix = buildNameAsPrefix(uniqueName);
	}

	protected String buildNameAsPrefix(String name) {
		String nameAsPrefix = null; 
		if (name.isEmpty()) { 
			nameAsPrefix = ""; 
		}
		else { 
			nameAsPrefix = name+"."; 
		}
		return nameAsPrefix; 
	}
	
	public Result<Place> addToInterface(Place place) throws IncludeException {
		IncludeHierarchyCommand<Place> addInterfacePlaceCommand = new AddInterfacePlaceCommand<Place>(place, this); 
		self(addInterfacePlaceCommand); 
		return interfacePlaceAccessScope.execute(addInterfacePlaceCommand); 
	}

	protected boolean addInterfacePlaceToMap(InterfacePlace interfacePlace) {
		if (!interfacePlaces.containsKey(interfacePlace.getId())) {
			interfacePlaces.put(interfacePlace.getId(), interfacePlace);
			return true; 
		}
		else return false; 
	}
	public void useInterfacePlace(String id) {
		InterfacePlace interfacePlace = getInterfacePlace(id); 
		boolean inuse = interfacePlace.use(); 
		if (inuse) {
			getPetriNet().addPlace(interfacePlace); 
		}
	}

	public void removeFromInterface(DiscretePlace place) {
		for (String id : interfacePlaces.keySet()) {
			if (interfacePlaces.get(id).getPlace().getId().equals(place.getId())) {
				interfacePlaces.remove(id); 
			}
		}
		//TODO do same for the rest of the hierarchy
	}
	/**
	 * Execute the command for the parents, if any, of this hierarchy.  
	 * This will result in the command being 
	 * executed in all of the parents of the target include hierarchy, 
	 * in order beginning with the lowest (immediate) parent and ending with the root. 
	 * An error encountered by the command at each level of the hierarchy 
	 * will be added as a message to the list of messages in the {@link Result} 
	 * <ul>
	 * <li>To execute the command for the immediate parent, use {@link #parent(IncludeHierarchyCommand)}
	 * <li>To execute the command for all children, use {@link #children(IncludeHierarchyCommand)}
	 * <li>To execute the command only for this include hierarchy, use {@link #self(IncludeHierarchyCommand)}
	 * <li>To execute the command for siblings under the immediate parent, use {@link #siblings(IncludeHierarchyCommand)}
	 * <li>To execute the command for all includes in the hierarchy, use {@link #all(IncludeHierarchyCommand)}
	 * </ul>
	 * 
	 * @param command
	 * @return Result accumulated results encountered when the command was executed at each level
	 * @throws IncludeException 
	 */
	public <T> Result<T> parents(IncludeHierarchyCommand<T> command)  {
		Result<T> result = command.getResult(); 
		if (parent != null) {
			result = command.execute(parent);
			result = parent.parents(command);
		}
		return result; 
	}
	/**
	 * Execute the command for the immediate parent, if any, of this hierarchy.  
	 * This will result in the command being 
	 * executed in the immediate parent of the target include hierarchy. 
	 * An error encountered by the command at each level of the hierarchy 
	 * will be added as a message to the list of messages in the {@link Result} 
	 * <ul>
	 * <li>To execute the command for all parents, use {@link #parents(IncludeHierarchyCommand)}
	 * <li>To execute the command for all children, use {@link #children(IncludeHierarchyCommand)}
	 * <li>To execute the command only for this include hierarchy, use {@link #self(IncludeHierarchyCommand)}
	 * <li>To execute the command for siblings under the immediate parent, use {@link #siblings(IncludeHierarchyCommand)}
	 * <li>To execute the command for all includes in the hierarchy, use {@link #all(IncludeHierarchyCommand)}
	 * </ul>
	 * @param command
	 * @return Result accumulated results encountered when the command was executed at each level
	 * @throws IncludeException 
	 */
	public <T> Result<T> parent(IncludeHierarchyCommand<T> command)  {
		Result<T> result = command.getResult(); 
		if (parent != null) {
			result = command.execute(parent);
		}
		return result; 
	}
	/**
	 * Execute an {@link IncludeHierarchyCommand} for the children of this IncludeHierarchy. 
	 * An error encountered by the command  
	 * will be added as a message to the list of messages in the {@link Result} 
	 * <ul>
	 * <li>To execute the command for the immediate parent, use {@link #parent(IncludeHierarchyCommand)}
	 * <li>To execute the command for all parents, use {@link #parents(IncludeHierarchyCommand)}
	 * <li>To execute the command only for this include hierarchy, use {@link #self(IncludeHierarchyCommand)}
	 * <li>To execute the command for siblings under the immediate parent, use {@link #siblings(IncludeHierarchyCommand)}
	 * <li>To execute the command for all includes in the hierarchy, use {@link #all(IncludeHierarchyCommand)}
	 * </ul>
	 * @param command
	 * @return Result accumulated results encountered when the command was executed at each level
	 * @throws IncludeException 
	 */
	public <T> Result<T> children(IncludeHierarchyCommand<T> command)  {
		Result<T> result = command.getResult(); 
		iterator = iterator();
		iterator.next(); // skip self
		while (iterator.hasNext()) {
			result = command.execute(iterator.next()); 
		}
		return result;
	}
	/**
	 * Execute an {@link IncludeHierarchyCommand} for the siblings of this IncludeHierarchy under the same immediate parent. 
	 * An error encountered by the command  
	 * will be added as a message to the list of messages in the {@link Result} 
	 * <ul>
	 * <li>To execute the command for the immediate parent, use {@link #parent(IncludeHierarchyCommand)}
	 * <li>To execute the command for all parents, use {@link #parents(IncludeHierarchyCommand)}
	 * <li>To execute the command for all children, use {@link #children(IncludeHierarchyCommand)}
	 * <li>To execute the command only for this include hierarchy, use {@link #self(IncludeHierarchyCommand)}
	 * <li>To execute the command for all includes in the hierarchy, use {@link #all(IncludeHierarchyCommand)}
	 * </ul>
	 * @param command
	 * @return Result accumulated results encountered when the command was executed at each level
	 * @throws IncludeException 
	 */
	public <T> Result<T> siblings(IncludeHierarchyCommand<T> command)  {
		Result<T> result = command.getResult(); 
		if (parent != null) {
			iterator = parent.iterator();
			IncludeHierarchy current = null; 
			while (iterator.hasNext()) {
				current = iterator.next(); 
				if ((!current.equals(this)) && (!(current.parent == null)) && (current.parent.equals(parent))) {
					result = command.execute(current); 
				}
			}
		}
		return result;
	}
	/**
	 * Execute an {@link IncludeHierarchyCommand} for this IncludeHierarchy. 
	 * An error encountered by the command  
	 * will be added as a message to the list of messages in the {@link Result} 
	 * <ul>
	 * <li>To execute the command for the immediate parent, use {@link #parent(IncludeHierarchyCommand)}
	 * <li>To execute the command for all parents, use {@link #parents(IncludeHierarchyCommand)}
	 * <li>To execute the command for all children, use {@link #children(IncludeHierarchyCommand)}
	 * <li>To execute the command for siblings under the immediate parent, use {@link #siblings(IncludeHierarchyCommand)}
	 * <li>To execute the command for all includes in the hierarchy, use {@link #all(IncludeHierarchyCommand)}
	 * </ul>
	 * @param command
	 * @return Result accumulated results encountered when the command was executed at each level
	 * @throws IncludeException 
	 */
	public <T> Result<T> self(IncludeHierarchyCommand<T> command)  {
		return command.execute(this); 
	}
	/**
	 * Execute an {@link IncludeHierarchyCommand} for all levels of this IncludeHierarchy. 
	 * An error encountered by the command  
	 * will be added as a message to the list of messages in the {@link Result} 
	 * <ul>
	 * <li>To execute the command for the immediate parent, use {@link #parent(IncludeHierarchyCommand)}
	 * <li>To execute the command for all parents, use {@link #parents(IncludeHierarchyCommand)}
	 * <li>To execute the command for all children, use {@link #children(IncludeHierarchyCommand)}
	 * <li>To execute the command only for this include hierarchy, use {@link #self(IncludeHierarchyCommand)}
	 * <li>To execute the command for siblings under the immediate parent, use {@link #siblings(IncludeHierarchyCommand)}
	 * </ul>
	 * @param command
	 * @return Result accumulated results encountered when the command was executed at each level
	 * @throws IncludeException 
	 */
	public <T> Result<T> all(IncludeHierarchyCommand<T> command) {
		Result<T> result = command.getResult(); 
		iterator = getRoot().iterator();
		while (iterator.hasNext()) {
			result = command.execute(iterator.next()); 
		}
		return result;
	}


	public void setInterfacePlaceAccessScope(IncludeHierarchyCommandScopeEnum scopeEnum) {
		interfacePlaceAccessScopeEnum = scopeEnum;
		interfacePlaceAccessScope = scopeEnum.buildScope(this);
	}
	public IncludeIterator iterator() {
		iterator = new IncludeIterator(this);
		return iterator; 
	}
	public Collection<InterfacePlace> getInterfacePlaces() {
		return interfacePlaces.values();
	}

	public InterfacePlace getInterfacePlace(String id) {
		return interfacePlaces.get(id);
	}

	public IncludeHierarchy getRoot() {
		return root; 
	}
	public IncludeHierarchyCommandScope getInterfacePlaceAccessScope() {
		return interfacePlaceAccessScope;
	}

	public String getFullyQualifiedName() {
		return fullyQualifiedName;
	}
	public PetriNet getPetriNet() {
		return petriNet;
	}

	public String getFullyQualifiedNameAsPrefix() {
		return fullyQualifiedNameAsPrefix;
	}
	public String getUniqueNameAsPrefix() {
		return uniqueNameAsPrefix;
	}

	public IncludeHierarchy getParent() {
		return parent;
	}
	public Map<String, IncludeHierarchy> includeMap() {
		return includeMap;
	}
	protected int getLevelRelativeToRoot() {
		return level;
	}
	
	protected Map<String, IncludeHierarchy> getIncludeMapAll() {
		return includeMapAll;
	}
	protected Map<String, IncludeHierarchy> getIncludeMap() {
		return includeMap;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name; 
	}

	public String getUniqueName() {
		return uniqueName;
	}

	protected void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	protected boolean isRoot() {
		return isRoot;
	}

	public void setFullyQualifiedName(String fullyQualifiedName) {
		this.fullyQualifiedName = fullyQualifiedName; 
	}

}
