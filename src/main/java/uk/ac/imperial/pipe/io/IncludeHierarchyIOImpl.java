package uk.ac.imperial.pipe.io;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import uk.ac.imperial.pipe.exceptions.IncludeException;
import uk.ac.imperial.pipe.io.adapters.model.UpdateMergeInterfaceStatusCommand;
import uk.ac.imperial.pipe.io.adapters.modelAdapter.IncludeHierarchyBuilderAdapter;
import uk.ac.imperial.pipe.io.adapters.modelAdapter.ListWrapper;
import uk.ac.imperial.pipe.models.IncludeHierarchyHolder;
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
	private IncludeHierarchyBuilder builder;
    /**
     * Constructor that sets the context to the {@link uk.ac.imperial.pipe.models.PetriNetHolder}
     *
     * @throws JAXBException
     */
    public IncludeHierarchyIOImpl() throws JAXBException {
        context = JAXBContext.newInstance(IncludeHierarchyBuilder.class);
//        context = JAXBContext.newInstance(IncludeHierarchyHolder.class, ListWrapper.class);
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
		builder = (IncludeHierarchyBuilder) um.unmarshal(new FileReader(fileLocation));
		IncludeHierarchy include = builder.buildIncludes(null);  // root include has no parent
		Result<InterfacePlaceAction> result = include.all(new UpdateMergeInterfaceStatusCommand());
		if (result.hasResult()) throw new IncludeException(result.getAllMessages()); 
		return include; 
	}


	@Override
	public IncludeHierarchyHolder getIncludeHierarchyHolder() {
		return holder;
	}
    /**
     * Writes the IncludeHierarchy to the given stream
     *
     * @param stream
     */
	//TODO reconcile holder and include, and create writeTo(String path...)
	@Override
	public void writeTo(Writer stream,
			IncludeHierarchyBuilder builder) throws JAXBException {
        Marshaller m = context.createMarshaller();
//        m.setAdapter(new IncludeHierarchyBuilderAdapter());
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(builder, stream);
	}


	public final IncludeHierarchyBuilder getBuilder() {
		return builder;
	}

//    @Override
//    public void writeTo(String path, PetriNet petriNet) throws JAXBException {
//        Marshaller m = context.createMarshaller();
//        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
//        PetriNetHolder holder = new PetriNetHolder();
//        holder.addNet(petriNet);
//        m.marshal(holder, new File(path));
//    }

}
