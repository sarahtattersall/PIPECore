package uk.ac.imperial.pipe.io.adapters.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.imperial.pipe.io.IncludeHierarchyBuilder;
import uk.ac.imperial.pipe.io.adapters.modelAdapter.IncludeHierarchyBuilderAdapter;

/**
 * Adapted include hierarchy for XML processing
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class AdaptedIncludeHierarchyBuilder {

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

    @XmlElement(name = "includes")
    private Includes includes;

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

    @XmlAccessorType(XmlAccessType.FIELD)
    public class Includes {

        @XmlJavaTypeAdapter(IncludeHierarchyBuilderAdapter.class)
        @XmlElement(name = "include")
        private Collection<IncludeHierarchyBuilder> include;

        @XmlElement
        private String thing;

        public final Collection<IncludeHierarchyBuilder> getInclude() {
            if (include == null) {
                include = new ArrayList<>();
                //	    		include = new TreeSet<IncludeHierarchyBuilder>(new IncludeHierarchyBuilderComparator());
            }
            return include;
        }

        public final void setInclude(Collection<IncludeHierarchyBuilder> include) {
            this.include = include;
        }

    }
}
