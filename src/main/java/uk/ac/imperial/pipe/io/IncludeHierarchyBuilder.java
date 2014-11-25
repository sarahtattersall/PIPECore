package uk.ac.imperial.pipe.io;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import uk.ac.imperial.pipe.exceptions.IncludeException;
import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;

//@XmlRootElement(name="include")
@XmlRootElement(name = "include")
@XmlAccessorType(XmlAccessType.FIELD)
public class IncludeHierarchyBuilder {
	@XmlAttribute
	private String name; 
	@XmlAttribute
	private String netLocation;
	@XmlElement(name = "include")
	private List<IncludeHierarchyBuilder> includeHierarchyBuilder = new ArrayList<>(); 
	private IncludeHierarchy parent; 
	
	public IncludeHierarchyBuilder() {
	}
	
	public IncludeHierarchyBuilder(IncludeHierarchy include) {
		this.name = include.getName(); 
		this.netLocation = include.getPetriNetLocation();
		for (IncludeHierarchy childInclude : include.includeMap().values()) {
			includeHierarchyBuilder.add(new IncludeHierarchyBuilder(childInclude)); 
		}
	}
	public IncludeHierarchy buildIncludes() throws JAXBException, FileNotFoundException, IncludeException {
		if (parent == null) {
			parent = buildHierarchy(); 
//			System.out.println("netlocation "+netLocation+" name "+name );
		}
		for (IncludeHierarchyBuilder builder : getIncludeHierarchyBuilder()) {
//			System.out.println("b netlocation "+builder.netLocation+" bname "+builder.name );
			parent.include(buildPetriNet(builder.netLocation), builder.name); 
			parent.getChildInclude(builder.name).setPetriNetLocation(builder.netLocation); 
		}
		return parent;
	}
	protected IncludeHierarchy buildHierarchy() throws JAXBException,
			FileNotFoundException {
		PetriNet net = buildPetriNet(netLocation); 
		IncludeHierarchy includes = new IncludeHierarchy(net, name);
		includes.setPetriNetLocation(netLocation); 
		return includes;
	}
	protected PetriNet buildPetriNet(String netLocation) throws JAXBException,
			FileNotFoundException {
		PetriNetIO petriNetIO = new PetriNetIOImpl(); 
		//TODO perhaps retry with / without leading slash 
		PetriNet net = petriNetIO.read(PetriNetIO.class.getResource(netLocation).getPath());
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
	public final List<IncludeHierarchyBuilder> getIncludeHierarchyBuilder() {
		return includeHierarchyBuilder;
	}
	public final void setIncludeHierarchyBuilder(
			List<IncludeHierarchyBuilder> includeHierarchyBuilder) {
		this.includeHierarchyBuilder = includeHierarchyBuilder;
	}
	
}
