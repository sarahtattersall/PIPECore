package uk.ac.imperial.pipe.models.petrinet;

import java.util.HashMap;
import java.util.Map;

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
