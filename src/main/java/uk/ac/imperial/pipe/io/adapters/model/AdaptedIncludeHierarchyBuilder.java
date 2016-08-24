package uk.ac.imperial.pipe.io.adapters.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.imperial.pipe.io.IncludeHierarchyBuilder;
import uk.ac.imperial.pipe.io.adapters.modelAdapter.IncludeHierarchyBuilderAdapter;
import uk.ac.imperial.pipe.io.adapters.valueAdapter.StringValueAdapter;

/**
 * Adapted include hierarchy for XML processing
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class AdaptedIncludeHierarchyBuilder  {

    /**
     * Include hierarchy name
     */
    @XmlAttribute
    private String name = "";

    /**
     * Path to petri net for this include hierarchy 
     */
    @XmlAttribute
    private String netLocation;
    
//    /**
//     * rate element
//     */
//    @XmlElement(name = "rate")
//    @XmlJavaTypeAdapter(StringValueAdapter.class)
//    private String rate = "";

    /**
     * rate element
     */
//    @XmlJavaTypeAdapter(ListWrapperAdapter.class)
//    @XmlElement(name = "rates")
    @XmlElement
//    @XmlJavaTypeAdapter(RatesAdapter.class)
    private List<String> rates = new ArrayList<>();
    
    @XmlElement
    @XmlJavaTypeAdapter(StringValueAdapter.class)
    private List<String> grates = new ArrayList<>();
    
//    @XmlElement
//    @XmlJavaTypeAdapter(StringValueAdapter.class)
//    private List<String> include = new ArrayList<>();
    
    @XmlElement(name = "includes")
    private Includes includes;
    
    /**
     * List of child include hierarchy builders under the current include hierarchy builder 
     */
//    @XmlElementWrapper(name = "include")
//    @XmlElementRef(name="include") // Invalid @XmlElementRef : Type "class uk.ac.imperial.pipe.io.adapters.model.AdaptedIncludeHierarchyBuilder" or any of its subclasses are not known to this context.


//    @XmlJavaTypeAdapter(IncludeHierarchyBuilderAdapter.class)
//    @XmlElement(name = "include")
//    private IncludeHierarchyBuilder includeHierarchyBuilder;

    
    
    /**
     * 
     * @return name   the name of this include hierarchy
     */
	public final String getName() {
		return name;
	}

	/**
	 * 
	 * @param name   the name of this include hierarchy 
	 */
	public final void setName(String name) {
		this.name = name;
	}
	
	/**
	 * 
	 * @return net  The path to the Petri net for this include hierarchy 
	 */
	public final String getNetLocation() {
		return netLocation;
	}
	
	/**
	 * 
	 * @param netLocation The path to the Petri net for this include hierarchy
	 */
	public final void setNetLocation(String netLocation) {
		this.netLocation = netLocation;
	}

//	public final List<IncludeHierarchyBuilder> getBuilders() {
//		return builders;
//	}
//
//	public final void setBuilders(List<IncludeHierarchyBuilder> builders) {
//		this.builders = builders;
//	}

//	public final String getRate() {
//		return rate;
//	}
//
//	public final void setRate(String rate) {
//		this.rate = rate;
//	}

	public final List<String> getRates() {
		return rates;
	}

	public final void setRates(List<String> rates) {
		this.rates = rates;
	}

	public final List<String> getGrates() {
		return grates;
	}

	public final void setGrates(List<String> grates) {
		this.grates = grates;
	}

	public final Includes getIncludes() {
		return includes;
	}
	
	public final void setIncludes(Includes includes) {
		this.includes = includes;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	public  class Includes {
		
//		@XmlElementRef(name = "include")  fails
	    @XmlJavaTypeAdapter(IncludeHierarchyBuilderAdapter.class)
	    @XmlElement(name = "include")
	    private Collection<IncludeHierarchyBuilder> include;

	    @XmlElement
	    private String thing; 
	    
	    public final Collection<IncludeHierarchyBuilder> getInclude() {
	    	if (include == null) {
	    		include = new ArrayList<IncludeHierarchyBuilder>();
//	    		include = new TreeSet<IncludeHierarchyBuilder>(new IncludeHierarchyBuilderComparator());
	    	}
	    	return include;
	    }
	    
	    public final void setInclude(Collection<IncludeHierarchyBuilder> include) {
	    	this.include = include;
	    }

		public final String getThing() {
			return thing;
		}

		public final void setThing(String thing) {
			this.thing = thing;
		}
		
	}
}
