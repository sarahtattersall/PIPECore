package uk.ac.imperial.pipe.models.petrinet;

import java.util.HashMap;
import java.util.Map;
/**
 * A composite Petri net is one that imports other Petri nets (@see uk.ac.imperial.pipe.models.petrinet.CompositePetriNet).  A Petri net that does not 
 * import other Petri nets is a "single Petri net", or just a "Petri net".    
 * The composite Petri net is a tree, with a single root, the Petri net that imports other Petri nets.
 * Imported Petri nets may be either single Petri nets or composite Petri nets.  An imported composite Petri net forms the root of its own sub-tree.
 * A composite Petri net may not be imported recursively, either directly or indirectly.     
 * <p>
 * A composite Petri net behaves differently depending on its status:
 * <ul>
 * <li>editable:  only the components that have been added or modified in this Petri net are accessible.  Components of imported Petri nets are not accessible.  
 * A persisted Petri net contains the XML for the components at the root level only, as well as XML for the statements that import other Petri nets.  
 * In this status, components may be added, modified or removed, and markings may be manually modified.  
 * <li>executable:  all the components for the root level and for all imported Petri nets are accessible, as part of an executable Petri net (@see uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet).  
 * An executable Petri net is one where all import statements have been replaced with the components that comprise the imported Petri net, resulting in a single Petri net.  
 * In this status:  the Petri net may be animated or analyzed by a module, and the markings that result from firing enabled transitions will be populated in the affected places.  
 * When the affected places are components in an imported Petri net, the markings in the updated places in the expanded Petri net are mirrored to the corresponding imported Petri net.
 * </ul>
 * As of PIPE 5.0, single Petri nets behave identically regardless of their status. 
 * <p>
 * In the PIPE 5.0 gui, each imported Petri net is displayed in its own tab, and may be edited and persisted separately.  
 * Expanded Petri nets are not visible in the gui; their updated markings are visible in the tabs of the corresponding imported Petri net. 
 */

public class PetriNetHierarchy
{

	private PetriNet topPetriNet;
	private Map<String, PetriNet> importMap;

	public PetriNetHierarchy(PetriNet petriNet)
	{
		if (petriNet == null) throw new IllegalArgumentException("Top level petri net may not be null.");
		this.topPetriNet = petriNet; 
		this.importMap = new HashMap<String, PetriNet>(); 	
	}

	public int size()
	{
		return 1+importMap.size();
	}

	public PetriNet getTopNet()
	{
		return topPetriNet;
	}

	public void importNet(PetriNet net, String alias)
	{
		importMap.put(alias, net); 
	}

	public PetriNet getImportedPetriNet(String alias)
	{
		return importMap.get(alias);
	}
}
