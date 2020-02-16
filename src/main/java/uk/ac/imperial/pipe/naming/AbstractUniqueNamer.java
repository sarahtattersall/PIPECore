package uk.ac.imperial.pipe.naming;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashSet;

/**
 * Used for naming components, listens to component
 *
 * This abstract class provides unique names 
 * Subclasses must call the constructor to set the namePrefix
 */
public abstract class AbstractUniqueNamer implements UniqueNamer {

    /**
     * Prefix for this namer
     */
    private final String namePrefix;

    /**
     * Names that exist already
     */
    protected final Collection<String> names = new HashSet<>();

    /**
     * Listens for name changes and will alter those that are in names
     */
    protected final PropertyChangeListener nameListener = new NameChangeListener(names);

    /**
     * @param namePrefix Value to prefix component names with, e.g. "P" for place
     */
    protected AbstractUniqueNamer(String namePrefix) {

        this.namePrefix = namePrefix;
    }

    /**
     *
     * @return guaranteed unique name starting with the prefix and having a unique number
     */
    @Override
    public final String getName() {
        int nameNumber = 0;
        String name = namePrefix + nameNumber;
        while (names.contains(name)) {
            nameNumber++;
            name = namePrefix + nameNumber;
        }
        return name;
    }

    /**
     *
     * @param name to check for duplicates 
     * @return true if this name has not already been used
     */
    @Override
    public final boolean isUniqueName(String name) {
        return !names.contains(name);
    }
}
