package uk.ac.imperial.pipe.models.petrinet;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.pipe.commands.IncludeHierarchyCommand;
import uk.ac.imperial.pipe.visitor.ClonePetriNet;

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

public class IncludeHierarchy extends AbstractPetriNetPubSub implements PropertyChangeListener {

	public static final String INCLUDE_HIERARCHY_ATTEMPTED_RENAME_AT_LEVEL = "IncludeHierarchy attempted rename at level ";
	public static final String WOULD_CAUSE_DUPLICATE = " would cause duplicate: ";
	public static final String INCLUDE_HIERARCHY_ATTEMPTED_RENAME_WOULD_CAUSE_DUPLICATE = "IncludeHierarchy attempted rename would cause duplicate: ";
	public static final String INCLUDE_ALIAS_NOT_FOUND_AT_LEVEL = "Include alias not found at level ";
	public static final String INCLUDE_ALIAS_NAME_DUPLICATED_AT_LEVEL = "Include alias name duplicated at level ";
	public static final String INCLUDE_ALIAS_NAME_MAY_NOT_BE_BLANK_OR_NULL = "Include alias name may not be blank or null";
	public static final String INCLUDE_HIERARCHY_PETRI_NET_MAY_NOT_BE_NULL = "IncludeHierarchy:  PetriNet may not be null";
	public static final String NEW_INCLUDE_ALIAS_NAME = "new include alias name";
	public static final String INCLUDE_ALIAS_NOT_FOUND = "Include alias not found: ";
	private Map<String, IncludeHierarchy> includeMap;
	private Map<String, IncludeHierarchy> includeFullyQualifiedMap;
	private String name;
	private IncludeHierarchy parent;
	private String fullyQualifiedName;
	private PetriNet petriNet;
	private String fullyQualifiedNameAsPrefix;
	private IncludeIterator iterator;
	private Map<String, InterfacePlace> interfacePlaces;

	public IncludeHierarchy(PetriNet net, String name) {
		this(net, null, name); 
	}
	
	private IncludeHierarchy(PetriNet petriNet, IncludeHierarchy parent, String name) {
		if (petriNet == null) throw new IllegalArgumentException(INCLUDE_HIERARCHY_PETRI_NET_MAY_NOT_BE_NULL);
		this.petriNet = petriNet; 
		this.includeMap = new HashMap<>();
		this.includeFullyQualifiedMap = new HashMap<>(); 
		this.interfacePlaces = new HashMap<>(); 
		this.parent = parent;
		if (!isValid(name)) name = "";
		this.name = name; 
		buildFullyQualifiedName();
	}
 
//	return alias.include(net, aliasName); 
//	clone.getPetriNetHierarchy().setAlias(alias.getAlias(aliasName)); 
//	includeMap.put(aliasName, clone); 

	private boolean isValid(String name) {
		if (name == null) return false;
		if (name.trim().isEmpty()) return false;
		return true;
	}

	public String getName() {
		return name;
	}

	public String getFullyQualifiedName() {
		return fullyQualifiedName;
	}

	private void buildFullyQualifiedName() {
		StringBuffer sb = new StringBuffer(); 
		sb.insert(0, getName()); 
		IncludeHierarchy parent = parent(); 
		while (parent != null) {
			sb.insert(0,".");
			sb.insert(0,parent.getName());
			parent = parent.parent(); 
		}
		fullyQualifiedName =  sb.toString();
		buildFullyQualifiedNameAsPrefix(); 
	}

	private void buildFullyQualifiedNameAsPrefix() {
		if (fullyQualifiedName.isEmpty()) { 
			fullyQualifiedNameAsPrefix = ""; 
		}
		else { 
			fullyQualifiedNameAsPrefix = fullyQualifiedName+"."; 
		}
	}

	public Map<String, IncludeHierarchy> includeMap() {
		return includeMap;
	}

	public IncludeHierarchy include(PetriNet net, String alias) {
		if (net == null) { 
			throw new IllegalArgumentException(INCLUDE_HIERARCHY_PETRI_NET_MAY_NOT_BE_NULL);
		}
		if (!isValid(alias)) { 
			throw new IllegalArgumentException(INCLUDE_ALIAS_NAME_MAY_NOT_BE_BLANK_OR_NULL);
		}
		if (includeMap.containsKey(alias)) { 
			throw new RuntimeException(INCLUDE_ALIAS_NAME_DUPLICATED_AT_LEVEL +	name + ": " + alias);
		}
		IncludeHierarchy childHierarchy = new IncludeHierarchy(ClonePetriNet.clone(net), this, alias);
		addPropertyChangeListener(childHierarchy); 
		includeMap.put(alias, childHierarchy);
		includeFullyQualifiedMap.put(childHierarchy.getFullyQualifiedName(), childHierarchy);
		return includeMap.get(alias); 
	}

