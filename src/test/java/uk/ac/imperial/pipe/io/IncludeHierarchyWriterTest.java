package uk.ac.imperial.pipe.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;

import javax.xml.bind.JAXBException;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.xml.sax.SAXException;

import uk.ac.imperial.pipe.exceptions.IncludeException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.runner.PetriNetRunner;

public class IncludeHierarchyWriterTest extends XMLTestCase {
    IncludeHierarchyWriter writer;
    IncludeHierarchyReader reader;
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

    @Override
    public void setUp() throws JAXBException, IncludeException, PetriNetComponentNotFoundException {
        XMLUnit.setIgnoreWhitespace(true);
        
        IncludeHierarchyIO io = new IncludeHierarchyIOImpl();
        writer = (IncludeHierarchyWriter) io;
        reader = (IncludeHierarchyReader) io; 
    }
    
    public static void main(String[] args) throws Exception {
	    PetriNet net = buildNet(); 
		IncludeHierarchyIO io = new IncludeHierarchyIOImpl();
		IncludeHierarchyWriter writer = (IncludeHierarchyWriter) io;
		writer.writeTo("src/test/resources/xml/include/twoNetsOneInterfaceStatus.xml", new IncludeHierarchyBuilder(net.getIncludeHierarchy())); 
//		for include hierarchies, see IncludeHierarchyWriterTest
//		IncludeHierarchy includes = new IncludeHierarchy(net, "top"); 
//		includes.setPetriNetLocation("net3.xml");
////		includeb.setPetriNetLocation("net4.xml");
//		IncludeHierarchyIO includeIO = new IncludeHierarchyIOImpl(); 
//		String includePath = "include.xml"; 
//		includeIO.writeTo(includePath, new IncludeHierarchyBuilder(includes));
	}

    private static PetriNet buildNet() {
    	return PetriNetRunner.getPetriNet("testInterfacePlaces");
//    	PetriNet net = null; 
//    	// build net ....
//		return net;
	}
    
     
    public void testMarshalsSingleIncludeHierarchy() throws Exception {
    	checkIncludeReadWriteMatches(FileUtils.fileLocation(XMLUtils.getSingleIncludeHierarchyFile()));
    }
    public void testMarshalsMultipleIncludeHierarchies() throws Exception {
    	checkIncludeReadWriteMatches(FileUtils.fileLocation(XMLUtils.getMultipleIncludeHierarchyFile()));
    }
    
	protected void checkIncludeReadWriteMatches(String includeXmlLocation) throws JAXBException,
			FileNotFoundException, IncludeException, IOException, SAXException {
		IncludeHierarchy include = reader.read(includeXmlLocation);
        assertResultsEqual(includeXmlLocation, include);
	}

    private void assertResultsEqual(String expectedPath, IncludeHierarchy include )
            throws IOException, SAXException, JAXBException {
        StringWriter stringWriter = new StringWriter();
        writer.writeTo(stringWriter, new IncludeHierarchyBuilder(include));

        String expected = XMLUtils.readFile(expectedPath, Charset.defaultCharset());

        String actual = stringWriter.toString();
        assertXMLEqual(expected, actual);
    }

}


