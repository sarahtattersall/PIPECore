package uk.ac.imperial.pipe.io;

import java.io.IOException;
import java.io.Writer;

import javax.xml.bind.JAXBException;

public interface IncludeHierarchyWriter {

    public void writeTo(Writer stream, IncludeHierarchyBuilder builder) throws JAXBException;

    public void writeTo(String path, IncludeHierarchyBuilder builder) throws JAXBException, IOException;
}
