package uk.ac.imperial.pipe.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.net.URL;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventLocator;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Node;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.io.adapters.modelAdapter.PetriNetAdapter;
import uk.ac.imperial.pipe.io.adapters.modelAdapter.TestingThrowsPetriNetAdapter;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;

public class PetriNetValidationEventHandlerTest {

    PetriNetReader reader;
    PetriNetValidationEventHandler petriNetValidationEventHandler;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws JAXBException {
        reader = new PetriNetIOImpl();
        //        reader = new PetriNetIOImpl(false, true); // equivalent to null constructor  
    }

    //TODO consolidate formatted events & formatted message
    @Test
    public void messagePrintedAndThrowsForFirstNonUnexpectedElementError()
            throws PetriNetComponentNotFoundException, JAXBException, FileNotFoundException {
        reader = new TestingThrowsPetriNetIOImpl(false, true); // true false
        String path = FileUtils.resourceLocation(XMLUtils.getInvalidPetriNetFile());
        try {
            @SuppressWarnings("unused")
            PetriNet petriNet = reader.read(path);
        } catch (JAXBException e) {
            assertEquals("PetriNetValidationEventHandler error attempting to build Petri net from file " + path +
                    ": java.lang.RuntimeException: TestingThrowsPetriNetAdapter exception.", e.getMessage());
        }
        PetriNetValidationEventHandler handler = ((PetriNetIOImpl) reader).getEventHandler();
        assertEquals(2, handler.getFormattedEvents().size());
        assertEquals("unexpected element message saved, but doesn't throw", true, handler.getFormattedEvents()
                .get(0).unexpected);
        assertFalse(handler.printMessage(handler.getFormattedEvents().get(0)));
        checkValidationMessage(handler);
        assertEquals("second error not unexpected element, so throws", false, handler.getFormattedEvents()
                .get(1).unexpected);
        assertEquals("PetriNetValidationEventHandler received a ValidationEvent, probably during processing by PetriNetIOImpl.  Details: \n" +
                "Message: java.lang.RuntimeException: TestingThrowsPetriNetAdapter exception.\n" +
                "Object: null\n" +
                "URL: null\n" +
                "Node: null\n" +
                "Line: 6\n" +
                "Column: 11\n" +
                "Linked exception: java.lang.RuntimeException: TestingThrowsPetriNetAdapter exception.", handler
                        .getFormattedEvents().get(1).formattedEvent);
        assertTrue(handler.printMessage(handler.getFormattedEvents().get(1)));
    }

    @Test
    public void noMessagePrintedAndDoesntThrowWhenUnexpectedElement()
            throws PetriNetComponentNotFoundException, JAXBException, FileNotFoundException {
        reader = new TestingPetriNetIOImpl(true, true);
        @SuppressWarnings("unused")
        PetriNet petriNet = reader.read(FileUtils.resourceLocation(XMLUtils.getInvalidPetriNetFile()));
        PetriNetValidationEventHandler handler = ((PetriNetIOImpl) reader).getEventHandler();
        assertEquals(true, handler.getFormattedEvents().get(0).unexpected);
        assertFalse(handler.printMessage(handler.getFormattedEvents().get(0)));
    }

    @Test
    public void messagePrintedAndThrowsWhenUnexpectedElement()
            throws PetriNetComponentNotFoundException, JAXBException, FileNotFoundException {
        reader = new TestingPetriNetIOImpl(false, false);
        checkPrintedAndDoesntThrowWhenUnexpectedElement();
    }

    @Test
    public void messagePrintedWithoutThrowingWhenUnexpectedElement()
            throws PetriNetComponentNotFoundException, JAXBException, FileNotFoundException {
        reader = new TestingPetriNetIOImpl(true, false);
        checkPrintedAndDoesntThrowWhenUnexpectedElement();
    }

    @Test
    public void generatesDetailedMessageForSpecificError()
            throws PetriNetComponentNotFoundException, JAXBException, FileNotFoundException {
        petriNetValidationEventHandler = new TestingPetriNetValidationEventHandler(false, true);
        petriNetValidationEventHandler.setFilename("petriNet.xml");
        petriNetValidationEventHandler.handleEvent(new TestingValidationEvent(new RuntimeException("some exception")));
        assertEquals("PetriNetValidationEventHandler error attempting to build Petri net from file petriNet.xml: some exception", petriNetValidationEventHandler
                .getMessage());
    }

