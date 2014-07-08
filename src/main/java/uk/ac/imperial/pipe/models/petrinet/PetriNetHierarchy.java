package uk.ac.imperial.pipe.models.petrinet;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.pipe.visitor.ClonePetriNet;

/**
 * A composite Petri net is one that include other Petri nets {@see uk.ac.imperial.pipe.models.petrinet.CompositePetriNet}.  A Petri net that does not 
 * include other Petri nets is a "single Petri net", or just a "Petri net".    
 * The composite Petri net is a tree, with a single root, the Petri net that imports other Petri nets.
 * Included Petri nets may be either single Petri nets or composite Petri nets.  An included composite Petri net forms the root of its own sub-tree.
 * A composite Petri net may not be imported recursively, either directly or indirectly.     
 * <p>
 * A composite Petri net behaves differently depending on its status:
 * <ul>
 * <li>editable:  only the components that have been added or modified in this Petri net are accessible.  Components of included Petri nets are not accessible.  
 * A persisted Petri net contains the XML for the components at the root level only, as well as XML for the statements that include other Petri nets.  
 * In this status, components may be added, modified or removed, and markings may be manually modified.  
 * <li>executable:  all the components for the root level and for all included Petri nets are accessible, as part of an executable Petri net (@see uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet).  
 * An executable Petri net is one where all include statements have been replaced with the components that comprise the included Petri net, resulting in a single Petri net.  
 * In this status:  the Petri net may be animated or analyzed by a module, and the markings that result from firing enabled transitions will be populated in the affected places.  
 * When the affected places are components in an included Petri net, the markings in the updated places in the expanded Petri net are mirrored to the corresponding included Petri net.
 * </ul>
 * As of PIPE 5.0, single Petri nets behave identically regardless of their status. 
 * <p>
 * In the PIPE 5.0 gui, each included Petri net is displayed in its own tab, and may be edited and persisted separately.  
 * Expanded Petri nets are not visible in the gui; their updated markings are visible in the tabs of the corresponding included Petri net. 
 */

public class PetriNetHierarchy
{

	private PetriNet rootPetriNet;
	private Map<String, PetriNet> includeMap;

	public PetriNetHierarchy(PetriNet petriNet)
	{
		if (petriNet == null) throw new IllegalArgumentException("Root level petri net may not be null.");
		this.rootPetriNet = petriNet; 
		this.includeMap = new HashMap<String, PetriNet>(); 	
	}

	public int size()
	{
		return 1+includeMap.size();
	}

	public PetriNet getTopNet()
	{
		return rootPetriNet;
	}
	//TODO should cloning be done here or in epn?   need also to mirror place counts
	public void includeNet(PetriNet net, String alias)
	{
		includeMap.put(alias, ClonePetriNet.clone(net)); 
	}

	public PetriNet getIncludedPetriNet(String alias)
	{
		return includeMap.get(alias);
	}
    public Map<String, Place> getPlaces() {
    	Map<String, Place> allPlaces = new HashMap<>();
    	allPlaces.putAll(rootPetriNet.getMapForClass(Place.class)); 
    	for (String alias: includeMap.keySet()) {
    		allPlaces.putAll(aliasPlaces(includeMap.get(alias).getPlaces(), alias));  
    	}
        return allPlaces;
    }

	private Map<String, Place> aliasPlaces(Collection<Place> places, String alias) {
		Map<String, Place> aliasPlaces = new HashMap<>();
		String aliasId = null;
		for (Place place : places) {
			aliasId = alias+"."+place.getId();
			place.setId(aliasId);
			aliasPlaces.put(aliasId, place); 
		}
		return aliasPlaces;
	}

}
