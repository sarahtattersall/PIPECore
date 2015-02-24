package uk.ac.imperial.pipe.io;

import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import uk.ac.imperial.pipe.exceptions.IncludeException;
import uk.ac.imperial.pipe.models.IncludeHierarchyHolder;
import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;

public interface IncludeHierarchyReader  {

    /**
     * Read an include hierarchy from the given path
     * @param path this path must point to an xml file that contains an include hierarchy
     * @return include hierarchy
     * @throws FileNotFoundException 
     * @throws javax.xml.bind.JAXBException, FileNotFoundException
     * @throws IncludeException 
     */

	public IncludeHierarchy read(String fileLocation) throws JAXBException, FileNotFoundException, IncludeException;

	public IncludeHierarchyHolder getIncludeHierarchyHolder();

	public IncludeHierarchyBuilder getBuilder();

}
