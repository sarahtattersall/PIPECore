package uk.ac.imperial.pipe.io;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

public class GenerateSchemaForDebugging extends SchemaOutputResolver {

    @Override
    public Result createOutput(String namespaceUri, String suggestedFileName)
            throws IOException {
        File file = new File(suggestedFileName);
        System.out.println(file.getAbsolutePath());
        StreamResult result = new StreamResult(file);
        result.setSystemId(file.toURI().toURL().toString());
        return result;
    }

    public static void main(String[] args) throws JAXBException, IOException {
        Class[] classes = new Class[1];
        //		classes[0] = uk.ac.imperial.pipe.models.PetriNetHolder.class; 
        //		classes[1] = uk.ac.imperial.pipe.io.adapters.model.AdaptedPetriNet.class; 
        //		classes[2] = uk.ac.imperial.pipe.io.adapters.model.AdaptedTransition.class; 
        classes[0] = uk.ac.imperial.pipe.io.IncludeHierarchyBuilder.class;
        //		classes[0] = uk.ac.imperial.pipe.models.IncludeHierarchyHolder.class; 
        //		classes[1] = uk.ac.imperial.pipe.io.adapters.model.AdaptedIncludeHierarchyBuilder.class; 
        JAXBContext jaxbContext = JAXBContext.newInstance(classes);

        SchemaOutputResolver sor = new GenerateSchemaForDebugging();
        jaxbContext.generateSchema(sor);

    }

}