	public IncludeHierarchy getInclude(String includeAlias) {
		IncludeHierarchy child = includeMap.get(includeAlias); 
		if (child == null) {			
			throw new RuntimeException(INCLUDE_ALIAS_NOT_FOUND_AT_LEVEL + name + ": " + includeAlias);
		}
		return child;
	}

	public IncludeHierarchy parent() {
		return parent;
	}

	public void rename(String newName) {
		String oldName = name; 
		String oldFullyQualifiedName = fullyQualifiedName; 
		name = newName; 
		buildFullyQualifiedName(); 
		if (parent != null) { 
			parent.renameChild(oldName, newName);
			parent.renameFullyQualifiedName(oldFullyQualifiedName, fullyQualifiedName); 
		}
		notifyChildren(newName, oldName);
 	}
	private void renameFullyQualifiedName(String oldFullyQualifiedName,
			String fullyQualifiedName) {
		if (includeFullyQualifiedMap.containsKey(fullyQualifiedName)) { 
			throw new RuntimeException(INCLUDE_HIERARCHY_ATTEMPTED_RENAME_WOULD_CAUSE_DUPLICATE + fullyQualifiedName);
		}
		IncludeHierarchy child = includeFullyQualifiedMap.get(oldFullyQualifiedName);
		includeMap.put(fullyQualifiedName, child); 	
		includeMap.remove(oldFullyQualifiedName);
	}

	public void renameChild(String oldName, String newName) {
		if (includeMap.containsKey(newName)) { 
			throw new RuntimeException(INCLUDE_HIERARCHY_ATTEMPTED_RENAME_AT_LEVEL + 
					name + WOULD_CAUSE_DUPLICATE + newName);
		}
		IncludeHierarchy child = includeMap.get(oldName);
		includeMap.put(newName, child); 	
		includeMap.remove(oldName);
	}

	private void notifyChildren(String newName, String oldName) {
		changeSupport.firePropertyChange(NEW_INCLUDE_ALIAS_NAME, oldName, newName);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(NEW_INCLUDE_ALIAS_NAME)) {
			buildFullyQualifiedName(); 
			notifyChildren((String) evt.getOldValue(), (String) evt.getNewValue());
		}
	}

	public PetriNet getPetriNet() {
		return petriNet;
	}


	public String getFullyQualifiedNameAsPrefix() {
		return fullyQualifiedNameAsPrefix;
	}
	public IncludeIterator iterator() {
		iterator = new IncludeIterator(this);
		return iterator; 
	}
	public void addToInterface(Place place) {
		InterfacePlace interfacePlace = place.buildInterfacePlace(); 
//		InterfaceDiscretePlace interfacePlace = new InterfaceDiscretePlace(place); 
		interfacePlace.setFullyQualifiedName(fullyQualifiedName); 
		if (!interfacePlaces.containsKey(interfacePlace.getId())) {
			interfacePlaces.put(interfacePlace.getId(), interfacePlace); 
		}
	}

	public Collection<InterfacePlace> getInterfacePlaces() {
		return interfacePlaces.values();
	}

	public InterfacePlace getInterfacePlace(String id) {
		return interfacePlaces.get(id);
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
	 * Execute the command for this hierarchy and pass to its parent.  
	 * This will result in the command being 
	 * executed in all of the parents of the target include hierarchy.  
	 * An error encountered by the command at each level of the hierarchy 
	 * will be added as a message to the list of messages 
	 * 
	 * @param command
	 * @return List<String> messages encountered when the command was executed at each level
	 */
	public List<String> parents(IncludeHierarchyCommand command) {
		List<String> messages = command.execute(this); 
		if (parent != null) {
			messages = parent.parents(command);  
//			List<String> parentMessages = parent.parents(command);  
//			messages.addAll(parentMessages); 
		}
		return messages; 
	}

	public IncludeHierarchy getFullyQualifiedInclude(String fullyQualifiedName) {
		IncludeHierarchy child = includeFullyQualifiedMap.get(fullyQualifiedName); 
		if (child == null) {			
			throw new RuntimeException(INCLUDE_ALIAS_NOT_FOUND+fullyQualifiedName);
		}
		return child;
	}
}
