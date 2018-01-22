package uk.ac.imperial.pipe.models;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.imperial.pipe.io.IncludeHierarchyBuilder;
import uk.ac.imperial.pipe.io.adapters.modelAdapter.IncludeHierarchyBuilderAdapter;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;

/**
 * This class is the base level of XML for include hierarchies 
 */
@XmlRootElement(name = "hierarchy")
public final class IncludeHierarchyHolder {
    /**
     * The root include hierarchy 
     */
    @XmlJavaTypeAdapter(IncludeHierarchyBuilderAdapter.class)
    @XmlElement(name = "include")
    private List<IncludeHierarchyBuilder> includeHierarchies = new ArrayList<>();

    public IncludeHierarchyBuilder getIncludeHierarchyBuilder(int index) {
        return includeHierarchies.get(index);
    }

    //	public final void setIncludeHierarchyBuilder(
    //			IncludeHierarchyBuilder includeHierarchyBuilder) {
    //		this.includeHierarchyBuilder = includeHierarchyBuilder;
    //	}

    /**
     *
     *
     *
     * @param index index of the net added in order
     * @return Petri net at the index
     */
    //    public PetriNet getNet(int index) {
    //        return nets.get(index);
    //    }
    //
    //    /**
    //     *
    //     * @return the number of Petri nets stored in this holder
    //     */
    //    public int size() {
    //        return nets.size();
    //    }
    //
    //    /**
    //     *
    //     * @return true if holder contains no Petri nets
    //     */
    //    public boolean isEmpty() {
    //        return nets.isEmpty();
    //    }
    //
    //    /**
    //     * Removes the petriNet from this holder
    //     * @param petriNet
    //     */
    //    public void remove(PetriNet petriNet) {
    //        nets.remove(petriNet);
    //    }
}
