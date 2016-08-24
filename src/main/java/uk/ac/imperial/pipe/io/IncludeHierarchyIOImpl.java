package uk.ac.imperial.pipe.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import uk.ac.imperial.pipe.exceptions.IncludeException;
import uk.ac.imperial.pipe.io.adapters.model.UpdateMergeInterfaceStatusCommand;
import uk.ac.imperial.pipe.io.adapters.modelAdapter.IncludeHierarchyBuilderAdapter;
import uk.ac.imperial.pipe.io.adapters.modelAdapter.ListWrapper;
import uk.ac.imperial.pipe.models.IncludeHierarchyHolder;
import uk.ac.imperial.pipe.models.PetriNetHolder;
import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.InterfacePlaceAction;
import uk.ac.imperial.pipe.models.petrinet.Result;
import uk.ac.imperial.pipe.io.adapters.modelAdapter.ListWrapper;

public class IncludeHierarchyIOImpl implements  IncludeHierarchyIO {

    /**
     * JAXB context initialised in constructor
     */
    private final JAXBContext context;
	private IncludeHierarchyHolder holder;

    /**
     * PetriNetValidationEventHandler used to process validation events 
     * Defaults to stopping processing in the event of a failure, but can be overridden to continue
     * (should only continue when testing)
     */
    protected PetriNetValidationEventHandler petriNetValidationEventHandler;
	
	private IncludeHierarchyBuilder builder;
    /**
     * Constructor that sets the context to the {@link uk.ac.imperial.pipe.models.PetriNetHolder}
     *
     * @throws JAXBException
     */
    public IncludeHierarchyIOImpl() throws JAXBException {
        context = JAXBContext.newInstance(IncludeHierarchyBuilder.class);
    	petriNetValidationEventHandler = new PetriNetValidationEventHandler(false, true);
//    	petriNetValidationEventHandler = new PetriNetValidationEventHandler(continueProcessing, suppressUnexpectedElementMessages);
    	// setting log level to FINEST to force continued reporting of errors; otherwise, suppressed 
    	// after 10 errors in static field, generating unpredictable test side effects, under Java 1.8
    	// https://java.net/projects/jaxb/lists/commits/archive/2013-08/message/4
    	// https://java.net/projects/jaxb/lists/users/archive/2015-11/message/6
    	Logger.getLogger("com.sun.xml.internal.bind").setLevel(Level.FINEST);
    }

	
    /**
     * Reads a Petri net from the given path
     *
     * @param path xml path containing a PNML representation of a Petri net
     * @return read Petri net
     * @throws IncludeException 
     */
	
	@Override
	public IncludeHierarchy read(String fileLocation) throws JAXBException, FileNotFoundException, IncludeException{
		Unmarshaller um = context.createUnmarshaller();
	    um.setEventHandler(getEventHandler());
	    getEventHandler().setFilename(fileLocation); 
	    try {
	    	builder = (IncludeHierarchyBuilder) um.unmarshal(new FileReader(fileLocation));
	    	getEventHandler().printMessages(); 
		} catch (JAXBException e) {
	//		getEventHandler().printMessages(); 
	//		e.printStackTrace(); 
			throw new JAXBException(getEventHandler().getMessage());  
	//		throw e;  
		} 
		IncludeHierarchy include = builder.buildIncludes(null);  // root include has no parent
		Result<InterfacePlaceAction> result = include.all(new UpdateMergeInterfaceStatusCommand());
		if (result.hasResult()) throw new IncludeException(result.getAllMessages()); 
		return include; 
	}

//    PetriNetHolder holder = null; 
//

	@Override
	public IncludeHierarchyHolder getIncludeHierarchyHolder() {
		return holder;
	}
    /**
     * Writes the IncludeHierarchyBuilder to the given stream
     *
     * @param stream
     * @param IncludeHierarchyBuilder
     */
	@Override
	public void writeTo(Writer stream, IncludeHierarchyBuilder builder) throws JAXBException {
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(builder, stream);
	}

	/**
	 * Writes the IncludeHierarchyBuilder to the given path
	 *
	 * @param path
	 * @param IncludeHierarchyBuilder
	 */
	@Override
	public void writeTo(String path, IncludeHierarchyBuilder builder) throws JAXBException, IOException {
		writeTo(new FileWriter(new File(path)), builder); 
	}

	public final IncludeHierarchyBuilder getBuilder() {
		return builder;
	}

	/**
	 * Gets the PetriNetValidationEventHandler, which accumulates any errors encountered during 
	 * JAXB processing of PNML files 
	 *
	 * @return petriNetValidationEventHandler 
	 */
	protected PetriNetValidationEventHandler getEventHandler() {
		return petriNetValidationEventHandler;
	}


}
