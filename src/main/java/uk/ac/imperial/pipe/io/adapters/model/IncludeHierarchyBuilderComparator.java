package uk.ac.imperial.pipe.io.adapters.model;

import java.util.Comparator;

import uk.ac.imperial.pipe.io.IncludeHierarchyBuilder;

public class IncludeHierarchyBuilderComparator implements Comparator<IncludeHierarchyBuilder> {

    @Override
    public int compare(IncludeHierarchyBuilder ihb1, IncludeHierarchyBuilder ihb2) {
        return ihb1.getName().compareTo(ihb2.getName());
    }
}