    @Test
    public void printsAllMessagesIfSpecificDetailedMessageNotPresent()
            throws PetriNetComponentNotFoundException, JAXBException, FileNotFoundException {
        petriNetValidationEventHandler = new TestingPetriNetValidationEventHandler(false, true);
        petriNetValidationEventHandler.setFilename("petriNet.xml");
        petriNetValidationEventHandler.handleEvent(new TestingValidationEvent("some message"));
        String formattedEventMessage = "PetriNetValidationEventHandler received a ValidationEvent, probably during processing by PetriNetIOImpl.  Details: \n" +
                "Message: some message\n" +
                "Object: null\n" +
                "URL: null\n" +
                "Node: null\n" +
                "Line: 0\n" +
                "Column: 0\n" +
                "Linked exception: null";
        assertEquals(formattedEventMessage, petriNetValidationEventHandler.getFormattedEvents().get(0).formattedEvent);
        assertEquals(formattedEventMessage, petriNetValidationEventHandler.getMessage());
    }

    protected void checkPrintedAndDoesntThrowWhenUnexpectedElement()
            throws JAXBException, FileNotFoundException {
        @SuppressWarnings("unused")
        PetriNet petriNet = reader.read(FileUtils.resourceLocation(XMLUtils.getInvalidPetriNetFile()));
        PetriNetValidationEventHandler handler = ((PetriNetIOImpl) reader).getEventHandler();
        checkValidationMessage(handler);
        assertTrue(handler.printMessage(handler.getFormattedEvents().get(0)));
    }

    private void checkValidationMessage(PetriNetValidationEventHandler handler) {
        String message = handler.getFormattedEvents().get(0).formattedEvent;
        assertTrue(message
                .startsWith("PetriNetValidationEventHandler received a ValidationEvent, probably during processing by PetriNetIOImpl.  Details: \n" +
                        "Message: unexpected element (uri:\"\", local:\"blah\"). Expected elements are "));
        assertTrue(message.endsWith("Object: null\n" +
                "URL: null\n" +
                "Node: null\n" +
                "Line: 4\n" +
                "Column: 16\n" +
                "Linked exception: null"));
        // the order of tags appears arbitrary between Java 7 & 8, 
        // and neither order follows @XmlType(propOrder ...) as specified in AdaptedPetriNet
        String[] tags = new String[] { "arc", "definition", "place", "transition", "token", "labels" };
        for (int i = 0; i < tags.length; i++) {
            assertFalse("tag not found: " + tags[i], (message.indexOf(tags[i]) == -1));
        }
        assertEquals("tags may have been added or removed", 334, message.length());
    }

    private class TestingPetriNetIOImpl extends PetriNetIOImpl {

        public TestingPetriNetIOImpl(boolean continueProcessing,
                boolean suppressUnexpectedElementMessages) throws JAXBException {
            super(continueProcessing, suppressUnexpectedElementMessages);
            petriNetValidationEventHandler = new TestingPetriNetValidationEventHandler(continueProcessing,
                    suppressUnexpectedElementMessages);
        }

        @Override
        protected void initialiseUnmarshaller() throws JAXBException {
            super.initialiseUnmarshaller();
        }
    }

    private class TestingThrowsPetriNetIOImpl extends TestingPetriNetIOImpl {

        public TestingThrowsPetriNetIOImpl(boolean continueProcessing,
                boolean suppressUnexpectedElementMessages) throws JAXBException {
            super(continueProcessing, suppressUnexpectedElementMessages);
            petriNetValidationEventHandler = new TestingPetriNetValidationEventHandler(continueProcessing,
                    suppressUnexpectedElementMessages);
        }

        @Override
        protected void initialiseUnmarshaller() throws JAXBException {
            super.initialiseUnmarshaller();
            getUnmarshaller().setAdapter(PetriNetAdapter.class, new TestingThrowsPetriNetAdapter());
        }
    }

    private class TestingPetriNetValidationEventHandler extends PetriNetValidationEventHandler {

        public TestingPetriNetValidationEventHandler(
                boolean continueProcessing,
                boolean suppressUnexpectedElementMessages) {
            super(continueProcessing, suppressUnexpectedElementMessages);
        }

        @Override
        public void printMessages() {
        }
    }

    private class TestingValidationEvent implements ValidationEvent {

        private Throwable exception;
        private String message;

        public TestingValidationEvent(Throwable exception) {
            this.exception = exception;
            this.message = exception.getMessage();
        }

        public TestingValidationEvent(String message) {
            this.message = message;
        }

        @Override
        public int getSeverity() {
            return 0;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public Throwable getLinkedException() {
            return exception;
        }

        @Override
        public ValidationEventLocator getLocator() {
            return new TestingValidationEventLocator();
        }

    }

    private class TestingValidationEventLocator implements ValidationEventLocator {

        @Override
        public URL getURL() {
            return null;
        }

        @Override
        public int getOffset() {
            return 0;
        }

        @Override
        public int getLineNumber() {
            return 0;
        }

        @Override
        public int getColumnNumber() {
            return 0;
        }

        @Override
        public Object getObject() {
            return null;
        }

        @Override
        public Node getNode() {
            return null;
        }

    }
}
