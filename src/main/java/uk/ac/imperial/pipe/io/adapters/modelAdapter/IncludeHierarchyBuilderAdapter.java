package uk.ac.imperial.pipe.io.adapters.modelAdapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import uk.ac.imperial.pipe.io.IncludeHierarchyBuilder;
import uk.ac.imperial.pipe.io.adapters.model.AdaptedIncludeHierarchyBuilder;

/**
 * Marshals an IncludeHierarchyBuilder into XML format 
 */
public final class IncludeHierarchyBuilderAdapter extends XmlAdapter<AdaptedIncludeHierarchyBuilder, IncludeHierarchyBuilder> {
	
	
	private IncludeHierarchyBuilder builder;
	private boolean first;
	
	public IncludeHierarchyBuilderAdapter() {
		first = true; 
	}
    /**
     *
     * @param v
     * @return unmarshaled IncludeHierarchyBuilder
     */
	@Override
	public IncludeHierarchyBuilder unmarshal(AdaptedIncludeHierarchyBuilder v)
			throws Exception {
		if (first) {
			builder = buildBuilder(v); 
			first = false;
		}
//		else builder.getIncludeHierarchies().add(buildBuilder(v)); 
		return builder;
	}
	protected IncludeHierarchyBuilder buildBuilder(
			AdaptedIncludeHierarchyBuilder v) {
		IncludeHierarchyBuilder builder = new IncludeHierarchyBuilder(); 
		builder.setName(v.getName()); 
		builder.setNetLocation(v.getNetLocation());
//		builder.setBuilders(v.getBuilders()); 
//		builder.setIncludeHierarchies(v.getIncludes().getInclude());
//		builder.setRates(v.getRates());
//		builder.setGrates(v.getGrates());
//		builder.setThing(v.getIncludes().getThing()); 
//		for (IncludeHierarchyBuilder abuilder : v.getBuilders()) {
//			builder.getBuilders().add(abuilder); 
//		}
		return builder;
	}
	/**
	 *
	 * @param v
	 * @return marshaled IncludeHierarchyBuilder
	 */
	@Override
	public AdaptedIncludeHierarchyBuilder marshal(IncludeHierarchyBuilder v)
			throws Exception {
		AdaptedIncludeHierarchyBuilder adapted = new AdaptedIncludeHierarchyBuilder(); 
		adapted.setName(v.getName());
		adapted.setNetLocation(v.getNetLocation()); 
//		adapted.setBuilders(v.getBuilders()); 
//		AdaptedIncludeHierarchyBuilder.Includes includes = new AdaptedIncludeHierarchyBuilder.Includes(); 
//		includes.setInclude(v.getIncludeHierarchies());
//		includes.setThing(v.getThing());
//		adapted.setIncludes(includes); 
//		adapted.setRates(v.getRates());
//		adapted.setGrates(v.getGrates());
//		adapted.getIncludes().setThing(v.getThing());
		return adapted;
	}


}
