package uk.ac.imperial.pipe.models.petrinet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;
import java.util.TreeSet;

public class IncludeIterator implements Iterator<IncludeHierarchy> {

    private Iterator<IncludeHierarchy> iterator;
    private IncludeHierarchy includeHierarchy;
    private Stack<Iterator<IncludeHierarchy>> stackIterator;
    private ArrayList<IncludeHierarchy> includes;

    public IncludeIterator(IncludeHierarchy includeHierarchy) {
        this.includeHierarchy = includeHierarchy;
        buildIncludeList();
    }

    private void buildIncludeList() {
        includes = new ArrayList<IncludeHierarchy>();
        includes.add(includeHierarchy);
        stackIterator = new Stack<Iterator<IncludeHierarchy>>();
        pushIteratorForOneLevel(includeHierarchy);
        IncludeHierarchy include = nextHierarchyFromCurrentIterator();
        while (include != null) {
            includes.add(include);
            include = nextHierarchyFromCurrentIterator();
        }
        iterator = includes.iterator();
    }

    private IncludeHierarchy nextHierarchyFromCurrentIterator() {
        IncludeHierarchy include = null;
        if (!stackIterator.empty()) {
            Iterator<IncludeHierarchy> it = stackIterator.pop();
            if (it.hasNext()) {
                include = it.next();
                if (it.hasNext())
                    stackIterator.push(it);
                pushIteratorForOneLevel(include);
            } else
                return nextHierarchyFromCurrentIterator();
        }
        return include;
    }

    private void pushIteratorForOneLevel(IncludeHierarchy includeHierarchy) {
        Collection<IncludeHierarchy> hierarchies = new TreeSet<IncludeHierarchy>(
                includeHierarchy.includeMap().values());
        Iterator<IncludeHierarchy> it = hierarchies.iterator();
        if (it.hasNext()) {
            stackIterator.push(it);
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Include Iterator does not support remove method.");
    }

    protected ArrayList<IncludeHierarchy> getIncludes() {
        return includes;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public IncludeHierarchy next() {
        return iterator.next();
    }
}
