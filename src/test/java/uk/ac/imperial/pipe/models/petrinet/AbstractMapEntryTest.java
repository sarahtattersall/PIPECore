package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class AbstractMapEntryTest {

    private IncludeHierarchy lastParent;

    protected void checkIncludeMapAllEntries(IncludeHierarchy includes, boolean print, ME... expectedEntries) {
        checkIncludeMapAllEntries("", includes, print, expectedEntries);
    }

    protected void checkIncludeMapAllEntries(String comment, IncludeHierarchy includes, boolean print,
            ME... expectedEntries) {
        checkMapEntries(comment, includes, "includeMapAll", includes.getIncludeMapAll(), expectedEntries, print, false);
    }

    protected void checkIncludeMapAllEntries(IncludeHierarchy includes, ME... expectedEntries) {
        checkIncludeMapAllEntries("", includes, false, expectedEntries);
    }

    protected void checkIncludeMapAllEntries(String comment, IncludeHierarchy includes,
            ME... expectedEntries) {
        checkMapEntries(comment, includes, "includeMapAll", includes.getIncludeMapAll(), expectedEntries, false, false);
    }

    protected void checkIncludeMapEntries(IncludeHierarchy includes, boolean print, boolean matchName,
            ME... expectedEntries) {
        checkIncludeMapEntries("", includes, print, matchName, expectedEntries);
    }

    protected void checkIncludeMapEntries(String comment, IncludeHierarchy includes, boolean print, boolean matchName,
            ME... expectedEntries) {
        checkMapEntries(comment, includes, "includeMap", includes.getIncludeMap(), expectedEntries, print, matchName);
    }

    protected void checkIncludeMapEntries(String comment, IncludeHierarchy includes, boolean print, boolean matchName,
            boolean matchFullyQualifiedName, FQNME... expectedEntries) {
        checkMapEntries(comment, includes, "includeMap", includes
                .getIncludeMap(), expectedEntries, print, matchName, matchFullyQualifiedName);
    }

    private void checkMapEntries(String comment, IncludeHierarchy includes, String mapName,
            Map<String, IncludeHierarchy> map, ME[] expectedEntries, boolean print, boolean matchName) {
        //				printDetails(map, expectedEntries);
        assertEquals("Number of entries does not match for map " + mapName + " in IncludeHierarchy named " +
                includes.getName(), expectedEntries.length, map.size());
        for (int i = 0; i < expectedEntries.length; i++) {
            boolean pass = false;
            for (Entry<String, IncludeHierarchy> entry : map.entrySet()) {
                if ((expectedEntries[i].key.equals(entry.getKey())) &&
                        (expectedEntries[i].include.equals(entry.getValue()))) {
                    if (matchName) {
                        if (entry.getKey().equals(entry.getValue().getName())) {
                            pass = true;
                            if (print)
                                System.out.println("hieararchy " + includes.getName() + " has map " + mapName +
                                        " with key " + entry.getKey() + " and include " + entry.getValue().getName());
                        } else {
                            fail("Map entry found for key: " + expectedEntries[i].key +
                                    " and specified IncludeHierarchy, but name was required to match key; found: " +
                                    expectedEntries[i].include.getName());
                        }
                    } else {
                        pass = true;
                        if (print)
                            System.out.println("hieararchy " + includes.getName() + " has map " + mapName +
                                    " with key " + entry.getKey() + " and include " + entry.getValue().getName());
                    }
                }
            }
            if (!pass) {
                fail("No map entry found for key: " + expectedEntries[i].key + " and IncludeHierarchy named: " +
                        expectedEntries[i].include.getName());
            }
        }
    }

    private void checkMapEntries(String comment, IncludeHierarchy includes, String mapName,
            Map<String, IncludeHierarchy> map, FQNME[] expectedEntries, boolean print, boolean matchName,
            boolean matchFullyQualifiedName) {
        //				printDetails(map, expectedEntries);
        assertEquals("Number of entries does not match for map " + mapName + " in IncludeHierarchy named " +
                includes.getName(), expectedEntries.length, map.size());
        for (int i = 0; i < expectedEntries.length; i++) {
            boolean pass = false;
            for (Entry<String, IncludeHierarchy> entry : map.entrySet()) {
                FQNME expectedEntry = expectedEntries[i];
                if ((expectedEntry.key.equals(entry.getKey())) &&
                        (expectedEntry.include.equals(entry.getValue()))) {
                    if (matchName) {
                        if (entry.getKey().equals(entry.getValue().getName())) {
                            pass = true;
                            if (print)
                                System.out.println("hierarchy " + includes.getName() + " has map " + mapName +
                                        " with key " + entry.getKey() + " and include " + entry.getValue().getName());
                            pass = checkFullyQualifiedName(expectedEntry, entry, print, matchFullyQualifiedName, pass);
                        } else {
                            fail("Map entry found for key: " + expectedEntries[i].key +
                                    " and specified IncludeHierarchy, but name was required to match key; found: " +
                                    expectedEntries[i].include.getName());
                        }
                    } else {
                        pass = true;
                        if (print)
                            System.out.println("hierarchy " + includes.getName() + " has map " + mapName +
                                    " with key " + entry.getKey() + " and include " + entry.getValue().getName());
                        pass = checkFullyQualifiedName(expectedEntry, entry, print, matchFullyQualifiedName, pass);
                    }
                }
            }
            if (!pass) {
                fail("No map entry found for key: " + expectedEntries[i].key + " and IncludeHierarchy named: " +
                        expectedEntries[i].include.getName());
            }
        }
    }

    protected boolean checkFullyQualifiedName(FQNME expectedEntry, Entry<String, IncludeHierarchy> entry, boolean print,
            boolean matchFullyQualifiedName, boolean pass) {
        if (matchFullyQualifiedName) {
            if (entry.getValue().getFullyQualifiedName().equals(expectedEntry.fullyQualifiedName)) {
                pass = true;
                if (print)
                    System.out.println("include " + entry.getValue().getName() +
                            " has fully qualified name " + entry.getValue().getFullyQualifiedName());
            } else {
                fail("Include " + entry.getValue().getName() + " expecting fully qualified name " +
                        expectedEntry.fullyQualifiedName +
                        " but found: " + entry.getValue().getFullyQualifiedName());
            }
        }
        return pass;
    }

    protected void printDetails(Map<String, IncludeHierarchy> map,
            ME[] expectedEntries) {
        System.out.println("expecting: ");
        for (int i = 0; i < expectedEntries.length; i++) {
            System.out.println(expectedEntries[i].key + " name " + expectedEntries[i].include.getName() +
                    " unique name " + expectedEntries[i].include.getUniqueName());
        }
        System.out.println("actual map: ");
        for (Map.Entry<String, IncludeHierarchy> entry : map.entrySet()) {
            System.out.println("map: " + entry.getKey() + " name " + entry.getValue().getName() + " unique name " +
                    entry.getValue().getUniqueName());
        }
    }

    protected void checkAllIncludesAllMapEntries(boolean print, String comment, ME... expectedEntries) {
        ME[] childEntries = null;
        IncludeHierarchy include = null;
        int totalEntries = expectedEntries.length;
        for (int i = 0; i < totalEntries; i++) {
            int numberChildEntries = totalEntries - i;
            childEntries = new ME[numberChildEntries];
            include = expectedEntries[i].include;
            for (int j = 0; j < numberChildEntries; j++) {
                childEntries[j] = expectedEntries[i + j];
            }
            checkIncludeMapAllEntries(comment + ": expecting " + totalEntries + " for " +
                    include.getName(), include, print, childEntries);
        }
    }

    protected void checkAllIncludesMapEntries(boolean print, String comment, ME[]... expectedEntryArrays) {
        for (int i = 0; i < expectedEntryArrays.length - 1; i++) {
            checkNextEntryArrays(print, comment, expectedEntryArrays[i], expectedEntryArrays[i + 1]);
        }
    }

    protected void checkFullyQualifiedNameEntries(boolean print, String comment, FQNME[]... expectedEntryArrays) {
        for (int i = 0; i < expectedEntryArrays.length - 1; i++) {
            checkNextEntryArrays(print, comment, expectedEntryArrays[i], expectedEntryArrays[i + 1]);
        }
    }

    private void checkNextEntryArrays(boolean print, String comment, ME[] parentExpectedEntries,
            ME[] childExpectedEntries) {
        List<ME> currentChildren = new ArrayList<>();
        IncludeHierarchy parent = null;
        lastParent = null;
        //		System.out.println("check parents "+parentExpectedEntries.length+" children "+childExpectedEntries.length);
        for (int i = 0; i < parentExpectedEntries.length; i++) {
            //			System.out.println("i "+i+"check parent "+parentExpectedEntries[i].include.getName());
            parent = parentExpectedEntries[i].include;
            for (int j = 0; j < childExpectedEntries.length; j++) {
                if (parent.equals(childExpectedEntries[j].include.getParent())) {
                    if (parent.equals(lastParent) || (lastParent == null)) {
                        currentChildren.add(childExpectedEntries[j]);
                        //						System.out.println("parent: "+parent.getName()+" child: "+childExpectedEntries[j].include.getName()+" size: "+currentChildren.size());
                    } else {
                        currentChildren = checkCurrentChildren(print, comment, childExpectedEntries, currentChildren, parent);
                        currentChildren.add(childExpectedEntries[j]);
                        lastParent = parent;
                    }
                } else {
                    lastParent = parent;
                }
            }
            currentChildren = checkCurrentChildren(print, comment, childExpectedEntries, currentChildren, parent);
        }
    }

    private void checkNextEntryArrays(boolean print, String comment, FQNME[] parentExpectedEntries,
            FQNME[] childExpectedEntries) {
        List<FQNME> currentChildren = new ArrayList<>();
        IncludeHierarchy parent = null;
        lastParent = null;
        //		System.out.println("check parents "+parentExpectedEntries.length+" children "+childExpectedEntries.length);
        for (int i = 0; i < parentExpectedEntries.length; i++) {
            //			System.out.println("i "+i+"check parent "+parentExpectedEntries[i].include.getName());
            parent = parentExpectedEntries[i].include;
            for (int j = 0; j < childExpectedEntries.length; j++) {
                if (parent.equals(childExpectedEntries[j].include.getParent())) {
                    if (parent.equals(lastParent) || (lastParent == null)) {
                        currentChildren.add(childExpectedEntries[j]);
                        //						System.out.println("parent: "+parent.getName()+" child: "+childExpectedEntries[j].include.getName()+" size: "+currentChildren.size());
                    } else {
                        currentChildren = checkCurrentChildren(print, comment, childExpectedEntries, currentChildren, parent);
                        currentChildren.add(childExpectedEntries[j]);
                        lastParent = parent;
                    }
                } else {
                    lastParent = parent;
                }
            }
            currentChildren = checkCurrentChildren(print, comment, childExpectedEntries, currentChildren, parent);
        }
    }

    protected List<ME> checkCurrentChildren(boolean print, String comment,
            ME[] childExpectedEntries, List<ME> currentChildren,
            IncludeHierarchy parent) {
        if (currentChildren.size() > 0) {
            //			System.out.println("parent: "+parent.getName()+": "+currentChildren.size());
            checkIncludeMapEntries(comment, parent, print, true, currentChildren.toArray(new ME[0]));
        }
        currentChildren = new ArrayList<>();
        return currentChildren;
    }

    protected List<FQNME> checkCurrentChildren(boolean print, String comment,
            FQNME[] childExpectedEntries, List<FQNME> currentChildren,
            IncludeHierarchy parent) {
        if (currentChildren.size() > 0) {
            //			System.out.println("parent: "+parent.getName()+": "+currentChildren.size());
            checkIncludeMapEntries(comment, parent, print, true, true, currentChildren.toArray(new FQNME[0]));
        }
        currentChildren = new ArrayList<>();
        return currentChildren;
    }

    protected void checkAllIncludesMapEntries(boolean print, String comment, ME... expectedEntries) {
        int totalEntries = expectedEntries.length;
        for (int i = 0; i < totalEntries - 1; i++) {
            checkIncludeMapEntries(comment, expectedEntries[i].include, print, true, expectedEntries[i + 1]);
        }
    }

    protected void checkAllIncludesAllMapEntries(ME... expectedEntries) {
        checkAllIncludesAllMapEntries(false, "", expectedEntries);
    }

    protected void checkAllIncludesMapEntries(ME... expectedEntries) {
        checkAllIncludesMapEntries(false, "", expectedEntries);
    }

    protected void checkAllIncludesAllMapEntries(String comment, ME... expectedEntries) {
        checkAllIncludesAllMapEntries(false, comment, expectedEntries);
    }

    protected void checkAllIncludesMapEntries(String comment, ME... expectedEntries) {
        checkAllIncludesMapEntries(false, comment, expectedEntries);
    }

    protected class ME {
        // Map Entry
        public String key;
        public IncludeHierarchy include;

        public ME(String key, IncludeHierarchy include) {
            this.key = key;
            this.include = include;
        }
    }

    protected class FQNME extends ME {
        // Map Entry + Fully Qualified Name
        public String fullyQualifiedName;

        public FQNME(String key, IncludeHierarchy include, String fullyQualifiedName) {
            super(key, include);
            this.fullyQualifiedName = fullyQualifiedName;
        }
    }

}
