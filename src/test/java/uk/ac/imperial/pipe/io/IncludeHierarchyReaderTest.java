package uk.ac.imperial.pipe.io;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.exceptions.IncludeException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.io.adapters.model.AdaptedIncludeHierarchyBuilder.Includes;
import uk.ac.imperial.pipe.models.petrinet.*;

import javax.xml.bind.JAXBException;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IncludeHierarchyReaderTest {

    IncludeHierarchyReader reader;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws JAXBException {
        reader = new IncludeHierarchyIOImpl();
    }

    //TODO include file includes another include file
    @Test
    public void createsSingleIncludeHierarchy()
            throws Exception {
        IncludeHierarchy include = reader.read(FileUtils.resourceLocation(XMLUtils.getSingleIncludeHierarchyFile()));
        assertEquals("a", include.getName());
        PetriNet net = include.getPetriNet();
        assertEquals(2, net.getPlaces().size());
        assertEquals(1, net.getTransitions().size());
        assertEquals(2, net.getArcs().size());
    }

    @Test
    public void readsFromFileSystemIfNotFoundAsResource() throws Exception {
        PetriNet petriNet = APetriNet.withOnly(APlace.withId("P0"));
        PetriNetIO writer = new PetriNetIOImpl();
        writer.writeTo("testnet.xml", petriNet);
        IncludeHierarchy include = new IncludeHierarchy(petriNet, "a");
        include.setPetriNetLocation("testnet.xml");
        IncludeHierarchyIO includeIO = new IncludeHierarchyIOImpl();
        includeIO.writeTo("testinclude.xml", new IncludeHierarchyBuilder(include));

        IncludeHierarchy newInclude = includeIO.read("testinclude.xml");
        assertEquals("a", newInclude.getName());
        assertEquals(1, newInclude.getPetriNet().getPlaces().size());
    }

    @After
    public void tearDown() throws Exception {
        deleteFile("testnet.xml");
        deleteFile("testinclude.xml");
        deleteFile("multipleIncludesRelativeLocations.xml");
        deleteFile("xml/simpleNet.xml");
        deleteFile("xml/petriNet.xml");
        deleteFile("xml/gspn1.xml");
        deleteFile("xml");
    }

    @Test
    public void interfaceStatusIsCorrectlyBuiltFromMultipleIncludes() throws Exception {
        IncludeHierarchy include = reader
                .read(FileUtils.resourceLocation(XMLUtils.getMultipleIncludeHierarchyWithInterfaceStatusFile()));
        assertEquals("top", include.getName());
        PetriNet net = include.getPetriNet();
        assertEquals(2, net.getPlaces().size());
        assertEquals(1, include.getInterfacePlaces().size());
        Place placetopIP0 = include.getInterfacePlace("b.P0");
        assertTrue(placetopIP0.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAway);
        assertEquals("b.P0", placetopIP0.getStatus().getMergeInterfaceStatus().getAwayId());
        IncludeHierarchy includea = include.getInclude("a");
        assertEquals("a", includea.getName());
        PetriNet neta = includea.getPetriNet();
        assertEquals("neta", neta.getNameValue());
        assertEquals(1, neta.getPlaces().size());
        assertEquals(1, includea.getInterfacePlaces().size());
        Place placeaIP0 = includea.getInterfacePlace("b.P0");
        assertTrue(placeaIP0.getStatus().getMergeInterfaceStatus() instanceof MergeInterfaceStatusAvailable);
        assertEquals("b.P0", placeaIP0.getStatus().getMergeInterfaceStatus().getAwayId());
        IncludeHierarchy includeb = includea.getInclude("b");
        assertEquals("b", includeb.getName());
        PetriNet netb = includeb.getPetriNet();
        assertEquals("netb", netb.getNameValue());
        assertEquals(1, netb.getPlaces().size());
        assertEquals(1, includeb.getInterfacePlaces().size());

        Place placehome = includeb.getInterfacePlace("P0");
        Place place = netb.getComponent("P0", Place.class);
        assertEquals(place, placehome);
        assertEquals(placehome, placetopIP0.getStatus().getMergeInterfaceStatus().getHomePlace());
        assertEquals(placehome, placeaIP0.getStatus().getMergeInterfaceStatus().getHomePlace());
        assertTrue(includeb.getInterfacePlace("P0").getStatus()
                .getMergeInterfaceStatus() instanceof MergeInterfaceStatusHome);
    }

    @Test
    public void createsIncludeHierarchyWithMultipleLevels()
            throws PetriNetComponentNotFoundException, JAXBException, FileNotFoundException, IncludeException {
        IncludeHierarchy include = reader.read(FileUtils.resourceLocation(XMLUtils.getMultipleIncludeHierarchyFile()));
        assertEquals("a", include.getName());
        PetriNet net = include.getPetriNet();
        assertEquals(5, net.getPlaces().size());
        assertEquals(5, net.getTransitions().size());
        assertEquals(12, net.getArcs().size());
        //b & bb at same level
        IncludeHierarchy includeb = include.getInclude("b");
        PetriNet netb = includeb.getPetriNet();
        assertEquals(2, netb.getPlaces().size());
        assertEquals(1, netb.getTransitions().size());
        assertEquals(2, netb.getArcs().size());
        assertEquals(135, netb.getComponent("P0", Place.class).getX());
        IncludeHierarchy includebb = include.getInclude("bb");
        PetriNet netbb = includebb.getPetriNet();
        assertEquals(2, netbb.getPlaces().size());
        assertEquals(1, netbb.getTransitions().size());
        assertEquals(2, netbb.getArcs().size());
        assertEquals(225, netbb.getComponent("P0", Place.class).getX());
        //c under b
        IncludeHierarchy includec = includeb.getInclude("c");
        PetriNet netc = includec.getPetriNet();
        assertEquals(2, netc.getPlaces().size());
        assertEquals(1, netc.getTransitions().size());
        assertEquals(2, netc.getArcs().size());
        assertEquals(225, netc.getComponent("P0", Place.class).getX());
    }

    @Test
    public void createsIncludeHierarchyWhereIncludedFileLocationsAreRelativeToRootInclude()
            throws PetriNetComponentNotFoundException, JAXBException, IncludeException, IOException {
        File includeFile = buildFilesInWorkingDirectory();
        IncludeHierarchy include = reader.read(includeFile.getAbsolutePath());
        assertEquals("a", include.getName());
        PetriNet net = include.getPetriNet();
        assertEquals(5, net.getPlaces().size());
        //b & bb at same level
        IncludeHierarchy includeb = include.getInclude("b");
        PetriNet netb = includeb.getPetriNet();
        assertEquals(2, netb.getPlaces().size());
        assertEquals(135, netb.getComponent("P0", Place.class).getX());
        IncludeHierarchy includebb = include.getInclude("bb");
        PetriNet netbb = includebb.getPetriNet();
        assertEquals(2, netbb.getPlaces().size());
        assertEquals(225, netbb.getComponent("P0", Place.class).getX());
        //c under b
        IncludeHierarchy includec = includeb.getInclude("c");
        PetriNet netc = includec.getPetriNet();
        assertEquals(2, netc.getPlaces().size());
        assertEquals(225, netc.getComponent("P0", Place.class).getX());
    }

    protected File buildFilesInWorkingDirectory() throws IOException {
        File includeFile = FileUtils
                .copyToWorkingDirectory(XMLUtils.getMultipleIncludeHierarchyFileWithRelativeLocations());
        FileUtils.copyToWorkingDirectorySubdirectory("xml", XMLUtils.getSimplePetriNet());
        FileUtils.copyToWorkingDirectorySubdirectory("xml", XMLUtils.getPetriNet());
        FileUtils.copyToWorkingDirectorySubdirectory("xml", XMLUtils.getGeneralizedStochasticPetriNet());
        return includeFile;
    }

    @Test
    public void throwsWhenIncludedPetriNetHasAnError()
            throws PetriNetComponentNotFoundException, JAXBException, FileNotFoundException, IncludeException {
        String path = FileUtils.resourceLocation(XMLUtils.getIncludeWithInvalidPetriNet());
        String includedPath = FileUtils.resourceLocation(XMLUtils.getArcWithoutPlaceFile());
        expectedException.expect(JAXBException.class);
        expectedException
                .expectMessage("PetriNetValidationEventHandler error attempting to build Petri net from file " +
                        includedPath +
                        ": uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException:  in uk.ac.imperial.pipe.io.adapters.modelAdapter.ArcAdapter: " +
                        "Arc 'P1 TO T0' references place P1 but P1 does not exist in file.");
        reader.read(path);
    }

    //TODO have filename in the eventhandler track the current file
    //TODO have constructor set flags
    private void deleteFile(String filename) {
        File file = new File(filename);
        if (file.exists())
            file.delete();
    }

}
