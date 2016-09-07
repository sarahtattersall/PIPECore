package uk.ac.imperial.pipe.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import uk.ac.imperial.pipe.exceptions.IncludeException;
import uk.ac.imperial.pipe.io.adapters.model.IncludeHierarchyBuilderComparator;
import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;

@XmlRootElement(name = "include")
@XmlAccessorType(XmlAccessType.FIELD)
public class IncludeHierarchyBuilder {
	@XmlAttribute
	private String name; 
	@XmlAttribute
	private String netLocation;
	@XmlElement(name = "include")
	private Collection<IncludeHierarchyBuilder> includeHierarchyBuilder;
	private String rootLocation; 
	
	public IncludeHierarchyBuilder() {
	}
	
	public IncludeHierarchyBuilder(IncludeHierarchy include) {
		this.name = include.getName(); 
		this.netLocation = include.getPetriNetLocation();
		for (IncludeHierarchy childInclude : include.includeMap().values()) {
			getIncludeHierarchyBuilder().add(new IncludeHierarchyBuilder(childInclude)); 
		}
	}
	public IncludeHierarchy buildIncludes(IncludeHierarchy parent) throws JAXBException, FileNotFoundException, IncludeException {
		IncludeHierarchy include = buildHierarchy(parent); 
		for (IncludeHierarchyBuilder builder : getIncludeHierarchyBuilder()) {
			builder.setRootLocation(rootLocation); 
			IncludeHierarchy child = builder.buildIncludes(include);
			include.include(child); 
		}
		return include;
	}
	protected IncludeHierarchy buildHierarchy(IncludeHierarchy parent) throws JAXBException,
			FileNotFoundException {
		PetriNet net = buildPetriNet(netLocation); 
		IncludeHierarchy include = new IncludeHierarchy(net, parent, name);
		include.setPetriNetLocation(netLocation); 
		return include;
	}
	protected PetriNet buildPetriNet(String netLocation) throws JAXBException,
			FileNotFoundException {
		PetriNetIO petriNetIO = new PetriNetIOImpl(); 
		PetriNet net; 
		URL url = PetriNetIO.class.getResource(netLocation);
		if (url != null) {
			net = petriNetIO.read(PetriNetIO.class.getResource(netLocation).getPath());
		}
		else {
			netLocation = this.rootLocation + File.separator + netLocation;
			net = petriNetIO.read(netLocation); 
		}
		return net;
	}
	public final String getName() {
		return name;
	}
	public final void setName(String name) {
		this.name = name;
	}
	public final String getNetLocation() {
		return netLocation;
	}
	public final void setNetLocation(String netLocation) {
		this.netLocation = netLocation;
	}
	public final Collection<IncludeHierarchyBuilder> getIncludeHierarchyBuilder() {
	    	if (includeHierarchyBuilder == null) {
//	    		includeHierarchyBuilder = new ArrayList<IncludeHierarchyBuilder>();
	    		includeHierarchyBuilder = new TreeSet<IncludeHierarchyBuilder>(new IncludeHierarchyBuilderComparator());
	    	}

		return includeHierarchyBuilder;
	}
	public final void setIncludeHierarchyBuilder(
			Collection<IncludeHierarchyBuilder> includeHierarchyBuilder) {
		this.includeHierarchyBuilder = includeHierarchyBuilder;
	}
	@Override
	public boolean equals(Object obj) {
		//TODO test and guard 
		return ((IncludeHierarchyBuilder) obj).getName().equals(name);  
	}

	public void setRootLocation(String rootLocation) {
		this.rootLocation = rootLocation; 
	}
}
