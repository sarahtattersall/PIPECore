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
import javax.xml.bind.ValidationEvent;

import uk.ac.imperial.pipe.exceptions.IncludeException;
import uk.ac.imperial.pipe.models.IncludeHierarchyHolder;
import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.InterfacePlaceAction;
import uk.ac.imperial.pipe.models.petrinet.RemoveOrphanedAwayPlacesFromInterfaceCommand;
import uk.ac.imperial.pipe.models.petrinet.Result;
import uk.ac.imperial.pipe.models.petrinet.UpdateMergeInterfaceStatusCommand;

public class IncludeHierarchyIOImpl implements IncludeHierarchyIO {

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
     * @throws JAXBException if the JAXBContext could not be created
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
     * @param fileLocation xml path containing a PNML representation of a Petri net
     * @return include hierarchy read from the xml path
     * @throws FileNotFoundException  if file not found
     * @throws JAXBException if errors occur during unmarshaling
     * @throws IncludeException if the include hierarchy is incorrectly structured
    
     */

    @Override
    public IncludeHierarchy read(String fileLocation) throws JAXBException, FileNotFoundException, IncludeException {
        Unmarshaller um = context.createUnmarshaller();
        um.setEventHandler(getEventHandler());
        getEventHandler().setFilename(fileLocation);
        //	    fileLocation = FileUtils.fileLocation(fileLocation);
        try {
            builder = (IncludeHierarchyBuilder) um.unmarshal(getReaderFromPath(fileLocation));
            getEventHandler().printMessages();
        } catch (JAXBException e) {
            throw new JAXBException(getEventHandler().getMessage());
        }
        File rootFile = new File(fileLocation);
        builder.setRootLocation(rootFile.getAbsoluteFile().getParent());

        IncludeHierarchy include = builder.buildIncludes(null); // root include has no parent
        Result<InterfacePlaceAction> result = include.all(new UpdateMergeInterfaceStatusCommand());
        if (result.hasResult()) {
            throw new IncludeException(result.getAllMessages());
        }
        //TODO determine whether this is ever needed
        result = include.all(new RemoveOrphanedAwayPlacesFromInterfaceCommand());
        if (result.hasResult()) {
            //TODO make visible in the GUI but don't fail.  (Console warnings sent)
            //            throw new IncludeException(result.getAllMessages());
        }
        return include;
    }

    protected FileReader getReaderFromPath(String path)
            throws FileNotFoundException {
        String normalizedPath = FileUtils.fileLocation(path);
        return new FileReader(normalizedPath);
    }

    @Override
    public IncludeHierarchyHolder getIncludeHierarchyHolder() {
        return holder;
    }

    /**
     * Writes the IncludeHierarchyBuilder to the given stream
     *
     * @param stream to write to
     * @param builder IncludeHierarchyBuilder to be written
     * @throws JAXBException if builder cannot be marshalled
     */
    @Override
    public void writeTo(Writer stream, IncludeHierarchyBuilder builder) throws JAXBException {
        Marshaller m;
        try {
            m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(builder, stream);
        } catch (JAXBException e) {
            throw e;
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Writes the IncludeHierarchyBuilder to the given path
     *
     * @param path to write to
     * @param builder IncludeHierarchyBuilder to be written
     */
    @Override
    public void writeTo(String path, IncludeHierarchyBuilder builder) throws JAXBException, IOException {
        writeTo(new FileWriter(new File(path)), builder);
    }

    @Override
    public final IncludeHierarchyBuilder getBuilder() {
        return builder;
    }

    /**
     * Gets the PetriNetValidationEventHandler, which accumulates any errors encountered during
     * JAXB processing of PNML files
     *
     * @return petriNetValidationEventHandler to handle any validation events that occur
     * @see ValidationEvent
     */
    protected PetriNetValidationEventHandler getEventHandler() {
        return petriNetValidationEventHandler;
    }

}
