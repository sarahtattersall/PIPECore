package uk.ac.imperial.pipe.naming;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.pipe.models.petrinet.AbstractPetriNetPubSub;

public class Alias extends AbstractPetriNetPubSub implements PropertyChangeListener {

	private static final String NEW_ALIAS_NAME = "new name";
	private Map<String, Alias> aliasMap;
	private String name;
	private Alias parent;
	private String fullyQualifiedName;

	public Alias(String name) {
		this(null, name); 
	}
	
	private Alias(Alias parent, String name) {
		this.aliasMap = new HashMap<>(); 
		this.parent = parent;	
		if (!isValid(name)) name = "root";
		this.name = name; 
		buildFullyQualifiedName();
	}

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
		Alias parent = parent(); 
		while (parent != null) {
			sb.insert(0,".");
			sb.insert(0,parent.getName());
			parent = parent.parent(); 
		}
		fullyQualifiedName =  sb.toString();
	}

	public Map<String, Alias> aliasMap() {
		return aliasMap;
	}

	public Alias buildAlias(String alias) {
		if (!isValid(alias)) throw new RuntimeException("Alias name may not be blank or null");
		if (aliasMap.containsKey(alias)) throw new RuntimeException("Alias name duplicated at level "+name+": "+alias);
		Alias childAlias = new Alias(this, alias);
		addPropertyChangeListener(childAlias); 
		aliasMap.put(alias, childAlias);
		return aliasMap.get(alias); 
	}

	public Alias getAlias(String alias) {
		Alias child = aliasMap.get(alias); 
		if (child == null) {			
			throw new RuntimeException("Alias not found at level "+name+": "+alias);
		}
		return child;
	}

	public Alias parent() {
		return parent;
	}

	public void rename(String newName) {
		String oldName = name; 
		name = newName; 
		if (parent != null) parent.renameChild(oldName, newName); 
		buildFullyQualifiedName(); 
		notifyChildren(newName, oldName);
 	}
	public void renameChild(String oldName, String newName) {
		if (aliasMap.containsKey(newName)) throw new RuntimeException("Alias attempted rename at level "+name+" would cause duplicate: "+newName);
		Alias child = aliasMap.get(oldName);
		aliasMap.put(newName, child); 
		aliasMap.remove(oldName);
	}

	private void notifyChildren(String newName, String oldName) {
		changeSupport.firePropertyChange(NEW_ALIAS_NAME, oldName, newName);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(NEW_ALIAS_NAME)) {
			buildFullyQualifiedName(); 
			notifyChildren((String) evt.getOldValue(), (String) evt.getNewValue());
		}
	}

}
