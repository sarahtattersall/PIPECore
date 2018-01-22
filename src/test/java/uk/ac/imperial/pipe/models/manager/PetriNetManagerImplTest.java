package uk.ac.imperial.pipe.models.manager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.io.FileUtils;
import uk.ac.imperial.pipe.io.XMLUtils;
import uk.ac.imperial.pipe.io.XmlFileEnum;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import utils.PropertyChangeUtils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PetriNetManagerImplTest implements PropertyChangeListener {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private PropertyChangeListener listener;

    private PetriNetManager manager;

    private int notifyCount;

    @Before
    public void setUp() {
        manager = new PetriNetManagerImpl();
        manager.addPropertyChangeListener(listener);
        notifyCount = 0;
    }

    @Test
    public void newPetriNetNotifiesListener() {
        manager.createNewPetriNet();
        verify(listener)
                .propertyChange(argThat(PropertyChangeUtils.hasName(PetriNetManagerImpl.NEW_PETRI_NET_MESSAGE)));
    }

    @Test
    public void newPetriNetHasDefaultToken() throws PetriNetComponentNotFoundException {
        manager.createNewPetriNet();
        PetriNet petriNet = manager.getLastNet();
        assertNotNull(petriNet.getComponent("Default", Token.class));
    }

    @Test
    public void throwsRuntimeExceptionIfNoPetriNets() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("No Petri nets stored in the manager");
        manager.getLastNet();
    }

    @Test
    public void createsSinglePetriNetFromPnmlFile() throws Exception {
        PetriNetManagerImpl managerImpl = (PetriNetManagerImpl) manager;
        assertTrue(managerImpl.petriNetNamer.isUniqueName("simpleNet"));
        manager.createFromFile(new File(FileUtils.resourceLocation(XMLUtils.getSimplePetriNet())));
        assertFalse(managerImpl.petriNetNamer.isUniqueName("simpleNet"));
        verify(listener)
                .propertyChange(argThat(PropertyChangeUtils.hasName(PetriNetManagerImpl.NEW_PETRI_NET_MESSAGE)));
    }

    @Test
    public void createsMultipleIncludeHierarchiesFromMultipleIncludeFileNamedWithIncludeName() throws Exception {
        PetriNetManagerImpl managerImpl = (PetriNetManagerImpl) manager;
        assertTrue("name not in use yet", managerImpl.petriNetNamer.isUniqueName("a"));
        manager.createFromFile(new File(FileUtils.resourceLocation(XMLUtils.getMultipleIncludeHierarchyFile())));
        assertFalse(managerImpl.petriNetNamer.isUniqueName("a"));
        assertFalse(managerImpl.petriNetNamer.isUniqueName("b"));
        assertFalse(managerImpl.petriNetNamer.isUniqueName("c"));
        assertFalse(managerImpl.petriNetNamer.isUniqueName("bb"));
        verify(listener, times(4)).propertyChange(argThat(PropertyChangeUtils
                .hasName(PetriNetManagerImpl.NEW_INCLUDE_HIERARCHY_MESSAGE)));
    }

    @Test
    public void notifiesListenerOfSingleRootLevelIncludeWhenCreatingMultipleIncludesAndBeforeIndividualIncludeMessages()
            throws Exception {
        manager.addPropertyChangeListener(this);
        manager.createFromFile(new File(FileUtils.resourceLocation(XMLUtils.getMultipleIncludeHierarchyFile())));
        // test is checkRootLevelIncludeMessageArrivesBeforeEachIndividualIncludeMessage
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        checkRootLevelIncludeMessageArrivesBeforeEachIndividualIncludeMessage(evt);
    }

    protected void checkRootLevelIncludeMessageArrivesBeforeEachIndividualIncludeMessage(
            PropertyChangeEvent evt) {
        if (notifyCount == 0) {
            assertEquals(PetriNetManagerImpl.NEW_ROOT_LEVEL_INCLUDE_HIERARCHY_MESSAGE, evt.getPropertyName());
        } else {
            assertEquals(PetriNetManagerImpl.NEW_INCLUDE_HIERARCHY_MESSAGE, evt.getPropertyName());
        }
        notifyCount++;
    }
}