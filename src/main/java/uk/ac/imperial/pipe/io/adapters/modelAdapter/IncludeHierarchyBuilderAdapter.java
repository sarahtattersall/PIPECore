package uk.ac.imperial.pipe.io.adapters.modelAdapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import uk.ac.imperial.pipe.io.IncludeHierarchyBuilder;
import uk.ac.imperial.pipe.io.adapters.model.AdaptedIncludeHierarchyBuilder;

/**
 * Marshals an IncludeHierarchyBuilder into XML format 
 */
public final class IncludeHierarchyBuilderAdapter
        extends XmlAdapter<AdaptedIncludeHierarchyBuilder, IncludeHierarchyBuilder> {

    private IncludeHierarchyBuilder builder;
    private boolean first;

    public IncludeHierarchyBuilderAdapter() {
        first = true;
    }

    /**
     * @param adaptedBuilder to unmarshal 
     * @return unmarshaled IncludeHierarchyBuilder
     * @throws Exception if the includeHierarchyBuilder could not be unmarshalled
     */
    @Override
    public IncludeHierarchyBuilder unmarshal(AdaptedIncludeHierarchyBuilder adaptedBuilder)
            throws Exception {
        if (first) {
            builder = buildBuilder(adaptedBuilder);
            first = false;
        }
        return builder;
    }

    protected IncludeHierarchyBuilder buildBuilder(
            AdaptedIncludeHierarchyBuilder adaptedBuilder) {
        IncludeHierarchyBuilder builder = new IncludeHierarchyBuilder();
        builder.setName(adaptedBuilder.getName());
        builder.setNetLocation(adaptedBuilder.getNetLocation());
        return builder;
    }

    /**
     *
     * @param includeHierarchyBuilder to marshal
     * @return marshaled AdaptedIncludeHierarchyBuilder
     * @throws Exception if the includeHierarchyBuilder could not be marshalled
     */
    @Override
    public AdaptedIncludeHierarchyBuilder marshal(IncludeHierarchyBuilder includeHierarchyBuilder)
            throws Exception {
        AdaptedIncludeHierarchyBuilder adapted = new AdaptedIncludeHierarchyBuilder();
        adapted.setName(includeHierarchyBuilder.getName());
        adapted.setNetLocation(includeHierarchyBuilder.getNetLocation());
        return adapted;
    }

}
